package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.request.LoginRequestDTO;
import com.allprocess.ecommerce.dtos.response.LoginResponseDTO;
import com.allprocess.ecommerce.entities.UsuarioENTITY;
import com.allprocess.ecommerce.repositories.UsuarioRepository;
import com.allprocess.ecommerce.security.JwtTokenProvider;
import com.allprocess.ecommerce.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        UsuarioENTITY usuario = usuarioRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);

        if (usuario == null) {
            return errorResponse("Credenciales inválidas");
        }

        if (!passwordEncoder.matches(loginRequest.getContrasenia(), usuario.getPasswordHash())) {
            return errorResponse("Credenciales inválidas");
        }

        if (!usuario.getActivo()) {
            return errorResponse("Cuenta desactivada. Contacte al administrador.");
        }

        String rolDb = usuario.getRol().getNombre().toUpperCase();
        String rolForm = loginRequest.getRol().toUpperCase();

        if (!rolDb.equals(rolForm)) {
            return errorResponse("El rol seleccionado no coincide con el usuario.");
        }

        // Generar el token JWT
        String token = jwtTokenProvider.generarToken(
                usuario.getEmail(),
                usuario.getRol().getNombre(),
                usuario.getIdUsuario()
        );

        String redirectUrl = switch (rolDb) {
            case "ADMINISTRADOR" -> "/homeadmin";
            case "VENDEDOR", "TRABAJADOR" -> "/homeworker";
            default -> "/homeclient";
        };

        return new LoginResponseDTO(
                true,
                redirectUrl,
                null,
                "Inicio de sesión exitoso",
                token,
                usuario.getNombres(),
                usuario.getRol().getNombre()
        );
    }

    private LoginResponseDTO errorResponse(String mensaje) {
        return new LoginResponseDTO(false, null, mensaje, null, null, null, null);
    }
}
