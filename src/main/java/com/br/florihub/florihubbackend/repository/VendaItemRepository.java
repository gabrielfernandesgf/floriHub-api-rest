package com.br.florihub.florihubbackend.repository;

import com.br.florihub.florihubbackend.model.VendaItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VendaItemRepository extends JpaRepository<VendaItem, UUID> {
    List<VendaItem> findByVendaId(UUID vendaId);
}
