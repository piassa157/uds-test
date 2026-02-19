# filesmove

Projeto simples de API + front para documentos (login, usuarios, upload e listagem).

## rodar com docker (jeito mais facil)

```bash
docker compose down -v
docker compose up --build
```

API: `http://localhost:8080`  
Front: `http://localhost:4200`

## rodar backend local (sem docker)

Precisa de MySQL rodando.

```bash
./mvnw spring-boot:run
```

## rodar front local

```bash
cd views
npm install
npm start
```

## login padrao

- admin / admin123
- usuarioteste / senha123
