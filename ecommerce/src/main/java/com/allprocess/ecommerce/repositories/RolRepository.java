package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.RolENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<RolENTITY, Integer> {
    // Aquí no necesitamos métodos extra por ahora.
}