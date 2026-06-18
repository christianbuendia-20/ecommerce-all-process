package com.allprocess.ecommerce.controllers.api;

import com.allprocess.ecommerce.dtos.request.CreatePreferenceRequestDTO;
import com.allprocess.ecommerce.dtos.response.PreferenceResponseDTO;
import com.allprocess.ecommerce.services.MercadoPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @Value("${mercadopago.back-url.success}")
    private String backUrlSuccess;

    @Value("${mercadopago.back-url.failure}")
    private String backUrlFailure;

    @Value("${mercadopago.back-url.pending}")
    private String backUrlPending;

    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    /** Diagnóstico rápido de configuración — GET /api/payments/diagnostico */
    @GetMapping("/diagnostico")
    public ResponseEntity<Map<String, Object>> diagnostico() {
        Map<String, Object> info = new LinkedHashMap<>();

        String tokenStatus;
        String tokenPreview = "";
        if (accessToken == null || accessToken.isBlank()) {
            tokenStatus = "VACIO — error crítico";
        } else if (accessToken.contains("0000000000000000")) {
            tokenStatus = "PLACEHOLDER — debes reemplazarlo con tu token real de Sandbox";
        } else if (accessToken.startsWith("TEST-")) {
            tokenStatus = "OK — Sandbox";
            tokenPreview = accessToken.substring(0, Math.min(20, accessToken.length())) + "...";
        } else if (accessToken.startsWith("APP_USR-")) {
            tokenStatus = "OK — Producción (¡no usar con Sandbox!)";
            tokenPreview = accessToken.substring(0, Math.min(20, accessToken.length())) + "...";
        } else {
            tokenStatus = "FORMATO INVÁLIDO";
            tokenPreview = accessToken.substring(0, Math.min(15, accessToken.length())) + "...";
        }

        info.put("token_status", tokenStatus);
        info.put("token_preview", tokenPreview);
        info.put("sdk_token_long", accessToken != null ? accessToken.length() : 0);
        info.put("back_url_success", backUrlSuccess);
        info.put("back_url_failure", backUrlFailure);
        info.put("back_url_pending", backUrlPending);
        info.put("notification_url", notificationUrl.isBlank() ? "(vacío — webhooks desactivados)" : notificationUrl);
        info.put("instrucciones",
                "Si token_status dice PLACEHOLDER, ve a https://www.mercadopago.com.pe/developers/panel/app "
                + "→ Selecciona o crea una aplicación → Credenciales de prueba → copia el Access Token (TEST-...)");

        return ResponseEntity.ok(info);
    }

    @PostMapping("/preference")
    public ResponseEntity<PreferenceResponseDTO> crearPreferencia(
            @Valid @RequestBody CreatePreferenceRequestDTO request) {
        PreferenceResponseDTO response = mercadoPagoService.crearPreferencia(request.getIdVenta());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> procesarWebhook(@RequestBody Map<String, Object> payload) {
        mercadoPagoService.procesarNotificacion(payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{paymentId}")
    public ResponseEntity<Map<String, String>> obtenerEstado(@PathVariable String paymentId) {
        return ResponseEntity.ok(mercadoPagoService.obtenerEstadoPago(paymentId));
    }
}
