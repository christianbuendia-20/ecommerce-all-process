package com.allprocess.ecommerce.mappers;

import com.allprocess.ecommerce.dtos.response.DetalleReciboDTO;
import com.allprocess.ecommerce.dtos.response.ReciboVentaDTO;
import com.allprocess.ecommerce.entities.VentaDetalleENTITY;
import com.allprocess.ecommerce.entities.VentaENTITY;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    // 1. Le enseñamos a traducir UN solo detalle de la compra
    @Mapping(source = "producto.nombre", target = "nombreProducto")
    @Mapping(source = "producto.imagenUrl", target = "imagenUrl")
    DetalleReciboDTO toDetalleDTO(VentaDetalleENTITY detalle);

    // 2. MapStruct usará el método de arriba para traducir toda la lista automáticamente
    // (Asumimos que en el Service le pasaremos la lista de detalles junto con la venta)
    @Mapping(source = "venta.idVenta", target = "idVenta")
    @Mapping(source = "detalles", target = "detalles")
    ReciboVentaDTO toReciboDTO(VentaENTITY venta, List<VentaDetalleENTITY> detalles);
}