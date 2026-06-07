package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.response.DireccionDTO;
import com.allprocess.ecommerce.entities.DireccionUsuarioENTITY;

import java.util.List;

public interface DireccionService {

    // Lista las direcciones de un usuario
    List<DireccionDTO> listarPorUsuario(Integer idUsuario);

    // Agrega una nueva dirección
    DireccionDTO agregarDireccion(Integer idUsuario, DireccionDTO direccionDTO);

    // Elimina una dirección (solo si pertenece al usuario)
    void eliminarDireccion(Integer idDireccion, Integer idUsuario);
}
