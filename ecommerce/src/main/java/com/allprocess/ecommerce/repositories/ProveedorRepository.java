package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.ProveedorENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends JpaRepository<ProveedorENTITY, Integer> {
}