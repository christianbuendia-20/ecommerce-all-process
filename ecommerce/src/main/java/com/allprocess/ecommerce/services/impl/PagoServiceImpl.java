package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.request.RegistroPagoDTO;
import com.allprocess.ecommerce.entities.PagoENTITY;
import com.allprocess.ecommerce.entities.VentaENTITY;
import com.allprocess.ecommerce.enums.EstadoPagoEnum;
import com.allprocess.ecommerce.enums.MetodoPagoEnum;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.repositories.PagoRepository;
import com.allprocess.ecommerce.repositories.VentaRepository;
import com.allprocess.ecommerce.services.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final VentaRepository ventaRepository;

    @Override
    @Transactional
    // Registra un pago contra una venta, validando que no exceda el total
    public PagoENTITY registrarPago(RegistroPagoDTO pagoDTO) {

        // 1. Validar que la venta exista
        VentaENTITY venta = ventaRepository.findById(pagoDTO.getIdVenta())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Venta con ID " + pagoDTO.getIdVenta() + " no encontrada"));

        // 2. Validar el método de pago
        MetodoPagoEnum metodoPago;
        try {
            metodoPago = MetodoPagoEnum.valueOf(pagoDTO.getMetodoPago().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Método de pago inválido: " + pagoDTO.getMetodoPago()
                    + ". Valores: TARJETA, YAPE, TRANSFERENCIA, MERCADO_PAGO");
        }

        // 3. Validar que el monto no exceda el total de la venta
        BigDecimal totalPagado = pagoRepository.findAll().stream()
                .filter(p -> p.getVenta().getIdVenta().equals(pagoDTO.getIdVenta())
                        && p.getEstado() == EstadoPagoEnum.APROBADO)
                .map(PagoENTITY::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPagado.add(pagoDTO.getMonto()).compareTo(venta.getTotal()) > 0) {
            throw new BusinessRuleException(
                    "El monto del pago excede el saldo pendiente. Total venta: S/"
                    + venta.getTotal() + ", ya pagado: S/" + totalPagado);
        }

        // 4. Crear el pago
        PagoENTITY pago = new PagoENTITY();
        pago.setVenta(venta);
        pago.setMetodoPago(metodoPago);
        pago.setMonto(pagoDTO.getMonto());
        pago.setTransaccionId(pagoDTO.getTransaccionId());
        pago.setEstado(EstadoPagoEnum.PENDIENTE);

        return pagoRepository.save(pago);
    }

    @Override
    @Transactional
    // Confirma un pago (cambia estado a APROBADO) y si el total está cubierto, marca la venta como PAGADA
    public PagoENTITY confirmarPago(Integer idPago) {
        PagoENTITY pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago con ID " + idPago + " no encontrado"));

        pago.setEstado(EstadoPagoEnum.APROBADO);
        pago = pagoRepository.save(pago);

        // Verificar si la venta ya está totalmente pagada
        VentaENTITY venta = pago.getVenta();
        BigDecimal totalPagado = pagoRepository.findAll().stream()
                .filter(p -> p.getVenta().getIdVenta().equals(venta.getIdVenta())
                        && p.getEstado() == EstadoPagoEnum.APROBADO)
                .map(PagoENTITY::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPagado.compareTo(venta.getTotal()) >= 0) {
            venta.setEstado(com.allprocess.ecommerce.enums.EstadoVentaEnum.PAGADA);
            ventaRepository.save(venta);
        }

        return pago;
    }

    @Override
    @Transactional(readOnly = true)
    // Lista los pagos asociados a una venta
    public List<PagoENTITY> listarPagosPorVenta(Integer idVenta) {
        // Filtramos manualmente ya que no hay un método en el repositorio
        return pagoRepository.findAll().stream()
                .filter(p -> p.getVenta().getIdVenta().equals(idVenta))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // Lista todos los pagos del sistema
    public List<PagoENTITY> listarTodos() {
        return pagoRepository.findAll();
    }

    @Override
    @Transactional
    // Cambia el estado de un pago (APROBADO, RECHAZADO, REEMBOLSADO)
    public PagoENTITY actualizarEstadoPago(Integer idPago, String nuevoEstado) {
        PagoENTITY pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new ResourceNotFoundException("Pago con ID " + idPago + " no encontrado"));

        try {
            EstadoPagoEnum estado = EstadoPagoEnum.valueOf(nuevoEstado.toUpperCase());
            pago.setEstado(estado);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Estado de pago inválido: " + nuevoEstado
                    + ". Valores: PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO");
        }

        return pagoRepository.save(pago);
    }
}
