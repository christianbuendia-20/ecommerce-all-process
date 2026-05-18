package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.response.ReciboVentaDTO;
import com.allprocess.ecommerce.enums.MetodoPagoEnum;

public interface PagoService {
    ReciboVentaDTO procesarPagoVenta(Integer idVenta, MetodoPagoEnum metodoPago);
}