package com.allprocess.ecommerce.entities;

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

    @Column(nullable = false, length = 30)
    private String estado = "PENDIENTE";

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;
}