# Sistema de Ponto Eletrônico (POC)

Uma API REST simplificada para controle de ponto e cálculo de banco de horas, desenvolvida como uma Prova de Conceito (POC). O projeto foca em **Clean Code**, princípios **SOLID** e utiliza as funcionalidades modernas do **Java 21**.

## 🚀 Tecnologias e Ferramentas

* **Java 21** (Utilização de `Records` para DTOs)
* **Spring Boot 3+** (Web, Validation, Data JPA)
* **H2 Database** (Banco de dados em memória para testes rápidos)
* **Lombok** (Redução de boilerplate)
* **Maven** (Gerenciamento de dependências)

## 🏗 Arquitetura e Design

O projeto segue uma arquitetura em camadas para respeitar o Princípio da Responsabilidade Única (SRP):

* **Controller:** Camada de entrada (REST), sem regras de negócio.
* **Service:** Contém a lógica de negócio (cálculo de horas, pares de entrada/saída).
* **Repository:** Interface de comunicação com o banco de dados (Spring Data JPA).
* **Domain (Entity):** Representação da tabela no banco de dados.
* **DTO (Records):** Objetos imutáveis para transferência de dados entre cliente e servidor.

## ⚙️ Pré-requisitos

* JDK 21 instalado.
* Maven instalado (ou usar o wrapper `./mvnw` incluso no projeto).

## 🏃‍♂️ Como Rodar

1.  **Clone o repositório:**
    ```bash
    git clone [https://github.com/seu-usuario/sistema-ponto.git](https://github.com/seu-usuario/sistema-ponto.git)
    cd sistema-ponto
    ```

2.  **Execute via Maven:**
    ```bash
    mvn spring-boot:run
    ```

A aplicação iniciará na porta `8080`.

## 📚 Documentação da API

### 1. Registrar Ponto
Bate o ponto (Entrada ou Saída). A data e hora são capturadas automaticamente pelo servidor (`LocalDateTime.now()`).

* **URL:** `POST /api/pontos`
* **Body (JSON):**
    ```json
    {
        "nomeFuncionario": "João Silva",
        "tipo": "ENTRADA" 
    }
    ```
  *(O campo `tipo` aceita: "ENTRADA" ou "SAIDA")*

* **Resposta:** `201 Created`

### 2. Consultar Saldo (Banco de Horas)
Calcula o saldo de horas em um determinado período. O sistema considera uma jornada padrão de **8 horas diárias**.

* **URL:** `GET /api/pontos/saldo`
* **Query Params:**
    * `nome`: Nome do funcionário
    * `inicio`: Data inicial (YYYY-MM-DD)
    * `fim`: Data final (YYYY-MM-DD)

* **Exemplo de Requisição:**
  `GET /api/pontos/saldo?nome=João Silva&inicio=2024-01-01&fim=2024-01-31`

* **Resposta (JSON):**
    ```json
    {
        "nomeFuncionario": "João Silva",
        "saldoTotal": "+02:30",
        "minutosTrabalhados": 510,
        "minutosEsperados": 480
    }
    ```

## 🗄️ Acesso ao Banco de Dados (H2 Console)

Como o projeto utiliza o banco em memória H2, você pode visualizar os dados inseridos via navegador enquanto a aplicação estiver rodando.

1.  Acesse: `http://localhost:8080/h2-console`
2.  **JDBC URL:** `jdbc:h2:mem:pontodb`
3.  **User Name:** `sa`
4.  **Password:** *(deixe em branco)*
5.  Clique em **Connect**.

## 🧠 Lógica de Cálculo

O sistema utiliza a seguinte lógica no `PontoService`:
1.  Busca todos os registros do período.
2.  Agrupa os registros por dia.
3.  Dentro de cada dia, busca pares cronológicos de `ENTRADA` seguidos de `SAIDA`.
4.  Soma o tempo trabalhado e subtrai a jornada de 8 horas (480 minutos).
5.  O resultado é formatado como saldo positivo (`+HH:mm`) ou negativo (`-HH:mm`).

## 🔮 Melhorias Futuras

* [ ] Implementar autenticação (Spring Security/JWT).
* [ ] Substituir H2 por PostgreSQL ou MySQL para produção.
* [ ] Adicionar tratamento de exceções globais (ControllerAdvice).
* [ ] Criar interface visual com Thymeleaf ou Angular.
* [ ] Permitir configuração dinâmica da jornada de trabalho (ex: 6h, 12x36).

---

Feito com ☕ e Java.