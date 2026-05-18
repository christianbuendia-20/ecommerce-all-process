package com.allprocess.ecommerce.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // ESTA ES LA LLAVE SECRETA (Debe tener al menos 256 bits).
    // En producción, esto debe ir en el archivo application.properties
    private static final String SECRET_KEY = "4a64356a427041764a514d336836463972626e5762316e6e33446b7371356e72";

    // Tiempo de vida del token: 24 horas (en milisegundos)
    private static final long JWT_EXPIRATION = 86400000;

    // 1. EXTRAER EL EMAIL (USERNAME) DEL TOKEN
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 2. EXTRAER CUALQUIER DATO ESPECÍFICO DEL TOKEN
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 3. GENERAR EL TOKEN CUANDO EL USUARIO SE LOGUEA
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // 4. GENERAR TOKEN CON DATOS EXTRA (Si quisieras agregar el ROL adentro del token)
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // 5. VALIDAR SI EL TOKEN ES VÁLIDO Y LE PERTENECE AL USUARIO
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // 6. VERIFICAR SI EL TOKEN YA VENCIÓ
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 7. LEER TODOS LOS DATOS DEL TOKEN USANDO LA LLAVE SECRETA
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 8. OBTENER LA LLAVE FIRMADA
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
