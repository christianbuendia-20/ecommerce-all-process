package com.allprocess.ecommerce.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseDTO {

    private boolean success;
    private String redirectUrl;
    private String error;
    private String mensaje;
    private String token;
    private String nombres;
    private String rol;
    // true cuando el login falla por email no verificado → frontend redirige a /verificar-email
    private boolean requiresVerification;
}
