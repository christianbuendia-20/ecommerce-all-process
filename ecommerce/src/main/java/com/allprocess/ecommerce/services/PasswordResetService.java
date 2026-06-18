package com.allprocess.ecommerce.services;

public interface PasswordResetService {
    void solicitarReset(String email);
    void verificarCodigo(String email, String codigo);
    void cambiarPassword(String email, String codigo, String nuevaPassword);
}
