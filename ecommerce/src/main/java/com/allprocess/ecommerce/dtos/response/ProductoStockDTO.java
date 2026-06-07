package com.allprocess.ecommerce.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProductoStockDTO {

    private Integer idProducto;
    private String nombre;
    private Integer stock;
}
