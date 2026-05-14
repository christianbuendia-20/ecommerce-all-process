package com.allprocess.ecommerce.mappers;

import com.allprocess.ecommerce.dtos.response.ProductoCatalogoDTO;
import com.allprocess.ecommerce.entities.ProductoENTITY;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    // Le decimos: "Saca el 'nombre' de la variable 'categoria' y ponlo en 'nombreCategoria'"
    @Mapping(source = "categoria.nombre", target = "nombreCategoria")
    @Mapping(source = "proveedor.nombre", target = "nombreProveedor")
    ProductoCatalogoDTO toCatalogoDTO(ProductoENTITY producto);
}