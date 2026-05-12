package com.allprocess.ecommerce.entities;

import com.allprocess.ecommerce.enums.EstadoPagoEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
public class PagoENTITY {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    // =======================================================
    // LLAVE FORÁNEA
    // =======================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_venta", nullable = false)
    private VentaENTITY venta;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    // =======================================================
    // RESTRICCIÓN FUERTE POR ENUM
    // =======================================================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPagoEnum estado = EstadoPagoEnum.PENDIENTE;

    // ID que te devuelve la pasarela (Ej: Niubiz, MercadoPago, PayPal)
    @Column(name = "transaccion_id", unique = true)
    private String transaccionId;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;
}