package com.allprocess.ecommerce.controllers.api.admin;

import com.allprocess.ecommerce.entities.PagoENTITY;
import com.allprocess.ecommerce.services.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/pagos")
@RequiredArgsConstructor
public class AdminPagoController {

    private final PagoService pagoService;

    // GET /api/admin/pagos — Lista todos los pagos
    @GetMapping
    public ResponseEntity<List<PagoENTITY>> listarTodos() {
        return ResponseEntity.ok(pagoService.listarTodos());
    }

    // PUT /api/admin/pagos/{id}/estado — Cambia el estado de un pago
    @PutMapping("/{id}/estado")
    public ResponseEntity<PagoENTITY> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(pagoService.actualizarEstadoPago(id, body.get("estado")));
    }
}
