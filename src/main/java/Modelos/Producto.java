package Modelos;

public class Producto {
    private int idProducto;
    private String name;
    private double price;
    private int stock;

    // Constructor vacío (necesario para trabajar con frameworks y bibliotecas que requieren instanciación sin parámetros)
    public Producto() {
    }

    // Constructor para nuevos productos sin ID (cuando se crea un nuevo producto antes de agregarlo a la base de datos)
    public Producto(String name, double price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // Constructor con ID (para cuando se recupera el producto desde la base de datos)
    public Producto(int idProducto, String name, double price, int stock) {
        this.idProducto = idProducto;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // Getters y setters
    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // Sobrescribe el método equals para comparar productos por su ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Producto producto = (Producto) o;

        return idProducto == producto.idProducto;
    }

    @Override
    public int hashCode() {
        return idProducto;
    }
}
