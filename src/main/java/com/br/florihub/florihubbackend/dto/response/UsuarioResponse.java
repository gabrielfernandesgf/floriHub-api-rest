package com.br.florihub.florihubbackend.dto.response;

import com.br.florihub.florihubbackend.model.Usuario;

import java.time.LocalDateTime;
import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        String role,
        Boolean ativo,
        LocalDateTime criadoEm
) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(),
                u.getRole(), u.getAtivo(), u.getCriadoEm());
    }

}
