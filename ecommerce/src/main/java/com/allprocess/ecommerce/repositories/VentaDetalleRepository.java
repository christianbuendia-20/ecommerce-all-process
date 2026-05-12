package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.VentaDetalleENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalleENTITY, Integer> {
    // Ver los productos de una venta específica
    List<VentaDetalleENTITY> findByVentaIdVenta(Integer idVenta);
}
