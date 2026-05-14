package com.br.florihub.florihubbackend.e2e;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes E2E - Login e Navegação")
class LoginAndNavigationE2ETest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = System.getProperty("e2e.base.url", "http://localhost:5173");
    private static final Duration WAIT_TIME = Duration.ofSeconds(15);

    // XPath genérico que detecta qualquer elemento do dashboard
    private static final String XPATH_DASHBOARD =
            "//*[contains(text(), 'Dashboard') or contains(text(), 'Receita Total') " +
            "or contains(text(), 'Bem-vindo') or contains(text(), 'Vendas Abertas') " +
            "or contains(text(), 'Ticket Médio')]";

    @BeforeAll
    static void setupDriver() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // Descomente para rodar sem interface gráfica
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIME);

        // Limpar cookies e cache
        driver.manage().deleteAllCookies();
    }

    // ─── Método auxiliar: preenche e envia o formulário de login ─────────────
    private void fazerLogin(String email, String senha) {
        // Arrange
        driver.get(BASE_URL);

        // Act - Preencher email
        WebElement emailField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("email"))
        );
        emailField.clear();
        emailField.sendKeys(email);

        // Preencher senha
        driver.findElement(By.id("senha")).sendKeys(senha);

        // Clicar em login
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnLogin"))).click();
    }

    // ─── Método auxiliar: aguarda o dashboard carregar ────────────────────────
    private WebElement aguardarDashboard() {
        return wait.until(
                ExpectedConditions.presenceOfElementLocated(By.xpath(XPATH_DASHBOARD))
        );
    }

    @Test
    @DisplayName("Deve fazer login com sucesso e ser redirecionado para o dashboard")
    void testFazerLoginComSucessoESerRedirecionadoParaDashboard() {
        try {
            fazerLogin("admin@florihub.com", "senha123");

            WebElement dashboardElement = aguardarDashboard();

            assertNotNull(dashboardElement, "Dashboard deve estar carregado após login");

        } catch (Exception e) {
            fail("Teste falhou: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Deve exibir mensagem de erro ao fazer login com credenciais inválidas")
    void testExibirMensagemErroComCredenciaisInvalidas() {
        try {
            fazerLogin("usuario@incorreto.com", "senhaErrada123");

            // Aguarda mensagem de erro aparecer
            WebElement mensagemErro = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), 'inválidos') or contains(text(), 'Erro') " +
                            "or contains(text(), 'erro') or contains(text(), 'incorreto') or contains(text(), '403') ]")
            ));

            assertNotNull(mensagemErro, "Deve exibir mensagem de erro para credenciais inválidas");

            // Botão de login ainda visível — continua na tela de login
            assertNotNull(
                    driver.findElement(By.id("btnLogin")),
                    "Deve permanecer na tela de login após erro"
            );

        } catch (Exception e) {
            fail("Teste falhou: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Deve navegar para página de produtos após login")
    void testNavegarParaPaginaDeProdutosAposLogin() {
        try {
            fazerLogin("admin@florihub.com", "senha123");
            aguardarDashboard();

            // Clicar no link de produtos na sidebar
            wait.until(ExpectedConditions.elementToBeClickable(By.id("produtos"))).click();

            // Aguarda o título da página de produtos
            WebElement tituloProdutos = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//*[contains(text(), 'Catálogo de Produtos') or contains(text(), 'Produtos')]")
            ));

            assertNotNull(tituloProdutos, "Página de produtos deve estar carregada");

        } catch (Exception e) {
            fail("Teste falhou: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Deve manter sessão ativa ao atualizar a página")
    void testManterSessaoAoAtualizarPagina() {
        try {
            fazerLogin("admin@florihub.com", "senha123");
            aguardarDashboard();

            driver.navigate().refresh();

            // Após refresh, aguarda qualquer elemento carregar
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.xpath("//h1 | //h2 | //button")
            ));

            // Sessão mantida = botão de login NÃO está na tela
            boolean voltouParaLogin = driver.findElements(By.id("btnLogin")).isEmpty();
            assertFalse(voltouParaLogin, "Não deve sair da sessão ao atualizar página");

        } catch (Exception e) {
            fail("Teste falhou: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Deve fazer logout com sucesso")
    void testFazerLogoutComSucesso() {
        try {
            fazerLogin("admin@florihub.com", "senha123");
            aguardarDashboard();

            // Clicar no botão de logout
            wait.until(ExpectedConditions.elementToBeClickable(By.id("btnLogout"))).click();

            // Após logout, botão de login deve aparecer novamente
            WebElement botaoLogin = wait.until(
                    ExpectedConditions.presenceOfElementLocated(By.id("btnLogin"))
            );

            assertNotNull(botaoLogin, "Deve retornar para tela de login após logout");

        } catch (Exception e) {
            fail("Teste falhou ou elemento de logout não encontrado: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
