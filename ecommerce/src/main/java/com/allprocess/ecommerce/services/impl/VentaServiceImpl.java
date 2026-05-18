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

// 1. CORREGIDO: Usamos getIdCliente() tal como está en tu DTO
        UsuarioENTITY cliente = usuarioRepository.findById(checkoutDTO.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado para procesar la venta."));

        // 2. Crear la cabecera de la Venta (Snapshot Inmutable)
        VentaENTITY nuevaVenta = new VentaENTITY();
        nuevaVenta.setCliente(cliente);
        nuevaVenta.setEstado(EstadoVentaEnum.PENDIENTE);

        // Mapear los datos de envío
        nuevaVenta.setDireccionEnvio(checkoutDTO.getDireccionEnvio());
        nuevaVenta.setCiudadEnvio(checkoutDTO.getCiudadEnvio());

        // CORREGIDO: Como no pides referencia en el DTO, lo dejamos en null
        nuevaVenta.setReferenciaEnvio(null);

        // CORREGIDO: Como no mandas costo de envío desde el frontend, lo seteamos a cero
        BigDecimal costoEnvio = BigDecimal.ZERO;
        nuevaVenta.setCostoEnvio(costoEnvio);

        // Guardamos para generar el id_venta
        VentaENTITY ventaGuardada = ventaRepository.save(nuevaVenta);

        // Inicializamos el total de la venta sumando el costo de envío inicial
        BigDecimal totalVenta = costoEnvio;
        List<VentaDetalleENTITY> listaDetalles = new ArrayList<>();

        // 3. Recorrer el carrito, validar stock y crear los detalles
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

            // Matemáticas de dinero con BigDecimal
            BigDecimal cantidadBD = BigDecimal.valueOf(item.getCantidad());
            BigDecimal subtotal = producto.getPrecio().multiply(cantidadBD);

            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotal);

            listaDetalles.add(detalle);
            totalVenta = totalVenta.add(subtotal);
        }

        // 4. Guardar todos los detalles en bloque
        ventaDetalleRepository.saveAll(listaDetalles);

        // 5. Actualizar el total general en la cabecera
        ventaGuardada.setTotal(totalVenta);
        ventaRepository.save(ventaGuardada);

        // 6. Traducir pasando la Venta y la lista de Detalles al Mapper
        return ventaMapper.toReciboDTO(ventaGuardada, listaDetalles);
    }

    @Override
    @Transactional(readOnly = true)
    public ReciboVentaDTO obtenerVentaPorId(Integer idVenta) {

        VentaENTITY venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la venta con el ID: " + idVenta));

        // Recuperamos los detalles manualmente para el mapper
        List<VentaDetalleENTITY> detalles = ventaDetalleRepository.findByVenta_IdVenta(idVenta);

        return ventaMapper.toReciboDTO(venta, detalles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReciboVentaDTO> listarVentasPorUsuario(Integer idUsuario) {

        // Buscamos todas las ventas del cliente
        List<VentaENTITY> ventas = ventaRepository.findByClienteIdUsuario(idUsuario);

        // Mapeamos cada venta buscando sus respectivos detalles
        return ventas.stream()
                .map(venta -> {
                    List<VentaDetalleENTITY> detalles = ventaDetalleRepository.findByVenta_IdVenta(venta.getIdVenta());
                    return ventaMapper.toReciboDTO(venta, detalles);
                })
                .collect(Collectors.toList());
    }
}