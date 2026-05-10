package com.br.florihub.florihubbackend.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VendaRequest(
        String observacao,
        @NotEmpty List<VendaItemRequest> itens
) {
}
