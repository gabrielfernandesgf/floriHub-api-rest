package com.br.florihub.florihubbackend.repository;

import com.br.florihub.florihubbackend.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
}
