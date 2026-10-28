package com.brandguard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.brandguard.config.AiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AiAnalysisServiceTest {
    @Test
    void invalidStructuredResponseFallsBackWithoutThrowing() throws IOException {
        HttpServer server = server(200, """
                {"choices":[{"message":{"content":"not-json"}}]}
                """);
        try {
            AiAnalysisService service = service(server);
            var result = service.analyze("Draft", "Approved facts", Set.of());
            assertThat(result.available()).isFalse();
            assertThat(result.findings()).isEmpty();
        } finally {
            server.stop(0);
        }
    }

    @Test
    void providerFailureFallsBackWithoutThrowing() throws IOException {
        HttpServer server = server(503, "provider unavailable");
        try {
            AiAnalysisService service = service(server);
            assertThat(service.analyze("Draft", "Approved facts", Set.of()).findings()).isEmpty();
            assertThat(service.analyze("Draft", "Approved facts", Set.of()).available()).isFalse();
        } finally {
            server.stop(0);
        }
    }

    private HttpServer server(int status, String body) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/chat", exchange -> {
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, response.length);
            try (var output = exchange.getResponseBody()) {
                output.write(response);
            }
        });
        server.start();
        return server;
    }

    private AiAnalysisService service(HttpServer server) {
        String endpoint = "http://localhost:" + server.getAddress().getPort() + "/chat";
        return new AiAnalysisService(new AiProperties("test-key", endpoint, "test-model"), new ObjectMapper());
    }
}
