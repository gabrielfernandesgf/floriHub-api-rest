package com.br.florihub.florihubbackend.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProdutoRequest(

        @NotBlank
        @Size(max = 120) String nome,
        String descricao,

        @NotNull
        @DecimalMin("0.01") BigDecimal preco,

        @NotNull
        @Min(0) Integer quantidadeEstoque,

        @Size(max = 80)
        String categoria
) {
}
