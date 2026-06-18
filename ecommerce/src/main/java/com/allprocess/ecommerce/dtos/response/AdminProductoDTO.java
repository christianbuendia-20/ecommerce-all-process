package com.allprocess.ecommerce.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AdminProductoDTO {

    private Integer idProducto;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private BigDecimal precio;
    private Integer stock;
    private Boolean activo;

    // Campos planos de categoría y proveedor: evitan proxies LAZY y relaciones circulares
    private Integer idCategoria;
    private String nombreCategoria;
    private Integer idProveedor;
    private String nombreProveedor;
}
