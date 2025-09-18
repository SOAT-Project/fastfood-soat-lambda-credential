package soat.project.lambdacredential.auth.service;

import soat.project.lambdacredential.auth.db.Database;
import soat.project.lambdacredential.auth.model.Staff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffService {
    private final Database db;

    public StaffService(Database db) {
        this.db = db;
    }

    public Staff findByIdentification(String identification) throws SQLException {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, cpf, name, email FROM staff WHERE email = ?")) {
            stmt.setString(1, identification);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Staff(
                        rs.getString("id"),
                        rs.getString("cpf"),
                        rs.getString("email"),
                        rs.getString("name")
                );
            }
        }
        return null;
    }
}
