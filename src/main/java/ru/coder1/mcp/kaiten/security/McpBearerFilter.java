package ru.coder1.mcp.kaiten.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Order(1)
@Component
public class McpBearerFilter extends OncePerRequestFilter {

    /**
     * [Optional] MCP token for MCP clients.
     * If is set, MCP clients need to send this token in the Authorization header
     */
    @Value("${security.bearer-token:}")
    private String mcpToken;


    @Override
    @SneakyThrows
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
        var uri = request.getRequestURI();
        boolean mcpPath = uri.startsWith("/mcp/");
        if (mcpPath && StringUtils.hasText(mcpToken)) {
            var header = request.getHeader(HttpHeaders.AUTHORIZATION);
            var expected = "Bearer " + mcpToken;
            if (!expected.equals(header)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Unauthorized");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}