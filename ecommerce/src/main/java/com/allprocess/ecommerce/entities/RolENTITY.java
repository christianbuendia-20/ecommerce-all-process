package com.allprocess.ecommerce.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rol") // Apunta explícitamente a tu tabla MySQL
@Getter
@Setter
@NoArgsConstructor
public class RolENTITY {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol") // Mapea el camelCase de Java al snake_case de SQL
    private Integer idRol;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(length = 150)
    private String descripcion;

    // No usamos @AllArgsConstructor aquí porque el idRol lo genera la BD,
    // no deberías pasarlo tú al crear un objeto nuevo.
}