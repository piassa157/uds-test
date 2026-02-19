# Filesmove

API para gerenciamento de documentos com autenticação JWT e versionamento de arquivos, com front Angular para uso básico.

## Stack

- Java 17 + Spring Boot
- Spring Security (JWT)
- Spring Data JPA + Flyway
- MySQL 8
- Angular 18 (`views/`)
- Docker Compose

## Subir o projeto (recomendado)

Pré-requisito: Docker + Docker Compose

```bash
docker compose up --build -d
```

Serviços:

- API: `http://localhost:8080`
- Front: `http://localhost:4200`
- MySQL: `localhost:3306`

Logs da API:

```bash
docker compose logs -f app
```

Parar:

```bash
docker compose down
```

Resetar tudo (inclui banco/volume):

```bash
docker compose down -v
```

## Rodar local sem Docker

### 1) Banco MySQL

Garanta um MySQL local com:

- database: `filesmove`
- user: `filesmove`
- password: `filesmove`

### 2) Backend

```bash
./mvnw spring-boot:run
```

### 3) Frontend

```bash
cd views
npm install
npm start
```

## Usuários padrão

- `admin` / `admin123`
- `usuarioteste` / `senha123`

## Autenticação

Login:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

Resposta:

```json
{
  "accessToken": "...",
  "tokenType": "Bearer",
  "expiresIn": 86400
}
```

Use o token em:

```http
Authorization: Bearer <token>
```

## Endpoints principais

### Admin

- `GET /api/admin/ping` (ROLE_ADMIN)

### Documentos (ROLE_ADMIN ou ROLE_USER)

- `POST /api/documents`
- `PUT /api/documents/{id}`
- `PATCH /api/documents/{id}/publish`
- `PATCH /api/documents/{id}/archive`
- `GET /api/documents/{id}`
- `GET /api/documents?title=&status=&page=&size=&sort=createdAt,desc`

### Arquivos e versões

- `POST /api/documents/{id}/files` (multipart, campo `file`)
- `GET /api/documents/{id}/files/current`
- `GET /api/documents/{id}/files`
- `GET /api/documents/{id}/files/{versionNumber}`

Tipos aceitos no upload:

- PDF (`application/pdf`)
- PNG (`image/png`)
- JPG/JPEG (`image/jpeg`, `image/jpg`)

## Exemplo rápido com curl

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.accessToken')

curl -s -X POST http://localhost:8080/api/documents \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title":"Contrato 2026",
    "description":"Documento de teste",
    "tags":["contrato","financeiro"],
    "ownerTenant":"tenant-a",
    "status":"DRAFT"
  }'
```

## Estrutura do projeto

- `src/main/java/.../controller`: endpoints REST
- `src/main/java/.../service`: regras de negócio
- `src/main/java/.../repository`: acesso ao banco
- `src/main/resources/db/migration`: migrations Flyway
- `views/`: aplicação Angular
- `compose.yaml`: orquestração local

## Testes

```bash
./mvnw test
```

## Problemas comuns

Erro de tabela ausente (`app_users`/`documents`):

```bash
docker compose down -v
docker compose up --build -d
```

Isso limpa o volume antigo e reaplica as migrations do zero.
