package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.VentaENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<VentaENTITY, Integer> {
    // Ver historial de compras de un cliente
    List<VentaENTITY> findByClienteIdUsuario(Integer idCliente);
}