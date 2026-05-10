package com.br.florihub.florihubbackend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VendaItemRequest(
        @NotNull
        UUID produtoId,

        @NotNull
        @Min(1)
        Integer quantidade
) {
}
