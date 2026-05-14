package com.br.florihub.florihubbackend.service;

import com.br.florihub.florihubbackend.dto.request.VendaItemRequest;
import com.br.florihub.florihubbackend.dto.request.VendaRequest;
import com.br.florihub.florihubbackend.dto.response.VendaResponse;
import com.br.florihub.florihubbackend.model.Produto;
import com.br.florihub.florihubbackend.model.Usuario;
import com.br.florihub.florihubbackend.model.Venda;
import com.br.florihub.florihubbackend.model.VendaItem;
import com.br.florihub.florihubbackend.repository.ProdutoRepository;
import com.br.florihub.florihubbackend.repository.UsuarioRepository;
import com.br.florihub.florihubbackend.repository.VendaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - VendaService")
class VendaServiceTest {

    @Mock
    private VendaRepository vendaRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VendaService vendaService;

    private Usuario usuario;
    private Produto produtoRosa;
    private Venda vendaEntity;
    private VendaRequest vendaRequest;
    private List<VendaItem> itensVenda;

    @BeforeEach
    void setUp() {
        // Setup do usuário
        usuario = new Usuario();
        usuario.setId(UUID.randomUUID());
        usuario.setNome("João Silva");
        usuario.setEmail("joao@florihub.com");
        usuario.setSenhaHash("senhaHash123");
        usuario.setRole("VENDEDOR");
        usuario.setAtivo(true);
        usuario.setCriadoEm(LocalDateTime.now());

        // Setup do produto Rosa
        produtoRosa = new Produto();
        produtoRosa.setId(UUID.randomUUID());
        produtoRosa.setNome("Rosa Vermelha");
        produtoRosa.setDescricao("Rosa vermelha premium");
        produtoRosa.setPreco(new BigDecimal("40.00"));
        produtoRosa.setQuantidadeEstoque(100);
        produtoRosa.setCategoria("Flores Cortadas");
        produtoRosa.setAtivo(true);
        produtoRosa.setCriadoEm(LocalDateTime.now());

        // Setup da requisição de venda com 2 itens
        vendaRequest = new VendaRequest(
                "Venda para evento especial",
                List.of(
                        new VendaItemRequest(produtoRosa.getId(), 5),
                        new VendaItemRequest(produtoRosa.getId(), 3)
                )
        );

        // Setup dos itens da venda
        itensVenda = new ArrayList<>();

        // Item 1: 5 unidades de Rosa Vermelha
        VendaItem item1 = new VendaItem();
        item1.setId(UUID.randomUUID());
        item1.setProduto(produtoRosa);
        item1.setQuantidade(5);
        item1.setPrecoUnitario(new BigDecimal("40.00"));
        item1.setSubTotal(new BigDecimal("200.00")); // 40 × 5
        itensVenda.add(item1);

        // Item 2: 3 unidades de Rosa Vermelha
        VendaItem item2 = new VendaItem();
        item2.setId(UUID.randomUUID());
        item2.setProduto(produtoRosa);
        item2.setQuantidade(3);
        item2.setPrecoUnitario(new BigDecimal("40.00"));
        item2.setSubTotal(new BigDecimal("120.00")); // 40 × 3
        itensVenda.add(item2);

        // Setup da entidade Venda COM ITENS POPULADOS
        vendaEntity = new Venda();
        vendaEntity.setId(UUID.randomUUID());
        vendaEntity.setUsuario(usuario);
        vendaEntity.setItens(itensVenda);
        vendaEntity.setValorTotal(new BigDecimal("320.00")); // (40 × 5) + (40 × 3) = 320
        vendaEntity.setStatus("ABERTA");
        vendaEntity.setObservacao("Venda para evento especial");
        vendaEntity.setDataVenda(LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve criar venda com cálculo correto do valor total e validar itens")
    void testCriarVendaComCalculoCorretoDoValorTotal() {
        // Arrange
        when(usuarioRepository.findByEmail("joao@florihub.com")).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(produtoRosa.getId())).thenReturn(Optional.of(produtoRosa));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoRosa);
        when(vendaRepository.save(any(Venda.class))).thenReturn(vendaEntity);

        // Act
        VendaResponse response = vendaService.criar(vendaRequest, "joao@florihub.com");

        // Assert
        assertNotNull(response, "A resposta da venda não deve ser nula");
        assertEquals("João Silva", response.nomeVendedor(), "Nome do vendedor incorreto");
        assertEquals(new BigDecimal("320.00"), response.valorTotal(), 
                "Valor total deve ser 320.00 (40×5 + 40×3)");
        assertEquals("ABERTA", response.status(), "Status inicial deve ser ABERTA");
        assertEquals("Venda para evento especial", response.observacao(), "Observação incorreta");
        assertEquals(2, response.itens().size(), "Deve ter 2 itens na venda");

        // Verify
        verify(usuarioRepository, times(1)).findByEmail("joao@florihub.com");
        verify(produtoRepository, times(2)).findById(produtoRosa.getId());
        verify(produtoRepository, times(2)).save(any(Produto.class));
        verify(vendaRepository, times(1)).save(any(Venda.class));
    }

