# mcp-kaiten

MCP Server for Kaiten, implemented with Spring AI.

## Quick start

```bash
export KAITEN_API_URL="https://YOUR_SPACE.kaiten.ru/api/v1"
export KAITEN_API_TOKEN="<your Kaiten token>"
export SERVER_PORT=8080  # use a free port if 8080 is busy

./mvnw spring-boot:run
```

### Notes
- `KAITEN_API_URL` and `KAITEN_API_TOKEN` are mandatory; the application fails fast if they are missing.
- The server listens on `${SERVER_PORT}` (defaults to 8080). Choose another port when 8080 is already in use.