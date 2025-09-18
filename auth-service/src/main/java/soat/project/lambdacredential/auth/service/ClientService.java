package soat.project.lambdacredential.auth.service;

import soat.project.lambdacredential.auth.db.Database;
import soat.project.lambdacredential.auth.model.Client;

import java.sql.*;

public class ClientService {
    private final Database db;

    public ClientService(Database db) {
        this.db = db;
    }

    public Client findByCpf(String cpf) throws SQLException {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT public_id, cpf, name, email FROM clients WHERE cpf = ?")) {
                stmt.setString(1, cpf);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return new Client(
                            rs.getString("public_id"),
                            rs.getString("cpf"),
                            rs.getString("email"),
                            rs.getString("name")
                    );
                }
        }
        return null;
    }

    public boolean cpfIsValid(String cpf) {
        return cpf != null && cpf.matches("\\d{11}");
    }
}
