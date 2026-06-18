package com.allprocess.ecommerce.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class AdminVentaListaDTO {

    private Integer idVenta;
    private String clienteNombre;
    private String clienteEmail;
    private Integer idCliente;
    private LocalDateTime fecha;
    private String estado;
    private BigDecimal total;
    private List<DetalleReciboDTO> detalles;
}
