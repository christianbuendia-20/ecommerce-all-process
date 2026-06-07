package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.entities.CategoriaProductoENTITY;
import com.allprocess.ecommerce.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final ProductoService productoService;

    // GET /api/categorias — Lista todas las categorías
    @GetMapping
    public ResponseEntity<List<CategoriaProductoENTITY>> listar() {
        return ResponseEntity.ok(productoService.listarCategorias());
    }
}
