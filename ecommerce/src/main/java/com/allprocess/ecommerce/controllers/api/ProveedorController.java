package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.entities.ProveedorENTITY;
import com.allprocess.ecommerce.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProductoService productoService;

    // GET /api/proveedores — Lista todos los proveedores
    @GetMapping
    public ResponseEntity<List<ProveedorENTITY>> listar() {
        return ResponseEntity.ok(productoService.listarProveedores());
    }
}
