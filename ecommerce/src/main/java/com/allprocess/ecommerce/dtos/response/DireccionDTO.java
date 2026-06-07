package com.allprocess.ecommerce.dtos.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DireccionDTO {

    private Integer idDireccion;
    private String alias;
    private String direccion;
    private String ciudad;
    private String referencia;
}
