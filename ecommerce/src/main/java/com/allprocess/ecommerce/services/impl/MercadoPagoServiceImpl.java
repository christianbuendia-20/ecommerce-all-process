package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.response.PreferenceResponseDTO;
import com.allprocess.ecommerce.entities.PagoENTITY;
import com.allprocess.ecommerce.entities.ProductoENTITY;
import com.allprocess.ecommerce.entities.VentaDetalleENTITY;
import com.allprocess.ecommerce.entities.VentaENTITY;
import com.allprocess.ecommerce.enums.EstadoPagoEnum;
import com.allprocess.ecommerce.enums.EstadoVentaEnum;
import com.allprocess.ecommerce.enums.MetodoPagoEnum;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.repositories.PagoRepository;
import com.allprocess.ecommerce.repositories.ProductoRepository;
import com.allprocess.ecommerce.repositories.VentaDetalleRepository;
import com.allprocess.ecommerce.repositories.VentaRepository;
import com.allprocess.ecommerce.services.MercadoPagoService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoServiceImpl implements MercadoPagoService {

    private final VentaRepository ventaRepository;
    private final PagoRepository pagoRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final ProductoRepository productoRepository;

    @Value("${mercadopago.back-url.success}")
    private String backUrlSuccess;

    @Value("${mercadopago.back-url.failure}")
    private String backUrlFailure;

    @Value("${mercadopago.back-url.pending}")
    private String backUrlPending;

    @Value("${mercadopago.notification-url:}")
    private String notificationUrl;

    @Override
    @Transactional
    public PreferenceResponseDTO crearPreferencia(Integer idVenta) {

        VentaENTITY venta = ventaRepository.findById(idVenta)
                .orElseThrow(() -> new ResourceNotFoundException("Venta " + idVenta + " no encontrada"));

        if (venta.getEstado() != EstadoVentaEnum.PENDIENTE_PAGO) {
            throw new BusinessRuleException(
                    "La venta no está en estado válido para pagar. Estado actual: " + venta.getEstado());
        }

        List<VentaDetalleENTITY> detalles = ventaDetalleRepository.findByVenta_IdVenta(idVenta);

        if (detalles.isEmpty()) {
            throw new BusinessRuleException("La venta " + idVenta + " no tiene productos");
        }

        // Validar que todos los ítems tengan datos válidos antes de llamar a MP
        for (VentaDetalleENTITY d : detalles) {
            if (d.getProducto() == null || d.getProducto().getNombre() == null) {
                throw new BusinessRuleException("Producto inválido en la venta " + idVenta);
            }
            if (d.getCantidad() == null || d.getCantidad() <= 0) {
                throw new BusinessRuleException(
                        "Cantidad inválida para producto: " + d.getProducto().getNombre());
            }
            if (d.getPrecioUnitario() == null || d.getPrecioUnitario().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessRuleException(
                        "Precio inválido para producto: " + d.getProducto().getNombre());
            }
        }

        List<PreferenceItemRequest> items = detalles.stream()
                .map(d -> PreferenceItemRequest.builder()
                        .title(d.getProducto().getNombre())
                        .quantity(d.getCantidad())
                        .unitPrice(d.getPrecioUnitario())
                        .currencyId("PEN")
                        .build())
                .toList();

        // ── Validar URLs antes de construir (falla rápido si el @Value no inyectó) ──
        if (backUrlSuccess == null || backUrlSuccess.isBlank()) {
            throw new BusinessRuleException(
                    "La URL de retorno success está vacía. Verifica 'mercadopago.back-url.success' en application.properties.");
        }

        // Construir backUrls e inspeccionar el objeto resultante antes de enviarlo a MP
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(backUrlSuccess)
                .failure(backUrlFailure)
                .pending(backUrlPending)
                .build();

        log.info("[MP] Creando preferencia — venta={}, ítems={}, total={}",
                idVenta, items.size(), venta.getTotal());
        log.info("[MP] BackUrls construidas — success='{}', failure='{}', pending='{}'",
                backUrls.getSuccess(), backUrls.getFailure(), backUrls.getPending());
        items.forEach(i -> log.info("[MP] Ítem: title='{}', qty={}, price={}, currency={}",
                i.getTitle(), i.getQuantity(), i.getUnitPrice(), i.getCurrencyId()));

        // Segunda validación: verificar que el objeto BackUrls tiene el valor correcto
        if (backUrls.getSuccess() == null || backUrls.getSuccess().isBlank()) {
            throw new BusinessRuleException(
                    "PreferenceBackUrlsRequest.success está null después de build(). "
                    + "backUrlSuccess=" + backUrlSuccess + ". Revisar SDK.");
        }

        // Email del comprador (cliente autenticado que realizó el checkout)
        String buyerEmail = (venta.getCliente() != null) ? venta.getCliente().getEmail() : null;
        log.info("[MP] Payer email para preferencia: {}", buyerEmail);

        // Construir el PreferenceRequest completo
        // NOTA: autoReturn("approved") requiere URL pública (HTTPS). No funciona con localhost.
        // Para activarlo en producción: configurar mercadopago.back-url.* con URLs HTTPS reales.
        PreferenceRequest.PreferenceRequestBuilder reqBuilder = PreferenceRequest.builder()
                .items(items)
                .backUrls(backUrls)
                .externalReference("VENTA-" + idVenta);

        if (buyerEmail != null && !buyerEmail.isBlank()) {
            reqBuilder.payer(PreferencePayerRequest.builder()
                    .email(buyerEmail)
                    .build());
        }

        if (notificationUrl != null && !notificationUrl.isBlank()) {
            reqBuilder.notificationUrl(notificationUrl);
        }

        PreferenceRequest preferenceRequest = reqBuilder.build();

        try {
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            log.info("[MP] Preferencia creada OK — id={}, sandboxInitPoint={}",
                    preference.getId(), preference.getSandboxInitPoint());

            venta.setEstado(EstadoVentaEnum.PENDIENTE_PAGO);
            ventaRepository.save(venta);

            Optional<PagoENTITY> existente = pagoRepository
                    .findFirstByVenta_IdVentaAndMetodoPago(idVenta, MetodoPagoEnum.MERCADO_PAGO);

            if (existente.isEmpty()) {
                PagoENTITY pago = new PagoENTITY();
                pago.setVenta(venta);
                pago.setMetodoPago(MetodoPagoEnum.MERCADO_PAGO);
                pago.setMonto(venta.getTotal());
                pago.setEstado(EstadoPagoEnum.PENDIENTE);
                pago.setTransaccionId("MP-PREF-" + preference.getId());
                pagoRepository.save(pago);
            }

            String checkoutUrl = preference.getSandboxInitPoint();
            if (checkoutUrl == null || checkoutUrl.isBlank()) {
                throw new BusinessRuleException(
                        "Mercado Pago devolvió una preferencia sin sandboxInitPoint. "
                        + "Verifica que estás usando un Access Token de SANDBOX (TEST-...).");
            }

            return new PreferenceResponseDTO(preference.getId(), checkoutUrl);

        } catch (MPApiException e) {
            // Extraer el error REAL que devuelve MP
            int statusCode = e.getApiResponse() != null ? e.getApiResponse().getStatusCode() : -1;
            String responseBody = e.getApiResponse() != null ? e.getApiResponse().getContent() : "(sin body)";
            log.error("[MP] Error API al crear preferencia — HTTP {}: {}", statusCode, responseBody);
            throw new BusinessRuleException(
                    "Mercado Pago rechazó la preferencia [HTTP " + statusCode + "]: " + responseBody);

        } catch (MPException e) {
            log.error("[MP] Error de SDK al crear preferencia: {}", e.getMessage(), e);
            throw new BusinessRuleException("Error de SDK Mercado Pago: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void procesarNotificacion(Map<String, Object> payload) {
        String type = (String) payload.get("type");
        log.info("[MP Webhook] type={}, payload={}", type, payload);

        if (!"payment".equals(type)) return;

        Object dataObj = payload.get("data");
        if (!(dataObj instanceof Map<?, ?> dataMap)) return;

        Object idObj = dataMap.get("id");
        if (idObj == null) return;

        try {
            actualizarEstadoDesdeMP(Long.parseLong(String.valueOf(idObj)));
        } catch (NumberFormatException e) {
            log.warn("[MP Webhook] ID de pago inválido: {}", idObj);
        }
    }

    @Override
    @Transactional
    public Map<String, String> obtenerEstadoPago(String paymentId) {
        try {
            actualizarEstadoDesdeMP(Long.parseLong(paymentId));
        } catch (NumberFormatException e) {
            return Map.of("status", "invalid_id");
        }

        Optional<PagoENTITY> pagoOpt = pagoRepository.findByTransaccionId(paymentId);
        if (pagoOpt.isEmpty()) {
            return Map.of("status", "not_found");
        }

        PagoENTITY pago = pagoOpt.get();
        return Map.of(
                "status", pago.getEstado().name(),
                "idVenta", String.valueOf(pago.getVenta().getIdVenta())
        );
    }

    // Ejecutado dentro de un método @Transactional del mismo bean
    private void actualizarEstadoDesdeMP(Long mpPaymentId) {
        try {
            PaymentClient paymentClient = new PaymentClient();
            Payment payment = paymentClient.get(mpPaymentId);

            String externalRef = payment.getExternalReference();
            log.info("[MP] Estado pago {} — status={}, external_ref={}", mpPaymentId, payment.getStatus(), externalRef);

            if (externalRef == null || !externalRef.startsWith("VENTA-")) return;

            Integer idVenta;
            try {
                idVenta = Integer.parseInt(externalRef.replace("VENTA-", ""));
            } catch (NumberFormatException e) {
                return;
            }

            String mpPaymentIdStr = String.valueOf(mpPaymentId);
            String mpStatus = payment.getStatus();

            // Idempotencia: si ya procesamos este payment_id con estado final, saltar
            Optional<PagoENTITY> byPaymentId = pagoRepository.findByTransaccionId(mpPaymentIdStr);
            if (byPaymentId.isPresent() && byPaymentId.get().getEstado() != EstadoPagoEnum.PENDIENTE) {
                log.info("[MP] Pago {} ya procesado (estado={}), saltando.", mpPaymentId,
                        byPaymentId.get().getEstado());
                return;
            }

            VentaENTITY venta = ventaRepository.findById(idVenta).orElse(null);
            if (venta == null) return;

            PagoENTITY pago;
            if (byPaymentId.isPresent()) {
                pago = byPaymentId.get();
            } else {
                pago = pagoRepository
                        .findFirstByVenta_IdVentaAndMetodoPago(idVenta, MetodoPagoEnum.MERCADO_PAGO)
                        .orElseGet(() -> {
                            PagoENTITY np = new PagoENTITY();
                            np.setVenta(venta);
                            np.setMetodoPago(MetodoPagoEnum.MERCADO_PAGO);
                            np.setMonto(venta.getTotal());
                            np.setEstado(EstadoPagoEnum.PENDIENTE);
                            return np;
                        });
                pago.setTransaccionId(mpPaymentIdStr);
            }

            switch (mpStatus) {
                case "approved" -> {
                    log.info("[MP] Pago {} APROBADO — venta {} → PAGADA", mpPaymentId, idVenta);
                    pago.setEstado(EstadoPagoEnum.APROBADO);
                    venta.setEstado(EstadoVentaEnum.PAGADA);
                }
                case "rejected" -> {
                    log.info("[MP] Pago {} RECHAZADO — venta {} → PAGO_RECHAZADO", mpPaymentId, idVenta);
                    if (venta.getEstado() != EstadoVentaEnum.PAGO_RECHAZADO) {
                        restaurarStock(idVenta);
                    }
                    pago.setEstado(EstadoPagoEnum.RECHAZADO);
                    venta.setEstado(EstadoVentaEnum.PAGO_RECHAZADO);
                }
                case "cancelled" -> {
                    log.info("[MP] Pago {} CANCELADO — venta {} → PAGO_CANCELADO", mpPaymentId, idVenta);
                    if (venta.getEstado() != EstadoVentaEnum.PAGO_CANCELADO) {
                        restaurarStock(idVenta);
                    }
                    pago.setEstado(EstadoPagoEnum.RECHAZADO);
                    venta.setEstado(EstadoVentaEnum.PAGO_CANCELADO);
                }
                default -> log.info("[MP] Pago {} en estado '{}' — sin acción", mpPaymentId, mpStatus);
            }

            pagoRepository.save(pago);
            ventaRepository.save(venta);

        } catch (MPApiException e) {
            int statusCode = e.getApiResponse() != null ? e.getApiResponse().getStatusCode() : -1;
            String responseBody = e.getApiResponse() != null ? e.getApiResponse().getContent() : "(sin body)";
            log.warn("[MP] Error API consultando pago {} — HTTP {}: {}", mpPaymentId, statusCode, responseBody);
        } catch (MPException e) {
            log.warn("[MP] Error SDK consultando pago {}: {}", mpPaymentId, e.getMessage());
        }
    }

    private void restaurarStock(Integer idVenta) {
        List<VentaDetalleENTITY> detalles = ventaDetalleRepository.findByVenta_IdVenta(idVenta);
        for (VentaDetalleENTITY detalle : detalles) {
            ProductoENTITY producto = detalle.getProducto();
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto);
            log.info("[MP] Stock restaurado — producto={}, +{}", producto.getNombre(), detalle.getCantidad());
        }
    }
}
