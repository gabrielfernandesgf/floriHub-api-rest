package com.br.florihub.florihubbackend.api;

import com.br.florihub.florihubbackend.dto.request.LoginRequest;
import com.br.florihub.florihubbackend.dto.request.VendaItemRequest;
import com.br.florihub.florihubbackend.dto.request.VendaRequest;
import com.br.florihub.florihubbackend.dto.response.AuthResponse;
import com.br.florihub.florihubbackend.dto.response.ProdutoResponse;
import com.br.florihub.florihubbackend.dto.response.VendaResponse;
import com.br.florihub.florihubbackend.model.Usuario;
import com.br.florihub.florihubbackend.repository.UsuarioRepository;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Testes de Integração (API REST) - Vendas
 * 
 * Testa endpoints relacionados a vendas e autenticação.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("Testes de API - Vendas")
public class VendaApiTest {

    @LocalServerPort
    private int port;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String token;
    private UUID produtoId;

    @BeforeAll
    static void setup() {
        RestAssured.basePath = "";
    }

    /**
     * Setup inicial: Cria usuário de teste, faz login e cria produto para uso nos testes.
     * Executado antes de cada teste.
     */
    @BeforeEach
    public void setUp() {
       
        RestAssured.port = port;
        
        usuarioRepository.deleteAll();

        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setNome("Admin Teste");
        usuarioAdmin.setEmail("admin@florihub.com");
        usuarioAdmin.setSenhaHash(passwordEncoder.encode("senha123"));
        usuarioAdmin.setRole("ADMIN");
        usuarioAdmin.setAtivo(true);
        usuarioAdmin.setCriadoEm(LocalDateTime.now());

        usuarioRepository.save(usuarioAdmin);

        AuthResponse authResponse = given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest("admin@florihub.com", "senha123"))
            .when()
            .post("/auth/login")
            .then()
            .statusCode(200)
            .extract()
            .as(AuthResponse.class);

        token = authResponse.token();

        ProdutoResponse produtoResponse = given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)
            .body("""
                {
                    "nome": "Rosa Vermelha",
                    "descricao": "Rosa vermelha premium",
                    "preco": 40.00,
                    "quantidadeEstoque": 100,
                    "categoria": "Flores"
                }
                """)
            .when()
            .post("/produtos")
            .then()
            .statusCode(201)
            .extract()
            .as(ProdutoResponse.class);

        produtoId = produtoResponse.id();
    }

    /**
     * Teste 1: Validar autenticação JWT
     * 
     * Cenário: Fazer login com credenciais válidas
     * Esperado: Token JWT válido gerado com dados corretos
     * 
     * Regra de Negócio: Token deve conter tipo "Bearer" e dados do usuário
     */
    @Test
    @DisplayName("Teste 1: Deve fazer login com sucesso e gerar token JWT válido")
    public void testLoginComSucessoGeraTokenJWT() {
        given()
            .contentType(ContentType.JSON)
            .body(new LoginRequest("admin@florihub.com", "senha123"))
            .when()
            .post("/auth/login")
            .then()
            .statusCode(200) 
            .body("token", notNullValue()) 
            .body("tipo", equalTo("Bearer"))
            .body("nome", notNullValue()) 
            .body("email", equalTo("admin@florihub.com")); 
    }

    /**
     * Teste 2: Validar criação de venda e cálculo de valor total
     * 
     * Cenário: Criar venda com 1 item (5 unidades de Rosa Vermelha a R$ 40,00)
     * Esperado: Valor total R$ 200,00 (5 × 40), status "ABERTA", 1 item
     * 
     * Regra de Negócio: 
     * - Venda inicia com status "ABERTA"
     * - Valor total = somatório de (preço × quantidade) de cada item
     * - Requer autenticação JWT válida
     */
    @Test
    @DisplayName("Teste 2: Deve criar venda com sucesso e calcular valor total")
    public void testCriarVendaComSucessoCalculaValorTotal() {
        
        VendaItemRequest item = new VendaItemRequest(produtoId, 5);

        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + token)  // Autenticação JWT
            .body(new VendaRequest("Venda teste", List.of(item)))  // Dados da venda
            .when()
            .post("/vendas")  // Endpoint de criação
            .then()
            .statusCode(201)  // Retorno: 201 Created
            .body("valorTotal", equalTo(200.0f))  // Valor total correto: 5 × 40 = 200
            .body("status", equalTo("ABERTA"))  // Status inicial correto
            .body("itens", hasSize(1));  // Quantidade de itens correta
    }
}

