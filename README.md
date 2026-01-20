# Sistema de Ponto Eletrônico (POC) - EspelhoPonto

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-green)
![Security](https://img.shields.io/badge/Spring_Security-6-red)

Uma API REST robusta para controle de ponto e cálculo de banco de horas. O projeto evoluiu de uma POC simples para uma aplicação com **Autenticação Stateless (JWT)**, **Auditoria de Dados (Envers)** e **Clean Code**, utilizando as funcionalidades modernas do **Java 21**.

🔗 **Repositório:** [https://github.com/Vinicius-Sousa-Duarte/espelhoPonto](https://github.com/Vinicius-Sousa-Duarte/espelhoPonto)

## 🚀 Tecnologias e Ferramentas

* **Java 21** (Utilização de `Records`, `UUID` e recursos modernos)
* **Spring Boot 3.4+** (Web, Validation, Data JPA)
* **Spring Security 6** (Autenticação e Autorização Stateless)
* **Auth0 Java JWT** (Geração e validação de Tokens)
* **Hibernate Envers** (Auditoria histórica e versionamento de dados)
* **H2 Database** (Banco em memória para desenvolvimento)
* **Lombok** (Redução de boilerplate)
* **Maven** (Gerenciamento de dependências)

## 🏗 Arquitetura e Segurança

O projeto segue uma arquitetura em camadas focada em segurança e rastreabilidade:

* **Security Layer:** Filtros que interceptam requisições, validam Tokens JWT e injetam o usuário autenticado no contexto (`SecurityContextHolder`).
* **Audit Layer:**
    * **JPA Auditing:** Rastreia automaticamente *quem* criou/modificou e *quando* (`@CreatedBy`, `@LastModifiedDate`).
    * **Envers:** Mantém tabelas de histórico (`_AUD`) para cada alteração, permitindo "voltar no tempo".
* **Domain:** Uso de **UUID** para identificadores de usuários (segurança e compatibilidade com Hibernate 7).

## ⚙️ Pré-requisitos

* JDK 21 instalado.
* Maven instalado.

## 🏃‍♂️ Como Rodar

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/Vinicius-Sousa-Duarte/espelhoPonto.git](https://github.com/Vinicius-Sousa-Duarte/espelhoPonto.git)
    cd espelhoPonto
    ```

2.  **Execute via Maven:**
    ```bash
    mvn spring-boot:run
    ```

A aplicação iniciará na porta `8080`.

---

## 📚 Documentação da API

### 🔐 1. Autenticação (Pública)

Antes de usar o sistema, você deve criar um usuário e fazer login para obter o Token.

#### **Registrar Usuário**
* **URL:** `POST /auth/register`
* **Body:**
    ```json
    {
        "login": "vinicius@email.com",
        "password": "123",
        "role": "USER"
    }
    ```

#### **Fazer Login**
* **URL:** `POST /auth/login`
* **Body:**
    ```json
    {
        "login": "vinicius@email.com",
        "password": "123"
    }
    ```
* **Resposta:** Retorna um JSON com o `token`. **Copie este token!**

---

### 🕒 2. Pontos (Requer Token)

⚠️ **Atenção:** Todas as requisições abaixo exigem o Header:
`Authorization: Bearer <SEU_TOKEN_AQUI>`

#### **Registrar Ponto**
Bate o ponto. O sistema identifica o usuário automaticamente pelo Token.
* **URL:** `POST /api/pontos`
* **Body:**
    ```json
    {
        "tipo": "ENTRADA" 
    }
    ```
  *(Aceita: "ENTRADA" ou "SAIDA")*

#### **Consultar Saldo**
Calcula o banco de horas do usuário logado (Jornada de 8h).
* **URL:** `GET /api/pontos/saldo`
* **Query Params:**
    * `inicio`: Data inicial (YYYY-MM-DD)
    * `fim`: Data final (YYYY-MM-DD)
* **Exemplo:** `GET /api/pontos/saldo?inicio=2026-01-01&fim=2026-01-31`

---

## 🕵️‍♂️ Auditoria e Banco de Dados (H2)

O sistema mantém um histórico completo de alterações.

1.  Acesse: `http://localhost:8080/h2-console`
2.  **JDBC URL:** `jdbc:h2:mem:pontodb`
3.  **User/Password:** `sa` / *(vazio)*

### Tabelas Principais:
* **TB_USUARIO / TB_PONTO:** Dados atuais.
* **TB_USUARIO_AUD / TB_PONTO_AUD:** Histórico de alterações (Envers).
* **REVINFO:** Tabela de controle de revisões (Timestamp das mudanças).

As colunas `CRIADO_POR` e `MODIFICADO_POR` contêm o UUID do usuário que realizou a ação.

## 🧠 Lógica de Negócio

1.  **Segurança:** O Controller não recebe ID de usuário. O `TokenService` extrai o usuário do JWT, garantindo que ninguém manipule dados de terceiros.
2.  **Cálculo:** O sistema busca pares cronológicos (Entrada -> Saída) do usuário logado, soma os minutos e compara com a jornada esperada (480min/dia).
3.  **Auto-Auditoria:** No cadastro (`/register`), um evento `@PrePersist` garante que o campo `criado_por` seja preenchido com o próprio ID do novo usuário, evitando erros de integridade.

## 🔮 Melhorias Futuras

* [x] Implementar autenticação (Spring Security/JWT).
* [x] Adicionar Auditoria (Envers).
* [ ] Adicionar testes de integração para o fluxo de Auditoria.
* [ ] Dockerizar a aplicação.
* [ ] Implementar Refresh Token.
* [ ] Permitir configuração dinâmica da jornada (ex: 12x36).

---

Feito com ☕ e Java.