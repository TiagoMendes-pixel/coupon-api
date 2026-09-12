# Coupon API

API REST desenvolvida como parte de um desafio técnico para gerenciamento de cupons.

A aplicação permite criar, consultar, publicar, resgatar e realizar a exclusão lógica de cupons, mantendo as principais regras de negócio encapsuladas na camada de domínio.

## Tecnologias

* Java 17
* Spring Boot 4.1.1
* Spring Web MVC
* Spring Data JPA
* H2 Database
* JUnit 5
* Mockito
* Swagger / OpenAPI
* Maven
* Docker
* Docker Compose

## Arquitetura

O projeto utiliza uma arquitetura em camadas, mantendo as regras de negócio encapsuladas no domínio da aplicação.

```text
Controller
    |
    v
Service
    |
    v
Domain
    |
    v
Repository
    |
    v
Database
```

### Controller

Responsável por disponibilizar os endpoints REST e tratar as requisições e respostas HTTP.

### Service

Responsável por coordenar os casos de uso da aplicação, como criação, consulta, publicação, resgate e exclusão dos cupons.

### Domain

Contém a entidade `Coupon` e suas principais regras de negócio.

Os comportamentos relacionados ao cupom são encapsulados dentro do próprio objeto de domínio, evitando que regras de negócio fiquem espalhadas pelos Controllers ou Services.

### Repository

Responsável pela persistência e consulta dos dados utilizando Spring Data JPA.

---

## Regras de Negócio

A entidade `Coupon` é responsável por garantir as principais regras de negócio da aplicação.

### Código do Cupom

* O código deve possuir exatamente 6 caracteres alfanuméricos.
* Caracteres especiais são removidos antes da validação.
* Por exemplo, `ABC-123` é armazenado como `ABC123`.

### Valor do Desconto

* O valor mínimo permitido para o desconto é `0.5`.
* Valores inferiores a `0.5` não são aceitos.

### Data de Expiração

* A data de expiração é obrigatória.
* A data de expiração deve estar no futuro.

### Descrição

* A descrição é obrigatória.
* Descrições vazias ou contendo apenas espaços não são aceitas.

### Publicação

* Um cupom pode ser publicado através de seu comportamento de domínio.
* Um cupom excluído não pode ser publicado.

### Resgate

Um cupom somente pode ser resgatado quando:

* Está publicado.
* Ainda não foi resgatado.
* Não está expirado.
* Não foi excluído.

### Exclusão

A aplicação utiliza **soft delete**.

Isso significa que o registro não é fisicamente removido do banco de dados. Em vez disso, o cupom é marcado como excluído através do atributo `deleted`.

As seguintes regras são aplicadas:

* Um cupom pode ser excluído mesmo após o resgate.
* Um cupom já excluído não pode ser excluído novamente.
* Cupons excluídos não são retornados nas consultas da API.
* Cupons excluídos não podem ser publicados.
* Cupons excluídos não podem ser resgatados.

---

## Endpoints da API

Os IDs são UUIDs. As respostas de criação e consulta contêm `status: "ACTIVE"`; o campo interno `deleted` não é exposto.

O contrato do desafio define POST, GET por ID e DELETE em `/coupon`. A listagem e as operações de publicar/resgatar já existentes foram mantidas como extensões.

A aplicação disponibiliza os seguintes endpoints:

| Método | Endpoint                | Descrição                             |
| ------ | ----------------------- | ------------------------------------- |
| POST   | `/coupon`              | Cria um novo cupom                    |
| GET    | `/coupon`              | Retorna todos os cupons ativos        |
| GET    | `/coupon/{id}`         | Retorna um cupom ativo pelo ID        |
| PATCH  | `/coupon/{id}/publish` | Publica um cupom                      |
| PATCH  | `/coupon/{id}/redeem`  | Resgata um cupom                      |
| DELETE | `/coupon/{id}`         | Realiza a exclusão lógica de um cupom |

---

## Criar um Cupom

### Requisição

```http
POST /coupon
Content-Type: application/json
```

