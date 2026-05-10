package com.br.florihub.florihubbackend.dto.response;

import com.br.florihub.florihubbackend.model.VendaItem;

import java.math.BigDecimal;
import java.util.UUID;

public record VendaItemResponse(
        UUID id,
        UUID produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subTotal
) {

    public static VendaItemResponse from(VendaItem i) {
        return new VendaItemResponse(i.getId(), i.getProduto().getId(),
                i.getProduto().getNome(), i.getQuantidade(),
                i.getPrecoUnitario(), i.getSubTotal());
    }

}
