package com.allprocess.ecommerce.dtos.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ReciboVentaDTO {

    private Integer idVenta;
    private LocalDateTime fecha;
    private String estado;

    private String direccionEnvio;
    private String ciudadEnvio;

    private BigDecimal costoEnvio;
    private BigDecimal total;

    private List<DetalleReciboDTO> detalles;
}