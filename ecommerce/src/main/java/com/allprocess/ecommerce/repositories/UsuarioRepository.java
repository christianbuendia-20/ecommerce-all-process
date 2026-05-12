package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.UsuarioENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioENTITY, Integer> {

    // MAGIA DE SPRING: Solo con nombrar el método así, Spring Boot
    // crea el SQL: SELECT * FROM usuario WHERE email = ?
    Optional<UsuarioENTITY> findByEmail(String email);

    // Para validar en el registro si el correo ya existe
    boolean existsByEmail(String email);
}