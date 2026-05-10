# FloriHub - Backend API REST

> **Projeto acadêmico** - SENAI Fatesg · Curso de Engenharia de Software 6° Periodo · 2026

## Equipe

| Nome | 
|---|
| Gabriel Fernandes Gomes Castanheira de Matos |
| João Vítor |
| Ozeias |
| Willian Junior |
| Henrique |

---

PDV (Ponto de Venda) para floricultura. API REST construída com Spring Boot 4, PostgreSQL e autenticação JWT.

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Framework | Spring Boot 4.0.6 |
| Linguagem | Java 21 |
| Segurança | Spring Security + JWT (JJWT 0.12.6) |
| Persistência | Spring Data JPA + Hibernate 7 |
| Banco de dados | PostgreSQL 16 |
| Validação | Bean Validation (Jakarta) |
| Build | Maven |

---

## Pré-requisitos

Antes de rodar o projeto, certifique-se de ter instalado:

- [JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven 3.9+](https://maven.apache.org/download.cgi) ou use o wrapper `./mvnw` incluído no projeto
- [PostgreSQL 16](https://www.postgresql.org/download/)
- Uma IDE como [IntelliJ IDEA](https://www.jetbrains.com/idea/) ou [VS Code](https://code.visualstudio.com/)

---

## Configuração do banco de dados

1. Abra o PostgreSQL (via psql, DBeaver ou pgAdmin)
2. Crie o banco de dados:

```sql
CREATE DATABASE "floriHub";
```

> O nome do banco é case-sensitive — use exatamente `floriHub`.

---

## Configuração da aplicação

Abra o arquivo `src/main/resources/application.properties` e ajuste as credenciais do banco:

```properties
# Servidor
server.port=8080

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/floriHub
spring.datasource.username=postgres
spring.datasource.password=SUA_SENHA_AQUI
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# JWT
jwt.secret=dGVzdGVmbG9yaWh1YnNlY3JldGtleXBhcmFqd3QyMDI2Zmxvcmlo
jwt.expiration=86400000

# Nome da aplicação
spring.application.name=florihub-backend
```

> Para produção, gere um secret seguro com: `openssl rand -base64 32`

---

## Como rodar localmente

### Opção 1 — Pela IDE (IntelliJ IDEA)

1. Clone ou abra o projeto no IntelliJ
2. Aguarde o Maven baixar as dependências
3. Localize a classe `FloriHubBackendApplication.java`
4. Clique com o botão direito → **Run**

### Opção 2 — Via terminal

```bash
# Na raiz do projeto
./mvnw spring-boot:run
```

Ou, no Windows:

```bash
mvnw.cmd spring-boot:run
```

### Opção 3 — Build + execução do JAR

```bash
./mvnw clean package -DskipTests
java -jar target/FloriHub-backend-0.0.1-SNAPSHOT.jar
```

---

## Primeiro acesso

As tabelas são criadas automaticamente pelo Hibernate ao subir a aplicação (`ddl-auto=update`).

Como não há nenhum usuário no banco inicialmente, o endpoint de registro está **aberto publicamente apenas no primeiro cadastro**. A partir do segundo usuário, é necessário um token de ADMIN.

**Crie o primeiro usuário administrador:**

```bash
POST http://localhost:8080/auth/registrar
Content-Type: application/json

{
  "nome": "Administrador",
  "email": "admin@florihub.com",
  "senha": "senha123",
  "role": "ADMIN"
}
```

**Faça login para obter o token JWT:**

```bash
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "admin@florihub.com",
  "senha": "senha123"
}
```

Use o token retornado no header `Authorization: Bearer {token}` nas demais requisições.

---

## Endpoints disponíveis

### Autenticação (público)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/login` | Login — retorna token JWT |
| `POST` | `/auth/registrar` | Registro (livre se banco vazio, senão exige ADMIN) |

### Usuários (requer ADMIN)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/usuarios` | Criar usuário |
| `GET` | `/usuarios` | Listar usuários |
| `GET` | `/usuarios/{id}` | Buscar usuário por ID |
| `PUT` | `/usuarios/{id}` | Atualizar usuário |
| `DELETE` | `/usuarios/{id}` | Desativar usuário (soft delete) |

### Produtos (requer autenticação)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/produtos` | Criar produto |
| `GET` | `/produtos` | Listar produtos ativos |
| `GET` | `/produtos/{id}` | Buscar produto por ID |
| `PUT` | `/produtos/{id}` | Atualizar produto |
| `DELETE` | `/produtos/{id}` | Desativar produto (soft delete) |

### Vendas (requer autenticação)

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/vendas` | Registrar nova venda |
| `GET` | `/vendas` | Listar todas as vendas |
| `GET` | `/vendas/{id}` | Buscar venda por ID |
| `PATCH` | `/vendas/{id}/status?status=FINALIZADA` | Atualizar status da venda |

**Status de venda disponíveis:** `ABERTA`, `FINALIZADA`, `CANCELADA`

---

## Estrutura do projeto

```
src/
└── main/
    └── java/com/br/florihub/florihubbackend/
        ├── controller/       # Endpoints REST
        ├── dto/
        │   ├── request/      # Objetos de entrada
        │   └── response/     # Objetos de saída
        ├── model/            # Entidades JPA
        ├── repository/       # Interfaces Spring Data
        ├── security/         # JWT, filtros, configuração Spring Security
        └── service/          # Lógica de negócio
```

---

## Modelo de dados

| Tabela | Descrição |
|---|---|
| `usuario` | Usuários do sistema (ADMIN / VENDEDOR) |
| `produto` | Catálogo de produtos da floricultura |
| `venda` | Cabeçalho das vendas realizadas |
| `venda_item` | Itens de cada venda (produto + quantidade + preço snapshot) |

---

## Observações importantes

- **Soft delete:** produtos e usuários nunca são removidos fisicamente. O campo `ativo = false` os oculta das listagens.
- **Snapshot de preço:** o `preco_unitario` em `venda_item` registra o preço no momento da venda. Alterações futuras no produto não afetam o histórico.
- **Controle de estoque:** a quantidade em estoque é decrementada automaticamente ao registrar uma venda. Tentativas de vender além do estoque disponível retornam erro.
- **JWT stateless:** nenhuma sessão é mantida no servidor. Cada requisição precisa enviar o token no header `Authorization`.