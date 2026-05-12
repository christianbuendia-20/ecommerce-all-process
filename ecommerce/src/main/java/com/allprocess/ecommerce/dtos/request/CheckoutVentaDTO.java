package com.allprocess.ecommerce.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class CheckoutVentaDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Integer idCliente;

    @NotBlank(message = "La dirección de envío es obligatoria")
    private String direccionEnvio;

    @NotBlank(message = "La ciudad de envío es obligatoria")
    private String ciudadEnvio;

    @NotEmpty(message = "El carrito no puede estar vacío")
    @Valid
    private List<ItemCarritoDTO> items;
}