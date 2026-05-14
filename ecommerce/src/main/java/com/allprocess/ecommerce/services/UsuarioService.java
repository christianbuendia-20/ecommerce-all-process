package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.request.RegistroUsuarioDTO;
import com.allprocess.ecommerce.dtos.response.PerfilUsuarioDTO;

public interface UsuarioService {

    // Lo que entra es el RequestDTO, lo que sale es el ResponseDTO
    PerfilUsuarioDTO registrarUsuario(RegistroUsuarioDTO registroDTO);

    PerfilUsuarioDTO obtenerPerfil(Integer idUsuario);
}