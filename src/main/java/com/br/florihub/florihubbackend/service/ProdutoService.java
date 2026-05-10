package com.br.florihub.florihubbackend.service;

import com.br.florihub.florihubbackend.dto.request.ProdutoRequest;
import com.br.florihub.florihubbackend.dto.response.ProdutoResponse;
import com.br.florihub.florihubbackend.model.Produto;
import com.br.florihub.florihubbackend.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository repository;

    public ProdutoResponse criar(ProdutoRequest request) {
        var produto = new Produto();
        mapear(produto, request);
        return ProdutoResponse.from(repository.save(produto));
    }

    public List<ProdutoResponse> listar() {
        return repository.findByAtivoTrue().stream().map(ProdutoResponse::from).toList();
    }

    public ProdutoResponse buscarPorId(UUID id) {
        return repository.findById(id)
                .map(ProdutoResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
    }

    public ProdutoResponse atualizar(UUID id, ProdutoRequest request) {
        var produto = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        mapear(produto, request);
        return ProdutoResponse.from(repository.save(produto));
    }

    public void desativar(UUID id) {
        var produto = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        produto.setAtivo(false);
        repository.save(produto);
    }

    private void mapear(Produto produto, ProdutoRequest request) {
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setQuantidadeEstoque(request.quantidadeEstoque());
        produto.setCategoria(request.categoria());
    }
}
