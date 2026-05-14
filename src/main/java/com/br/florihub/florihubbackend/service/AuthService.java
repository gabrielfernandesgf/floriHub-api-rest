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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authManager, JwtService jwtService,
                       UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            // Autenticar
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

            // Buscar usuário
            var usuario = usuarioRepository.findByEmail(request.email())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // Gerar token
            var token = jwtService.gerarToken(new org.springframework.security.core.userdetails.User(
                    usuario.getEmail(), 
                    usuario.getSenhaHash(),
                    org.springframework.security.core.authority.AuthorityUtils
                            .createAuthorityList("ROLE_" + usuario.getRole())
            ));

            return new AuthResponse(token, "Bearer", usuario.getNome(),
                    usuario.getEmail(), usuario.getRole());
        } catch (Exception e) {
            throw new RuntimeException("Falha na autenticação: " + e.getMessage(), e);
        }
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
