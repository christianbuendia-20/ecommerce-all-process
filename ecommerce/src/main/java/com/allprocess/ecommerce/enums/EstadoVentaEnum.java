package com.allprocess.ecommerce.enums;

public enum EstadoVentaEnum {
    PENDIENTE,  // El cliente creó la orden pero no ha pagado
    PAGADA,     // El pago se confirmó, lista para preparar
    ENVIADA,    // El courier ya la tiene
    ENTREGADA,  // El cliente la recibió (Fin del flujo feliz)
    CANCELADA   // El cliente se arrepintió o no pagó a tiempo
}