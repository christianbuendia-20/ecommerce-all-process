package com.allprocess.ecommerce.repositories;

import com.allprocess.ecommerce.entities.UsuarioENTITY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioENTITY, Integer> {

    // ==========================================
    // PARA EL SISTEMA (Login y Registro)
    // ==========================================
    Optional<UsuarioENTITY> findByEmail(String email);
    boolean existsByEmail(String email);

    // ==========================================
    // PARA EL PANEL DE ADMINISTRACIÓN (Tus listas)
    // ==========================================

    // Para ver solo usuarios activos o inactivos
    List<UsuarioENTITY> findByActivo(Boolean activo);

    // Para el buscador de la tabla (Buscar por nombre o apellido)
    List<UsuarioENTITY> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(String nombres, String apellidos);
}