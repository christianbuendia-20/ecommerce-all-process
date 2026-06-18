package com.allprocess.ecommerce.converters;

import com.allprocess.ecommerce.enums.MetodoPagoEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Convierte la columna pago.metodo_pago hacia/desde MetodoPagoEnum.
 * Mapea valores legacy de la BD a los nombres actuales del enum
 * para evitar IllegalArgumentException con datos históricos.
 *
 * Valores legacy reconocidos:
 *   TARJETA_CREDITO      → TARJETA
 *   TARJETA_DEBITO       → TARJETA
 *   TRANSFERENCIA_BANCARIA → TRANSFERENCIA
 *   PAYPAL               → TRANSFERENCIA
 */
@Slf4j
@Converter(autoApply = false)
public class MetodoPagoConverter implements AttributeConverter<MetodoPagoEnum, String> {

    @Override
    public String convertToDatabaseColumn(MetodoPagoEnum attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public MetodoPagoEnum convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return MetodoPagoEnum.TRANSFERENCIA;
        }
        return switch (dbData.trim().toUpperCase()) {
            case "TARJETA_CREDITO" -> {
                log.warn("[MetodoPago] Valor legacy '{}' mapeado a TARJETA. Ejecutar migración SQL.", dbData);
                yield MetodoPagoEnum.TARJETA;
            }
            case "TARJETA_DEBITO" -> {
                log.warn("[MetodoPago] Valor legacy '{}' mapeado a TARJETA. Ejecutar migración SQL.", dbData);
                yield MetodoPagoEnum.TARJETA;
            }
            case "TRANSFERENCIA_BANCARIA" -> {
                log.warn("[MetodoPago] Valor legacy '{}' mapeado a TRANSFERENCIA. Ejecutar migración SQL.", dbData);
                yield MetodoPagoEnum.TRANSFERENCIA;
            }
            case "PAYPAL" -> {
                log.warn("[MetodoPago] Valor legacy '{}' mapeado a TRANSFERENCIA. Ejecutar migración SQL.", dbData);
                yield MetodoPagoEnum.TRANSFERENCIA;
            }
            default -> {
                try {
                    yield MetodoPagoEnum.valueOf(dbData.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.error("[MetodoPago] Valor desconocido '{}' en BD. Usando TRANSFERENCIA como fallback.", dbData);
                    yield MetodoPagoEnum.TRANSFERENCIA;
                }
            }
        };
    }
}
