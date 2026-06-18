package com.allprocess.ecommerce.entities;

import com.allprocess.ecommerce.converters.EstadoVentaConverter;
import com.allprocess.ecommerce.enums.EstadoVentaEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
@Getter
@Setter
@NoArgsConstructor
public class VentaENTITY {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_venta")
    private Integer idVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private UsuarioENTITY cliente;

    // =======================================================
    // DATOS DE ENVÍO (Inmutables)
    // =======================================================
    @Column(name = "direccion_envio", nullable = false, columnDefinition = "TEXT")
    private String direccionEnvio;

    @Column(name = "ciudad_envio", nullable = false, length = 100)
    private String ciudadEnvio;

    @Column(name = "referencia_envio", columnDefinition = "TEXT")
    private String referenciaEnvio;

    // =======================================================
    // DATOS FINANCIEROS
    // =======================================================
    @Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoEnvio;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    // Converter personalizado: maneja valores legacy de la BD (PROCESANDO, EN_CAMINO, COMPLETADO)
    @Convert(converter = EstadoVentaConverter.class)
    @Column(nullable = false, length = 30)
    private EstadoVentaEnum estado = EstadoVentaEnum.PENDIENTE;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;
}
