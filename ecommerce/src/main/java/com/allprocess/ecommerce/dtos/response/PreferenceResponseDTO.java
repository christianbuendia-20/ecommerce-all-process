package com.allprocess.ecommerce.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PreferenceResponseDTO {
    private String preferenceId;
    private String checkoutUrl;
}
