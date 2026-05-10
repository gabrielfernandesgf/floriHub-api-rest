package com.br.florihub.florihubbackend.service;


import com.br.florihub.florihubbackend.dto.request.VendaRequest;
import com.br.florihub.florihubbackend.dto.response.VendaResponse;
import com.br.florihub.florihubbackend.model.Venda;
import com.br.florihub.florihubbackend.model.VendaItem;
import com.br.florihub.florihubbackend.repository.ProdutoRepository;
import com.br.florihub.florihubbackend.repository.UsuarioRepository;
import com.br.florihub.florihubbackend.repository.VendaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VendaService {

    @Autowired
    private VendaRepository vendaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Transactional
    public VendaResponse criar(VendaRequest request, String emailUsuario) {
        var usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        var venda = new Venda();
        venda.setUsuario(usuario);
        venda.setObservacao(request.observacao());

        List<VendaItem> itens = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : request.itens()) {
            var produto = produtoRepository.findById(itemReq.produtoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado: " + itemReq.produtoId()));

            if (produto.getQuantidadeEstoque() < itemReq.quantidade())
                throw new IllegalArgumentException(
                        "Estoque insuficiente para: " + produto.getNome());

            var item = new VendaItem();
            item.setVenda(venda);
            item.setProduto(produto);
            item.setQuantidade(itemReq.quantidade());
            item.setPrecoUnitario(produto.getPreco());
            item.setSubTotal(produto.getPreco().multiply(
                    BigDecimal.valueOf(itemReq.quantidade())));

            produto.setQuantidadeEstoque(
                    produto.getQuantidadeEstoque() - itemReq.quantidade());
            produtoRepository.save(produto);

            total = total.add(item.getSubTotal());
            itens.add(item);
        }

        venda.setItens(itens);
        venda.setValorTotal(total);
        return VendaResponse.from(vendaRepository.save(venda));
    }

    public List<VendaResponse> listar() {
        return vendaRepository.findAllComItens()
                .stream().map(VendaResponse::from).toList();
    }

    public VendaResponse buscarPorId(UUID id) {
        return vendaRepository.findByIdComItens(id)
                .map(VendaResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));
    }

    @Transactional
    public VendaResponse atualizarStatus(UUID id, String status) {
        var venda = vendaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));
        venda.setStatus(status.toUpperCase());
        return VendaResponse.from(vendaRepository.save(venda));
    }

}
