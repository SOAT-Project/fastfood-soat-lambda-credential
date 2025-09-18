package soat.project.lambdacredential.middleware;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import soat.project.lambdacredential.common.security.JwtService;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Handler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final JwtService jwtService;

    private static final Map<String, List<String>> USER_ROUTE_NOT_ALLOWED = Map.of(
            "GET", List.of("/auths", "/orders/staff"),
            "POST", List.of("/auths", "/orders", "/webhooks/mercadopago", "/mock", "/products"),
            "PUT", List.of("/auths", "/orders", "/products"),
            "DELETE", List.of("/auths", "/orders", "/products")
    );

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

            Map<String, Object> requestContext = (Map<String, Object>) event.get("requestContext");
            Map<String, Object> http = (Map<String, Object>) requestContext.get("http");
            String path = (String) http.get("path");
            String method = (String) http.get("method");

            if (!isAuthorized(role, path, method)) {
                return Map.of(
                        "statusCode", 403,
                        "body", String.format("Access denied for role=%s on path=%s", role, path)
                );
            }

            return Map.of(
                    "statusCode", 200,
                    "body", String.format("Authorized! subject=%s, role=%s, name=%s", subject, role, name)
            );
        } catch (Exception e) {
            return unauthorized("Invalid or expired token: " + e.getMessage());
        }
    }

    private boolean isAuthorized(String role, String path, String method) {
        if (Objects.equals(role, "STAFF")) return true;

        List<String> routes = USER_ROUTE_NOT_ALLOWED.get(method);
        if (routes == null) return false;

        return routes.contains(path);
    }

    private Map<String, Object> unauthorized(String message) {
        return Map.of(
                "statusCode", 401,
                "body", message
        );
    }
}
