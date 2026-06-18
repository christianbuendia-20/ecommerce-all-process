package com.allprocess.ecommerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desactivamos CSRF porque usamos JWT (stateless)
            .csrf(csrf -> csrf.disable())

            // Configuramos las reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // Páginas públicas (Thymeleaf)
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/homeclient/**", "/catalogclient/**", "/carclient/**", "/myorders/**").permitAll()
                .requestMatchers("/homeadmin/**", "/productmanagement/**", "/ordermanagement/**").permitAll()
                .requestMatchers("/customermanagement/**", "/usermanagement/**", "/adminreports/**").permitAll()
                .requestMatchers("/homeworker/**", "/profile/**", "/logout").permitAll()
                .requestMatchers("/verificar-email").permitAll()

                // API pública
                .requestMatchers("/api/auth/**", "/api/usuarios/registro").permitAll()
                .requestMatchers("/api/auth/verificar-email", "/api/auth/reenviar-codigo").permitAll()
                .requestMatchers("/api/productos/**", "/api/categorias/**").permitAll()

                // Mercado Pago: webhook (MP servers) y diagnóstico (dev tool) son públicos
                .requestMatchers("/api/payments/webhook", "/api/payments/diagnostico").permitAll()
                // Estado de pago y creación de preferencia requieren autenticación
                .requestMatchers("/api/payments/**").authenticated()

                // API de cliente (requiere autenticación)
                .requestMatchers("/api/ventas/**", "/api/direcciones/**", "/api/profile/**").authenticated()
                .requestMatchers("/api/pagos/**").authenticated()

                // API de administrador
                .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")

                // Cualquier otra request es pública (para las vistas Thymeleaf)
                .anyRequest().permitAll()
            )

            // Usamos IF_REQUIRED para que las vistas con sesión sigan funcionando
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

            // Agregamos nuestro filtro JWT antes del filtro de autenticación por defecto
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }
}
