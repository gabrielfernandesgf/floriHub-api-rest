package com.br.florihub.florihubbackend.service;

import com.br.florihub.florihubbackend.dto.request.LoginRequest;
import com.br.florihub.florihubbackend.dto.request.UsuarioRequest;
import com.br.florihub.florihubbackend.dto.response.AuthResponse;
import com.br.florihub.florihubbackend.dto.response.UsuarioResponse;
import com.br.florihub.florihubbackend.model.Usuario;
import com.br.florihub.florihubbackend.repository.UsuarioRepository;
import com.br.florihub.florihubbackend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AuthenticationManager authManager, JwtService jwtService,
                       UserDetailsService userDetailsService, UsuarioRepository usuarioRepository) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.usuarioRepository = usuarioRepository;
    }

    public AuthResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        var userDetails = userDetailsService.loadUserByUsername(request.email());
        var token = jwtService.gerarToken(userDetails);

        var usuario = usuarioRepository.findByEmail(request.email()).orElseThrow();
        return new AuthResponse(token, "Bearer", usuario.getNome(),
                usuario.getEmail(), usuario.getRole());
    }

    public UsuarioResponse registrar(UsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("E-mail já cadastrado.");

        var usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setRole(request.role().toUpperCase());
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

}
