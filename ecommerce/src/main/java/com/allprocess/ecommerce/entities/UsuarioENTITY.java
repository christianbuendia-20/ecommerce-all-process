package com.allprocess.ecommerce.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
public class UsuarioENTITY {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @JsonIgnore // Nunca exponer el hash en respuestas JSON
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(unique = true, length = 20)
    private String dni;

    @Column(length = 20)
    private String telefono;

    // =======================================================
    // LA MAGIA DE LA LLAVE FORÁNEA (Nivel Senior)
    // =======================================================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_rol", nullable = false) // Esta será la columna en la tabla usuario
    private RolENTITY rol;

    @Column(nullable = false)
    private Boolean activo = true;

    // insertable = false y updatable = false le dice a JPA:
    // "No toques esto, deja que MySQL ponga la fecha por defecto (CURRENT_TIMESTAMP)"
    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private LocalDateTime fechaRegistro;

}