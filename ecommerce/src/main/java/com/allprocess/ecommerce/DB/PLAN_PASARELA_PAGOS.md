# Plan de Integración — Pasarela de Pagos (Yape, BCP, Tarjeta)

## Estado actual del sistema de pagos

| Componente | Estado |
|---|---|
| `PagoENTITY` | ✅ Creado (idPago, idVenta, metodoPago, monto, estado, transaccionId, fecha) |
| `MetodoPagoEnum` | ✅ `TARJETA`, `YAPE`, `TRANSFERENCIA` |
| `EstadoPagoEnum` | ✅ `PENDIENTE`, `APROBADO`, `RECHAZADO`, `REEMBOLSADO` |
| `PagoService` | ✅ Registrar, confirmar, listar, cambiar estado |
| `PagoController` | ✅ Endpoints REST (`/api/pagos/**`) |
| Confirmación | ⚠️ Manual: admin llama `POST /api/pagos/{id}/confirmar` |
| Integración con pasarela real | ❌ No implementado |

---

## Opciones de pasarela para Perú

| Pasarela | Tarjeta | Yape | BCP Transferencia | Webhook | Comisión | Dificultad |
|---|---|---|---|---|---|---|
| **Mercado Pago** | ✅ | ✅ (QR) | ✅ (Transfer) | ✅ | ~4-6% | Media |
| **Niubiz** (ex Visanet) | ✅ | ❌ | ❌ | ✅ | ~3-5% | Alta |
| **Culqui** | ✅ | ✅ | ✅ | ✅ | ~4-6% | Baja |
| **Izipay** | ✅ | ❌ | ❌ | ✅ | ~3-5% | Alta |
| **PagoEfectivo** | ❌ | ❌ | ✅ (Código CIP) | ✅ | ~3-4% | Media |
| **Flujo manual** (QR + transferencia) | ❌ | ⚠️ QR fijo | ⚠️ Cuenta BCP | ❌ | 0% | Baja |

---

## Opción recomendada: Mercado Pago

### Por qué
- **Un solo proveedor** cubre tarjeta, Yape (QR) y transferencia bancaria
- Webhooks para confirmación automática
- SDK oficial para Java (Spring Boot)
- Sandbox para pruebas
- Documentación en español

### Flujo general

```
Frontend                           Backend                         Mercado Pago
   │                                  │                                │
   │  1. POST /api/ventas/checkout    │                                │
   │─────────────────────────────────>│                                │
   │  2. Recibe idVenta + total       │                                │
   │<─────────────────────────────────│                                │
   │                                  │                                │
   │  3. Elige método de pago         │                                │
   │  (Yape/BCP/Tarjeta)              │                                │
   │                                  │                                │
   │  4. POST /api/pagos/crear-preferencia                            │
   │─────────────────────────────────>│                                │
   │                                  │  5. POST /v1/payments          │
   │                                  │───────────────────────────────>│
   │                                  │  6. init_point / QR / link     │
   │                                  │<───────────────────────────────│
   │  7. Recibe link/QR de pago       │                                │
   │<─────────────────────────────────│                                │
   │                                  │                                │
   │  8. Usuario paga (app/web)       │                                │
   │─────────────────────────────────────────────────────────────────>│
   │                                  │                                │
   │                                  │  9. Webhook: pago aprobado     │
   │                                  │<───────────────────────────────│
   │                                  │  10. Confirma pago + cambia    │
   │                                  │      estado venta a PAGADA     │
   │  11. GET /api/ventas/{id}/recibo │                                │
   │─────────────────────────────────>│                                │
```

---

## Implementación paso a paso

### Fase 1: Configuración de Mercado Pago

