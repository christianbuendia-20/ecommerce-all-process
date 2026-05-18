package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.request.CheckoutVentaDTO;
import com.allprocess.ecommerce.dtos.response.ReciboVentaDTO;

import java.util.List;

public interface VentaService {

    // 1. Cuando el cliente procesa su carrito de compras
    ReciboVentaDTO registrarVenta(CheckoutVentaDTO checkoutDTO);

    // 2. Para ver el detalle de un recibo específico
    ReciboVentaDTO obtenerVentaPorId(Integer idVenta);

    // 3. Para el historial de compras del usuario
    List<ReciboVentaDTO> listarVentasPorUsuario(Integer idUsuario);

}