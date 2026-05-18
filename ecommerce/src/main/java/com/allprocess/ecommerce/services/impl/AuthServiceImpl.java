package com.allprocess.ecommerce.services.impl;

import com.allprocess.ecommerce.dtos.request.LoginRequestDTO;
import com.allprocess.ecommerce.dtos.response.TokenResponseDTO;
import com.allprocess.ecommerce.services.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Override
    public TokenResponseDTO iniciarSesion(LoginRequestDTO loginRequest) {
        // TODO: Integrar Spring Security (AuthenticationManager) y JwtProvider
        // Aquí validaremos que el email y password coincidan en la BD para emitir el JWT.
        return null;
    }
}