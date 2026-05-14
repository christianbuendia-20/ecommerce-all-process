package com.allprocess.ecommerce.mappers;

import com.allprocess.ecommerce.dtos.response.PerfilUsuarioDTO;
import com.allprocess.ecommerce.entities.UsuarioENTITY;
import org.mapstruct.Mapper;

// El componentModel = "spring" es vital para que luego podamos inyectarlo en los Servicios
@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // ========================================================
    // MAGIA PURA: Convierte una Entidad a DTO con una línea
    // ========================================================
    // Como los nombres de las variables coinciden (nombres, apellidos, email, telefono),
    // MapStruct hace el mapeo automáticamente.
    PerfilUsuarioDTO toPerfilDTO(UsuarioENTITY usuario);
}