package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.entities.PasswordResetENTITY;
import com.allprocess.ecommerce.entities.UsuarioENTITY;
import com.allprocess.ecommerce.exceptions.BusinessRuleException;
import com.allprocess.ecommerce.exceptions.ResourceNotFoundException;
import com.allprocess.ecommerce.repositories.PasswordResetRepository;
import com.allprocess.ecommerce.repositories.UsuarioRepository;
import com.allprocess.ecommerce.services.EmailService;
import com.allprocess.ecommerce.services.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetRepository resetRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${verificacion.expiracion.minutos:15}")
    private int expiracionMinutos;

    @Value("${verificacion.max-intentos:5}")
    private int maxIntentos;

    @Value("${verificacion.cooldown.segundos:60}")
    private int cooldownSegundos;

    @Override
    @Transactional
    public void solicitarReset(String email) {
        UsuarioENTITY usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una cuenta registrada con ese correo."));

        long recientes = resetRepository.countByUsuario_EmailAndFechaCreacionAfter(
                email, LocalDateTime.now().minusSeconds(cooldownSegundos));
        if (recientes > 0) {
            throw new BusinessRuleException(
                    "Espera " + cooldownSegundos + " segundos antes de solicitar otro codigo.");
        }

        resetRepository.invalidarResetsPrevios(email);

        String rawCode = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        PasswordResetENTITY reset = new PasswordResetENTITY();
        reset.setUsuario(usuario);
        reset.setCodigoHash(hashear(rawCode));
        reset.setFechaExpiracion(LocalDateTime.now().plusMinutes(expiracionMinutos));
        resetRepository.save(reset);

        emailService.enviarCodigoReset(email, usuario.getNombres(), rawCode);
        log.info("Codigo de recuperacion enviado a {}", email);
    }

    @Override
    @Transactional
    public void verificarCodigo(String email, String codigo) {
        validarCodigoInterno(email, codigo, false);
    }

    @Override
    @Transactional
    public void cambiarPassword(String email, String codigo, String nuevaPassword) {
        PasswordResetENTITY reset = validarCodigoInterno(email, codigo, true);
        UsuarioENTITY usuario = reset.getUsuario();
        usuario.setPasswordHash(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
        log.info("Contrasena actualizada para {}", email);
    }

    private PasswordResetENTITY validarCodigoInterno(String email, String codigo, boolean marcarUsado) {
        PasswordResetENTITY reset = resetRepository
                .findTopByUsuario_EmailAndUsadoFalseOrderByFechaCreacionDesc(email)
                .orElseThrow(() -> new BusinessRuleException(
                        "No hay un codigo activo para este correo. Solicita uno nuevo."));

        if (LocalDateTime.now().isAfter(reset.getFechaExpiracion())) {
            throw new BusinessRuleException("El codigo ha expirado. Solicita uno nuevo.");
        }

        if (reset.getIntentos() >= maxIntentos) {
            throw new BusinessRuleException(
                    "Has superado el numero maximo de intentos. Solicita un nuevo codigo.");
        }

        if (!hashear(codigo).equals(reset.getCodigoHash())) {
            reset.setIntentos(reset.getIntentos() + 1);
            resetRepository.save(reset);
            int restantes = maxIntentos - reset.getIntentos();
            if (restantes <= 0) {
                throw new BusinessRuleException(
                        "Codigo incorrecto. Has agotado todos los intentos. Solicita uno nuevo.");
            }
            throw new BusinessRuleException(
                    "Codigo incorrecto. Te quedan " + restantes + " intento(s).");
        }

        if (marcarUsado) {
            reset.setUsado(true);
            resetRepository.save(reset);
        }
        return reset;
    }

    private String hashear(String codigo) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codigo.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear el codigo", e);
        }
    }
}
