package com.allprocess.ecommerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Extraer el token del header Authorization
        String token = extraerToken(request);

        // Si hay token, es válido y no está en la blacklist, autenticamos al usuario
        if (StringUtils.hasText(token)
                && jwtTokenProvider.validarToken(token)
                && !tokenBlacklistService.estaInvalidado(token)) {
            String email = jwtTokenProvider.obtenerEmailDelToken(token);
            String rol = jwtTokenProvider.obtenerRolDelToken(token);

            // Spring Security espera el formato "ROLE_ADMINISTRADOR"
            List<SimpleGrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority("ROLE_" + rol)
            );

            Integer idUsuario = jwtTokenProvider.obtenerIdUsuarioDelToken(token);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            authentication.setDetails(idUsuario);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    // Extrae el token del header "Authorization: Bearer <token>"
    private String extraerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
