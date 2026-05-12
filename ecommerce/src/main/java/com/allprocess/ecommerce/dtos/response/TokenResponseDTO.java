package com.allprocess.ecommerce.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TokenResponseDTO {

    private String token;
    private String tipo = "Bearer";

    public TokenResponseDTO(String token) {
        this.token = token;
    }
}