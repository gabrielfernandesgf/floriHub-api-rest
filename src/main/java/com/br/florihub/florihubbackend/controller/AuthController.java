package com.br.florihub.florihubbackend.controller;

import com.br.florihub.florihubbackend.dto.request.LoginRequest;
import com.br.florihub.florihubbackend.dto.request.UsuarioRequest;
import com.br.florihub.florihubbackend.dto.response.AuthResponse;
import com.br.florihub.florihubbackend.dto.response.UsuarioResponse;
import com.br.florihub.florihubbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registrar")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registrar(request));
    }
}