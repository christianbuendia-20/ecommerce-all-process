package com.allprocess.ecommerce.dtos.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreatePreferenceRequestDTO {

    @NotNull
    private Integer idVenta;
}
