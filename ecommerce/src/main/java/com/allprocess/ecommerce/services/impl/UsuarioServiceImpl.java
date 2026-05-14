package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.request.RegistroUsuarioDTO;
import com.allprocess.ecommerce.dtos.response.PerfilUsuarioDTO;
import com.allprocess.ecommerce.entities.RolENTITY;
import com.allprocess.ecommerce.entities.UsuarioENTITY;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.mappers.UsuarioMapper;
import com.allprocess.ecommerce.repositories.RolRepository;
import com.allprocess.ecommerce.repositories.UsuarioRepository;
import com.allprocess.ecommerce.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    @Transactional // Asegura que si algo falla, no se guarde nada a medias
    public PerfilUsuarioDTO registrarUsuario(RegistroUsuarioDTO registroDTO) {

        // 1. Validar si el correo ya está en uso
        if (usuarioRepository.existsByEmail(registroDTO.getEmail())) {
            throw new BusinessRuleException("El correo " + registroDTO.getEmail() + " ya está registrado en All Process.");
        }

        // 2. Buscar el Rol "CLIENTE" en la tabla de roles
        RolENTITY rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("Error: El rol CLIENTE no existe en la base de datos."));

        // 3. Crear la nueva entidad y mapear los datos del DTO
        UsuarioENTITY nuevoUsuario = new UsuarioENTITY();
        nuevoUsuario.setEmail(registroDTO.getEmail());

        // Nota: Más adelante en la sección de Security encriptaremos esta clave
        nuevoUsuario.setPasswordHash(registroDTO.getPassword());

        nuevoUsuario.setNombres(registroDTO.getNombres());
        nuevoUsuario.setApellidos(registroDTO.getApellidos());
        nuevoUsuario.setTelefono(registroDTO.getTelefono());
        nuevoUsuario.setActivo(true);

        // Asignamos el objeto Rol que encontramos
        nuevoUsuario.setRol(rolCliente);

        // 4. Guardar en la base de datos
        UsuarioENTITY usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // 5. Devolver los datos limpios a través del Mapper
        return usuarioMapper.toPerfilDTO(usuarioGuardado);
    }

    @Override
    @Transactional(readOnly = true) // Optimiza la consulta ya que solo es de lectura
    public PerfilUsuarioDTO obtenerPerfil(Integer idUsuario) {

        // Buscar usuario por ID o lanzar error 404
        UsuarioENTITY usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + idUsuario));

        return usuarioMapper.toPerfilDTO(usuario);
    }
}