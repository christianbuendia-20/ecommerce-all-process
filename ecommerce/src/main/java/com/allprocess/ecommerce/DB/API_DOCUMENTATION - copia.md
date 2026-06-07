# Documentación de la API — AllProcess E-commerce Backend

**Base URL:** `http://localhost:8081/api`

---

## Índice

1. [Arquitectura del Backend](#1-arquitectura-del-backend)
2. [Tecnologías](#2-tecnologías)
3. [Modelo de Datos (Entidades)](#3-modelo-de-datos-entidades)
4. [Autenticación y Seguridad](#4-autenticación-y-seguridad)
5. [Endpoints Públicos](#5-endpoints-públicos)
6. [Endpoints Autenticados](#6-endpoints-autenticados)
7. [Endpoints Admin](#7-endpoints-admin)
8. [Rutas de Vistas (Thymeleaf)](#8-rutas-de-vistas-thymeleaf)
9. [Flujo Recomendado (Frontend)](#9-flujo-recomendado-frontend)
10. [Estados y Ciclo de Vida](#10-estados-y-ciclo-de-vida)
11. [Códigos de Error](#11-códigos-de-error)
12. [Configuración y Despliegue](#12-configuración-y-despliegue)

---

## 1. Arquitectura del Backend

```
ecommerce/
├── Dockerfile
├── docker-compose.yml
├── pom.xml                          # Maven (Spring Boot 3.2.5, Java 17)
├── src/main/java/com/allprocess/ecommerce/
│   ├── AllProcessBackend.java       # Entry point
│   ├── controllers/
│   │   ├── api/                     # REST JSON controllers
│   │   │   ├── AuthRestController.java
│   │   │   ├── ProfileRestController.java
│   │   │   ├── ProductoController.java
│   │   │   ├── CategoriaController.java
│   │   │   ├── ProveedorController.java
│   │   │   ├── VentaController.java
│   │   │   ├── PagoController.java
│   │   │   ├── DireccionController.java
│   │   │   └── admin/
│   │   │       ├── AdminProductoController.java
│   │   │       ├── AdminVentaController.java
│   │   │       ├── AdminUsuarioController.java
│   │   │       ├── AdminPagoController.java
│   │   │       └── AdminReporteController.java
│   │   ├── Client/                  # View controllers (client pages)
│   │   ├── Admin/                   # View controllers (admin pages)
│   │   └── Worker/                  # View controllers (worker pages)
│   ├── dtos/
│   │   ├── request/                 # LoginRequestDTO, RegistroUsuarioDTO, CheckoutVentaDTO, etc.
│   │   └── response/                # LoginResponseDTO, PerfilUsuarioDTO, ReciboVentaDTO, etc.
│   ├── entities/                    # JPA entities (9 entidades)
│   ├── enums/                       # RolUsuario, EstadoVentaEnum, EstadoPagoEnum, MetodoPagoEnum
│   ├── exceptions/                  # ResourceNotFoundException, BusinessRuleException, GlobalExceptionHandler
│   ├── mappers/                     # MapStruct: UsuarioMapper, ProductoMapper, VentaMapper
│   ├── repositories/                # Spring Data JPA repos (9 repos)
│   ├── security/                    # SecurityConfig, JwtAuthenticationFilter, JwtTokenProvider
│   └── services/
│       ├── AuthService, UsuarioService, ProductoService, VentaService, PagoService, DireccionService
│       └── impl/                    # Implementations
└── src/main/resources/
    ├── application.properties       # Config local (MySQL localhost:3306, puerto 8081)
    ├── application-docker.properties
    ├── static/js/                   # JS frontend (legacy)
    ├── static/img/                  # Imágenes
    └── templates/                   # Thymeleaf HTML templates
```

### Capas:
- **Controller** → Recibe requests, delega a Services, retorna JSON o vistas
- **Service** (Interface + Impl) → Lógica de negocio, validaciones, transacciones
- **Repository** (Spring Data JPA) → Acceso a BD
- **Mapper** (MapStruct) → Conversión Entity ↔ DTO
- **Security** → JWT + Spring Security filter chain

---

## 2. Tecnologías

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 17 | Lenguaje |
| Spring Boot | 3.2.5 | Framework principal |
| Spring MVC | — | REST controllers + Thymeleaf views |
| Spring Data JPA (Hibernate) | — | ORM y acceso a BD |
| Spring Security | — | Autenticación y autorización |
| MySQL | 8 | Base de datos |
| JWT (jjwt) | 0.12.5 | Tokens de autenticación |
| MapStruct | 1.5.5.Final | Mapeo Entity ↔ DTO |
| Lombok | — | Reducción de boilerplate |
| Thymeleaf | — | Templates HTML server-side |
| BCrypt | — | Hashing de contraseñas |
| Docker | — | Contenedores (MySQL + App + phpMyAdmin) |

---

## 3. Modelo de Datos (Entidades)

### `usuario`
| Campo | Tipo | Descripción |
|---|---|---|
| id_usuario | INT (PK) | Auto-increment |
| email | VARCHAR(255) | Único, not null |
| password_hash | VARCHAR(255) | BCrypt hash |
| nombres | VARCHAR(100) | |
| apellidos | VARCHAR(100) | |
| dni | VARCHAR(20) | Nullable |
| telefono | VARCHAR(20) | |
| id_rol | INT (FK → rol) | CLIENTE, VENDEDOR, ADMINISTRADOR |
| activo | BOOLEAN | Soft delete flag |
| fecha_registro | DATETIME | |

### `rol`
| Campo | Tipo | Descripción |
|---|---|---|
| id_rol | INT (PK) | 1=ADMIN, 2=VENDEDOR, 3=CLIENTE |
| nombre | VARCHAR(50) | Único |
| descripcion | VARCHAR(255) | |

### `producto`
| Campo | Tipo | Descripción |
|---|---|---|
| id_producto | INT (PK) | |
| nombre | VARCHAR(200) | |
| descripcion | TEXT | |
| imagen_url | VARCHAR(500) | |
| precio | DECIMAL(10,2) | |
| stock | INT | |
| id_categoria | INT (FK → categoria_producto) | |
| id_proveedor | INT (FK → proveedor) | |
| activo | BOOLEAN | Soft delete |
| fecha_registro | DATETIME | |

### `categoria_producto`
| Campo | Tipo |
|---|---|
| id_categoria | INT (PK) |
| nombre | VARCHAR(100) |

### `proveedor`
| Campo | Tipo |
|---|---|
| id_proveedor | INT (PK) |
| nombre | VARCHAR(200) |

### `venta`
| Campo | Tipo | Descripción |
|---|---|---|
| id_venta | INT (PK) | |
| id_cliente | INT (FK → usuario) | |
| direccion_envio | VARCHAR(255) | |
| ciudad_envio | VARCHAR(100) | |
| referencia_envio | VARCHAR(255) | Nullable |
| costo_envio | DECIMAL(10,2) | |
| total | DECIMAL(10,2) | |
| estado | ENUM('PENDIENTE','PAGADA','ENVIADA','ENTREGADA','CANCELADA') | |
| fecha | DATETIME | |

### `venta_detalle`
| Campo | Tipo |
|---|---|
| id_detalle_venta | INT (PK) |
| id_venta | INT (FK → venta) |
| id_producto | INT (FK → producto) |
| cantidad | INT |
| precio_unitario | DECIMAL(10,2) |
| subtotal | DECIMAL(10,2) |

### `pago`
| Campo | Tipo |
|---|---|
| id_pago | INT (PK) |
| id_venta | INT (FK → venta) |
| metodo_pago | ENUM('TARJETA','YAPE','TRANSFERENCIA') |
| monto | DECIMAL(10,2) |
| estado | ENUM('PENDIENTE','APROBADO','RECHAZADO','REEMBOLSADO') |
| transaccion_id | VARCHAR(100) | Único |
| fecha | DATETIME |

### `direccion_usuario`
| Campo | Tipo |
|---|---|
| id_direccion | INT (PK) |
| id_usuario | INT (FK → usuario) |
| alias | VARCHAR(100) |
| direccion | VARCHAR(255) |
| ciudad | VARCHAR(100) |
| referencia | VARCHAR(255) |

---

## 4. Autenticación y Seguridad

### Mecanismo
- **JWT (Bearer token)** con HMAC-SHA
- Claims del token: `subject` = email, `rol`, `idUsuario`
- Expiración: 24 horas (configurable via `jwt.expiration`)
- Se envía en header: `Authorization: Bearer <token>`

### Reglas de Autorización

| Patrón | Acceso |
|---|---|
| `/`, `/login`, `/register` | Público |
| `/api/auth/**`, `/api/usuarios/registro` | Público |
| `/api/productos/**`, `/api/categorias/**`, `/api/proveedores` | Público |
| `/api/ventas/**`, `/api/direcciones/**`, `/api/profile/**`, `/api/pagos/**` | Autenticado (cualquier rol) |
| `/api/admin/**` | Solo ADMINISTRADOR |
| `/homeclient/**`, `/catalogclient/**`, etc. | Público (vistas) |

### Login
- `POST /api/auth/login` devuelve token + datos de usuario
- El frontend debe guardar el token y enviarlo en cada request autenticado

### Registro
- `POST /api/usuarios/registro` crea usuario con rol CLIENTE
- Contraseña hasheada con BCrypt

---

## 5. Endpoints Públicos

### 5.1 Auth

#### POST `/api/auth/login`
Inicia sesión y devuelve token JWT + datos de usuario.

**Body:**
```json
{
  "email": "admin@allprocess.com",
  "contrasenia": "password123",
  "rol": "ADMINISTRADOR"
}
```

**Roles válidos:** `CLIENTE`, `VENDEDOR`, `ADMINISTRADOR`

**Respuesta exitosa (200):**
```json
{
  "success": true,
  "redirectUrl": "/homeadmin",
  "error": null,
  "mensaje": "Inicio de sesión exitoso",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "nombres": "Carlos",
  "rol": "ADMINISTRADOR"
}
```

**Respuesta fallida (200):**
```json
{
  "success": false,
  "redirectUrl": null,
  "error": "Credenciales inválidas",
  "mensaje": null,
  "token": null,
  "nombres": null,
  "rol": null
}
```

#### POST `/api/usuarios/registro`
Registra un nuevo usuario con rol CLIENTE.

**Body:**
```json
{
  "email": "cliente@correo.com",
  "password": "minimo8caracteres",
  "nombres": "Juan",
  "apellidos": "Pérez",
  "telefono": "999888777"
}
```

**Validaciones:** email formato, password min 8 chars, nombres/apellidos required

**Respuesta (201):**
```json
{
  "idUsuario": 8,
  "email": "cliente@correo.com",
  "nombres": "Juan",
  "apellidos": "Pérez",
  "telefono": "999888777"
}
```

### 5.2 Productos

#### GET `/api/productos`
Lista todos los productos activos.

**Respuesta (200):**
```json
[
  {
    "idProducto": 1,
    "nombre": "Motor Trifásico 5HP",
    "descripcion": "Motor asíncrono de inducción 380V, 4 polos.",
    "imagenUrl": "/img/p1.jpg",
    "precio": 550.00,
    "stock": 15,
    "nombreCategoria": "Motores Eléctricos",
    "nombreProveedor": "Siemens"
  }
]
```

#### GET `/api/productos/buscar?q=motor`
Busca productos por nombre (LIKE, case-insensitive).

#### GET `/api/productos/{id}`
Obtiene detalle de un producto específico.

**Response:** `ProductoCatalogoDTO`

#### GET `/api/productos/categoria/{id}`
Filtra productos activos por ID de categoría.

**Response:** `List<ProductoCatalogoDTO>`

### 5.3 Categorías

#### GET `/api/categorias`
Lista todas las categorías.

```json
[
  { "idCategoria": 1, "nombre": "Motores Eléctricos" }
]
```

### 5.4 Proveedores

#### GET `/api/proveedores`
Lista todos los proveedores.

```json
[
  { "idProveedor": 1, "nombre": "Siemens" }
]
```

---

## 6. Endpoints Autenticados

> Requieren header `Authorization: Bearer <token>`. Cualquier rol autenticado puede acceder.

### 6.1 Perfil

#### GET `/api/profile?usuarioId=4`
Obtiene datos del perfil del usuario.

**Response:** `PerfilUsuarioDTO`
```json
{
  "idUsuario": 4,
  "email": "cliente@correo.com",
  "nombres": "Juan",
  "apellidos": "Pérez",
  "telefono": "999888777"
}
```

#### PUT `/api/profile?usuarioId=4`
Actualiza nombres, apellidos y teléfono.

**Body:**
```json
{
  "nombres": "Laura",
  "apellidos": "Gómez",
  "telefono": "999111222"
}
```

**Response:** `PerfilUsuarioDTO`

#### PUT `/api/profile/password?usuarioId=4`
Cambia la contraseña.

**Body:**
```json
{
  "passwordActual": "miClaveVieja",
  "passwordNueva": "miClaveNueva123"
}
```

**Response:** `200 OK`

### 6.2 Direcciones

#### GET `/api/direcciones?usuarioId=4`
Lista direcciones del usuario.

**Response:**
```json
[
  {
    "idDireccion": 1,
    "alias": "Casa",
    "direccion": "Av. Siempre Viva 123",
    "ciudad": "Lima",
    "referencia": "Cerca del parque"
  }
]
```

#### POST `/api/direcciones?usuarioId=4`
Agrega una nueva dirección.

**Body:**
```json
{
  "alias": "Oficina",
  "direccion": "Av. Siempre Viva 742",
  "ciudad": "Lima",
  "referencia": "Cerca del parque"
}
```

**Response (201):** `DireccionDTO`

#### DELETE `/api/direcciones/{id}?usuarioId=4`
Elimina una dirección (solo si pertenece al usuario).

**Response:** `204 No Content`

### 6.3 Ventas (Checkout)

> **Nota:** El carrito se maneja desde el frontend (localStorage, estado React, etc.). El backend solo recibe el carrito finalizado.

#### POST `/api/ventas/checkout`
Procesa checkout: valida stock, descuenta inventario y crea la venta.

**Body:**
```json
{
  "idCliente": 4,
  "direccionEnvio": "Av. Los Faisanes 123, Zona Industrial",
  "ciudadEnvio": "Lima",
  "items": [
    { "idProducto": 1, "cantidad": 2 },
    { "idProducto": 3, "cantidad": 1 }
  ]
}
```

**Response (201):**
```json
{
  "idVenta": 5,
  "fecha": "2026-06-06T19:30:00",
  "estado": "PENDIENTE",
  "direccionEnvio": "Av. Los Faisanes 123, Zona Industrial",
  "ciudadEnvio": "Lima",
  "costoEnvio": 0.00,
  "total": 1145.00,
  "detalles": [
    {
      "nombreProducto": "Motor Trifásico 5HP",
      "cantidad": 2,
      "precioUnitario": 550.00,
      "subtotal": 1100.00
    },
    {
      "nombreProducto": "Sensor Inductivo M12",
      "cantidad": 1,
      "precioUnitario": 45.00,
      "subtotal": 45.00
    }
  ]
}
```

#### GET `/api/ventas/mis-ventas?clienteId=4`
Historial de compras del cliente.

**Response:** `List<ReciboVentaDTO>`

#### GET `/api/ventas/{id}/recibo`
Obtiene el recibo completo de una venta (con detalles de productos).

**Response:** `ReciboVentaDTO`

### 6.4 Pagos

#### POST `/api/pagos/registrar`
Registra un pago contra una venta.

**Body:**
```json
{
  "idVenta": 5,
  "metodoPago": "TARJETA",
  "monto": 1145.00,
  "transaccionId": "TRX-ABC-123-XYZ"
}
```

**Métodos válidos:** `TARJETA`, `YAPE`, `TRANSFERENCIA`

**Response (201):**
```json
{
  "idPago": 5,
  "venta": { "idVenta": 5 },
  "metodoPago": "TARJETA",
  "monto": 1145.00,
  "estado": "PENDIENTE",
  "transaccionId": "TRX-ABC-123-XYZ",
  "fecha": "2026-06-06T19:35:00"
}
```

#### POST `/api/pagos/{id}/confirmar`
Confirma un pago (cambia estado a `APROBADO`). Si el total de la venta está cubierto, la venta pasa automáticamente a `PAGADA`.

**Response:** `PagoENTITY`

#### GET `/api/pagos/venta/{idVenta}`
Lista todos los pagos asociados a una venta.

**Response:** `List<PagoENTITY>`

---

## 7. Endpoints Admin

> Requieren rol `ADMINISTRADOR` y token JWT en header `Authorization: Bearer <token>`.

### 7.1 Admin — Productos

#### GET `/api/admin/productos`
Lista TODOS los productos (activos e inactivos).

**Response:** `List<ProductoENTITY>`

#### GET `/api/admin/productos/{id}`
Obtiene un producto por ID.

**Response:** `ProductoENTITY`

#### POST `/api/admin/productos`
Crea un nuevo producto.

**Body:**
```json
{
  "nombre": "Nuevo Producto",
  "descripcion": "Descripción",
  "imagenUrl": "/img/nuevo.jpg",
  "precio": 199.99,
  "stock": 50,
  "categoria": { "idCategoria": 1 },
  "proveedor": { "idProveedor": 1 }
}
```

**Response (201):** `ProductoENTITY`

#### PUT `/api/admin/productos/{id}`
Actualiza un producto existente.

**Body:** `ProductoENTITY`

**Response:** `ProductoENTITY`

#### DELETE `/api/admin/productos/{id}`
Desactiva un producto (borrado lógico: `activo = false`).

**Response:** `204 No Content`

### 7.2 Admin — Ventas

#### GET `/api/admin/ventas`
Lista todas las ventas del sistema.

**Response:** `List<VentaENTITY>`

#### GET `/api/admin/ventas/{id}`
Obtiene detalle completo de una venta.

**Response:** `VentaENTITY`

#### GET `/api/admin/ventas/{id}/recibo`
Obtiene el recibo con productos.

**Response:** `ReciboVentaDTO`

#### PUT `/api/admin/ventas/{id}/estado`
Actualiza el estado de una venta.

**Body:**
```json
{ "estado": "PAGADA" }
```

**Estados válidos:** `PENDIENTE`, `PAGADA`, `ENVIADA`, `ENTREGADA`, `CANCELADA`

**Response:** `VentaENTITY`

### 7.3 Admin — Usuarios

#### GET `/api/admin/usuarios`
Lista todos los usuarios del sistema.

**Response:**
```json
[
  {
    "idUsuario": 1,
    "email": "admin@allprocess.com",
    "nombres": "Carlos",
    "apellidos": "Admin",
    "dni": null,
    "telefono": "999000111",
    "rol": "ADMINISTRADOR",
    "activo": true,
    "fechaRegistro": "2024-01-01T00:00:00"
  }
]
```

#### GET `/api/admin/usuarios/buscar?q=juan`
Busca usuarios por nombre o apellido.

**Response:** `List<UsuarioAdminDTO>`

#### PUT `/api/admin/usuarios/{id}/rol`
Cambia el rol de un usuario.

**Body:**
```json
{ "idRol": 2 }
```

**Roles disponibles:** `1=ADMINISTRADOR`, `2=VENDEDOR`, `3=CLIENTE`

**Response:** `UsuarioAdminDTO`

#### PUT `/api/admin/usuarios/{id}/activar`
Activa o desactiva un usuario.

**Body:**
```json
{ "activo": false }
```

**Response:** `200 OK`

### 7.4 Admin — Pagos

#### GET `/api/admin/pagos`
Lista todos los pagos del sistema.

**Response:** `List<PagoENTITY>`

#### PUT `/api/admin/pagos/{id}/estado`
Cambia el estado de un pago.

**Body:**
```json
{ "estado": "APROBADO" }
```

**Estados válidos:** `PENDIENTE`, `APROBADO`, `RECHAZADO`, `REEMBOLSADO`

**Response:** `PagoENTITY`

### 7.5 Admin — Reportes

#### GET `/api/admin/reportes/resumen`
Devuelve métricas para el dashboard administrativo.

```json
{
  "totalProductos": 8,
  "totalUsuarios": 7,
  "totalVentas": 4,
  "ventasDelMes": 905.00,
  "productosBajoStock": [
    { "idProducto": 8, "nombre": "Variador de Frecuencia 10HP", "stock": 8 }
  ],
  "ventasPorMes": [
    { "anio": 2026, "mes": 6, "cantidad": 4, "total": 2355.00 }
  ],
  "productosMasVendidos": [
    { "idProducto": 1, "nombre": "Motor Trifásico 5HP", "totalVendido": 1 }
  ]
}
```

Campos del reporte:
- `totalProductos` — Cantidad total de productos activos
- `totalUsuarios` — Cantidad total de usuarios activos
- `totalVentas` — Cantidad total de ventas
- `ventasDelMes` — Suma total de ventas del mes actual
- `productosBajoStock` — Productos con stock ≤ 10
- `ventasPorMes` — Agrupación de ventas por mes
- `productosMasVendidos` — Top productos por cantidad vendida

---

## 8. Rutas de Vistas (Thymeleaf)

Estas rutas devuelven HTML renderizado en servidor (NO JSON). Son las páginas del frontend legacy integrado.

| Método | Ruta | Vista | Descripción |
|---|---|---|---|
| GET | `/` | index.html | Landing page |
| GET | `/login` | login.html | Página de inicio de sesión |
| GET | `/register` | register.html | Página de registro |
| GET | `/logout` | — | Cierra sesión y redirige |
| GET | `/homeclient` | homeclient.html | Dashboard cliente |
| GET | `/catalogclient` | catalogclient.html | Catálogo de productos |
| GET | `/carclient` | carclient.html | Carrito de compras |
| GET | `/myorders` | myorders.html | Historial de pedidos |
| GET | `/profileclient` | profileclient.html | Perfil del cliente |
| GET | `/homeadmin` | homeadmin.html | Dashboard admin |
| GET | `/productmanagement` | productmanagement.html | Gestión de productos |
| GET | `/ordermanagement` | ordermanagement.html | Gestión de pedidos |
| GET | `/usermanagement` | usermanagement.html | Gestión de usuarios |
| GET | `/customermanagement` | customermanagement.html | Gestión de clientes |
| GET | `/adminreports` | adminreports.html | Reportes |
| GET | `/profileadmin` | profileadmin.html | Perfil admin |
| GET | `/homeworker` | homeworker.html | Dashboard vendedor |
| GET | `/profile` | profile.html | Perfil genérico |

> Si tu frontend es un proyecto separado (React, Vue, etc.), **NO necesitas estas rutas**. Solo usa los endpoints `/api/*`.

---

## 9. Flujo Recomendado (Frontend)

```
1. USUARIO NUEVO    → POST /api/usuarios/registro
2. INICIAR SESIÓN   → POST /api/auth/login → guardar token JWT
3. VER CATÁLOGO     → GET /api/productos
4. BUSCAR           → GET /api/productos/buscar?q=...
5. FILTRAR          → GET /api/productos/categoria/{id}
6. CARRITO          → Manejar en frontend (localStorage, estado React, etc.)
7. DIRECCIONES      → GET/POST/DELETE /api/direcciones?usuarioId=...
8. CHECKOUT         → POST /api/ventas/checkout (enviar carrito finalizado)
9. PAGAR            → POST /api/pagos/registrar
10. CONFIRMAR PAGO  → POST /api/pagos/{id}/confirmar
11. HISTORIAL       → GET /api/ventas/mis-ventas?clienteId=...
12. PERFIL          → GET/PUT /api/profile?usuarioId=...
```

### Uso del Token JWT:
```javascript
// Ejemplo con fetch
const token = localStorage.getItem('jwt');

fetch('http://localhost:8081/api/ventas/mis-ventas?clienteId=4', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

## 10. Estados y Ciclo de Vida

### Estados de Venta
```
PENDIENTE → PAGADA → ENVIADA → ENTREGADA
     ↘         ↘
   CANCELADA   CANCELADA
```

### Estados de Pago
```
PENDIENTE → APROBADO
     ↘
   RECHAZADO → REEMBOLSADO
```

### Roles de Usuario
| ID | Nombre | Descripción |
|---|---|---|
| 1 | ADMINISTRADOR | Acceso total al sistema |
| 2 | VENDEDOR | Acceso a gestión de ventas |
| 3 | CLIENTE | Comprador en tienda |

---

## 11. Códigos de Error

| Código | Significado |
|---|---|
| 200 | OK |
| 201 | Creado |
| 204 | Sin contenido (eliminación exitosa) |
| 400 | Error de negocio (stock insuficiente, datos inválidos, validación) |
| 401 | No autenticado (token faltante o inválido) |
| 403 | No autorizado (rol insuficiente) |
| 404 | Recurso no encontrado |
| 500 | Error interno del servidor |

**Formato de error:**
```json
{
  "fechaHora": "2026-06-06T19:30:00",
  "estado": 400,
  "error": "Bad Request",
  "mensaje": "Stock insuficiente para Motor Trifásico 5HP",
  "ruta": "/api/ventas/checkout"
}
```

---

## 12. Configuración y Despliegue

### Local (desarrollo)
```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerceallprocess
spring.datasource.username=integrador
spring.datasource.password=integrador
spring.jpa.hibernate.ddl-auto=none
```

### Docker
```yaml
# docker-compose.yml expone:
#   MySQL en puerto 3307 (host) → 3306 (container)
#   App en puerto 8080 (host) → 8080 (container)
#   phpMyAdmin en puerto 8082
```

### Variables de Entorno
| Variable | Default | Descripción |
|---|---|---|
| `SPRING_DATASOURCE_USERNAME` | root | Usuario BD (Docker) |
| `SPRING_DATASOURCE_PASSWORD` | root123 | Password BD (Docker) |
| `SPRING_PROFILES_ACTIVE` | docker | Perfil Spring activo |

### Propiedades JWT
```properties
jwt.secret=AllProcessSecretKey2024SuperSeguraParaEcommerce!
jwt.expiration=86400000    # 24 horas en milisegundos
```

### BD
- Motor: MySQL 8
- Base de datos: `ecommerceallprocess`
- Script SQL: `DB/Script.sql`

---

> Documentación generada para integración con frontend externo.  
> Backend: Spring Boot 3.2.5 / Java 17 / MySQL 8  
> Puerto: `8081` (local) / `8080` (Docker)
