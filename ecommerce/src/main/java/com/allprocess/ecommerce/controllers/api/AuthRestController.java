package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.dtos.request.LoginRequestDTO;
import com.allprocess.ecommerce.dtos.request.RegistroUsuarioDTO;
import com.allprocess.ecommerce.dtos.response.LoginResponseDTO;
import com.allprocess.ecommerce.dtos.response.PerfilUsuarioDTO;
import com.allprocess.ecommerce.security.TokenBlacklistService;
import com.allprocess.ecommerce.services.AuthService;
import com.allprocess.ecommerce.services.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final TokenBlacklistService tokenBlacklistService;

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
        PerfilUsuarioDTO resultado = usuarioService.registrarUsuario(registroDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }
}
