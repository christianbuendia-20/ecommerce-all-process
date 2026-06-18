package com.allprocess.ecommerce.controllers.api.admin;

import com.allprocess.ecommerce.dtos.response.AdminProductoDTO;
import com.allprocess.ecommerce.entities.ProductoENTITY;
import com.allprocess.ecommerce.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/productos")
@RequiredArgsConstructor
public class AdminProductoController {

    private final ProductoService productoService;

    // GET /api/admin/productos — Lista todos los productos (activos e inactivos) como DTO
    @GetMapping
    public ResponseEntity<List<AdminProductoDTO>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    // GET /api/admin/productos/{id} — Obtiene un producto por ID como DTO
    @GetMapping("/{id}")
    public ResponseEntity<AdminProductoDTO> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtenerAdminById(id));
    }

    // POST /api/admin/productos — Crea un nuevo producto
    @PostMapping
    public ResponseEntity<ProductoENTITY> crear(@RequestBody ProductoENTITY producto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoService.crearProducto(producto));
    }

    // PUT /api/admin/productos/{id} — Actualiza un producto
    @PutMapping("/{id}")
    public ResponseEntity<AdminProductoDTO> actualizar(
            @PathVariable Integer id,
            @RequestBody ProductoENTITY producto) {
        productoService.actualizarProducto(id, producto);
        return ResponseEntity.ok(productoService.obtenerAdminById(id));
    }

    // DELETE /api/admin/productos/{id} — Desactiva un producto (borrado lógico)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        productoService.desactivarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/admin/productos/{id}/activar — Reactiva un producto desactivado
    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Integer id) {
        productoService.activarProducto(id);
        return ResponseEntity.ok().build();
    }

    // POST /api/admin/productos/{id}/imagen — Sube o reemplaza la imagen del producto
    @PostMapping(value = "/{id}/imagen", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdminProductoDTO> subirImagen(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        productoService.actualizarImagen(id, file);
        return ResponseEntity.ok(productoService.obtenerAdminById(id));
    }

    // DELETE /api/admin/productos/{id}/imagen — Elimina la imagen del producto
    @DeleteMapping("/{id}/imagen")
    public ResponseEntity<Void> eliminarImagen(@PathVariable Integer id) {
        productoService.eliminarImagen(id);
        return ResponseEntity.noContent().build();
    }
}
