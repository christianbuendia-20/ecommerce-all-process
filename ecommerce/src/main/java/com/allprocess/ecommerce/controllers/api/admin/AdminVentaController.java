package com.allprocess.ecommerce.controllers.api.admin;

import com.allprocess.ecommerce.dtos.response.AdminVentaListaDTO;
import com.allprocess.ecommerce.dtos.response.ReciboVentaDTO;
import com.allprocess.ecommerce.services.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ventas")
@RequiredArgsConstructor
public class AdminVentaController {

    private final VentaService ventaService;

    // GET /api/admin/ventas — Lista todas las ventas con info de cliente y detalles
    @GetMapping
    public ResponseEntity<List<AdminVentaListaDTO>> listarTodas() {
        return ResponseEntity.ok(ventaService.listarTodas());
    }

    // GET /api/admin/ventas/{id} — Obtiene detalle de una venta
    @GetMapping("/{id}")
    public ResponseEntity<AdminVentaListaDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.obtenerVentaConDetalle(id));
    }

    // GET /api/admin/ventas/{id}/recibo — Obtiene el recibo completo
    @GetMapping("/{id}/recibo")
    public ResponseEntity<ReciboVentaDTO> recibo(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.obtenerRecibo(id));
    }

    // PUT /api/admin/ventas/{id}/estado — Actualiza el estado de una venta
    @PutMapping("/{id}/estado")
    public ResponseEntity<Void> actualizarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, String> body) {
        ventaService.actualizarEstado(id, body.get("estado"));
        return ResponseEntity.ok().build();
    }
}
