# Sistema de Ponto Eletrônico (POC) - EspelhoPonto

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-green)
![Spring Security](https://img.shields.io/badge/Spring_Security-6-red)
![Design Pattern](https://img.shields.io/badge/Pattern-Strategy-blue)

Uma API REST robusta para controle de ponto e cálculo de banco de horas. O projeto evoluiu de uma POC simples para uma aplicação corporativa com **Autenticação Stateless (JWT)**, **Auditoria de Dados (Envers)**, **Regras de Negócio Complexas** e uso intensivo de **Design Patterns**, utilizando as funcionalidades modernas do **Java 21**.

🔗 **Repositório:** [https://github.com/Vinicius-Sousa-Duarte/espelhoPonto](https://github.com/Vinicius-Sousa-Duarte/espelhoPonto)

## 🚀 Tecnologias e Ferramentas

* **Java 21** (Records, UUID, Var, Text Blocks)
* **Spring Boot 3.4+** (Web, Validation, Data JPA)
* **Spring Security 6** (Autenticação Stateless)
* **Auth0 Java JWT** (Assinatura HMAC256)
* **Hibernate Envers** (Auditoria Histórica)
* **H2 Database** (Banco em memória)
* **Lombok** (Produtividade)
* **Maven** (Gerenciamento de dependências)

## 🏗 Arquitetura e Design

O projeto segue uma arquitetura limpa e focada em extensibilidade:

* **Security Layer:** Filtros interceptam requisições, validam Tokens JWT e injetam o usuário no `SecurityContextHolder`.
* **Strategy Pattern:** A lógica de cálculo de horas (Adicional Noturno, Fim de Semana) foi desacoplada do Service usando interfaces (`CalculoHoraStrategy`), facilitando a manutenção.
* **Audit Layer:** Rastreamento automático de criação/modificação (JPA Auditing) e versionamento histórico de tabelas (Envers).
* **Rich DTOs:** A API não retorna apenas status HTTP, mas objetos ricos com mensagens, alertas e metadados.

## 🧠 Regras de Negócio Implementadas

O sistema vai além do CRUD básico e valida regras trabalhistas reais:

1.  **Anti-Bounce (5 Minutos):**
    * Bloqueia registros consecutivos com intervalo menor que 5 minutos.
    * Retorno: `422 Unprocessable Entity` com mensagem explicativa.
2.  **Alerta de Intervalo Intrajornada:**
    * Ao registrar o retorno do almoço, o sistema calcula se o intervalo foi menor que 1 hora.
    * Ação: Registra o ponto (Sucesso), mas retorna um campo `aviso` no JSON alertando sobre a infração.
3.  **Adicional Noturno (Strategy):**
    * Horas trabalhadas entre **22:00 e 05:00** têm peso **1.2x** (20% de acréscimo).
4.  **Horas de Fim de Semana (Strategy):**
    * Trabalho aos Sábados e Domingos tem peso **2.0x** (100% de Hora Extra).
5.  **Auto-Auditoria:**
    * No cadastro (`/register`), um evento `@PrePersist` garante que o campo `criado_por` seja preenchido com o próprio ID do usuário, garantindo integridade no banco.

---

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

#### **Registrar Usuário**
* **URL:** `POST /auth/register`
* **Body:** `{ "login": "vinicius@email.com", "password": "123", "role": "USER" }`
* **Resposta (201):**
    ```json
    {
        "login": "vinicius@email.com",
        "role": "USER",
        "mensagem": "Usuário criado com sucesso!",
        "dataCriacao": "2026-01-26T21:00:00"
    }
    ```

#### **Fazer Login**
* **URL:** `POST /auth/login`
* **Body:** `{ "login": "vinicius@email.com", "password": "123" }`
* **Resposta:** Retorna JSON com o `token`.

---

### 🕒 2. Pontos (Requer Token)

⚠️ **Header Obrigatório:** `Authorization: Bearer <SEU_TOKEN>`

#### **Registrar Ponto**
* **URL:** `POST /api/pontos`
* **Body:** `{ "tipo": "ENTRADA" }` *(ou "SAIDA")*
* **Cenário 1: Sucesso**
    ```json
    {
        "mensagem": "Ponto de ENTRADA registrado com sucesso!",
        "aviso": null,
        "tipo": "ENTRADA",
        "dataHora": "2026-01-26T08:00:00"
    }
    ```
* **Cenário 2: Sucesso com Alerta (Almoço Curto)**
    ```json
    {
        "mensagem": "Ponto de ENTRADA registrado com sucesso!",
        "aviso": "ALERTA: Intervalo de descanso inferior a 1 hora (30 min).",
        "tipo": "ENTRADA",
        "dataHora": "2026-01-26T12:30:00"
    }
    ```
* **Cenário 3: Erro (Regra dos 5 min)**
    * **Status:** `422 Unprocessable Entity`
    ```json
    {
        "erro": "Regra de Negócio Violada",
        "mensagem": "Espere 5 minutos! Último registro foi há 1 min.",
        "timestamp": "..."
    }
    ```

#### **Consultar Saldo**
* **URL:** `GET /api/pontos/saldo?inicio=2026-01-01&fim=2026-01-31`
* **Resposta:**
    ```json
    {
        "nomeFuncionario": "vinicius@email.com",
        "saldoTotal": "+02:30",
        "minutosTrabalhados": 510,
        "minutosEsperados": 480,
        "avisos": [
            "Dia 2026-01-24: Fim de semana contabilizado (100%)."
        ]
    }
    ```

---

## 🗄️ Estrutura do Projeto

```text
src/main/java/com/dunk/espelhoponto
├── controller          # Endpoints REST
├── entity              # Entidades JPA (Usuario, Ponto, Auditable)
├── dto                 # Records para tráfego de dados (Request/Response)
├── infra
│   ├── audit           # Configuração JPA Auditing e Envers
│   ├── exception       # GlobalExceptionHandler
│   └── security        # Filtros, TokenService e Configurações
├── repository          # Interfaces Spring Data
├── service             # Regras de Negócio (Orquestrador)
└── strategy            # Lógica de Cálculo (Noturno, FDS)