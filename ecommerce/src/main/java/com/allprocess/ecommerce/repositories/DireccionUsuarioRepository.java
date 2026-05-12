package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.DireccionUsuarioENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DireccionUsuarioRepository extends JpaRepository<DireccionUsuarioENTITY, Integer> {
    // Para ver todas las direcciones de un solo cliente
    List<DireccionUsuarioENTITY> findByUsuarioIdUsuario(Integer idUsuario);
}