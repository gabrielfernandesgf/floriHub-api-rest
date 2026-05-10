package com.br.florihub.florihubbackend.dto.response;

import com.br.florihub.florihubbackend.model.Produto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProdutoResponse(

        UUID id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer quantidadeEstoque,
        String categoria,
        Boolean ativo,
        LocalDateTime criadoEm
) {

    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(),
                p.getPreco(), p.getQuantidadeEstoque(), p.getCategoria(),
                p.getAtivo(), p.getCriadoEm());
    }

}
