package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.dtos.response.PerfilUsuarioDTO;
import com.allprocess.ecommerce.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileRestController {

    private final UsuarioService usuarioService;

    // GET /api/profile?usuarioId= — Obtiene el perfil del usuario
    @GetMapping
    public ResponseEntity<PerfilUsuarioDTO> obtenerPerfil(@RequestParam Integer usuarioId) {
        return ResponseEntity.ok(usuarioService.obtenerPerfil(usuarioId));
    }

    // PUT /api/profile?usuarioId= — Actualiza datos personales
    @PutMapping
    public ResponseEntity<PerfilUsuarioDTO> actualizar(
            @RequestParam Integer usuarioId,
            @RequestBody PerfilUsuarioDTO perfilDTO) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(usuarioId, perfilDTO));
    }

    // PUT /api/profile/password?usuarioId= — Cambia la contraseña
    @PutMapping("/password")
    public ResponseEntity<Void> cambiarPassword(
            @RequestParam Integer usuarioId,
            @RequestBody Map<String, String> body) {
        usuarioService.cambiarPassword(
                usuarioId,
                body.get("passwordActual"),
                body.get("passwordNueva")
        );
        return ResponseEntity.ok().build();
    }
}
