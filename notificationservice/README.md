# notificationservice

Serviço responsável pelo processamento e envio de notificações da plataforma `mini-cluster-apis`.

## Visão geral

A `notificationservice` é um microserviço Java desenvolvido com Spring Boot e gerenciado com Maven. O projeto possui perfis de configuração para diferentes ambientes e suporta execução local e via Docker.

## Tecnologias utilizadas

- Java 21
- Spring Boot 4
- Spring WebFlux
- Spring Boot Actuator
- Spring AMQP / RabbitMQ
- Spring Data Redis Reactive / Redis
- Spring Boot Validation
- Lombok
- Maven
- Docker / Docker Compose
- Testcontainers para testes de integração

## Pré-requisitos

Antes de executar o projeto, verifique se você possui:

- JDK 21
- Maven instalado, ou utilize o wrapper do projeto (`mvnw` / `mvnw.cmd`)
- Docker e Docker Compose, caso queira subir dependências em contêiner

## Estrutura do projeto

- `src/main/java`: código-fonte principal da aplicação
- `src/main/resources`: configurações, recursos estáticos e migrações
- `src/test/java`: testes automatizados
- `docker`: arquivos de infraestrutura local
- `pom.xml`: dependências e build da aplicação

## Configuração

Os arquivos de configuração principais ficam em:

- `src/main/resources/application.yaml`
- `src/main/resources/application-local.yaml`
- `src/main/resources/application-test.yaml`

Ajuste as propriedades conforme o ambiente desejado, como filas RabbitMQ, cache Redis, portas e integrações externas.

## Como executar localmente

### Windows

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

### Linux / macOS

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Build da aplicação

### Windows

```powershell
.\mvnw.cmd clean package
```

### Linux / macOS

```bash
./mvnw clean package
```

## Testes

### Windows

```powershell
.\mvnw.cmd test
```

### Linux / macOS

```bash
./mvnw test
```

## Execução com Docker

O projeto contém suporte a Docker Compose em `docker/docker-compose.yaml`.

Para subir os contêineres:

```bash
docker compose -f docker/docker-compose.yaml up -d
```

Depois, execute a aplicação conforme o perfil desejado.

## Principais integrações

- **RabbitMQ**: usado para mensageria assíncrona e troca de eventos.
- **Redis**: usado como banco de dados em memória / cache reativo.
- **WebFlux**: base reativa da aplicação para processamento não bloqueante.
- **Actuator**: endpoints de saúde e monitoramento da aplicação.
- **Testcontainers**: suporte para testes de integração com dependências reais em contêiner.

## Perfis de ambiente

- `local`: ambiente de desenvolvimento
- `test`: ambiente de testes
- `default`: configuração padrão da aplicação

## Observações

- Revise as configurações antes de executar em ambientes diferentes de `local`.
- Caso haja serviços dependentes, suba-os antes de iniciar a aplicação.

