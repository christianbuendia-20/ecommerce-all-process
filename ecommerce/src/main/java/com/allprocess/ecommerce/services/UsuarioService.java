package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.request.RegistroUsuarioDTO;
import com.allprocess.ecommerce.dtos.response.PerfilUsuarioDTO;
import com.allprocess.ecommerce.dtos.response.UsuarioAdminDTO;
import com.allprocess.ecommerce.entities.UsuarioENTITY;

import java.util.List;

public interface UsuarioService {

    // Lo que entra es el RequestDTO, lo que sale es el ResponseDTO
    PerfilUsuarioDTO registrarUsuario(RegistroUsuarioDTO registroDTO);

    PerfilUsuarioDTO obtenerPerfil(Integer idUsuario);

    // ==========================================
    // MÉTODOS ADICIONALES PARA EL PERFIL
    // ==========================================

    // Actualiza los datos personales del usuario
    PerfilUsuarioDTO actualizarPerfil(Integer idUsuario, PerfilUsuarioDTO perfilDTO);

    // Cambia la contraseña del usuario (requiere contraseña actual)
    void cambiarPassword(Integer idUsuario, String passwordActual, String passwordNueva);

    // ==========================================
    // MÉTODOS DE ADMINISTRACIÓN
    // ==========================================

    // Lista todos los usuarios
    List<UsuarioAdminDTO> listarTodos();

    // Busca usuarios por nombre o apellido
    List<UsuarioAdminDTO> buscarPorNombre(String termino);

    // Cambia el rol de un usuario
    UsuarioAdminDTO cambiarRol(Integer idUsuario, Integer idRol);

    // Activa o desactiva un usuario
    void cambiarActivo(Integer idUsuario, Boolean activo);
}