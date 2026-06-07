package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.dtos.request.RegistroPagoDTO;
import com.allprocess.ecommerce.entities.PagoENTITY;
import com.allprocess.ecommerce.services.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    // POST /api/pagos/registrar — Registra un nuevo pago
    @PostMapping("/registrar")
    public ResponseEntity<PagoENTITY> registrar(@Valid @RequestBody RegistroPagoDTO pagoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(pagoDTO));
    }

    // POST /api/pagos/{id}/confirmar — Confirma un pago (admin o pasarela)
    @PostMapping("/{id}/confirmar")
    public ResponseEntity<PagoENTITY> confirmar(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoService.confirmarPago(id));
    }

    // GET /api/pagos/venta/{idVenta} — Lista pagos de una venta
    @GetMapping("/venta/{idVenta}")
    public ResponseEntity<List<PagoENTITY>> porVenta(@PathVariable Integer idVenta) {
        return ResponseEntity.ok(pagoService.listarPagosPorVenta(idVenta));
    }
}
