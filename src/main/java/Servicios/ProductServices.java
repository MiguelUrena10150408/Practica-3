package Servicios;

import Modelos.Producto;
import Util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductServices {
    private static ProductServices instance;
    private static List<Producto> Carrito = new ArrayList<>();

    private ProductServices() {
        // Los productos ahora se gestionan completamente desde la base de datos
    }

    public static ProductServices getInstance() {
        if (instance == null) {
            instance = new ProductServices();
        }
        return instance;
    }

    // Obtener todos los productos desde la base de datos
    public static List<Producto> getAllProducts() {
        List<Producto> products = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM producto")) {

            while (rs.next()) {
                Producto product = new Producto();
                product.setIdProducto(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                product.setStock(rs.getInt("stock"));
                products.add(product);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    // Agregar un producto nuevo a la base de datos, validar si el producto ya existe
    public static void addProduct(Producto producto) {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement("SELECT * FROM producto WHERE name = ?");
             PreparedStatement pstmt = connection.prepareStatement("INSERT INTO producto (name, price, stock) VALUES (?, ?, ?)")) {

            checkStmt.setString(1, producto.getName());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("El producto ya existe en la base de datos");
                    return;  // Si el producto ya existe, no lo agregamos
                }
            }

            // Agregar el producto si no existe
            pstmt.setString(1, producto.getName());
            pstmt.setDouble(2, producto.getPrice());
            pstmt.setInt(3, producto.getStock());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtener el carrito (memoria)
    public static List<Producto> getCarrito() {
        return Carrito;
    }

    // Agregar un producto al carrito
    public static void addCarrito(Producto producto) {
        if (Carrito.stream().noneMatch(p -> p.getName().equals(producto.getName()))) {
            Carrito.add(producto);
        }
    }

    // Contar los elementos en el carrito
    public static int countCarrito() {
        return Carrito.size();
    }

    // Actualizar un producto en la base de datos
    public static void updateProduct(int id, Producto updatedProduct) {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("UPDATE producto SET name = ?, price = ?, stock = ? WHERE id = ?")) {

            pstmt.setString(1, updatedProduct.getName());
            pstmt.setDouble(2, updatedProduct.getPrice());
            pstmt.setInt(3, updatedProduct.getStock());
            pstmt.setInt(4, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtener el ID del último producto (máximo id en la tabla producto)
    public static int getLastProduct() {
        int lastId = 0;
        try (Connection connection = DatabaseUtil.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(id) AS lastId FROM producto")) {

            if (rs.next()) {
                lastId = rs.getInt("lastId");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastId;
    }

    // Eliminar un producto de la base de datos por su nombre
    public static void deleteProduct(String name) {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("DELETE FROM producto WHERE name = ?")) {

            pstmt.setString(1, name);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Eliminar un producto del carrito en memoria
    public static void deleteFromCarrito(String name) {
        Carrito.removeIf(product -> product.getName().equals(name));
    }
}