Exemplo:

```json
{
  "code": "ABC-123",
  "description": "Desconto de teste",
  "discountValue": 10.00,
  "expirationDate": "2026-12-31T23:59:59",
  "published": false
}
```

### Resposta

```json
{
  "id": "00000000-0000-0000-0000-000000000001",
  "code": "ABC123",
  "description": "Desconto de teste",
  "discountValue": 10.00,
  "expirationDate": "2026-12-31T23:59:59",
  "published": false,
  "redeemed": false,
  "status": "ACTIVE"
}
```

Quando o cupom é criado com sucesso, a API retorna:

```text
201 Created
```

Observe que o código enviado como `ABC-123` é armazenado como `ABC123`, pois os caracteres especiais são removidos pelo domínio.

---

## Consultar Cupons

### Consultar todos

```http
GET /coupon
```

Retorna todos os cupons ativos.

Cupons que passaram por soft delete não são retornados.

### Consultar por ID

```http
GET /coupon/{id}
```

Retorna um cupom ativo pelo seu identificador.

Caso o cupom não exista ou tenha sido excluído, a API retorna:

```text
404 Not Found
```

---

## Publicar um Cupom

```http
PATCH /coupon/{id}/publish
```

Publica um cupom existente e ativo.

Um cupom que já foi excluído não pode ser publicado.

---

## Resgatar um Cupom

```http
PATCH /coupon/{id}/redeem
```

Para realizar o resgate, o cupom precisa estar publicado.

A operação não é permitida quando o cupom:

* Não está publicado.
* Já foi resgatado.
* Está expirado.
* Foi excluído.

Quando existe um conflito com o estado atual do cupom, a API retorna:

```text
409 Conflict
```

---

## Excluir um Cupom

```http
DELETE /coupon/{id}
```

A exclusão é realizada através de **soft delete**.

O registro permanece armazenado no banco de dados, porém seu atributo `deleted` passa a possuir o valor `true`.

A exclusão retorna `204 No Content`, sem corpo. Cupons resgatados também podem ser excluídos. Uma nova tentativa pela API retorna `404 Not Found`, pois as consultas filtram cupons excluídos; o domínio também impede a exclusão repetida diretamente.

---

## Status HTTP

A API utiliza principalmente os seguintes códigos HTTP:

| Status            | Descrição                                         |
| ----------------- | ------------------------------------------------- |
| `200 OK`          | Operação realizada com sucesso                    |
| `204 No Content` | Cupom excluído, sem corpo de resposta |
| `201 Created`     | Cupom criado com sucesso                          |
| `400 Bad Request` | Dados inválidos ou violação de regra de validação |
| `404 Not Found`   | Cupom inexistente ou não mais ativo               |
| `409 Conflict`    | Operação incompatível com o estado atual do cupom |

---

## Tratamento de Exceções

A aplicação utiliza tratamento centralizado de exceções através de `@RestControllerAdvice`.

As principais exceções tratadas são:

* `CouponNotFoundException` → `404 Not Found`
* `IllegalArgumentException` → `400 Bad Request`
* `IllegalStateException` → `409 Conflict`

Dessa forma, as regras de negócio permanecem separadas da responsabilidade de conversão dos erros para respostas HTTP.

---

## Banco de Dados

A aplicação utiliza o banco de dados H2 em memória.

Configuração:

```properties
spring.datasource.url=jdbc:h2:mem:coupondb
spring.datasource.username=sa
spring.datasource.password=
```

### Console H2

Com a aplicação em execução, o console pode ser acessado em:

```text
http://localhost:8080/h2-console
```

Utilize:

```text
JDBC URL: jdbc:h2:mem:coupondb
User Name: sa
Password:
```

Como o banco utilizado é em memória, os dados são recriados quando a aplicação é reiniciada.

---

## Swagger / OpenAPI

A documentação da API é disponibilizada através do SpringDoc OpenAPI.

Com a aplicação em execução, o Swagger UI pode ser acessado em:

```text
http://localhost:8080/swagger-ui.html
```

