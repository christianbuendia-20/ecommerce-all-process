package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.PasswordResetENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetRepository extends JpaRepository<PasswordResetENTITY, Integer> {

    Optional<PasswordResetENTITY> findTopByUsuario_EmailAndUsadoFalseOrderByFechaCreacionDesc(String email);

    long countByUsuario_EmailAndFechaCreacionAfter(String email, LocalDateTime desde);

    @Modifying
    @Query("UPDATE PasswordResetENTITY p SET p.usado = true WHERE p.usuario.email = :email AND p.usado = false")
    void invalidarResetsPrevios(@Param("email") String email);
}
