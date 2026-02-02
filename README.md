# EspelhoPonto - Sistema de Ponto Full Stack

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4+-green?logo=springboot)
![Angular](https://img.shields.io/badge/Angular-19+-dd0031?logo=angular)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?logo=postgresql)
![Swagger](https://img.shields.io/badge/Swagger-OpenAPI-85EA2D?logo=swagger)

Uma solução corporativa completa para gestão de ponto eletrônico. O projeto evoluiu de uma API isolada para um **Sistema Full Stack End-to-End**, integrando um Backend robusto com Spring Security a um Frontend moderno em Angular, tudo orquestrado via Docker.

🔗 **Repositório:** [https://github.com/Vinicius-Sousa-Duarte/espelhoPonto](https://github.com/Vinicius-Sousa-Duarte/espelhoPonto)

---

## 🚀 Tecnologias e Ferramentas

### Back-End (API)
* **Java 21** (Records, UUID, Text Blocks)
* **Spring Boot 3.4+** (Web, Validation, Data JPA)
* **Spring Security 6** (Autenticação Stateless via JWT)
* **Hibernate Envers** (Auditoria e Versionamento de Dados)
* **PostgreSQL 15** (Banco de dados relacional robusto)
* **Swagger / OpenAPI** (Documentação viva)

### Front-End (Interface)
* **Angular 19+** (Standalone Components, Signals)
* **Angular Material 3** (Customizado com CSS Variables)
* **SCSS & Design System** (Fonte Inter, Layouts Modernos, Sombras Suaves)
* **Proxy Reverso** (Integração transparente em ambiente dev)

### Infraestrutura
* **Docker & Docker Compose** (Containerização e Orquestração)
* **Maven** (Build Java) & **NPM** (Build Angular)

---

## 🏗 Arquitetura e Design

O sistema segue padrões de mercado para garantir escalabilidade e manutenibilidade:

1.  **Segurança Stateless:** Comunicação via Tokens JWT (Bearer). O Frontend armazena o token e o injeta automaticamente via Interceptors (ou manual no Service).
2.  **Strategy Pattern (Backend):** A lógica de cálculo de horas (Adicional Noturno, Fim de Semana) é desacoplada via interfaces, permitindo fácil adição de novas regras (ex: Feriados).
3.  **Audit Layer:** Rastreabilidade total. Tabelas `_AUD` (Envers) gravam o histórico de alterações, e JPA Auditing marca datas de criação/modificação.
4.  **UX/UI Moderno:**
    * **Login Split-Screen:** Layout dividido com validação reativa.
    * **Dashboard KPI:** Cards flutuantes com feedback visual e "watermarks".
    * **Personalização:** O sistema reconhece o usuário ("Olá, Vinicius") através da integração Back/Front.

---

## 🧠 Regras de Negócio

### Backend (Core)
1.  **Anti-Bounce (5 Minutos):** Bloqueia registros duplicados/acidentais em curto intervalo.
2.  **Intervalo Intrajornada:** Alerta visual e no JSON se o almoço for menor que 1 hora.
3.  **Cálculos Automáticos:**
    * **Adicional Noturno:** Peso 1.2x (22h - 05h).
    * **Fim de Semana:** Peso 2.0x (Sáb/Dom).

### Frontend (Experiência)
1.  **Feedback Visual:** Snackbars coloridos para Sucesso (Verde), Erro (Vermelho) ou Alertas de Negócio (Laranja).
2.  **Tratamento de Erros:** Captura exceções da API (ex: 422 Unprocessable Entity) e exibe mensagens amigáveis ao usuário.

---

## 🏃‍♂️ Como Rodar (Docker)

A maneira mais simples de rodar a aplicação completa (Banco + API) é usando Docker.

### Pré-requisitos
* Docker e Docker Compose instalados.

### Passos
1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/Vinicius-Sousa-Duarte/espelhoPonto.git](https://github.com/Vinicius-Sousa-Duarte/espelhoPonto.git)
    cd espelhoPonto
    ```

2.  **Suba os containers:**
    ```bash
    docker compose up --build
    ```
    *Isso irá compilar o JAR do Java, baixar a imagem do Postgres e subir ambos na rede interna.*

3.  **Acesse a Aplicação:**
    * **API (Swagger):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
    * **Frontend (Dev):** Em outro terminal, entre na pasta `espelho-ponto-front` e rode `ng serve` (Acesse em [http://localhost:4200](http://localhost:4200)).

---

## 📚 Documentação da API

### 🔐 1. Autenticação

#### **Registrar Usuário**
* **URL:** `POST /auth/register`
* **Body:**
    ```json
    {
      "login": "vinicius@email.com",
      "password": "123",
      "nome": "Vinicius Sousa",
      "role": "USER"
    }
    ```

#### **Fazer Login**
* **URL:** `POST /auth/login`
* **Resposta (200 OK):**
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsIn...",
      "nome": "Vinicius Sousa"
    }
    ```

### 🕒 2. Pontos (Requer Token)

#### **Registrar Ponto**
* **URL:** `POST /api/pontos`
* **Body:** `{ "tipo": "ENTRADA" }`
* **Resposta com Alerta (Ex: Almoço curto):**
    ```json
    {
        "mensagem": "Ponto registrado com sucesso!",
        "aviso": "ALERTA: Intervalo inferior a 1 hora.",
        "tipo": "ENTRADA",
        "dataHora": "2026-02-02T12:30:00"
    }
    ```

#### **Consultar Saldo**
* **URL:** `GET /api/pontos/saldo?inicio=2026-02-01&fim=2026-02-28`
* **Resposta:** Retorna saldo total, horas esperadas vs. realizadas e lista de avisos.

---

## 🗄️ Estrutura do Projeto

```text
/espelhoPonto
├── docker-compose.yml       # Orquestrador (API + Banco)
├── pgdata/                  # Volume de dados (Persistência)
│
├── src/                     # ☕ BACKEND (Java Spring Boot)
│   ├── main/java/com/dunk/espelhoponto
│   │   ├── auth             # Login/Register Logic
│   │   ├── infra            # Security, Swagger, Audit, ExceptionHandler
│   │   ├── domain           # Entidades (Usuario, Ponto)
│   │   └── service          # Regras de Negócio & Strategy
│   └── Dockerfile           # Build da imagem Java
│
└── espelho-ponto-front/     # 🅰️ FRONTEND (Angular)
    ├── proxy.conf.json      # Configuração de Proxy (CORS)
    ├── src/app
    │   ├── layouts/         # Sidenav, Toolbar
    │   ├── pages/           # Login Split-Screen, Dashboard Moderno
    │   └── services/        # Integração HTTP
    └── styles.scss          # Tema Global (Inter Font, Material Overrides)