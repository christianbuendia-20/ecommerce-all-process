package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.dtos.request.CheckoutVentaDTO;
import com.allprocess.ecommerce.dtos.response.ReciboVentaDTO;
import com.allprocess.ecommerce.services.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    // POST /api/ventas/checkout — Procesa el carrito y crea la venta
    @PostMapping("/checkout")
    public ResponseEntity<ReciboVentaDTO> checkout(@Valid @RequestBody CheckoutVentaDTO checkoutDTO) {
        ReciboVentaDTO recibo = ventaService.realizarCheckout(checkoutDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(recibo);
    }

    // GET /api/ventas/mis-ventas?clienteId= — Historial de compras del cliente
    @GetMapping("/mis-ventas")
    public ResponseEntity<List<ReciboVentaDTO>> misVentas(@RequestParam Integer clienteId) {
        return ResponseEntity.ok(ventaService.obtenerHistorialPorCliente(clienteId));
    }

    // GET /api/ventas/{id}/recibo — Obtiene el recibo de una venta
    @GetMapping("/{id}/recibo")
    public ResponseEntity<ReciboVentaDTO> recibo(@PathVariable Integer id) {
        return ResponseEntity.ok(ventaService.obtenerRecibo(id));
    }
}
