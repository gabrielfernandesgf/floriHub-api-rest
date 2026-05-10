package com.br.florihub.florihubbackend.dto.response;

public record AuthResponse(
        String token,
        String tipo,
        String nome,
        String email,
        String role
) {
}
