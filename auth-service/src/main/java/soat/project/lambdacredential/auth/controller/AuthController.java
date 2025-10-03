package soat.project.lambdacredential.auth.controller;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import soat.project.lambdacredential.auth.model.Staff;
import soat.project.lambdacredential.auth.model.Client;
import soat.project.lambdacredential.auth.service.StaffService;
import soat.project.lambdacredential.auth.service.ClientService;
import soat.project.lambdacredential.common.security.JwtService;

import java.util.Map;

public class AuthController {
    private final ClientService clientService;
    private final StaffService staffService;
    private final JwtService jwtService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AuthController(ClientService clientService, StaffService staffService, JwtService jwtService) {
        this.clientService = clientService;
        this.staffService = staffService;
        this.jwtService = jwtService;
    }

    public APIGatewayProxyResponseEvent userAuth(Map<String, Object> body) {
        try {
            String cpf = (String) body.get("identification");
            if (cpf == null) {
                String tokenGuest = jwtService.generateToken(
                        "guest",
                        Map.of("role", "USER_GUEST", "name", "Guest")
                );

                return successResponse(tokenGuest);
            }

            if (!clientService.cpfIsValid(cpf)) return errorResponse("Invalid CPF format", 400);

            Client client = clientService.findByCpf(cpf);
            if (client == null) return errorResponse("Client not found", 404);

            String token = jwtService.generateToken(
                    client.getPublicId(),
                    Map.of("role", "USER", "name", client.getName(), "email", client.getEmail(), "cpf", client.getCpf())
            );

            return successResponse(token);
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500)
                    .withBody("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    public APIGatewayProxyResponseEvent staffAuth(Map<String, Object> body) {
        try {
            String identification = (String) body.get("identification");
            if (identification == null) return errorResponse("Identification is required", 400);

            Staff staff = staffService.findByIdentification(identification);
            if (staff == null) return errorResponse("Staff not found", 404);

            String token = jwtService.generateToken(
                    staff.getId(),
                    Map.of("role", "STAFF", "name", staff.getName(), "email", staff.getEmail(), "cpf", staff.getCpf())
            );

            return successResponse(token);
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Erro desconhecido";

            return errorResponse(errorMsg, 500);
        }
    }

    private APIGatewayProxyResponseEvent successResponse(String token) throws JsonProcessingException {
        return new APIGatewayProxyResponseEvent().withStatusCode(200)
                .withBody(mapper.writeValueAsString(Map.of("accessToken", token)));
    }

    private APIGatewayProxyResponseEvent errorResponse(String message, Integer statusCode) {
        return new APIGatewayProxyResponseEvent().withStatusCode(statusCode)
                .withBody("{\"error\":\"" + message + "\"}");
    }
}
