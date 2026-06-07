package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.response.ProductoCatalogoDTO;
import com.allprocess.ecommerce.entities.CategoriaProductoENTITY;
import com.allprocess.ecommerce.entities.ProductoENTITY;
import com.allprocess.ecommerce.entities.ProveedorENTITY;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.mappers.ProductoMapper;
import com.allprocess.ecommerce.repositories.CategoriaProductoRepository;
import com.allprocess.ecommerce.repositories.ProductoRepository;
import com.allprocess.ecommerce.repositories.ProveedorRepository;
import com.allprocess.ecommerce.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaProductoRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional(readOnly = true)
    // Devuelve todos los productos activos convertidos a DTO para el catálogo
    public List<ProductoCatalogoDTO> listarProductosActivos() {
        return productoRepository.findByActivoTrue()
                .stream()
                .map(productoMapper::toCatalogoDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // Busca productos cuyo nombre contenga el texto (sin distinguir mayúsculas)
    public List<ProductoCatalogoDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre)
                .stream()
                .map(productoMapper::toCatalogoDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // Filtra productos activos por ID de categoría
    public List<ProductoCatalogoDTO> listarPorCategoria(Integer idCategoria) {
        return productoRepository.findByCategoriaIdCategoriaAndActivoTrue(idCategoria)
                .stream()
                .map(productoMapper::toCatalogoDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // Obtiene un producto por ID, lanza 404 si no existe o está inactivo
    public ProductoCatalogoDTO obtenerProductoPorId(Integer idProducto) {
        ProductoENTITY producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + idProducto + " no encontrado"));

        if (!producto.getActivo()) {
            throw new ResourceNotFoundException("Producto con ID " + idProducto + " no disponible");
        }

        return productoMapper.toCatalogoDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    // Lista todos los productos (incluyendo inactivos) para el panel de administración
    public List<ProductoENTITY> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional
    // Crea un nuevo producto validando que la categoría y proveedor existan
    public ProductoENTITY crearProducto(ProductoENTITY producto) {
        // Verificar que la categoría exista
        CategoriaProductoENTITY categoria = categoriaRepository.findById(
                producto.getCategoria().getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Categoría con ID " + producto.getCategoria().getIdCategoria() + " no encontrada"));

        // Verificar que el proveedor exista
        ProveedorENTITY proveedor = proveedorRepository.findById(
                producto.getProveedor().getIdProveedor())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Proveedor con ID " + producto.getProveedor().getIdProveedor() + " no encontrado"));

        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setActivo(true);

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    // Actualiza los campos editables de un producto
    public ProductoENTITY actualizarProducto(Integer idProducto, ProductoENTITY productoActualizado) {
        ProductoENTITY producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + idProducto + " no encontrado"));

        producto.setNombre(productoActualizado.getNombre());
        producto.setDescripcion(productoActualizado.getDescripcion());
        producto.setImagenUrl(productoActualizado.getImagenUrl());
        producto.setPrecio(productoActualizado.getPrecio());
        producto.setStock(productoActualizado.getStock());

        // Si se cambió la categoría, validar que exista
        if (productoActualizado.getCategoria() != null
                && !productoActualizado.getCategoria().getIdCategoria().equals(producto.getCategoria().getIdCategoria())) {
            CategoriaProductoENTITY categoria = categoriaRepository.findById(
                    productoActualizado.getCategoria().getIdCategoria())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada"));
            producto.setCategoria(categoria);
        }

        // Si se cambió el proveedor, validar que exista
        if (productoActualizado.getProveedor() != null
                && !productoActualizado.getProveedor().getIdProveedor().equals(producto.getProveedor().getIdProveedor())) {
            ProveedorENTITY proveedor = proveedorRepository.findById(
                    productoActualizado.getProveedor().getIdProveedor())
                    .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));
            producto.setProveedor(proveedor);
        }

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    // Desactiva un producto (borrado lógico) en lugar de eliminarlo
    public void desactivarProducto(Integer idProducto) {
        ProductoENTITY producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("Producto con ID " + idProducto + " no encontrado"));
        producto.setActivo(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaProductoENTITY> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorENTITY> listarProveedores() {
        return proveedorRepository.findAll();
    }
}
