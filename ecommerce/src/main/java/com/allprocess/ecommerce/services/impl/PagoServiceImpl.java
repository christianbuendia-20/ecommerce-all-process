package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.response.ReciboVentaDTO;
import com.allprocess.ecommerce.entities.VentaDetalleENTITY;
import com.allprocess.ecommerce.entities.VentaENTITY;
import com.allprocess.ecommerce.enums.EstadoVentaEnum;
import com.allprocess.ecommerce.enums.MetodoPagoEnum;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.mappers.VentaMapper;
import com.allprocess.ecommerce.repositories.VentaDetalleRepository; // Inyectar esto
import com.allprocess.ecommerce.repositories.VentaRepository;
import com.allprocess.ecommerce.services.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository; // <-- 1. Añadir repositorio
    private final VentaMapper ventaMapper;

    @Override
    @Transactional
    public ReciboVentaDTO procesarPagoVenta(Integer idVenta, MetodoPagoEnum metodoPago) {
        VentaENTITY venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada."));

        if (venta.getEstado() != EstadoVentaEnum.PENDIENTE) {
            throw new BusinessRuleException("Solo se pueden pagar ventas en estado PENDIENTE.");
        }

        venta.setEstado(EstadoVentaEnum.PAGADA);
        VentaENTITY ventaActualizada = ventaRepository.save(venta);

        // <-- 2. Recuperar los detalles de la venta desde la BD
        List<VentaDetalleENTITY> detalles = ventaDetalleRepository.findByVenta_IdVenta(idVenta);

        // <-- 3. Pasar AMBOS parámetros al mapper
        return ventaMapper.toReciboDTO(ventaActualizada, detalles);
    }
}