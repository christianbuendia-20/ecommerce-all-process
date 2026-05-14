package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.RolENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolRepository extends JpaRepository<RolENTITY, Integer> {

    // Spring Boot escribirá la consulta SQL automáticamente para buscar por nombre
    Optional<RolENTITY> findByNombre(String nombre);
}