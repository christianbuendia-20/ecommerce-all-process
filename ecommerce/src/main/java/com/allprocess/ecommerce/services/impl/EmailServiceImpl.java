package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    @Override
    public void enviarCodigoVerificacion(String destinatario, String nombres, String codigo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(remitente, "All Process S.A.C.");
            helper.setTo(destinatario);
            helper.setSubject("Código de verificación — All Process S.A.C.");
            helper.setText(construirHtml(nombres, codigo), true);
            mailSender.send(message);
            log.info("Código de verificación enviado a {}", destinatario);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Error enviando email de verificación a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de verificación", e);
        }
    }

    @Override
    public void enviarCodigoReset(String destinatario, String nombres, String codigo) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(remitente, "All Process S.A.C.");
            helper.setTo(destinatario);
            helper.setSubject("Recuperacion de contrasena -- All Process S.A.C.");
            helper.setText(construirHtmlReset(nombres, codigo), true);
            mailSender.send(message);
            log.info("Codigo de recuperacion enviado a {}", destinatario);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Error enviando email de recuperacion a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo de recuperacion", e);
        }
    }

    private String construirHtmlReset(String nombres, String codigo) {
        String[] digitos = codigo.split("");
        StringBuilder casillasCodigo = new StringBuilder();
        for (String d : digitos) {
            casillasCodigo.append(
                "<td style=\"width:48px;height:56px;background:#f4f2ff;border:2px solid #6b42f8;" +
                "border-radius:10px;text-align:center;vertical-align:middle;\">" +
                "<span style=\"font-size:28px;font-weight:800;color:#6b42f8;font-family:monospace;\">"
                + d + "</span></td>" +
                "<td style=\"width:8px;\"></td>"
            );
        }
        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'></head><body " +
            "style='margin:0;padding:0;background:#f4f6ff;font-family:Montserrat,Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f6ff;padding:40px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='560' cellpadding='0' cellspacing='0' " +
            "style='background:#fff;border-radius:22px;box-shadow:0 12px 40px rgba(103,111,168,.15);overflow:hidden;max-width:560px;width:100%;'>" +
            "<tr><td style='background:linear-gradient(135deg,#2a67f4,#6b42f8);padding:36px 40px;text-align:center;'>" +
            "<p style='margin:0;color:#fff;font-size:22px;font-weight:800;letter-spacing:.02em;'>All Process S.A.C.</p>" +
            "<p style='margin:6px 0 0;color:rgba(255,255,255,.8);font-size:13px;'>Recuperacion de contrasena</p>" +
            "</td></tr>" +
            "<tr><td style='padding:40px 40px 32px;'>" +
            "<p style='margin:0 0 8px;font-size:16px;font-weight:700;color:#342266;'>Hola, " + nombres + " &#128075;</p>" +
            "<p style='margin:0 0 28px;font-size:14px;color:#73758a;line-height:1.6;'>" +
            "Recibimos una solicitud para restablecer la contrasena de tu cuenta. " +
            "Usa el siguiente codigo de 6 digitos. Es valido por <strong>15 minutos</strong>.</p>" +
            "<table cellpadding='0' cellspacing='0' style='margin:0 auto 28px;'>" +
            "<tr>" + casillasCodigo + "</tr></table>" +
            "<p style='margin:0 0 28px;font-size:12px;color:#9091a3;text-align:center;'>" +
            "Si no solicitaste este cambio, ignora este correo. Tu contrasena no sera modificada.</p>" +
            "<hr style='border:none;border-top:1px solid #e4e7f2;margin:0 0 24px;'>" +
            "<p style='margin:0;font-size:11px;color:#9091a3;text-align:center;'>" +
            "&#169; 2026 All Process S.A.C. &#8212; Este es un correo automatico, no respondas.</p>" +
            "</td></tr></table></td></tr></table></body></html>";
    }

    private String construirHtml(String nombres, String codigo) {
        String[] digitos = codigo.split("");
        StringBuilder casillasCodigo = new StringBuilder();
        for (String d : digitos) {
            casillasCodigo.append(
                "<td style=\"width:48px;height:56px;background:#f4f2ff;border:2px solid #6b42f8;" +
                "border-radius:10px;text-align:center;vertical-align:middle;\">" +
                "<span style=\"font-size:28px;font-weight:800;color:#6b42f8;font-family:monospace;\">"
                + d + "</span></td>" +
                "<td style=\"width:8px;\"></td>"
            );
        }

        return "<!DOCTYPE html><html lang='es'><head><meta charset='UTF-8'></head><body " +
            "style='margin:0;padding:0;background:#f4f6ff;font-family:Montserrat,Arial,sans-serif;'>" +
            "<table width='100%' cellpadding='0' cellspacing='0' style='background:#f4f6ff;padding:40px 0;'>" +
            "<tr><td align='center'>" +
            "<table width='560' cellpadding='0' cellspacing='0' " +
            "style='background:#fff;border-radius:22px;box-shadow:0 12px 40px rgba(103,111,168,.15);overflow:hidden;max-width:560px;width:100%;'>" +
            // Header
            "<tr><td style='background:linear-gradient(135deg,#2a67f4,#6b42f8);padding:36px 40px;text-align:center;'>" +
            "<p style='margin:0;color:#fff;font-size:22px;font-weight:800;letter-spacing:.02em;'>All Process S.A.C.</p>" +
            "<p style='margin:6px 0 0;color:rgba(255,255,255,.8);font-size:13px;'>Verificación de cuenta</p>" +
            "</td></tr>" +
            // Body
            "<tr><td style='padding:40px 40px 32px;'>" +
            "<p style='margin:0 0 8px;font-size:16px;font-weight:700;color:#342266;'>Hola, " + nombres + " 👋</p>" +
            "<p style='margin:0 0 28px;font-size:14px;color:#73758a;line-height:1.6;'>" +
            "Usa el siguiente código de 6 dígitos para verificar tu cuenta. " +
            "Es válido por <strong>15 minutos</strong>.</p>" +
            // Code boxes
            "<table cellpadding='0' cellspacing='0' style='margin:0 auto 28px;'>" +
            "<tr>" + casillasCodigo + "</tr></table>" +
            "<p style='margin:0 0 28px;font-size:12px;color:#9091a3;text-align:center;'>" +
            "Si no creaste esta cuenta, ignora este correo.</p>" +
            "<hr style='border:none;border-top:1px solid #e4e7f2;margin:0 0 24px;'>" +
            "<p style='margin:0;font-size:11px;color:#9091a3;text-align:center;'>" +
            "© 2026 All Process S.A.C. — Este es un correo automático, no respondas.</p>" +
            "</td></tr></table></td></tr></table></body></html>";
    }
}
