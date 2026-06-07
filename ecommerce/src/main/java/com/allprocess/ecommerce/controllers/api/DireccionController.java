package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.dtos.response.DireccionDTO;
import com.allprocess.ecommerce.services.DireccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/direcciones")
@RequiredArgsConstructor
public class DireccionController {

    private final DireccionService direccionService;

    // GET /api/direcciones?usuarioId= — Lista direcciones de un usuario
    @GetMapping
    public ResponseEntity<List<DireccionDTO>> listar(@RequestParam Integer usuarioId) {
        return ResponseEntity.ok(direccionService.listarPorUsuario(usuarioId));
    }

    // POST /api/direcciones?usuarioId= — Agrega una nueva dirección
    @PostMapping
    public ResponseEntity<DireccionDTO> agregar(
            @RequestParam Integer usuarioId,
            @RequestBody DireccionDTO direccionDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(direccionService.agregarDireccion(usuarioId, direccionDTO));
    }

    // DELETE /api/direcciones/{id}?usuarioId= — Elimina una dirección
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id, @RequestParam Integer usuarioId) {
        direccionService.eliminarDireccion(id, usuarioId);
        return ResponseEntity.noContent().build();
    }
}
