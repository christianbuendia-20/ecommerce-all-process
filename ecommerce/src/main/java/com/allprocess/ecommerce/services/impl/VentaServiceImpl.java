package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.request.CheckoutVentaDTO;
import com.allprocess.ecommerce.dtos.request.ItemCarritoDTO;
import com.allprocess.ecommerce.dtos.response.AdminVentaListaDTO;
import com.allprocess.ecommerce.dtos.response.DetalleReciboDTO;
import com.allprocess.ecommerce.dtos.response.ReciboVentaDTO;
import com.allprocess.ecommerce.entities.PagoENTITY;
import com.allprocess.ecommerce.entities.ProductoENTITY;
import com.allprocess.ecommerce.entities.UsuarioENTITY;
import com.allprocess.ecommerce.entities.VentaDetalleENTITY;
import com.allprocess.ecommerce.entities.VentaENTITY;
import com.allprocess.ecommerce.enums.EstadoVentaEnum;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.mappers.VentaMapper;
import com.allprocess.ecommerce.repositories.PagoRepository;
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
    private final PagoRepository pagoRepository;
    private final VentaMapper ventaMapper;

    @Override
    @Transactional
    public ReciboVentaDTO realizarCheckout(CheckoutVentaDTO checkoutDTO) {

        UsuarioENTITY cliente = usuarioRepository.findById(checkoutDTO.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente con ID " + checkoutDTO.getIdCliente() + " no encontrado"));

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

            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            BigDecimal subtotal = producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            VentaDetalleENTITY detalle = new VentaDetalleENTITY();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotal);

            detalles.add(detalle);
        }

        VentaENTITY venta = new VentaENTITY();
        venta.setCliente(cliente);
        venta.setDireccionEnvio(checkoutDTO.getDireccionEnvio());
        venta.setCiudadEnvio(checkoutDTO.getCiudadEnvio());
        venta.setReferenciaEnvio(null);
        venta.setCostoEnvio(BigDecimal.ZERO);
        venta.setTotal(total);
        venta.setEstado(EstadoVentaEnum.PENDIENTE_PAGO);
        venta = ventaRepository.save(venta);

        VentaENTITY ventaFinal = venta;
        List<VentaDetalleENTITY> detallesGuardados = detalles.stream()
                .map(d -> {
                    d.setVenta(ventaFinal);
                    return ventaDetalleRepository.save(d);
                })
                .toList();

        return ventaMapper.toReciboDTO(ventaFinal, detallesGuardados);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReciboVentaDTO> obtenerHistorialPorCliente(Integer idCliente) {
        List<VentaENTITY> ventas = ventaRepository.findByClienteIdUsuario(idCliente);

        return ventas.stream()
                .map(v -> {
                    List<VentaDetalleENTITY> detalles = ventaDetalleRepository.findByVenta_IdVenta(v.getIdVenta());
                    ReciboVentaDTO dto = ventaMapper.toReciboDTO(v, detalles);
                    pagoRepository.findFirstByVenta_IdVenta(v.getIdVenta())
                            .ifPresent(p -> dto.setMetodoPago(p.getMetodoPago().name()));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ReciboVentaDTO obtenerRecibo(Integer idVenta) {
        VentaENTITY venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta con ID " + idVenta + " no encontrada"));

        List<VentaDetalleENTITY> detalles = ventaDetalleRepository.findByVenta_IdVenta(idVenta);
        ReciboVentaDTO dto = ventaMapper.toReciboDTO(venta, detalles);
        pagoRepository.findFirstByVenta_IdVenta(idVenta)
                .ifPresent(p -> dto.setMetodoPago(p.getMetodoPago().name()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    // Mapea a DTO dentro de la transacción para evitar LazyInitializationException al serializar
    public List<AdminVentaListaDTO> listarTodas() {
        return ventaRepository.findAll()
                .stream()
                .map(this::toAdminDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminVentaListaDTO obtenerVentaConDetalle(Integer idVenta) {
        VentaENTITY venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta con ID " + idVenta + " no encontrada"));
        return toAdminDTO(venta);
    }

    @Override
    @Transactional
    public void actualizarEstado(Integer idVenta, String nuevoEstado) {
        VentaENTITY venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta con ID " + idVenta + " no encontrada"));

        try {
            EstadoVentaEnum estado = EstadoVentaEnum.valueOf(nuevoEstado.toUpperCase());
            venta.setEstado(estado);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Estado inválido: " + nuevoEstado
                    + ". Valores permitidos: PENDIENTE, PENDIENTE_PAGO, PAGADA, PAGO_RECHAZADO, PAGO_CANCELADO, EN_PROCESO, ENVIADA, ENTREGADA, CANCELADA");
        }

        ventaRepository.save(venta);
    }

    // Convierte VentaENTITY a DTO dentro de la transacción activa (LAZY loading seguro)
    private AdminVentaListaDTO toAdminDTO(VentaENTITY venta) {
        UsuarioENTITY cliente = venta.getCliente();
        String clienteNombre = cliente != null
                ? (cliente.getNombres() + " " + cliente.getApellidos()).trim()
                : "Cliente desconocido";
        String clienteEmail = cliente != null ? cliente.getEmail() : "";
        Integer idCliente = cliente != null ? cliente.getIdUsuario() : null;

        List<VentaDetalleENTITY> detallesEntidad =
                ventaDetalleRepository.findByVenta_IdVenta(venta.getIdVenta());

        List<DetalleReciboDTO> detallesDTO = detallesEntidad.stream()
                .map(d -> {
                    DetalleReciboDTO dto = new DetalleReciboDTO();
                    dto.setNombreProducto(d.getProducto() != null ? d.getProducto().getNombre() : "Producto");
                    dto.setCantidad(d.getCantidad());
                    dto.setPrecioUnitario(d.getPrecioUnitario());
                    dto.setSubtotal(d.getSubtotal());
                    return dto;
                })
                .toList();

        return new AdminVentaListaDTO(
                venta.getIdVenta(),
                clienteNombre,
                clienteEmail,
                idCliente,
                venta.getFecha(),
                venta.getEstado() != null ? venta.getEstado().name() : "PENDIENTE",
                venta.getTotal(),
                detallesDTO
        );
    }
}
