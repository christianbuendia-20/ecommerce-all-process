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
import java.util.stream.Collectors;

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
    public ReciboVentaDTO registrarVenta(CheckoutVentaDTO checkoutDTO) {

        UsuarioENTITY cliente = usuarioRepository.findById(checkoutDTO.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para procesar la venta."));

        // 1. Crear la cabecera de la Venta
        VentaENTITY nuevaVenta = new VentaENTITY();
        nuevaVenta.setCliente(cliente); // Cambiado a setCliente según tu entidad
        nuevaVenta.setEstado(EstadoVentaEnum.PENDIENTE);

        // Mapear los datos de envío inmutables (asumiendo que tu DTO tiene estos getters)
        nuevaVenta.setDireccionEnvio(checkoutDTO.getDireccionEnvio());
        nuevaVenta.setCiudadEnvio(checkoutDTO.getCiudadEnvio());
        nuevaVenta.setReferenciaEnvio(checkoutDTO.getReferenciaEnvio());

        // Asignamos el costo de envío (si viene null, asignamos 0 por seguridad)
        BigDecimal costoEnvio = checkoutDTO.getCostoEnvio() != null ? checkoutDTO.getCostoEnvio() : BigDecimal.ZERO;
        nuevaVenta.setCostoEnvio(costoEnvio);

        // Nota: No usamos setFecha() porque tu entidad tiene insertable = false (la BD lo hará sola)

        VentaENTITY ventaGuardada = ventaRepository.save(nuevaVenta);

        // Inicializamos el total con el costo de envío
        BigDecimal totalVenta = costoEnvio;
        List<VentaDetalleENTITY> listaDetalles = new ArrayList<>();

        // 2. Recorrer el carrito y validar
        for (ItemCarritoDTO item : checkoutDTO.getItems()) {

            ProductoENTITY producto = productoRepository.findById(item.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("El producto con ID " + item.getIdProducto() + " no existe."));

            if (producto.getStock() < item.getCantidad()) {
                throw new BusinessRuleException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            // Descontar stock
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);

            VentaDetalleENTITY detalle = new VentaDetalleENTITY();
            detalle.setVenta(ventaGuardada);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());

            // Matemáticas con BigDecimal
            // Asumo que tu ProductoENTITY tiene private BigDecimal precio;
            BigDecimal cantidadBD = BigDecimal.valueOf(item.getCantidad());
            BigDecimal subtotal = producto.getPrecio().multiply(cantidadBD);

            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotal);

            listaDetalles.add(detalle);

            // Sumar al total general
            totalVenta = totalVenta.add(subtotal);
        }

        ventaDetalleRepository.saveAll(listaDetalles);

        // 3. Guardar el Total calculado
        ventaGuardada.setTotal(totalVenta);
        ventaRepository.save(ventaGuardada);

        return ventaMapper.toReciboDTO(ventaGuardada);
    }

    @Override
    @Transactional(readOnly = true)
    public ReciboVentaDTO obtenerVentaPorId(Integer idVenta) {
        VentaENTITY venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la venta con el ID: " + idVenta));
        return ventaMapper.toReciboDTO(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReciboVentaDTO> listarVentasPorUsuario(Integer idUsuario) {
        // En tu repositorio, el método ahora debe llamarse findByCliente_IdUsuario(Integer idUsuario)
        List<VentaENTITY> ventas = ventaRepository.findByClienteIdUsuario(idUsuario);
        return ventas.stream()
                .map(ventaMapper::toReciboDTO)
                .collect(Collectors.toList());
    }
}