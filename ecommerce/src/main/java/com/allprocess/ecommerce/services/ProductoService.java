package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.response.ProductoCatalogoDTO;
import com.allprocess.ecommerce.entities.CategoriaProductoENTITY;
import com.allprocess.ecommerce.entities.ProveedorENTITY;
import com.allprocess.ecommerce.entities.ProductoENTITY;

import java.util.List;

public interface ProductoService {

    // ==========================================
    // MÉTODOS PÚBLICOS (Catálogo)
    // ==========================================

    // Lista todos los productos activos
    List<ProductoCatalogoDTO> listarProductosActivos();

    // Busca productos por nombre (LIKE)
    List<ProductoCatalogoDTO> buscarPorNombre(String nombre);

    // Filtra productos por categoría
    List<ProductoCatalogoDTO> listarPorCategoria(Integer idCategoria);

    // Obtiene el detalle de un producto por ID
    ProductoCatalogoDTO obtenerProductoPorId(Integer idProducto);

    // ==========================================
    // MÉTODOS DE ADMINISTRACIÓN
    // ==========================================

    // Lista TODOS los productos (incluyendo inactivos)
    List<ProductoENTITY> listarTodos();

    // Crea un nuevo producto
    ProductoENTITY crearProducto(ProductoENTITY producto);

    // Actualiza un producto existente
    ProductoENTITY actualizarProducto(Integer idProducto, ProductoENTITY producto);

    // Desactiva un producto (borrado lógico)
    void desactivarProducto(Integer idProducto);

    // ==========================================
    // CATÁLOGOS AUXILIARES
    // ==========================================

    List<CategoriaProductoENTITY> listarCategorias();
    List<ProveedorENTITY> listarProveedores();
}
