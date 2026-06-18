package com.allprocess.ecommerce.enums;

public enum EstadoVentaEnum {
    PENDIENTE,          // Orden creada sin pago (flujo legado)
    PENDIENTE_PAGO,     // Esperando confirmación de Mercado Pago
    PAGADA,             // Pago confirmado — lista para preparar
    PAGO_RECHAZADO,     // Pago rechazado por MP — stock restaurado
    PAGO_CANCELADO,     // Pago cancelado por el cliente — stock restaurado
    EN_PROCESO,         // En preparación para envío
    ENVIADA,            // Entregada al courier
    ENTREGADA,          // Recibida por el cliente
    CANCELADA           // Cancelada por administrador
}
