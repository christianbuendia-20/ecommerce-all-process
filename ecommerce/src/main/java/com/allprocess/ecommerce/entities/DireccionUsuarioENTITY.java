package com.allprocess.ecommerce.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "direccion_usuario")
@Getter
@Setter
@NoArgsConstructor
public class DireccionUsuarioENTITY {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_direccion")
    private Integer idDireccion;

    // =======================================================
    // LA LLAVE FORÁNEA: Conexión directa al Usuario
    // =======================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UsuarioENTITY usuario;

    @Column(nullable = false, length = 50)
    private String alias; // Ej: 'Casa de mis papás', 'Oficina ArmandoPC'

    @Column(nullable = false, columnDefinition = "TEXT")
    private String direccion;

    @Column(nullable = false, length = 100)
    private String ciudad;

    @Column(columnDefinition = "TEXT")
    private String referencia;
}