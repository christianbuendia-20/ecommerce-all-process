package com.allprocess.ecommerce.dtos.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UsuarioAdminDTO {

    private Integer idUsuario;
    private String email;
    private String nombres;
    private String apellidos;
    private String dni;
    private String telefono;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
}
