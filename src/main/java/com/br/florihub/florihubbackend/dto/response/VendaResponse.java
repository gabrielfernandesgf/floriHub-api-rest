package com.br.florihub.florihubbackend.dto.response;

import com.br.florihub.florihubbackend.model.Venda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VendaResponse(
        UUID id,
        UUID usuarioId,
        String nomeVendedor,
        BigDecimal valorTotal,
        String status,
        String observacao,
        LocalDateTime dataVenda,
        List<VendaItemResponse> itens
) {

    public static VendaResponse from(Venda v) {
        List<VendaItemResponse> itens = v.getItens().stream()
                .map(VendaItemResponse::from).toList();
        return new VendaResponse(v.getId(), v.getUsuario().getId(),
                v.getUsuario().getNome(), v.getValorTotal(), v.getStatus(),
                v.getObservacao(), v.getDataVenda(), itens);
    }

}
