package Servicios;

import Modelos.Compra;
import Modelos.Producto;
import Util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PurchaseServices {
    private static PurchaseServices instance;

    private PurchaseServices() {}

    public static PurchaseServices getInstance() {
        if (instance == null) {
            instance = new PurchaseServices();
        }
        return instance;
    }

    // Obtener todas las compras desde la base de datos
    public List<Compra> getAllPurchases() {
        List<Compra> purchases = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM compra")) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");

                // Aquí puedes agregar lógica para recuperar productos si están relacionados con la compra
                List<Producto> productos = getProductosForCompra(id);

                Compra compra = new Compra(id, username, productos);
                purchases.add(compra);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return purchases;
    }

    // Método para agregar una nueva compra a la base de datos
    public static void addPurchase(Compra compra) {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("INSERT INTO compra (username) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, compra.getUsername());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int compraId = generatedKeys.getInt(1);

                        // Agregar los productos relacionados con la compra
                        addProductosToCompra(compraId, compra.getProductos());
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Obtener los productos relacionados con una compra específica
    private List<Producto> getProductosForCompra(int compraId) {
        List<Producto> productos = new ArrayList<>();
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM producto_compra WHERE compra_id = ?")) {

            pstmt.setInt(1, compraId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int productoId = rs.getInt("producto_id");
                    Producto producto = getProductoById(productoId);
                    if (producto != null) {
                        productos.add(producto);
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productos;
    }

    // Método auxiliar para obtener un producto por su ID
    private Producto getProductoById(int productoId) {
        Producto producto = null;
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("SELECT * FROM producto WHERE id = ?")) {

            pstmt.setInt(1, productoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    producto = new Producto();
                    producto.setIdProducto(rs.getInt("id"));
                    producto.setName(rs.getString("name"));
                    producto.setPrice(rs.getDouble("price"));
                    producto.setStock(rs.getInt("stock"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return producto;
    }

    // Método para agregar productos relacionados a una compra
    private static void addProductosToCompra(int compraId, List<Producto> productos) {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement pstmt = connection.prepareStatement("INSERT INTO producto_compra (compra_id, producto_id) VALUES (?, ?)")) {

            for (Producto producto : productos) {
                pstmt.setInt(1, compraId);
                pstmt.setInt(2, producto.getIdProducto());
                pstmt.addBatch();
            }
            pstmt.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
