package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.PagoENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<PagoENTITY, Integer> {
    // Buscar un pago por el ID de la pasarela (Niubiz/Paypal/etc)
    Optional<PagoENTITY> findByTransaccionId(String transaccionId);
}