    @Test
    @DisplayName("Deve reduzir estoque do produto quando venda é criada com sucesso")
    void testReduzirEstoqueDoProduoAoVender() {
        // Arrange
        int estoqueInicial = 100;
        int quantidadeVendida1 = 5;
        int quantidadeVendida2 = 3;
        int totalVendido = quantidadeVendida1 + quantidadeVendida2;
        int estoqueEsperado = estoqueInicial - totalVendido; // 100 - 8 = 92

        produtoRosa.setQuantidadeEstoque(estoqueInicial);

        when(usuarioRepository.findByEmail("joao@florihub.com")).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(produtoRosa.getId())).thenReturn(Optional.of(produtoRosa));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto p = invocation.getArgument(0);
            return p;
        });
        when(vendaRepository.save(any(Venda.class))).thenReturn(vendaEntity);

        // Act
        VendaResponse response = vendaService.criar(vendaRequest, "joao@florihub.com");

        // Assert
        assertNotNull(response);
        assertEquals(new BigDecimal("320.00"), response.valorTotal());

        // Verify que o estoque foi reduzido corretamente
        verify(produtoRepository, atLeast(2)).save(any(Produto.class));
    }

    @Test
    @DisplayName("Deve lançar exceção quando estoque é insuficiente")
    void testLancarExcecaoQuandoEstoqueInsuficiente() {
        // Arrange
        produtoRosa.setQuantidadeEstoque(3); // Menos do que será vendido (5 + 3)
        
        VendaRequest vendaComEstoqueInsuficiente = new VendaRequest(
                "Venda com problema",
                List.of(
                        new VendaItemRequest(produtoRosa.getId(), 5)
                )
        );

        when(usuarioRepository.findByEmail("joao@florihub.com")).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(produtoRosa.getId())).thenReturn(Optional.of(produtoRosa));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vendaService.criar(vendaComEstoqueInsuficiente, "joao@florihub.com"),
                "Deve lançar exceção quando estoque é insuficiente"
        );

        assertTrue(exception.getMessage().contains("Estoque insuficiente"),
                "Mensagem de erro deve mencionar estoque insuficiente");

        verify(usuarioRepository, times(1)).findByEmail("joao@florihub.com");
        verify(produtoRepository, times(1)).findById(produtoRosa.getId());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não é encontrado")
    void testLancarExcecaoQuandoUsuarioNaoEncontrado() {
        // Arrange
        when(usuarioRepository.findByEmail("usuario@inexistente.com")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vendaService.criar(vendaRequest, "usuario@inexistente.com"),
                "Deve lançar exceção quando usuário não existe"
        );

        assertEquals("Usuário não encontrado.", exception.getMessage());
        verify(usuarioRepository, times(1)).findByEmail("usuario@inexistente.com");
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto não é encontrado")
    void testLancarExcecaoQuandoProdutoNaoEncontrado() {
        // Arrange
        UUID produtoInexistente = UUID.randomUUID();
        VendaRequest vendaComProdutoInexistente = new VendaRequest(
                "Venda com produto inexistente",
                List.of(
                        new VendaItemRequest(produtoInexistente, 5)
                )
        );

        when(usuarioRepository.findByEmail("joao@florihub.com")).thenReturn(Optional.of(usuario));
        when(produtoRepository.findById(produtoInexistente)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> vendaService.criar(vendaComProdutoInexistente, "joao@florihub.com"),
                "Deve lançar exceção quando produto não existe"
        );

        assertTrue(exception.getMessage().contains("Produto não encontrado"),
                "Mensagem de erro deve mencionar produto não encontrado");

        verify(usuarioRepository, times(1)).findByEmail("joao@florihub.com");
        verify(produtoRepository, times(1)).findById(produtoInexistente);
    }

    @Test
    @DisplayName("Deve atualizar status da venda para FINALIZADA")
    void testAtualizarStatusVendaParaFinalizada() {
        // Arrange
        UUID vendaId = vendaEntity.getId();
        when(vendaRepository.findById(vendaId)).thenReturn(Optional.of(vendaEntity));
        
        Venda vendaFinalizada = new Venda();
        vendaFinalizada.setId(vendaId);
        vendaFinalizada.setUsuario(usuario);
        vendaFinalizada.setItens(itensVenda);
        vendaFinalizada.setValorTotal(new BigDecimal("320.00"));
        vendaFinalizada.setStatus("FINALIZADA");
        vendaFinalizada.setObservacao("Venda para evento especial");
        vendaFinalizada.setDataVenda(LocalDateTime.now());

        when(vendaRepository.save(any(Venda.class))).thenReturn(vendaFinalizada);

        // Act
        VendaResponse response = vendaService.atualizarStatus(vendaId, "FINALIZADA");

        // Assert
        assertEquals("FINALIZADA", response.status(), "Status deve ser FINALIZADA");
        verify(vendaRepository, times(1)).findById(vendaId);
        verify(vendaRepository, times(1)).save(any(Venda.class));
    }
}
