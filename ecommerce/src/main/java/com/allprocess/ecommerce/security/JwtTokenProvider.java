package com.allprocess.ecommerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long expirationMs;

    // Inyectamos la clave secreta y el tiempo de expiración desde application.properties
    public JwtTokenProvider(
            @Value("${jwt.secret:AllProcessSecretKey2024SuperSeguraParaEcommerce!}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // Genera un token JWT con el email y rol del usuario
    public String generarToken(String email, String rol, Integer idUsuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("idUsuario", idUsuario)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key)
                .compact();
    }

    // Extrae el email (subject) del token
    public String obtenerEmailDelToken(String token) {
        return obtenerClaims(token).getPayload().getSubject();
    }

    // Extrae el rol del token
    public String obtenerRolDelToken(String token) {
        return obtenerClaims(token).getPayload().get("rol", String.class);
    }

    // Extrae el ID del usuario del token
    public Integer obtenerIdUsuarioDelToken(String token) {
        return obtenerClaims(token).getPayload().get("idUsuario", Integer.class);
    }

    // Valida que el token sea válido (no expirado, firma correcta)
    public boolean validarToken(String token) {
        try {
            obtenerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Parsea y devuelve los claims del token
    private Jws<Claims> obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }
}
