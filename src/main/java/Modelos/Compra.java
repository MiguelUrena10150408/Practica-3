package Modelos;

import java.util.List;

public class Compra {
    private int id;  // Debería ser gestionado automáticamente por la base de datos.
    private String username;
    private List<Producto> productos;

    // Constructor para cuando la compra aún no tiene un ID asignado (nueva compra)
    public Compra(String username, List<Producto> productos) {
        this.username = username;
        this.productos = productos;
    }

    // Constructor con ID (por ejemplo, cuando se recupera una compra de la base de datos)
    public Compra(int id, String username, List<Producto> productos) {
        this.id = id;
        this.username = username;
        this.productos = productos;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}
