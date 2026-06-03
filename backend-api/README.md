Digital Bank - backend-api

Resumo rápido

Aplicação backend em Java (Spring Boot) que implementa autenticação via JWT, operações de transferência entre contas e publicação de eventos de notificação via RabbitMQ. O projeto usa JPA + Flyway para persistência e migrações de banco.

Stacks principais

- Java 21 (toolchain configurado no Gradle)
- Spring Boot 4 (Web MVC, Security, Data JPA, AMQP, Actuator)
- Spring Security + OAuth2 Resource Server (JWT)
- Spring AMQP (RabbitMQ) para publicação de eventos de notificação
- Flyway para migrações de banco (migrations em src/main/resources/db/migration)
- Banco (driver): MySQL (configurado por dependência, mas a migration é genérica)
- OpenAPI (springdoc) para documentação
- Lombok para redução de boilerplate

Como a autenticação funciona

- Endpoint de login: POST /auth/login
  - Request JSON: {"username":"admin","password":"admin123"}
  - Response JSON: {"accessToken":"<token>","tokenType":"Bearer","expiresIn":900}
  - A aplicação usa o `UserDetailsService` (`DatabaseUserDetailsService`) que carrega usuários da tabela `users`.
  - Senhas são armazenadas como hash BCrypt na coluna `password_hash` e o `PasswordEncoder` configurado é `BCryptPasswordEncoder`.
  - O token JWT é gerado pelo `TokenService` usando HS256. As propriedades estão em `app.security.jwt.*` (issuer, expiration, secret).

Usando o token

- Use o header Authorization: Bearer <accessToken> nas requisições aos endpoints protegidos.

Endpoints (implementados até o momento)

1) Autenticação
- POST /auth/login
  - Request: LoginRequest { username, password }
  - Response: LoginResponse { accessToken, tokenType, expiresIn }
  - Permite autenticar e receber um JWT para chamadas subsequentes.

2) Transferências
- POST /transfers
  - Headers: Idempotency-Key (string, obrigatório), Authorization: Bearer <token>
  - Request JSON: TransferRequest {
      "fromAccountId": "<UUID>",
      "toAccountId": "<UUID>",
      "amount": <decimal>
    }
  - Response JSON: TransferResponse {
      "transferId",
      "idempotencyKey",
      "fromAccountId",
      "toAccountId",
      "amount",
      "status",
      "createdAt"
    }
  - Comportamento:
    - Valida ids diferentes e saldo suficiente.
    - Faz débito/crédito nas contas e grava movimentos.
    - Usa idempotência: se a mesma `Idempotency-Key` já foi processada retorna o mesmo resultado.
    - Ao confirmar a transação (após commit), publica um evento `TransferCompletedEvent` para o RabbitMQ.

Comunicação com serviço de notificação

- A publicação é feita por `NotificationEventPublisher` via `RabbitTemplate`.
- As propriedades de RabbitMQ são carregadas por `app.rabbitmq.exchange` e `app.rabbitmq.routing-key` (bind em `RabbitProperties`).
- Evento publicado: `TransferCompletedEvent` com campos: eventId, eventType ("TRANSFER_COMPLETED"), transferId, fromAccount (AccountSummaryEvent), toAccount (AccountSummaryEvent), amount, currency, occurredAt.
- A publicação é agendada para ocorrer somente após o commit da transação (usando TransactionSynchronization), garantindo consistência entre DB e mensagens.

Banco de dados e migrações

- Migrações Flyway em `src/main/resources/db/migration`.
- A primeira migration (`V1__create_users_table.sql`) insere um usuário `admin` com um hash BCrypt em `password_hash`.

Exemplos de uso

1) Login (curl):

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Resposta esperada: JSON com `accessToken`.

2) Criar transferência (curl):

```bash
curl -X POST http://localhost:8080/transfers \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <accessToken>" \
  -H "Idempotency-Key: my-key-123" \
  -d '{"fromAccountId":"22222222-2222-2222-2222-222222222222","toAccountId":"33333333-3333-3333-3333-333333333333","amount":100.50}'
```

Observações e pontos importantes

- Verifique em `src/main/resources/application*.yaml` as configurações para DB, JWT (`app.security.jwt`) e RabbitMQ (`app.rabbitmq`).
- Se você está recebendo 401 ao tentar autenticar com o usuário `admin` e senha `admin123`, confira:
  - Se as migrations foram executadas e o usuário `admin` existe (veja `V1__create_users_table.sql` para o hash inicial).
  - Se o header `Content-Type: application/json` está sendo enviado.
  - Se a aplicação está apontando para o banco correto (credenciais/URL). Um 401 normalmente indica que o par username/senha não corresponde ao hash gravado no banco.
  - As senhas são verificadas com BCrypt; se o hash no banco foi trocado ou a seed/salt for diferente, a checagem falhará.

Onde procurar no código

- Autenticação: `com.digitalbank.backend_api.auth` (AuthController, DatabaseUserDetailsService, UserEntity, UserRepository)
- Security / JWT: `com.digitalbank.backend_api.config.security` (SecurityConfig, TokenService, JwtProperties)
- Transferências: `com.digitalbank.backend_api.transfer` (TransferController, TransferService, DTOs)
- Notificações: `com.digitalbank.backend_api.notification` (NotificationEventPublisher, DTOs)
- Migrations: `src/main/resources/db/migration`

Fim.

