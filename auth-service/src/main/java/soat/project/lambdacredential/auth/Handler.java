package soat.project.lambdacredential.auth;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import soat.project.lambdacredential.auth.controller.AuthController;
import soat.project.lambdacredential.auth.db.Database;
import soat.project.lambdacredential.auth.service.StaffService;
import soat.project.lambdacredential.auth.service.ClientService;
import soat.project.lambdacredential.common.security.JwtService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Handler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final AuthController authController;

    public Handler() {
        String jwtSecret = System.getenv("JWT_SECRET");

        Database db = new Database();
        ClientService clientService = new ClientService(db);
        StaffService staffService = new StaffService(db);
        JwtService jwtService = new JwtService(jwtSecret, java.time.Duration.ofHours(2));
        this.authController = new AuthController(clientService, staffService, jwtService);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            List<String> availablePathList = Arrays.asList("/auths/client", "/auths/staff");
            String availableMethod = "POST";

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> body = mapper.readValue(event.getBody(), Map.class);

            if (!availablePathList.contains(event.getPath())) {
                return new APIGatewayProxyResponseEvent().withStatusCode(404).withBody("Not Found");
            }

            if (!availableMethod.equals(event.getHttpMethod())) {
                return new APIGatewayProxyResponseEvent().withStatusCode(405).withBody("Method Not Allowed");
            }

            boolean isStaffPath = event.getPath().equals("/auths/staff");
            if (isStaffPath) {
                return authController.staffAuth(body);
            }

            return authController.userAuth(body);
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withBody("Internal Error");
        }
    }
}
