package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.request.RegistroUsuarioDTO;
import com.allprocess.ecommerce.dtos.response.PerfilUsuarioDTO;
import com.allprocess.ecommerce.dtos.response.UsuarioAdminDTO;
import com.allprocess.ecommerce.entities.RolENTITY;
import com.allprocess.ecommerce.entities.UsuarioENTITY;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.mappers.UsuarioMapper;
import com.allprocess.ecommerce.repositories.RolRepository;
import com.allprocess.ecommerce.repositories.UsuarioRepository;
import com.allprocess.ecommerce.services.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

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

        nuevoUsuario.setPasswordHash(passwordEncoder.encode(registroDTO.getPassword()));

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

    @Override
    @Transactional
    // Actualiza nombres, apellidos y teléfono del usuario
    public PerfilUsuarioDTO actualizarPerfil(Integer idUsuario, PerfilUsuarioDTO perfilDTO) {
        UsuarioENTITY usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + idUsuario));

        usuario.setNombres(perfilDTO.getNombres());
        usuario.setApellidos(perfilDTO.getApellidos());
        usuario.setTelefono(perfilDTO.getTelefono());

        usuario = usuarioRepository.save(usuario);
        return usuarioMapper.toPerfilDTO(usuario);
    }

    @Override
    @Transactional
    // Cambia la contraseña validando la contraseña actual
    public void cambiarPassword(Integer idUsuario, String passwordActual, String passwordNueva) {
        UsuarioENTITY usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + idUsuario));

        if (!passwordEncoder.matches(passwordActual, usuario.getPasswordHash())) {
            throw new BusinessRuleException("La contraseña actual no es correcta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(passwordNueva));
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    // Lista todos los usuarios para el panel de administración
    public List<UsuarioAdminDTO> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toAdminDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // Busca usuarios por nombre o apellido
    public List<UsuarioAdminDTO> buscarPorNombre(String termino) {
        return usuarioRepository
                .findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(termino, termino)
                .stream()
                .map(this::toAdminDTO)
                .toList();
    }

    @Override
    @Transactional
    // Cambia el rol de un usuario (solo administradores)
    public UsuarioAdminDTO cambiarRol(Integer idUsuario, Integer idRol) {
        UsuarioENTITY usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + idUsuario));

        RolENTITY nuevoRol = rolRepository.findById(idRol)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el rol con ID: " + idRol));

        usuario.setRol(nuevoRol);
        usuario = usuarioRepository.save(usuario);
        return toAdminDTO(usuario);
    }

    @Override
    @Transactional
    // Activa o desactiva un usuario
    public void cambiarActivo(Integer idUsuario, Boolean activo) {
        UsuarioENTITY usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el usuario con ID: " + idUsuario));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    // Convierte una entidad Usuario a UsuarioAdminDTO
    private UsuarioAdminDTO toAdminDTO(UsuarioENTITY usuario) {
        UsuarioAdminDTO dto = new UsuarioAdminDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setEmail(usuario.getEmail());
        dto.setNombres(usuario.getNombres());
        dto.setApellidos(usuario.getApellidos());
        dto.setDni(usuario.getDni());
        dto.setTelefono(usuario.getTelefono());
        dto.setRol(usuario.getRol().getNombre());
        dto.setActivo(usuario.getActivo());
        dto.setFechaRegistro(usuario.getFechaRegistro());
        return dto;
    }
}
