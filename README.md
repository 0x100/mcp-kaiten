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
- MCP clients should connect to the SSE endpoint at `http://localhost:${SERVER_PORT}/mcp/sse` and send JSON-RPC
  messages to `http://localhost:${SERVER_PORT}/mcp/message?sessionId={id}`.

### Manual smoke test
1. Open the SSE subscription: `curl -sN http://localhost:${SERVER_PORT}/mcp/sse`. The response will include a `sessionId` and a 
   `message` endpoint.
2. Perform `initialize`:
    `bash curl -s -X POST "http://localhost:${SERVER_PORT}/mcp/message?sessionId="
    -H 'Content-Type: application/json'
    -d '{"jsonrpc":"2.0","id":"1","method":"initialize","params":{"protocolVersion":"1.0","client":{"name":"cli","version":"0.0.1"},"capabilities":{"tools":{"list":true,"call":true}}}}'`

3. Confirm readiness:
`curl -s -X POST "http://localhost:${SERVER_PORT}/mcp/message?sessionId=<id>" -H 'Content-Type: application/json' -d '{"jsonrpc":"2.0","method":"notifications/initialized","params":{}}'`.

4. Now you can request and invoke tools (`tools/list`, `tools/call`). Responses are received through the SSE stream.
