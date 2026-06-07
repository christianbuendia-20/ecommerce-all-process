package com.allprocess.ecommerce.controllers.api.admin;

import com.allprocess.ecommerce.dtos.response.UsuarioAdminDTO;
import com.allprocess.ecommerce.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    // GET /api/admin/usuarios — Lista todos los usuarios
    @GetMapping
    public ResponseEntity<List<UsuarioAdminDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // GET /api/admin/usuarios/buscar?q= — Busca usuarios por nombre/apellido
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioAdminDTO>> buscar(@RequestParam("q") String query) {
        return ResponseEntity.ok(usuarioService.buscarPorNombre(query));
    }

    // PUT /api/admin/usuarios/{id}/rol — Cambia el rol de un usuario
    @PutMapping("/{id}/rol")
    public ResponseEntity<UsuarioAdminDTO> cambiarRol(
            @PathVariable Integer id,
            @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(usuarioService.cambiarRol(id, body.get("idRol")));
    }

    // PUT /api/admin/usuarios/{id}/activar — Activa/desactiva un usuario
    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> cambiarActivo(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {
        usuarioService.cambiarActivo(id, body.get("activo"));
        return ResponseEntity.ok().build();
    }
}
