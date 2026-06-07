package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.dtos.response.ProductoCatalogoDTO;
import com.allprocess.ecommerce.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // GET /api/productos — Lista todos los productos activos
    @GetMapping
    public ResponseEntity<List<ProductoCatalogoDTO>> listar() {
        return ResponseEntity.ok(productoService.listarProductosActivos());
    }

    // GET /api/productos/buscar?q= — Busca productos por nombre
    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoCatalogoDTO>> buscar(@RequestParam("q") String query) {
        return ResponseEntity.ok(productoService.buscarPorNombre(query));
    }

    // GET /api/productos/{id} — Obtiene el detalle de un producto
    @GetMapping("/{id}")
    public ResponseEntity<ProductoCatalogoDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtenerProductoPorId(id));
    }

    // GET /api/productos/categoria/{id} — Filtra productos por categoría
    @GetMapping("/categoria/{id}")
    public ResponseEntity<List<ProductoCatalogoDTO>> porCategoria(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.listarPorCategoria(id));
    }
}
