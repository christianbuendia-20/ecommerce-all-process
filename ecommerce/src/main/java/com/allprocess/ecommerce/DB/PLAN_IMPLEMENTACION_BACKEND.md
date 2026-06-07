# Plan de Implementación — Backend AllProcess E-commerce

## Estado Actual

| Capa | Estado |
|------|--------|
| Entidades JPA (8) | ✅ Completo |
| Enums (4) | ✅ Completo |
| Repositorios (9) | ✅ Completo |
| Mappers MapStruct (3) | ✅ Completo |
| Exception Handling | ✅ Completo |
| Auth (login/logout) | ✅ Completo |
| Registro de usuarios | ✅ Completo |
| Obtención de perfil | ✅ Completo |
| Controladores de vista (Thymeleaf) | ✅ Rutas servidas |
| Servicios de negocio (Producto, Venta, Pago) | ❌ Stubs vacíos |
| Controladores REST / API JSON | ❌ No existen |
| Seguridad (Spring Security / JWT) | ❌ No implementado |

---

## 1. Seguridad y Autenticación

### 1.1. Reemplazar validación manual por Spring Security
- **Archivos**: Nuevo `SecurityConfig.java` + `JwtAuthenticationFilter.java`
- Configurar `SecurityFilterChain` con:
  - `/api/auth/**`, `/register`, `/login`, `/` → **permitAll**
  - `/api/client/**` → `hasRole('CLIENTE')`
  - `/api/admin/**` → `hasRole('ADMINISTRADOR')`
  - `/api/worker/**` → `hasRole('VENDEDOR')`
  - `/api/profile/**` → `isAuthenticated()`

### 1.2. Implementar JWT
- Generar token en login y devolverlo en `LoginResponseDTO`
- Validar token en cada request vía `JwtAuthenticationFilter`
- Extraer usuario y rol del token para establecer `SecurityContext`

### 1.3. Actualizar `AuthServiceImpl`
- Eliminar validación hardcodeada de credenciales
- Usar `BCryptPasswordEncoder` para comparar `password_hash`
- Devolver token JWT + datos de sesión

---

## 2. Catálogo de Productos — `ProductoServiceImpl`

### 2.1. Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/productos` | Listar todos los productos activos |
| `GET` | `/api/productos/{id}` | Obtener detalle de un producto |
| `GET` | `/api/productos/buscar?q=` | Buscar por nombre (LIKE) |
| `GET` | `/api/productos/categoria/{idCategoria}` | Filtrar por categoría |
| `GET` | `/api/categorias` | Listar categorías |
| `GET` | `/api/proveedores` | Listar proveedores |
| `POST` | `/api/admin/productos` | Crear producto (ADMIN/VENDEDOR) |
| `PUT` | `/api/admin/productos/{id}` | Actualizar producto |
| `DELETE` | `/api/admin/productos/{id}` | Desactivar producto (borrado lógico) |

### 2.2. Lógica de negocio
- Solo retornar productos con `activo = true` (excepto admin)
- Validar stock disponible antes de operaciones
- Usar `ProductoMapper.toCatalogoDTO()` para respuesta

---

## 3. Carrito de Compras — `VentaServiceImpl`

### 3.1. Estrategia
- Carrito **sin sesión ni BD**: se maneja desde el frontend (localStorage)
- El backend solo recibe el carrito finalizado en el checkout

### 3.2. Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/ventas/checkout` | Crear venta desde `CheckoutVentaDTO` |
| `GET` | `/api/ventas/mis-ventas` | Historial del cliente autenticado |
| `GET` | `/api/admin/ventas` | Listar todas las ventas (ADMIN) |
| `GET` | `/api/admin/ventas/{id}` | Detalle de venta + pagos |
| `PUT` | `/api/admin/ventas/{id}/estado` | Actualizar estado de venta |
| `GET` | `/api/ventas/{id}/recibo` | Obtener recibo completo con `ReciboVentaDTO` |
| `GET` | `/api/ventas/{id}/detalle` | Obtener detalle de productos de una venta |

### 3.3. Lógica de negocio — Checkout
1. Validar que todos los productos existan y estén activos
2. Validar **stock suficiente** para cada producto
3. **Descontar stock** de cada producto
4. Calcular subtotales y total
5. Crear `VentaENTITY` con estado `PENDIENTE`
6. Crear `VentaDetalleENTITY` por cada item
7. Devolver `ReciboVentaDTO` con el resumen

---

## 4. Pagos — `PagoServiceImpl`

