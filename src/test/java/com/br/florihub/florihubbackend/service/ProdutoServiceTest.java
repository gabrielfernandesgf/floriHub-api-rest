package com.br.florihub.florihubbackend.service;

import com.br.florihub.florihubbackend.dto.request.ProdutoRequest;
import com.br.florihub.florihubbackend.dto.response.ProdutoResponse;
import com.br.florihub.florihubbackend.model.Produto;
import com.br.florihub.florihubbackend.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - ProdutoService")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private ProdutoRequest produtoRequest;
    private Produto produtoEntity;
    private UUID produtoId;

    @BeforeEach
    void setUp() {
        produtoId = UUID.randomUUID();

        // Setup do ProdutoRequest com dados realistas
        produtoRequest = new ProdutoRequest(
                "Rosa Vermelha Premium",
                "Rosa vermelha de excelente qualidade para arranjos",
                new BigDecimal("45.90"),
                100,
                "Flores Cortadas"
        );

        // Setup da entidade Produto
        produtoEntity = new Produto();
        produtoEntity.setId(produtoId);
        produtoEntity.setNome("Rosa Vermelha Premium");
        produtoEntity.setDescricao("Rosa vermelha de excelente qualidade para arranjos");
        produtoEntity.setPreco(new BigDecimal("45.90"));
        produtoEntity.setQuantidadeEstoque(100);
        produtoEntity.setCategoria("Flores Cortadas");
        produtoEntity.setAtivo(true);
        produtoEntity.setCriadoEm(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar um produto com todos os atributos corretos")
    void testCriarProdutoComAtributosCorretos() {
        // Arrange
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoEntity);

        // Act
        ProdutoResponse response = produtoService.criar(produtoRequest);

        // Assert
        assertNotNull(response, "A resposta do produto não deve ser nula");
        assertEquals("Rosa Vermelha Premium", response.nome(), "Nome do produto incorreto");
        assertEquals(new BigDecimal("45.90"), response.preco(), "Preço do produto incorreto");
        assertEquals(100, response.quantidadeEstoque(), "Quantidade em estoque incorreta");
        assertEquals("Flores Cortadas", response.categoria(), "Categoria do produto incorreta");
        assertTrue(response.ativo(), "Produto deve estar ativo");
        assertNotNull(response.criadoEm(), "Data de criação não deve ser nula");

        // Verify
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve buscar produto por ID e validar todos os atributos")
    void testBuscarProdutoPorIdComAtributosCompletos() {
        // Arrange
        when(produtoRepository.findById(produtoId)).thenReturn(Optional.of(produtoEntity));

        // Act
        ProdutoResponse response = produtoService.buscarPorId(produtoId);

        // Assert
        assertNotNull(response, "A resposta do produto não deve ser nula");
        assertEquals(produtoId, response.id(), "ID do produto incorreto");
        assertEquals("Rosa Vermelha Premium", response.nome(), "Nome do produto incorreto");
        assertEquals(new BigDecimal("45.90"), response.preco(), "Preço do produto incorreto");
        assertEquals(100, response.quantidadeEstoque(), "Quantidade em estoque incorreta");
        assertEquals("Flores Cortadas", response.categoria(), "Categoria do produto incorreta");
        assertTrue(response.ativo(), "Produto deve estar ativo");

        // Verify
        verify(produtoRepository, times(1)).findById(produtoId);
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não encontrado")
    void testBuscarProdutoInexistenteDeveLancarExcecao() {
        // Arrange
        UUID idInexistente = UUID.randomUUID();
        when(produtoRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> produtoService.buscarPorId(idInexistente),
                "Deve lançar IllegalArgumentException quando produto não existe"
        );

        assertEquals("Produto não encontrado.", exception.getMessage());
        verify(produtoRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Deve atualizar produto mantendo integridade de dados")
    void testAtualizarProdutoComNovosDados() {
        // Arrange
        UUID id = produtoId;
        ProdutoRequest novaRequisicao = new ProdutoRequest(
                "Rosa Vermelha Premium - Atualizada",
                "Rosa vermelha PREMIUM com desconto especial",
                new BigDecimal("39.90"),
                150,
                "Flores Cortadas Premium"
        );

        Produto produtoAtualizado = new Produto();
        produtoAtualizado.setId(id);
        produtoAtualizado.setNome("Rosa Vermelha Premium - Atualizada");
        produtoAtualizado.setDescricao("Rosa vermelha PREMIUM com desconto especial");
        produtoAtualizado.setPreco(new BigDecimal("39.90"));
        produtoAtualizado.setQuantidadeEstoque(150);
        produtoAtualizado.setCategoria("Flores Cortadas Premium");
        produtoAtualizado.setAtivo(true);
        produtoAtualizado.setCriadoEm(LocalDateTime.now());

        when(produtoRepository.findById(id)).thenReturn(Optional.of(produtoEntity));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoAtualizado);

        // Act
        ProdutoResponse response = produtoService.atualizar(id, novaRequisicao);

        // Assert
        assertEquals("Rosa Vermelha Premium - Atualizada", response.nome());
        assertEquals(new BigDecimal("39.90"), response.preco());
        assertEquals(150, response.quantidadeEstoque());
        assertEquals("Flores Cortadas Premium", response.categoria());

        verify(produtoRepository, times(1)).findById(id);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve desativar produto corretamente")
    void testDesativarProdutoComSucesso() {
        // Arrange
        UUID id = produtoId;
        Produto produtoDesativado = new Produto();
        produtoDesativado.setId(id);
        produtoDesativado.setNome("Rosa Vermelha Premium");
        produtoDesativado.setAtivo(false);

        when(produtoRepository.findById(id)).thenReturn(Optional.of(produtoEntity));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoDesativado);

        // Act
        produtoService.desativar(id);

        // Assert & Verify
        verify(produtoRepository, times(1)).findById(id);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }
}
