package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.dtos.request.ReenviarCodigoDTO;
import com.allprocess.ecommerce.dtos.request.VerificarEmailDTO;
import com.allprocess.ecommerce.services.VerificacionEmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificacionEmailController {

    private final VerificacionEmailService verificacionService;

    // POST /api/auth/verificar-email — valida el código de 6 dígitos
    @PostMapping("/verificar-email")
    public ResponseEntity<Map<String, String>> verificar(@Valid @RequestBody VerificarEmailDTO dto) {
        verificacionService.verificarCodigo(dto);
        return ResponseEntity.ok(Map.of("mensaje", "¡Cuenta verificada exitosamente! Ya puedes iniciar sesión."));
    }

    // POST /api/auth/reenviar-codigo — genera y reenvía un nuevo código
    @PostMapping("/reenviar-codigo")
    public ResponseEntity<Map<String, String>> reenviar(@Valid @RequestBody ReenviarCodigoDTO dto) {
        verificacionService.reenviarCodigo(dto);
        return ResponseEntity.ok(Map.of("mensaje", "Código reenviado a tu correo electrónico."));
    }
}
