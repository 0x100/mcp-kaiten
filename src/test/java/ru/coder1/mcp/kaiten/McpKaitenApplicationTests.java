package ru.coder1.mcp.kaiten;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@org.springframework.test.context.TestPropertySource(properties = {
        "kaiten.api-url=https://example.com",
        "kaiten.api-token=dummy"
})
class McpKaitenApplicationTests {

	@Test
	void contextLoads() {
	}

}