A especificação OpenAPI também está disponível em:

```text
http://localhost:8080/v3/api-docs
```

O Swagger permite visualizar e testar os endpoints diretamente pelo navegador.

---

## Executando Localmente

### Pré-requisitos

Para executar o projeto localmente é necessário possuir:

* Java 17
* Maven ou utilizar o Maven Wrapper incluído no projeto

### Utilizando Maven Wrapper no Windows

Na raiz do projeto:

```bash
mvnw.cmd clean package
mvnw.cmd spring-boot:run
```

Caso o Maven esteja instalado globalmente:

```bash
mvn clean package
mvn spring-boot:run
```

Após a inicialização, a aplicação estará disponível em:

```text
http://localhost:8080
```

---

## Executando com Docker

O projeto contém um `Dockerfile` e um arquivo `docker-compose.yml`.

Certifique-se primeiro de que o Docker Desktop esteja em execução.

Gere o `.jar` da aplicação:

```bash
mvn clean package
```

Em seguida, execute:

```bash
docker compose up --build
```

Após a inicialização do container, a aplicação estará disponível em:

```text
http://localhost:8080
```

E o Swagger em:

```text
http://localhost:8080/swagger-ui.html
```

Para encerrar os containers:

```bash
docker compose down
```

---

## Testes

O projeto possui testes automatizados para as camadas de domínio, serviço e controller.

Entre os cenários testados estão:

* Validação do valor mínimo de desconto.
* Sanitização e validação do código do cupom.
* Validação da data de expiração.
* Validação da descrição.
* Estado inicial do cupom.
* Publicação do cupom.
* Resgate do cupom.
* Bloqueio de resgate duplicado.
* Bloqueio de resgate antes da publicação.
* Bloqueio de resgate de cupom expirado.
* Soft delete.
* Exclusão de cupom resgatado e bloqueio da exclusão repetida.
* Bloqueio de operações em cupons excluídos.
* Consulta de cupons.
* Cenários de cupom não encontrado.
* Validação dos status HTTP.
* Tratamento das exceções da API.

Para executar os testes:

```bash
mvn test
```

Os testes utilizam:

* JUnit 5
* Mockito
* MockMvc

---

## Estrutura do Projeto

A estrutura principal segue a separação por responsabilidades:

```text
src
├── main
│   ├── java
│   │   └── com.desafioTenda.cupon_api
│   │       ├── controller
│   │       ├── domain
│   │       │   └── coupon
│   │       ├── dto
│   │       │   └── coupon
│   │       ├── exception
│   │       └── service
│   │           └── coupon
│   └── resources
│       └── application.properties
│
└── test
    └── java
        └── com.desafioTenda.cupon_api
```

---

## Decisões de Projeto

Algumas decisões importantes adotadas durante o desenvolvimento:

### Encapsulamento das Regras de Negócio

As principais regras são implementadas diretamente no objeto de domínio `Coupon`.

Dessa forma, o próprio domínio controla operações como publicação, resgate e exclusão.

### Soft Delete

A exclusão de cupons é lógica, e não física.

O registro continua no banco de dados, porém passa a ser considerado inativo pela aplicação.

### DTOs

A API utiliza objetos específicos para entrada e saída de dados, como `CreateCouponRequest` e `CouponResponse`.

Isso evita expor diretamente a entidade de persistência através da API.

### Tratamento Centralizado de Erros

As exceções são convertidas em respostas HTTP através de um `GlobalExceptionHandler`.

Isso evita duplicação de tratamento de erros nos Controllers.

### Filtragem de Cupons Excluídos

O Repository possui consultas específicas para retornar somente cupons que não passaram por soft delete.

### Testes Automatizados

As regras de domínio, os serviços e o comportamento HTTP da API possuem testes automatizados.

A cobertura percentual ainda precisa ser medida com uma ferramenta de cobertura; a quantidade de testes não comprova o requisito de 80%.

---

## Autor

Desenvolvido por Tiago Carvalho como parte de um desafio técnico para Software Engineer.
