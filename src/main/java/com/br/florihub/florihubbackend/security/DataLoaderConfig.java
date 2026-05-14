package com.br.florihub.florihubbackend.security;

import com.br.florihub.florihubbackend.model.Usuario;
import com.br.florihub.florihubbackend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * Configuração para carregar dados iniciais no banco de dados
 * Executa automaticamente ao iniciar a aplicação (profile: default/dev)
 * NÃO executa em testes
 */
@Configuration
@Profile("!test")
public class DataLoaderConfig {

    @Bean
    CommandLineRunner loadData(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                System.out.println("🔄 Carregando dados iniciais...");

                // Carregar admin
                criarUsuarioSeNaoExistir(
                    usuarioRepository,
                    passwordEncoder,
                    "admin@florihub.com",
                    "Administrador",
                    "senha123",
                    "ADMIN"
                );

                // Carregar vendedor
                criarUsuarioSeNaoExistir(
                    usuarioRepository,
                    passwordEncoder,
                    "vendedor@florihub.com",
                    "Vendedor Teste",
                    "senha123",
                    "VENDEDOR"
                );

                System.out.println("Dados iniciais carregados com sucesso!");

            } catch (Exception e) {
                System.err.println("Erro ao carregar dados iniciais: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    /**
     * Cria um usuário se ele não existir no banco
     */
    private void criarUsuarioSeNaoExistir(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            String email,
            String nome,
            String senha,
            String role) {

        try {
            //VERIFICAR SE JÁ EXISTE
            var usuarioExistente = usuarioRepository.findByEmail(email);

            if (usuarioExistente.isPresent()) {
                System.out.println("ℹ️  Usuário " + email + " já existe no banco");
                return;
            }

            //CRIAR NOVO USUÁRIO
            Usuario novoUsuario = new Usuario();
            novoUsuario.setNome(nome);
            novoUsuario.setEmail(email);
            novoUsuario.setSenhaHash(passwordEncoder.encode(senha));
            novoUsuario.setRole(role);
            novoUsuario.setAtivo(true);
            novoUsuario.setCriadoEm(LocalDateTime.now());

            usuarioRepository.save(novoUsuario);
            System.out.println("Usuário " + email + " criado com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao criar usuário " + email + ": " + e.getMessage());
        }
    }
}
