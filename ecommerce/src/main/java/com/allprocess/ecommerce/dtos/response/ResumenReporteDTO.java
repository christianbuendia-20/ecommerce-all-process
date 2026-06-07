package com.allprocess.ecommerce.dtos.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class ResumenReporteDTO {

    private long totalProductos;
    private long totalUsuarios;
    private long totalVentas;
    private BigDecimal ventasDelMes;
    private List<ProductoStockDTO> productosBajoStock;
    private List<VentaPorMesDTO> ventasPorMes;
    private List<ProductoMasVendidoDTO> productosMasVendidos;
}
