package cz.cvut.ear.sem.aletheia.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class StatusController {

    @Value("${server.port:8080}")
    private String port;

    @Value("${spring.datasource.url:jdbc:h2:mem:aletheia}")
    private String dbUrl;

    @GetMapping("/")
    public String home(Principal principal) {
        // 1. Check if the user is logged in
        String authBlock;
        if (principal == null) {
            // If anonymous -> show button LOGIN
            authBlock = "<a href='/login'>🔐 Login</a>";
        } else {
            // If logged in -> show name and LOGOUT
            authBlock = "👤 <b>" + principal.getName() + "</b> | <a href='/logout'>🚪 Logout</a>";
        }

        boolean isH2 = dbUrl.contains("h2");

        String h2Block = "";
        if (isH2) {
            h2Block = """
                <br>
                <br>
                Use to login:
                <br>
                <pre style="background:#f0f0f0;padding:10px;border-left:4px solid #2ecc71;">
                JDBC URL: %s
                User:     sa
                Password: (leave empty)
                </pre>
                <a href="http://localhost:%s/h2-console">Open H2 Console</a>
                """.formatted(dbUrl, port);
        } else {
            h2Block = """
                <br>
                <br>
                <div style="background:#fff3cd;padding:10px;border-left:4px solid #ffc107;">
                Running in Production Mode (PostgreSQL).<br>
                H2 Console is disabled.
                </div>
                """;
        }

        return """
                <html>
                <body style="font-family: sans-serif; padding: 20px;">
                <h1>Aletheia is running 🚀</h1>

                <p>
                <a href="/swagger-ui/index.html">📄 Swagger API Documentation</a> |
                %s
                </p>

                %s

                <br><hr>
                <small>Health check: <a href="/actuator/health">/actuator/health</a></small>
                </body>
                </html>
                """.formatted(authBlock, h2Block);
    }

    @GetMapping("/api/status")
    public String status() {
        return "{\"status\":\"OK\",\"db\":\"%s\"}".formatted(dbUrl.contains("h2") ? "H2" : "PostgreSQL");
    }
}