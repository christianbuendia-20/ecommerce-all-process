package com.allprocess.ecommerce.controllers.api.admin;

import com.allprocess.ecommerce.entities.ProductoENTITY;
import com.allprocess.ecommerce.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
public class AdminProductoController {

    private final ProductoService productoService;

    // GET /api/admin/productos — Lista todos los productos (activos e inactivos)
    @GetMapping
    public ResponseEntity<List<ProductoENTITY>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    // GET /api/admin/productos/{id} — Obtiene un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<ProductoENTITY> obtener(@PathVariable Integer id) {
        ProductoENTITY producto = productoService.listarTodos().stream()
                .filter(p -> p.getIdProducto().equals(id))
                .findFirst()
                .orElse(null);
        if (producto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(producto);
    }

    // POST /api/admin/productos — Crea un nuevo producto
    @PostMapping
    public ResponseEntity<ProductoENTITY> crear(@RequestBody ProductoENTITY producto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoService.crearProducto(producto));
    }

    // PUT /api/admin/productos/{id} — Actualiza un producto
    @PutMapping("/{id}")
    public ResponseEntity<ProductoENTITY> actualizar(
            @PathVariable Integer id,
            @RequestBody ProductoENTITY producto) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, producto));
    }

    // DELETE /api/admin/productos/{id} — Desactiva un producto (borrado lógico)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        productoService.desactivarProducto(id);
        return ResponseEntity.noContent().build();
    }
}
