package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.request.CheckoutVentaDTO;
import com.allprocess.ecommerce.dtos.request.ItemCarritoDTO;
import com.allprocess.ecommerce.dtos.response.ReciboVentaDTO;
import com.allprocess.ecommerce.entities.ProductoENTITY;
import com.allprocess.ecommerce.entities.UsuarioENTITY;
import com.allprocess.ecommerce.entities.VentaDetalleENTITY;
import com.allprocess.ecommerce.entities.VentaENTITY;
import com.allprocess.ecommerce.enums.EstadoVentaEnum;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.mappers.VentaMapper;
import com.allprocess.ecommerce.repositories.ProductoRepository;
import com.allprocess.ecommerce.repositories.UsuarioRepository;
import com.allprocess.ecommerce.repositories.VentaDetalleRepository;
import com.allprocess.ecommerce.repositories.VentaRepository;
import com.allprocess.ecommerce.services.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaMapper ventaMapper;

    @Override
    @Transactional
    // Procesa el checkout completo: valida stock, descuenta inventario y crea la venta
    public ReciboVentaDTO realizarCheckout(CheckoutVentaDTO checkoutDTO) {

        // 1. Validar que el cliente exista
        UsuarioENTITY cliente = usuarioRepository.findById(checkoutDTO.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente con ID " + checkoutDTO.getIdCliente() + " no encontrado"));

        // 2. Validar stock para cada producto y calcular totales
        BigDecimal total = BigDecimal.ZERO;
        List<VentaDetalleENTITY> detalles = new ArrayList<>();

        for (ItemCarritoDTO item : checkoutDTO.getItems()) {
            ProductoENTITY producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto con ID " + item.getIdProducto() + " no encontrado"));

            if (!producto.getActivo()) {
                throw new BusinessRuleException("El producto " + producto.getNombre() + " no está disponible");
            }

            if (producto.getStock() < item.getCantidad()) {
                throw new BusinessRuleException(
                        "Stock insuficiente para " + producto.getNombre()
                        + ". Disponible: " + producto.getStock()
                        + ", solicitado: " + item.getCantidad());
            }

            // 3. Descontar stock del producto
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            // 4. Calcular subtotal del detalle
            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            // 5. Crear el detalle de la venta
            VentaDetalleENTITY detalle = new VentaDetalleENTITY();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotal);

            detalles.add(detalle);
        }

        // 6. Crear la venta (sin costo de envío por ahora, se puede personalizar después)
        VentaENTITY venta = new VentaENTITY();
        venta.setCliente(cliente);
        venta.setDireccionEnvio(checkoutDTO.getDireccionEnvio());
        venta.setCiudadEnvio(checkoutDTO.getCiudadEnvio());
        venta.setReferenciaEnvio(null);
        venta.setCostoEnvio(BigDecimal.ZERO);
        venta.setTotal(total);
        venta.setEstado(EstadoVentaEnum.PENDIENTE);
        venta = ventaRepository.save(venta);

        // 7. Guardar todos los detalles asociados a la venta
        VentaENTITY ventaFinal = venta;
        List<VentaDetalleENTITY> detallesGuardados = detalles.stream()
                .map(d -> {
                    d.setVenta(ventaFinal);
                    return ventaDetalleRepository.save(d);
                })
                .toList();

        // 8. Devolver el recibo completo
        return ventaMapper.toReciboDTO(ventaFinal, detallesGuardados);
    }

    @Override
    @Transactional(readOnly = true)
    // Obtiene el historial de compras de un cliente con sus recibos
    public List<ReciboVentaDTO> obtenerHistorialPorCliente(Integer idCliente) {
        List<VentaENTITY> ventas = ventaRepository.findByClienteIdUsuario(idCliente);

        return ventas.stream()
                .map(v -> {
                    List<VentaDetalleENTITY> detalles = ventaDetalleRepository.findByVenta_IdVenta(v.getIdVenta());
                    return ventaMapper.toReciboDTO(v, detalles);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    // Obtiene el recibo completo de una venta específica
    public ReciboVentaDTO obtenerRecibo(Integer idVenta) {
        VentaENTITY venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta con ID " + idVenta + " no encontrada"));

        List<VentaDetalleENTITY> detalles = ventaDetalleRepository.findByVenta_IdVenta(idVenta);
        return ventaMapper.toReciboDTO(venta, detalles);
    }

    @Override
    @Transactional(readOnly = true)
    // Lista todas las ventas del sistema (para administración)
    public List<VentaENTITY> listarTodas() {
        return ventaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    // Obtiene una venta con toda su información (para administración)
    public VentaENTITY obtenerVentaConDetalle(Integer idVenta) {
        return ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta con ID " + idVenta + " no encontrada"));
    }

    @Override
    @Transactional
    // Actualiza el estado de una venta (ej: PENDIENTE -> PAGADA -> ENVIADA -> ENTREGADA)
    public VentaENTITY actualizarEstado(Integer idVenta, String nuevoEstado) {
        VentaENTITY venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta con ID " + idVenta + " no encontrada"));

        try {
            EstadoVentaEnum estado = EstadoVentaEnum.valueOf(nuevoEstado.toUpperCase());
            venta.setEstado(estado);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Estado inválido: " + nuevoEstado
                    + ". Valores permitidos: PENDIENTE, PAGADA, ENVIADA, ENTREGADA, CANCELADA");
        }

        return ventaRepository.save(venta);
    }
}
