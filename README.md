# SnackSys 🍔🍟

A comprehensive diner management system developed as a university project to streamline ordering, table management, and billing.

## Configuração do ambiente de desenvolvimento

### Pré-requisitos

- Java 21
- Git
- Docker Desktop
- DBeaver (opcional)
- Maven Wrapper (já disponível no projeto)

### Configuração do banco de dados

O SnackSys utiliza PostgreSQL 17 com Docker Compose e Flyway para o versionamento do banco de dados.

#### 1. Clonar o repositório

```powershell
git clone https://github.com/A3-qualidade/snacksys.git
cd snacksys
```

Caso já tenha o repositório clonado, atualize sua branch com as alterações da main.

#### 2. Configurar as variáveis de ambiente

Caso ainda não possua o arquivo .env, execute:

```powershell
Copy-Item .env.example .env
notepad .env
```

Defina uma senha para o PostgreSQL no arquivo .env, sem aspas ou espaços. Esse arquivo não deve ser enviado ao GitHub.

#### 3. Inicializar o banco de dados

Com o Docker Desktop aberto, execute:

```powershell
docker compose up -d
docker compose ps
```

Aguarde o container ficar saudável (healthy).

#### 4. Configurar a senha no PowerShell

```powershell
$env:POSTGRES_PASSWORD = (Get-Content .env | Where-Object { $_ -match '^POSTGRES_PASSWORD=' } | Select-Object -First 1) -replace '^POSTGRES_PASSWORD=', ''
```

#### 5. Executar a aplicação

No mesmo PowerShell, execute:

```powershell
.\mvnw.cmd spring-boot:run
```

O Flyway realizará automaticamente as migrações pendentes do banco de dados.

A aplicação estará disponível em http://localhost:8080.

### Pagamento simulado

Após criar um pedido, registre o pagamento em `POST /pedidos/{id}/pagamentos` com uma das formas `PIX`, `CARTAO_CREDITO` ou `CARTAO_DEBITO`. O campo `resultado` simula a resposta do pagamento (`APROVADO` ou `RECUSADO`); quando aprovado, o pedido avança automaticamente para `RECEBIDO`. Exemplo:

```json
{
  "forma": "PIX",
  "resultado": "APROVADO"
}
```

Use `GET /pedidos/{id}/pagamentos` para consultar as tentativas registradas.

### Conexão pelo DBeaver

| Configuração | Valor |
|---|---|
| Host | localhost |
| Porta | 5433 |
| Banco | snacksys |
| Usuário | snacksys |
| Senha | Definida no arquivo .env |

### Versionamento do banco de dados

As migrações estão localizadas em:

`src/main/resources/db/migration/`

As alterações na estrutura do banco devem ser realizadas por meio de novas migrações, seguindo o padrão:

- V1.0__criacao_tabelas.sql
- V1.1__descricao_da_alteracao.sql
- V1.2__descricao_da_alteracao.sql
- V2.0__descricao_da_alteracao.sql

Não altere migrações que já foram executadas.

Cada integrante possui seu próprio banco local. A estrutura é compartilhada pelo GitHub, mas os dados são independentes.

## Dados iniciais de desenvolvimento (opcional)

Após iniciar a aplicação pela primeira vez e executar as migrações do Flyway, é possível popular o banco de dados com produtos fictícios para desenvolvimento e testes.

Execute os seguintes comandos:

```powershell
docker compose cp scripts/dev/dados_iniciais.sql postgres:/tmp/dados_iniciais.sql
docker compose exec -T postgres psql -v ON_ERROR_STOP=1 -U snacksys -d snacksys -f /tmp/dados_iniciais.sql
```

O script disponibiliza:

- 4 categorias;
- 16 produtos;
- 12 produtos com controle de estoque;
- 4 produtos sem controle de estoque.

A execução é opcional e não faz parte das migrações do Flyway.

Os produtos, preços e quantidades são provisórios. A reexecução do script não deve duplicar os produtos iniciais nem sobrescrever os estoques existentes.

### Testes automatizados

Com o banco em funcionamento e a variável de ambiente da senha configurada, execute:

```powershell
.\mvnw.cmd clean verify
```

O relatório de cobertura JaCoCo estará disponível em:

`target/site/jacoco/index.html`

### Tecnologias utilizadas

- Java 21
- Spring Boot
- Maven
- PostgreSQL
- Docker
- Flyway
- Thymeleaf
- JUnit
- JaCoCo
- GitHub Actions
