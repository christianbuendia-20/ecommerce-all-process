package com.allprocess.ecommerce.controllers.api.admin;

import com.allprocess.ecommerce.dtos.response.ProductoMasVendidoDTO;
import com.allprocess.ecommerce.dtos.response.ProductoStockDTO;
import com.allprocess.ecommerce.dtos.response.ResumenReporteDTO;
import com.allprocess.ecommerce.dtos.response.VentaPorMesDTO;
import com.allprocess.ecommerce.entities.ProductoENTITY;
import com.allprocess.ecommerce.entities.VentaENTITY;
import com.allprocess.ecommerce.repositories.ProductoRepository;
import com.allprocess.ecommerce.repositories.UsuarioRepository;
import com.allprocess.ecommerce.repositories.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reportes")
@RequiredArgsConstructor
public class AdminReporteController {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    // GET /api/admin/reportes/resumen — Totales generales del dashboard
    @GetMapping("/resumen")
    public ResponseEntity<ResumenReporteDTO> resumen() {
        long totalProductos = productoRepository.count();
        long totalUsuarios = usuarioRepository.count();
        long totalVentas = ventaRepository.count();

        // Ventas del mes actual
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal ventasDelMes = ventaRepository.sumTotalByFechaBetween(inicioMes, finMes);

        // Productos con stock bajo (menos de 10 unidades)
        List<ProductoStockDTO> bajoStock = productoRepository.findByActivoTrue().stream()
                .filter(p -> p.getStock() < 10)
                .map(p -> new ProductoStockDTO(p.getIdProducto(), p.getNombre(), p.getStock()))
                .toList();

        // Ventas agrupadas por mes
        List<VentaPorMesDTO> ventasPorMes = ventaRepository.ventasAgrupadasPorMes().stream()
                .map(row -> new VentaPorMesDTO(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3]))
                .toList();

        // Productos más vendidos
        List<ProductoMasVendidoDTO> masVendidos = ventaRepository.productosMasVendidos().stream()
                .limit(10)
                .map(row -> new ProductoMasVendidoDTO(
                        ((Number) row[0]).intValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()))
                .toList();

        ResumenReporteDTO reporte = ResumenReporteDTO.builder()
                .totalProductos(totalProductos)
                .totalUsuarios(totalUsuarios)
                .totalVentas(totalVentas)
                .ventasDelMes(ventasDelMes)
                .productosBajoStock(bajoStock)
                .ventasPorMes(ventasPorMes)
                .productosMasVendidos(masVendidos)
                .build();

        return ResponseEntity.ok(reporte);
    }
}
