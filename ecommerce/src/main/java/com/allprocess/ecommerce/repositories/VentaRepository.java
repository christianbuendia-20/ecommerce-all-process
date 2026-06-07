package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.VentaENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<VentaENTITY, Integer> {

    // Ver historial de compras de un cliente
    List<VentaENTITY> findByClienteIdUsuario(Integer idCliente);

    // Contar ventas en un rango de fechas
    long countByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Sumar total de ventas en un rango de fechas
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM VentaENTITY v WHERE v.fecha BETWEEN :inicio AND :fin")
    BigDecimal sumTotalByFechaBetween(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // Ventas agrupadas por mes (para reportes)
    @Query("SELECT YEAR(v.fecha) as anio, MONTH(v.fecha) as mes, COUNT(v) as cantidad, COALESCE(SUM(v.total), 0) as total " +
           "FROM VentaENTITY v GROUP BY YEAR(v.fecha), MONTH(v.fecha) ORDER BY anio DESC, mes DESC")
    List<Object[]> ventasAgrupadasPorMes();

    // Productos más vendidos
    @Query("SELECT vd.producto.idProducto, vd.producto.nombre, SUM(vd.cantidad) as total " +
           "FROM VentaDetalleENTITY vd GROUP BY vd.producto.idProducto, vd.producto.nombre " +
           "ORDER BY total DESC")
    List<Object[]> productosMasVendidos();
}