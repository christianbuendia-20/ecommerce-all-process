package com.allprocess.ecommerce.converters;

import com.allprocess.ecommerce.enums.EstadoVentaEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * Convierte la columna venta.estado hacia/desde EstadoVentaEnum.
 * Mapea valores legacy de la BD a los nombres actuales del enum
 * para evitar IllegalArgumentException con datos históricos.
 *
 * Valores legacy reconocidos:
 *   PROCESANDO → EN_PROCESO
 *   EN_CAMINO  → ENVIADA
 *   COMPLETADO → ENTREGADA
 */
@Slf4j
@Converter(autoApply = false)
public class EstadoVentaConverter implements AttributeConverter<EstadoVentaEnum, String> {

    @Override
    public String convertToDatabaseColumn(EstadoVentaEnum attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public EstadoVentaEnum convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return EstadoVentaEnum.PENDIENTE;
        }
        return switch (dbData.trim().toUpperCase()) {
            // Mapeos de compatibilidad con valores anteriores
            case "PROCESANDO" -> {
                log.warn("[EstadoVenta] Valor legacy '{}' mapeado a EN_PROCESO. Ejecutar migración SQL.", dbData);
                yield EstadoVentaEnum.EN_PROCESO;
            }
            case "EN_CAMINO" -> {
                log.warn("[EstadoVenta] Valor legacy '{}' mapeado a ENVIADA. Ejecutar migración SQL.", dbData);
                yield EstadoVentaEnum.ENVIADA;
            }
            case "COMPLETADO" -> {
                log.warn("[EstadoVenta] Valor legacy '{}' mapeado a ENTREGADA. Ejecutar migración SQL.", dbData);
                yield EstadoVentaEnum.ENTREGADA;
            }
            default -> {
                try {
                    yield EstadoVentaEnum.valueOf(dbData.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    log.error("[EstadoVenta] Valor desconocido '{}' en BD. Usando PENDIENTE como fallback.", dbData);
                    yield EstadoVentaEnum.PENDIENTE;
                }
            }
        };
    }
}
