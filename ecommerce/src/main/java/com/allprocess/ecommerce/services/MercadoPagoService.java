package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.response.PreferenceResponseDTO;

import java.util.Map;

public interface MercadoPagoService {

    PreferenceResponseDTO crearPreferencia(Integer idVenta);

    void procesarNotificacion(Map<String, Object> payload);

    Map<String, String> obtenerEstadoPago(String paymentId);
}
