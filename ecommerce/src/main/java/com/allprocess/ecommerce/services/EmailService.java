package com.allprocess.ecommerce.services;

public interface EmailService {
    void enviarCodigoVerificacion(String destinatario, String nombres, String codigo);
}
