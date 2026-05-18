package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.response.ProductoCatalogoDTO;
import java.util.List;

public interface ProductoService {
    List<ProductoCatalogoDTO> listarCatalogo();
    ProductoCatalogoDTO obtenerProductoPorId(Integer idProducto);
}