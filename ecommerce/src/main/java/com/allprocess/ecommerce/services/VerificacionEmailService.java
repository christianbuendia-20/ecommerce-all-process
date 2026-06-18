package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.request.ReenviarCodigoDTO;
import com.allprocess.ecommerce.dtos.request.VerificarEmailDTO;

public interface VerificacionEmailService {

    // Genera código, lo guarda hasheado y envía el email
    void generarYEnviarCodigo(String email);

    // Valida el código, marca la cuenta como verificada
    void verificarCodigo(VerificarEmailDTO dto);

    // Reenvía un nuevo código respetando el cooldown
    void reenviarCodigo(ReenviarCodigoDTO dto);
}
