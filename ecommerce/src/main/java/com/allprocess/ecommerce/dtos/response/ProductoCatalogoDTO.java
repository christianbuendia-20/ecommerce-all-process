package com.allprocess.ecommerce.dtos.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductoCatalogoDTO {

    private Integer idProducto;
    private String nombre;
    private String descripcion;
    private String imagenUrl;
    private BigDecimal precio;
    private Integer stock;

    // ===================================================
    // EL TRUCO DEL DTO: Aplanamos las relaciones
    // ===================================================
    // En lugar de mandar entidades completas, solo mandamos los nombres.
    // Esto hace que tu JSON sea súper ligero y rápido de descargar.
    private String nombreCategoria;
    private String nombreProveedor;
}