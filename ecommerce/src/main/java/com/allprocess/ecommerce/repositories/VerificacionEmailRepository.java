package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.VerificacionEmailENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificacionEmailRepository extends JpaRepository<VerificacionEmailENTITY, Integer> {

    // Último código válido (sin usar y no expirado) para un email
    Optional<VerificacionEmailENTITY> findTopByUsuario_EmailAndUsadoFalseAndFechaExpiracionAfterOrderByFechaCreacionDesc(
            String email, LocalDateTime ahora);

    // Último código sin usar (independientemente de expiración) — para verificar expiración
    Optional<VerificacionEmailENTITY> findTopByUsuario_EmailAndUsadoFalseOrderByFechaCreacionDesc(String email);

    // Cantidad de códigos generados en el último minuto (control de cooldown)
    long countByUsuario_EmailAndFechaCreacionAfter(String email, LocalDateTime desde);

    // Marcar todos los códigos no usados de un usuario como usados (al regenerar)
    @Modifying
    @Query("UPDATE VerificacionEmailENTITY v SET v.usado = true WHERE v.usuario.email = :email AND v.usado = false")
    void invalidarCodigosPrevios(@Param("email") String email);
}
