package com.allprocess.ecommerce.dtos.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class DetalleReciboDTO {

    private String nombreProducto;
    private String imagenUrl;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