### 4.1. Endpoints REST

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/pagos/registrar` | Registrar pago para una venta |
| `POST` | `/api/pagos/{idPago}/confirmar` | Confirmar pago (ADMIN) |
| `GET` | `/api/pagos/venta/{idVenta}` | Ver pagos de una venta |
| `GET` | `/api/admin/pagos` | Listar todos los pagos (ADMIN) |
| `PUT` | `/api/admin/pagos/{id}/estado` | Cambiar estado del pago |

### 4.2. Lógica de negocio
- Validar que el monto del pago no exceda el total de la venta
- Soporte para pagos parciales (múltiples pagos contra una misma venta)
- Actualizar estado de la venta a `PAGADA` cuando el total esté cubierto
- Protección: **sin CASCADE** en la FK, no eliminar pagos

---

## 5. Gestión de Usuarios — `UsuarioServiceImpl`

### 5.1. Endpoints REST adicionales

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/profile` | Perfil del usuario autenticado |
| `PUT` | `/api/profile` | Actualizar datos personales |
| `PUT` | `/api/profile/password` | Cambiar contraseña |
| `GET` | `/api/admin/usuarios` | Listar todos los usuarios |
| `PUT` | `/api/admin/usuarios/{id}/rol` | Cambiar rol de usuario |
| `PUT` | `/api/admin/usuarios/{id}/activar` | Activar/desactivar usuario |
| `GET` | `/api/direcciones` | Listar direcciones del usuario |
| `POST` | `/api/direcciones` | Agregar dirección |
| `DELETE` | `/api/direcciones/{id}` | Eliminar dirección |

---

## 6. Reportes y Dashboard — Admin

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/admin/reportes/resumen` | Totales (ventas del mes, productos bajos en stock, etc.) |
| `GET` | `/api/admin/reportes/ventas-por-mes` | Ventas agrupadas por mes |
| `GET` | `/api/admin/reportes/productos-mas-vendidos` | Top productos |

---

## 7. Controladores REST (Nuevos)

Crear en `controllers/`:

| Controller | Package | Rutas |
|------------|---------|-------|
| `ProductoController.java` | `api/` | `/api/productos/**` |
| `CategoriaController.java` | `api/` | `/api/categorias/**` |
| `VentaController.java` | `api/` | `/api/ventas/**` |
| `PagoController.java` | `api/` | `/api/pagos/**` |
| `DireccionController.java` | `api/` | `/api/direcciones/**` |
| `AdminProductoController.java` | `api/admin/` | `/api/admin/productos/**` |
| `AdminVentaController.java` | `api/admin/` | `/api/admin/ventas/**` |
| `AdminUsuarioController.java` | `api/admin/` | `/api/admin/usuarios/**` |
| `AdminPagoController.java` | `api/admin/` | `/api/admin/pagos/**` |
| `AdminReporteController.java` | `api/admin/` | `/api/admin/reportes/**` |

---

## 8. Limpieza de código

| Tarea | Detalle |
|-------|---------|
| Eliminar `services/interfaces/` | Contiene clases vacías que no se usan (`IAuthService`, `IUsuarioService`, etc.) |
| Eliminar endpoints duplicados en controladores de vista | Los controladores Thymeleaf deben servir **solo la vista**, la data se obtiene desde el frontend vía Fetch/API |
| Unificar estilo de respuesta | Todos los endpoints REST deben devolver `ResponseEntity<ApiResponse<T>>` con estructura consistente |

---

## 9. Dependencias requeridas (pom.xml)

```xml
<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## Orden de implementación sugerido

```
Fase 1 — Fundación
  1. Agregar dependencias (Spring Security + JWT)
  2. Implementar SecurityConfig + JwtAuthenticationFilter + JwtTokenProvider
  3. Refactorizar AuthServiceImpl (BCrypt + JWT)
  4. Agregar BCryptPasswordEncoder bean

Fase 2 — Catálogo
  5. Implementar ProductoServiceImpl (CRUD + búsqueda)
  6. Crear ProductoController + CategoriaController
  7. Crear AdminProductoController

Fase 3 — Ventas (transaccional)
  8. Implementar VentaServiceImpl (checkout)
  9. Crear VentaController
  10. Crear AdminVentaController

Fase 4 — Pagos
  11. Implementar PagoServiceImpl
  12. Crear PagoController + AdminPagoController

Fase 5 — Usuarios y direcciones
  13. Endpoints faltantes de usuario
  14. Endpoints de direcciones

Fase 6 — Reportes
  15. Implementar query de reportes
  16. Crear AdminReporteController

Fase 7 — Limpieza
  17. Eliminar stubs vacíos (services/interfaces/)
  18. Verificar consistencia de todos los endpoints
```
