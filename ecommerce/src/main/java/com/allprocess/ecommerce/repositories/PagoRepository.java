package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.PagoENTITY;
import com.allprocess.ecommerce.enums.MetodoPagoEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<PagoENTITY, Integer> {
    Optional<PagoENTITY> findByTransaccionId(String transaccionId);
    Optional<PagoENTITY> findFirstByVenta_IdVentaAndMetodoPago(Integer idVenta, MetodoPagoEnum metodoPago);
    Optional<PagoENTITY> findFirstByVenta_IdVenta(Integer idVenta);
}