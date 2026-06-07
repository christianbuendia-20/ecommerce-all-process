package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.request.RegistroPagoDTO;
import com.allprocess.ecommerce.entities.PagoENTITY;

import java.util.List;

public interface PagoService {

    // Registra un pago contra una venta
    PagoENTITY registrarPago(RegistroPagoDTO pagoDTO);

    // Confirma un pago (cambia estado a APROBADO)
    PagoENTITY confirmarPago(Integer idPago);

    // Lista los pagos de una venta específica
    List<PagoENTITY> listarPagosPorVenta(Integer idVenta);

    // ==========================================
    // MÉTODOS DE ADMINISTRACIÓN
    // ==========================================

    // Lista todos los pagos del sistema
    List<PagoENTITY> listarTodos();

    // Cambia el estado de un pago
    PagoENTITY actualizarEstadoPago(Integer idPago, String nuevoEstado);
}
