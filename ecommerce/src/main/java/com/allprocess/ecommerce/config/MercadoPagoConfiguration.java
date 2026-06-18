package com.allprocess.ecommerce.config;

import com.mercadopago.MercadoPagoConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MercadoPagoConfiguration {

    @Value("${mercadopago.access-token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        if (accessToken == null || accessToken.isBlank()) {
            log.error("[MP Config] ¡ATENCIÓN! mercadopago.access-token está VACÍO. El checkout fallará.");
            return;
        }

        // Detectar token placeholder (contiene solo ceros)
        if (accessToken.matches("TEST-0+-.+") || accessToken.contains("0000000000000000")) {
            log.warn("[MP Config] ¡ATENCIÓN! mercadopago.access-token parece ser un PLACEHOLDER.");
            log.warn("[MP Config] Reemplázalo con tu token Sandbox real desde:");
            log.warn("[MP Config] https://www.mercadopago.com.pe/developers/panel/app -> Credenciales de prueba");
        }

        if (!accessToken.startsWith("TEST-") && !accessToken.startsWith("APP_USR-")) {
            log.warn("[MP Config] El access token no empieza con TEST- (Sandbox) ni APP_USR- (Producción). Valor: {}",
                    accessToken.substring(0, Math.min(20, accessToken.length())) + "...");
        } else {
            log.info("[MP Config] Access Token configurado — entorno: {}",
                    accessToken.startsWith("TEST-") ? "SANDBOX" : "PRODUCCION");
        }

        MercadoPagoConfig.setAccessToken(accessToken);
    }
}
