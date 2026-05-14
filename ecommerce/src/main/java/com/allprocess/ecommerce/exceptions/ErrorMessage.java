package com.allprocess.ecommerce.exceptions;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ErrorMessage {
    private LocalDateTime fechaHora;
    private Integer estado; // Ej: 400, 404
    private String error;   // Ej: "Not Found", "Bad Request"
    private String mensaje; // Ej: "El producto no existe"
    private String ruta;    // Ej: "/api/productos/5"
}