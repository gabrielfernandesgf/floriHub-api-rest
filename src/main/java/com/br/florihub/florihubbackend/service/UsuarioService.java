package com.br.florihub.florihubbackend.service;


import com.br.florihub.florihubbackend.dto.request.UsuarioRequest;
import com.br.florihub.florihubbackend.dto.response.UsuarioResponse;
import com.br.florihub.florihubbackend.model.Usuario;
import com.br.florihub.florihubbackend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public UsuarioResponse criar(UsuarioRequest request) {
        if (repository.existsByEmail(request.email()))
            throw new IllegalArgumentException("E-mail já cadastrado.");

        var usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setRole(request.role().toUpperCase());
        return UsuarioResponse.from(repository.save(usuario));
    }

    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(UsuarioResponse::from).toList();
    }

    public UsuarioResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .map(UsuarioResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    public UsuarioResponse atualizar(UUID id, UsuarioRequest request) {
        var usuario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setRole(request.role().toUpperCase());
        return UsuarioResponse.from(repository.save(usuario));
    }

    public void desativar(UUID id) {
        var usuario = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        usuario.setAtivo(false);
        repository.save(usuario);
    }

}
