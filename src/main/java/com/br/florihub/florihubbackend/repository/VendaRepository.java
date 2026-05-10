package com.br.florihub.florihubbackend.repository;

import com.br.florihub.florihubbackend.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendaRepository extends JpaRepository<Venda, UUID> {

    List<Venda> findByUsuarioId(UUID usuarioId);
    List<Venda> findByStatus(String status);

    @Query("SELECT v FROM Venda v JOIN FETCH v.itens i JOIN FETCH i.produto JOIN FETCH v.usuario")
    List<Venda> findAllComItens();

    @Query("SELECT v FROM Venda v JOIN FETCH v.itens i JOIN FETCH i.produto JOIN FETCH v.usuario WHERE v.id = :id")
    Optional<Venda> findByIdComItens(UUID id);
}
