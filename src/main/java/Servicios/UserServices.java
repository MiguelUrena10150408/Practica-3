package Servicios;

import Modelos.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserServices {
    private static UserServices instance;
    private static final String JDBC_URL = "jdbc:h2:~/test";  // Asegúrate de ajustar la URL según tu configuración
    private static final String JDBC_USER = "sa";
    private static final String JDBC_PASSWORD = "";

    private UserServices() {
    }

    public static UserServices getRama() {
        if (instance == null) {
            instance = new UserServices();
        }
        return instance;
    }

    // Método para obtener todos los usuarios desde la base de datos
    public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {

            while (rs.next()) {
                int id = rs.getInt("id"); // Verifica que los nombres de columna coincidan con la tabla
                String username = rs.getString("username");
                String name = rs.getString("name");
                String password = rs.getString("password");
                boolean isAdmin = rs.getBoolean("is_admin");

                // Crear el objeto User basado en los datos de la BD
                User user = new User(id, username, name, password, isAdmin);
                users.add(user);
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener los usuarios: " + e.getMessage());
            e.printStackTrace();  // Detalle del error para depurar
        }
        return users;
    }

    // Método para obtener un usuario por nombre de usuario
    public User getUserByUsername(String username) {
        User user = null;
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String password = rs.getString("password");
                    boolean isAdmin = rs.getBoolean("is_admin");

                    // Crear el objeto User basado en los datos de la BD
                    user = new User(id, username, name, password, isAdmin);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al obtener el usuario: " + e.getMessage());
            e.printStackTrace();
        }
        return user;
    }

    // Método para agregar un nuevo usuario a la base de datos
    public void addUser(User user) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = connection.prepareStatement(
                     "INSERT INTO users (username, name, password, is_admin) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getPassword());
            stmt.setBoolean(4, user.isAdmin());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1)); // Asignar el ID generado
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al agregar el usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método para eliminar un usuario por su ID
    public void deleteUser(int id) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
             PreparedStatement stmt = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar el usuario: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
