package soat.project.lambdacredential.middleware;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import soat.project.lambdacredential.common.security.JwtService;

import java.time.Duration;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final JwtService jwtService;

    public Handler() {
        String secret = System.getenv("JWT_SECRET");
        this.jwtService = new JwtService(secret, Duration.ofHours(1));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        try {
            Map<String, String> headers = (Map<String, String>) event.get("headers");

            if (headers == null || !headers.containsKey("authorization")) {
                return unauthorized("Missing Authorization header");
            }

            String authHeader = headers.get("authorization");
            if (!authHeader.startsWith("Bearer ")) {
                return unauthorized("Invalid Authorization format");
            }

            String token = authHeader.substring(7);

            if (!jwtService.isValid(token)) {
                return unauthorized("Invalid or expired token");
            }

            Jws<Claims> parsed = jwtService.parseToken(token);

            String subject = parsed.getBody().getSubject();
            String role = parsed.getBody().get("role", String.class);
            String name = parsed.getBody().get("name", String.class);

            return Map.of(
                    "statusCode", 200,
                    "body", String.format("Authorized! subject=%s, role=%s, name=%s", subject, role, name)
            );
        } catch (Exception e) {
            return unauthorized("Invalid or expired token: " + e.getMessage());
        }
    }

    private Map<String, Object> unauthorized(String message) {
        return Map.of(
                "statusCode", 401,
                "body", message
        );
    }
}
