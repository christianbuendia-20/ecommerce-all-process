package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.response.ProductoCatalogoDTO;
import com.allprocess.ecommerce.entities.ProductoENTITY;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.mappers.ProductoMapper;
import com.allprocess.ecommerce.repositories.ProductoRepository;
import com.allprocess.ecommerce.services.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @Override
    @Transactional(readOnly = true) // Solo lectura, más rápido
    public List<ProductoCatalogoDTO> listarCatalogo() {

        // 1. Buscamos TODOS los productos en la base de datos
        List<ProductoENTITY> productos = productoRepository.findAll();

        // 2. Usamos el Mapper para traducir la lista de Entidades a lista de DTOs
        // (Usamos un "Stream" de Java que es la forma moderna y pro de recorrer listas)
        return productos.stream()
                .map(producto -> productoMapper.toCatalogoDTO(producto))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoCatalogoDTO obtenerProductoPorId(Integer idProducto) {

        // 1. Buscamos el producto. Si no existe, escudo de error 404
        ProductoENTITY producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new ResourceNotFoundException("El producto con ID " + idProducto + " no se encuentra disponible en All Process."));

        // 2. Lo traducimos y lo enviamos
        return productoMapper.toCatalogoDTO(producto);
    }
}