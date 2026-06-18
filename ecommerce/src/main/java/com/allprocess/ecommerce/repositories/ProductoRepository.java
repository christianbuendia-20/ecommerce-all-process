package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.ProductoENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoENTITY, Integer> {
    // Listar productos para el cliente
    List<ProductoENTITY> findByActivoTrue();

    // Buscador de la tienda
    List<ProductoENTITY> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    // Filtrar por categoría
    List<ProductoENTITY> findByCategoriaIdCategoriaAndActivoTrue(Integer idCategoria);

    // Top 8 productos activos ordenados por stock descendente
    List<ProductoENTITY> findTop8ByActivoTrueOrderByStockDesc();
}