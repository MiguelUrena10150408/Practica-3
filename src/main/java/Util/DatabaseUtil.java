package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String URL = "jdbc:h2:~/test;INIT=runscript from 'classpath:/shoppy/schema.sql'";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    // Método para obtener la conexión
    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos: " + e.getMessage());
            throw e;  // Re-lanzar la excepción para ser manejada por el código llamante
        }
    }
}
