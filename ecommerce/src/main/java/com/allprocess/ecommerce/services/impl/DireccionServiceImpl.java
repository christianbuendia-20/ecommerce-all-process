package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.response.DireccionDTO;
import com.allprocess.ecommerce.entities.DireccionUsuarioENTITY;
import com.allprocess.ecommerce.entities.UsuarioENTITY;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.repositories.DireccionUsuarioRepository;
import com.allprocess.ecommerce.repositories.UsuarioRepository;
import com.allprocess.ecommerce.services.DireccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DireccionServiceImpl implements DireccionService {

    private final DireccionUsuarioRepository direccionRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    // Devuelve todas las direcciones de un usuario
    public List<DireccionDTO> listarPorUsuario(Integer idUsuario) {
        return direccionRepository.findByUsuarioIdUsuario(idUsuario)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    @Transactional
    // Agrega una nueva dirección a un usuario
    public DireccionDTO agregarDireccion(Integer idUsuario, DireccionDTO direccionDTO) {
        UsuarioENTITY usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con ID " + idUsuario + " no encontrado"));

        DireccionUsuarioENTITY direccion = new DireccionUsuarioENTITY();
        direccion.setUsuario(usuario);
        direccion.setAlias(direccionDTO.getAlias());
        direccion.setDireccion(direccionDTO.getDireccion());
        direccion.setCiudad(direccionDTO.getCiudad());
        direccion.setReferencia(direccionDTO.getReferencia());

        direccion = direccionRepository.save(direccion);
        return toDTO(direccion);
    }

    @Override
    @Transactional
    // Elimina una dirección verificando que pertenezca al usuario
    public void eliminarDireccion(Integer idDireccion, Integer idUsuario) {
        DireccionUsuarioENTITY direccion = direccionRepository.findById(idDireccion)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección con ID " + idDireccion + " no encontrada"));

        if (!direccion.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new com.allprocess.ecommerce.exceptions.BusinessRuleException(
                    "La dirección no pertenece al usuario especificado");
        }

        direccionRepository.delete(direccion);
    }

    // Convierte entidad a DTO
    private DireccionDTO toDTO(DireccionUsuarioENTITY entity) {
        DireccionDTO dto = new DireccionDTO();
        dto.setIdDireccion(entity.getIdDireccion());
        dto.setAlias(entity.getAlias());
        dto.setDireccion(entity.getDireccion());
        dto.setCiudad(entity.getCiudad());
        dto.setReferencia(entity.getReferencia());
        return dto;
    }
}
