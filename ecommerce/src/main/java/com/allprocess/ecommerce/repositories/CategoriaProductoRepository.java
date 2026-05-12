package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.CategoriaProductoENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProductoENTITY, Integer> {
}