package cz.cvut.ear.sem.aletheia.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    @Value("${server.port:8080}")
    private String port;

    @GetMapping("/")
    public String home() {
        return """
                Aletheia is running. <a href="http://localhost:%s/h2-console">H2 Console</a>

                <br>
                <br>
                Use to login:
                <br>
                <pre style="background:#f0f0f0;padding:10px;border-left:4px solid #2ecc71;">
                JDBC URL: jdbc:h2:mem:aletheia
                User:     sa
                Password: (leave empty)
                </pre>

                <br>
                Health check: <a href="http://localhost:%s/actuator/health">/actuator/health</a>
                """.formatted(port, port);
    }

    @GetMapping("/api/status")
    public String status() {
        return "{\"status\":\"OK\",\"db\":\"H2 in-memory\",\"h2_url\":\"http://localhost:%s/h2-console\"}".formatted(port);
    }
}