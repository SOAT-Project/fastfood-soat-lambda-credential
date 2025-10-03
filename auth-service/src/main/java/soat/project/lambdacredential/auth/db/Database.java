package soat.project.lambdacredential.auth.db;

import java.sql.*;
import java.util.Properties;

public class Database {
    private final String url;
    private final String user;
    private final String password;

    public Database() {
        this.url = "jdbc:postgresql://" + System.getenv("DB_HOST") + "/" + System.getenv("DB_NAME" + "?sslmode=disable");
        this.user = System.getenv("DB_USER");
        this.password = System.getenv("DB_PASSWORD");
    }

    public Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", this.user);
        props.setProperty("password", this.password);
        return DriverManager.getConnection(url, props);
    }
}
