package com.allprocess.ecommerce.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilUsuarioDTO {

    // Aquí sí mandamos el ID por si React lo necesita para otras consultas
    private Integer idUsuario;
    private String email;
    private String nombres;
    private String apellidos;
    private String telefono;

    // NUNCA PONEMOS EL PASSWORD AQUÍ
}