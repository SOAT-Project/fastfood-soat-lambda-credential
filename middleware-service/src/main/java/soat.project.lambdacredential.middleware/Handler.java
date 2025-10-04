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
            String authHeader = (String) event.get("authorizationToken");
            if (authHeader == null) {
                return deny("user", "*", "Authorization header is missing");
            }

            if (!authHeader.startsWith("Bearer ")) {
                return deny("user", "*", "Invalid authorization header format");
            }

            String token = authHeader.substring(7);

            if (!jwtService.isValid(token)) {
                return deny("user", "*", "Invalid or expired token");
            }

            Jws<Claims> parsed = jwtService.parseToken(token);

            String subject = parsed.getBody().getSubject();
            String role = parsed.getBody().get("role", String.class);
            String name = parsed.getBody().get("name", String.class);

            String methodArn = (String) event.get("methodArn");

            String[] arnParts = methodArn.split(":");
            String apiGatewayArnPart = arnParts[5];

            String[] arnDetails = apiGatewayArnPart.split("/");
            String stage = arnDetails[1];
            String method = arnDetails[2];
            String path = arnDetails.length > 3 ? "/" + String.join("/", java.util.Arrays.copyOfRange(arnDetails, 3, arnDetails.length)) : "/";


            if (!isAuthorized(role, path, method)) {
                return deny(subject, "*", "User not authorized for this route");
            }

            return allow(subject, "*", role, name);
        } catch (Exception e) {
            return deny("user", "*", "Error: " + e.getMessage());
        }
    }

    private boolean isAuthorized(String role, String path, String method) {
        if (Objects.equals(role, "STAFF")) return true;

        List<String> routes = USER_ROUTE_NOT_ALLOWED.get(method);
        if (routes == null) return true;

        return !routes.contains(path);
    }

    private Map<String, Object> allow(String principalId, String resource, String role, String name) {
        return Map.of(
                "principalId", principalId,
                "policyDocument", Map.of(
                        "Version", "2012-10-17",
                        "Statement", List.of(
                                Map.of(
                                        "Action", "execute-api:Invoke",
                                        "Effect", "Allow",
                                        "Resource", resource
                                )
                        )
                ),
                "context", Map.of(
                        "role", role,
                        "name", name
                )
        );
    }

    private Map<String, Object> deny(String principalId, String resource, String message) {
        return Map.of(
                "principalId", principalId,
                "policyDocument", Map.of(
                        "Version", "2012-10-17",
                        "Statement", List.of(
                                Map.of(
                                        "Action", "execute-api:Invoke",
                                        "Effect", "Deny",
                                        "Resource", resource
                                )
                        )
                ),
                "context", Map.of(
                        "error", message
                )
        );
    }

    private String getAuthorizationHeader(Map<String, String> headers) {
        if (headers == null) return null;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if ("authorization".equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
