package com.allprocess.ecommerce.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class VentaPorMesDTO {

    private int anio;
    private int mes;
    private long cantidad;
    private BigDecimal total;
}
