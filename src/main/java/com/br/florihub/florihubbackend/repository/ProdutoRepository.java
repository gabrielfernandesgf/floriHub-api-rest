package com.br.florihub.florihubbackend.repository;

import com.br.florihub.florihubbackend.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    List<Produto> findByAtivoTrue();
    List<Produto> findByCategoria(String categoria);
}
