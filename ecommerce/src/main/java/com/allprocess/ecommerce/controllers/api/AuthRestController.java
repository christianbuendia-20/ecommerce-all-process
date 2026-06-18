package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.dtos.request.LoginRequestDTO;
import com.allprocess.ecommerce.dtos.request.RegistroUsuarioDTO;
import com.allprocess.ecommerce.dtos.response.LoginResponseDTO;
import com.allprocess.ecommerce.dtos.response.PerfilUsuarioDTO;
import com.allprocess.ecommerce.security.TokenBlacklistService;
import com.allprocess.ecommerce.services.AuthService;
import com.allprocess.ecommerce.services.UsuarioService;
import com.allprocess.ecommerce.services.VerificacionEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthRestController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final TokenBlacklistService tokenBlacklistService;
    private final VerificacionEmailService verificacionEmailService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/auth/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.invalidarToken(token);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/usuarios/registro")
    public ResponseEntity<PerfilUsuarioDTO> registrar(@Valid @RequestBody RegistroUsuarioDTO registroDTO) {
        // 1. Crear el usuario (commit dentro del @Transactional del servicio)
        PerfilUsuarioDTO resultado = usuarioService.registrarUsuario(registroDTO);

        // 2. Enviar código de verificación (el usuario ya está en la BD)
        try {
            verificacionEmailService.generarYEnviarCodigo(registroDTO.getEmail());
        } catch (Exception e) {
            log.warn("No se pudo enviar el email de verificación a {}: {}", registroDTO.getEmail(), e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