#### 1.1. Crear cuenta y obtener credenciales
- Registrarse en [Mercado Pago Developers](https://developers.mercadopago.com.pe)
- Obtener `ACCESS_TOKEN` (producción) y `ACCESS_TOKEN` (sandbox)
- Configurar la URL del webhook (ej: `https://tu-dominio.com/api/webhooks/mercadopago`)

#### 1.2. Agregar dependencia al `pom.xml`

```xml
<!-- Mercado Pago SDK -->
<dependency>
    <groupId>com.mercadopago</groupId>
    <artifactId>sdk-java</artifactId>
    <version>2.1.27</version>
</dependency>
```

#### 1.3. Configurar credenciales en `application.properties`

```properties
mercadopago.access-token=APP_USR-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx-xxxxxx
mercadopago.sandbox=true
mercadopago.webhook-secret=tu-secreto-de-webhook
```

#### 1.4. Crear clase de configuración

```
security/
  └── MercadoPagoConfig.java           ← Inicializa SDK con access token
```

---

### Fase 2: Servicio de pasarela

#### 2.1. Crear `PasarelaPagoService.java`

```
services/
  ├── PasarelaPagoService.java         ← Interface
  └── impl/
       └── MercadoPagoServiceImpl.java  ← Implementación con SDK
```

**Métodos del servicio:**

```java
public interface PasarelaPagoService {

    // Crea una preferencia de pago y devuelve link/QR para el frontend
    PreferenciaPagoDTO crearPago(PagoRequestDTO request);

    // Procesa el webhook cuando Mercado Pago notifica un cambio de estado
    void procesarWebhook(String jsonBody, Map<String, String> headers);

    // Consulta el estado de un pago en Mercado Pago
    EstadoPagoDTO consultarPago(String transaccionId);
}
```

#### 2.2. DTOs necesarios

```
dtos/
  └── request/
  │     ├── PagoPasarelaRequestDTO.java   ← idVenta, metodoPago, emailPagador
  │     └── WebhookNotificationDTO.java    ← type, data.id, action
  └── response/
        ├── PreferenciaPagoDTO.java       ← initPoint, qrCode, transaccionId, monto
        └── EstadoPagoDTO.java            ← estado, detalle, fecha
```

#### 2.3. Lógica por método de pago

| Método | SDK de Mercado Pago | Flujo |
|--------|---------------------|-------|
| **Tarjeta** | `Payment` + tarjeta tokenizada desde frontend | Frontend tokeniza tarjeta con MercadoPago.js, backend crea el payment |
| **Yape** | `Payment` con `payment_method_id = "yape"` | Genera QR, usuario escanea y paga desde Yape |
| **Transferencia BCP** | `Payment` con `payment_method_id = "bank_transfer"` | Genera instructivo de transferencia, usuario transfiere |

---

### Fase 3: Webhook para confirmación automática

#### 3.1. Crear `WebhookController.java`

```
controllers/
  └── api/
       └── WebhookController.java    ← POST /api/webhooks/mercadopago
```

**Flujo del webhook:**
1. Mercado Pago envía POST con JSON `{ "action": "payment.created", "data": { "id": "12345" } }`
2. Validar firma del webhook (header `X-Signature`)
3. Consultar `Payment.get(transaccionId)` a Mercado Pago
4. Si estado es `approved`:
   - Buscar el pago en BD por `transaccionId`
   - Si no existe, crearlo con estado APROBADO
   - Actualizar venta a PAGADA
5. Responder `200 OK` a Mercado Pago

---

### Fase 4: Nuevos endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/pagos/crear-preferencia` | Crea preferencia en Mercado Pago y devuelve link/QR |
| `POST` | `/api/webhooks/mercadopago` | Webhook para notificaciones automáticas |
| `GET` | `/api/pagos/estado/{transaccionId}` | Consulta estado actual del pago en la pasarela |
| `GET` | `/api/pagos/{id}/confirmar-pasarela` | Fuerza consulta a pasarela para actualizar estado |

---

### Fase 5: Actualizar `PagoServiceImpl`

Modificar el confirmar pago para que, en lugar de solo cambiar estado manualmente:
- Consulte el estado real en Mercado Pago
- Valide que el monto coincida
- Actualice la venta automáticamente

---

### Fase 6: Almacenar datos de la transacción

Agregar campos a `PagoENTITY` o crear tabla `transaccion_pasarela`:

```sql
CREATE TABLE transaccion_pasarela (
    id_transaccion INT AUTO_INCREMENT PRIMARY KEY,
    id_pago INT NOT NULL,
    pasarela VARCHAR(50) NOT NULL,          -- MERCADO_PAGO, NIUBlZ, etc
    transaccion_id_externo VARCHAR(255),     -- ID en la pasarela
    estado_pasarela VARCHAR(50),             -- approved, rejected, pending
    raw_response JSON,                       -- Respuesta completa de la pasarela
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaccion_pago FOREIGN KEY (id_pago) REFERENCES pago(id_pago)
);
```

O más simple: agregar un campo `json_response` TEXT a `PagoENTITY`.

---

### Fase 7: Manejo de errores y reintentos

| Situación | Acción |
|-----------|--------|
| Pago rechazado (fondos insuficientes) | Pago queda RECHAZADO, venta sigue PENDIENTE |
| Timeout de conexión con pasarela | Reintentar 3 veces con backoff |
| Webhook no recibido | Endpoint `confirmar-pasarela` permite al admin forzar consulta |
| Notificación duplicada | Validar por `transaccionId` antes de crear |
| Monto no coincide | Rechazar y notificar al admin |

---

## Alternativa híbrida (más simple, sin SDK)

Si se prefiere evitar depender de una pasarela externa:

### Yape — QR manual
1. El frontend muestra un **código QR fijo** de Yape del negocio
2. El usuario paga desde su app Yape
3. El frontend muestra un formulario donde el usuario ingresa: nombre del banco, número de operación, monto
4. El backend registra el pago como `YAPE` con estado `PENDIENTE`
5. El admin verifica manualmente y confirma desde el panel

**Ventajas:** Sin comisiones, sin integración técnica
**Desventajas:** Proceso manual, no automatizado

### BCP — Transferencia bancaria
1. El frontend muestra los datos de la cuenta BCP del negocio
2. El usuario transfiere desde su banca en línea
3. Ingresa el número de operación en el formulario
4. El admin verifica el abono y confirma manualmente

### Endpoints adicionales para flujo manual

```java
// El usuario reporta haber realizado el pago
POST /api/pagos/reportar-pago
Body: { idVenta, metodoPago, monto, numeroOperacion, fechaOperacion }

// El admin lista pagos pendientes de verificación
GET /api/admin/pagos/pendientes

// El admin confirma que el pago fue recibido
POST /api/admin/pagos/{id}/verificar
Body: { estado: "APROBADO" }
```

---

## Orden de implementación sugerido

```
Fase 1 — Configuración
  1. Crear cuenta Mercado Pago Developer
  2. Obtener access_token (sandbox + producción)
  3. Agregar dependencia SDK al pom.xml
  4. Crear MercadoPagoConfig.java

Fase 2 — Servicio base
  5. Crear DTOs (PreferenciaPagoDTO, PagoPasarelaRequestDTO, etc.)
  6. Crear PasarelaPagoService interface
  7. Implementar MercadoPagoServiceImpl.crearPago()
  8. Probar en sandbox

Fase 3 — Webhook
  9. Crear WebhookController
  10. Implementar procesarWebhook() con validación de firma
  11. Probar con ngrok + sandbox

Fase 4 — Integración con frontend
  12. Frontend: integrar MercadoPago.js para tokenizar tarjetas
  13. Frontend: mostrar QR para Yape
  14. Frontend: mostrar datos de transferencia BCP

Fase 5 — Administración
  15. Panel admin: ver pagos pendientes de verificación
  16. Panel admin: forzar consulta de estado a pasarela
  17. Panel admin: ver historial de transacciones

Fase 6 — Producción
  18. Cambiar a modo producción en Mercado Pago
  19. Configurar webhook con dominio real
  20. Pruebas integrales en producción
```

---

## Resumen de archivos a crear/modificar

### Nuevos archivos
```
ecommerce/src/main/java/com/allprocess/ecommerce/
├── security/
│   └── MercadoPagoConfig.java
├── services/
│   ├── PasarelaPagoService.java
│   └── impl/
│       └── MercadoPagoServiceImpl.java
├── controllers/api/
│   └── WebhookController.java
└── dtos/
    ├── request/
    │   ├── PagoPasarelaRequestDTO.java
    │   └── ReportarPagoDTO.java
    └── response/
        ├── PreferenciaPagoDTO.java
        └── EstadoPagoDTO.java
```

### Archivos a modificar
```
├── pom.xml                                    ← + sdk-java
├── src/main/resources/application.properties  ← + credenciales MP
├── services/impl/PagoServiceImpl.java         ← + integración con pasarela
├── controllers/api/PagoController.java        ← + crear-preferencia
├── controllers/api/admin/AdminPagoController.java ← + pendientes, verificar
├── entities/PagoENTITY.java                   ← + jsonResponse (opcional)
└── enums/MetodoPagoEnum.java                  ← + QR_YAPE, BCP_TRANSFERENCIA (opcional)
```
