package com.allprocess.ecommerce.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificarEmailDTO {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String email;

    @NotBlank(message = "El código es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El código debe ser de 6 dígitos numéricos")
    private String codigo;
}
