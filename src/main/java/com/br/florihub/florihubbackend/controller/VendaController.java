package com.br.florihub.florihubbackend.controller;


import com.br.florihub.florihubbackend.dto.request.VendaRequest;
import com.br.florihub.florihubbackend.dto.response.VendaResponse;
import com.br.florihub.florihubbackend.service.VendaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vendas")
public class VendaController {

    @Autowired
    private VendaService service;

    @PostMapping
    public ResponseEntity<VendaResponse> criar(@Valid @RequestBody VendaRequest request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.criar(request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<VendaResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VendaResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VendaResponse> atualizarStatus(@PathVariable UUID id,
                                                         @RequestParam String status) {
        return ResponseEntity.ok(service.atualizarStatus(id, status));
    }
}
