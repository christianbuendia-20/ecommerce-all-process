package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.request.ReenviarCodigoDTO;
import com.allprocess.ecommerce.dtos.request.VerificarEmailDTO;
import com.allprocess.ecommerce.entities.UsuarioENTITY;
import com.allprocess.ecommerce.entities.VerificacionEmailENTITY;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.repositories.UsuarioRepository;
import com.allprocess.ecommerce.repositories.VerificacionEmailRepository;
import com.allprocess.ecommerce.services.EmailService;
import com.allprocess.ecommerce.services.VerificacionEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificacionEmailServiceImpl implements VerificacionEmailService {

    private final VerificacionEmailRepository verificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;

    @Value("${verificacion.expiracion.minutos:15}")
    private int expiracionMinutos;

    @Value("${verificacion.max-intentos:5}")
    private int maxIntentos;

    @Value("${verificacion.cooldown.segundos:60}")
    private int cooldownSegundos;

    // ─────────────────────────────────────────────────────
    // Genera, guarda y envía un código de verificación
    // ─────────────────────────────────────────────────────
    @Override
    @Transactional
    public void generarYEnviarCodigo(String email) {
        UsuarioENTITY usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + email));

        // Invalidar códigos anteriores
        verificacionRepository.invalidarCodigosPrevios(email);

        // Generar código de 6 dígitos con SecureRandom
        String rawCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        String hashedCode = hashear(rawCode);

        // Persistir
        VerificacionEmailENTITY verificacion = new VerificacionEmailENTITY();
        verificacion.setUsuario(usuario);
        verificacion.setCodigoHash(hashedCode);
        verificacion.setFechaExpiracion(LocalDateTime.now().plusMinutes(expiracionMinutos));
        verificacionRepository.save(verificacion);

        // Enviar email (si falla se loguea, no se revierte el usuario)
        try {
            emailService.enviarCodigoVerificacion(email, usuario.getNombres(), rawCode);
        } catch (Exception e) {
            log.error("No se pudo enviar el email de verificación a {}: {}", email, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    // Valida el código y marca la cuenta como verificada
    // ─────────────────────────────────────────────────────
    @Override
    @Transactional
    public void verificarCodigo(VerificarEmailDTO dto) {
        // Buscar el último código sin usar (expirado o no) para dar mensajes precisos
        VerificacionEmailENTITY verificacion = verificacionRepository
                .findTopByUsuario_EmailAndUsadoFalseOrderByFechaCreacionDesc(dto.getEmail())
                .orElseThrow(() -> new BusinessRuleException(
                        "No existe un código de verificación activo para este correo. Solicita uno nuevo."));

        // Verificar expiración
        if (LocalDateTime.now().isAfter(verificacion.getFechaExpiracion())) {
            throw new BusinessRuleException(
                    "El código ha expirado. Solicita un nuevo código de verificación.");
        }

        // Verificar intentos
        if (verificacion.getIntentos() >= maxIntentos) {
            throw new BusinessRuleException(
                    "Has superado el número máximo de intentos. Solicita un nuevo código.");
        }

        // Comparar hashes
        String hashedInput = hashear(dto.getCodigo());
        if (!hashedInput.equals(verificacion.getCodigoHash())) {
            verificacion.setIntentos(verificacion.getIntentos() + 1);
            verificacionRepository.save(verificacion);

            int restantes = maxIntentos - verificacion.getIntentos();
            if (restantes <= 0) {
                throw new BusinessRuleException(
                        "Código incorrecto. Has agotado todos los intentos. Solicita un nuevo código.");
            }
            throw new BusinessRuleException(
                    "Código incorrecto. Te quedan " + restantes + " intento(s).");
        }

        // Código correcto → marcar como usado y verificar el usuario
        verificacion.setUsado(true);
        verificacionRepository.save(verificacion);

        UsuarioENTITY usuario = verificacion.getUsuario();
        usuario.setEmailVerificado(true);
        usuarioRepository.save(usuario);

        log.info("Cuenta verificada exitosamente para {}", dto.getEmail());
    }

    // ─────────────────────────────────────────────────────
    // Reenvía un nuevo código respetando el cooldown
    // ─────────────────────────────────────────────────────
    @Override
    @Transactional
    public void reenviarCodigo(ReenviarCodigoDTO dto) {
        // Verificar que el usuario existe
        UsuarioENTITY usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + dto.getEmail()));

        if (usuario.getEmailVerificado()) {
            throw new BusinessRuleException("Esta cuenta ya está verificada.");
        }

        // Cooldown: no más de 1 código por minuto
        long recientes = verificacionRepository.countByUsuario_EmailAndFechaCreacionAfter(
                dto.getEmail(), LocalDateTime.now().minusSeconds(cooldownSegundos));

        if (recientes > 0) {
            throw new BusinessRuleException(
                    "Debes esperar " + cooldownSegundos + " segundos antes de solicitar un nuevo código.");
        }

        generarYEnviarCodigo(dto.getEmail());
        log.info("Código de verificación reenviado a {}", dto.getEmail());
    }

    // ─────────────────────────────────────────────────────
    // SHA-256 del código (nunca almacenamos texto plano)
    // ─────────────────────────────────────────────────────
    private String hashear(String codigo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codigo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear el código de verificación", e);
        }
    }
}
