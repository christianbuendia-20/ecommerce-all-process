package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.request.CheckoutVentaDTO;
import com.allprocess.ecommerce.dtos.response.AdminVentaListaDTO;
import com.allprocess.ecommerce.dtos.response.ReciboVentaDTO;

import java.util.List;

public interface VentaService {

    // Procesa el checkout: valida stock, descuenta inventario y crea la venta
    ReciboVentaDTO realizarCheckout(CheckoutVentaDTO checkoutDTO);

    // Obtiene el historial de compras de un cliente
    List<ReciboVentaDTO> obtenerHistorialPorCliente(Integer idCliente);

    // Obtiene el recibo de una venta específica
    ReciboVentaDTO obtenerRecibo(Integer idVenta);

    // ==========================================
    // MÉTODOS DE ADMINISTRACIÓN
    // ==========================================

    // Lista todas las ventas del sistema (con info de cliente y detalles)
    List<AdminVentaListaDTO> listarTodas();

    // Obtiene el detalle completo de una venta para administración
    AdminVentaListaDTO obtenerVentaConDetalle(Integer idVenta);

    // Actualiza el estado de una venta
    void actualizarEstado(Integer idVenta, String nuevoEstado);
}
