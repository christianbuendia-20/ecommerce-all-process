package com.allprocess.ecommerce.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCarritoDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Integer idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "Debe comprar al menos 1 unidad")
    private Integer cantidad;
}