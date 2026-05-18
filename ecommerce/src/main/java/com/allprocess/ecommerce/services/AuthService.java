package com.allprocess.ecommerce.services;

import com.allprocess.ecommerce.dtos.request.LoginRequestDTO;
import com.allprocess.ecommerce.dtos.response.TokenResponseDTO;

public interface AuthService {
    TokenResponseDTO iniciarSesion(LoginRequestDTO loginRequest);
}