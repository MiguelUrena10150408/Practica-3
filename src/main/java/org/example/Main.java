package org.example;

import Controladores.CrudController;
import Modelos.Producto;
import Servicios.ProductServices;
import Servicios.UserServices;
import Util.DatabaseUtil;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import org.h2.tools.Server;

import jakarta.servlet.http.Cookie;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws SQLException {
        // Iniciar las instancias de servicios
        ProductServices.getInstance();
        UserServices.getRama();

        // Iniciar la consola H2
        startH2Console();

        // Iniciar la conexión a la base de datos
        try {
            DatabaseUtil.getConnection();  // Asegúrate de cerrar la conexión después de usarla
        } catch (SQLException e) {
            System.out.println("Error al conectar a la base de datos: " + e.getMessage());
        }

        // Configurar Javalin
        var app = Javalin.create(config -> {
            config.staticFiles.add(staticFile -> {
                staticFile.hostedPath = "/";
                staticFile.directory = "shoppy";
                staticFile.location = Location.CLASSPATH;
                staticFile.precompress = false;
                staticFile.aliasCheck = null;
            });

            // Configurar el sistema de plantilla por defecto
            config.fileRenderer(new JavalinThymeleaf());
        }).start(7070);

        // Rutas de la aplicación
        app.get("/hello", ctx -> {
            ctx.sessionAttribute("funda", (ProductServices.getCarrito() != null) ? ProductServices.countCarrito() : 0);
            ctx.attribute("name", "Javalin");
            ctx.render("shoppy/prueba2.html");
        });

        app.get("/", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("funda", (ProductServices.getCarrito() != null) ? ProductServices.countCarrito() : 0);
            ctx.render("shoppy/templates/index.html", modelo);
        });

        app.get("/addCart", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("products", ProductServices.getAllProducts());
            modelo.put("funda", (ProductServices.getCarrito() != null) ? ProductServices.countCarrito() : 0);
            ctx.render("shoppy/NuevosArchivos/NewProductos.html", modelo);
        });

        app.post("/addCart", ctx -> {
            List<String> productoName = ctx.formParams("productName");
            List<String> precio = ctx.formParams("price");
            List<String> quantity = ctx.formParams("quantity");
            for (int i = 0; i < productoName.size(); i++) {
                if (Integer.parseInt(quantity.get(i)) > 0) {
                    ProductServices.addCarrito(new Producto(productoName.get(i), Double.parseDouble(precio.get(i)), Integer.parseInt(quantity.get(i))));
                }
            }
            ctx.redirect("/");
        });

        app.get("/ProductCreateOrDelete", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("products", ProductServices.getAllProducts());
            modelo.put("funda", (ProductServices.getCarrito() != null) ? ProductServices.countCarrito() : 0);
            ctx.render("shoppy/NuevosArchivos/ProductCD.html", modelo);
        });

        app.post("/add-product", CrudController::procesarCreacionProducto);
        app.post("/delete-product", CrudController::procesarBorrarProducto);

        app.get("/UserList", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("users", UserServices.getAllUsers());
            modelo.put("funda", (ProductServices.getCarrito() != null) ? ProductServices.countCarrito() : 0);
            ctx.render("shoppy/NuevosArchivos/NewUserList.html", modelo);
        });

        // Implementación del login con sesión y cookies
        app.get("/login", CrudController::FormCrearUser);

        app.post("/login", ctx -> {
            String username = ctx.formParam("username");
            String password = ctx.formParam("password");
            String rememberMe = ctx.formParam("rememberMe"); // Recordar sesión

            if (username != null && password != null) {
                boolean isAdmin = username.equals("admin") && password.equals("admin");

                ctx.sessionAttribute("logeado", username);
                ctx.sessionAttribute("isAdmin", isAdmin);

                if ("on".equals(rememberMe)) {
                    // Crear cookie para recordar la sesión
                    Cookie cookie = new Cookie("user", username);
                    cookie.setMaxAge(7 * 24 * 60 * 60); // 7 días
                    cookie.setPath("/");
                    ctx.res().addCookie(cookie);
                }

                ctx.redirect("/");
            } else {
                ctx.status(401).result("Credenciales incorrectas");
            }
        });

        app.get("/cart", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            modelo.put("cart", ProductServices.getCarrito());
            ctx.render("shoppy/NuevosArchivos/Carrito.html", modelo);
        });

        app.post("/removeCarrito", CrudController::procesarBorrarProductoCarrito);

        app.get("/register", ctx -> {
            Map<String, Object> modelo = new HashMap<>();
            ctx.render("shoppy/NuevosArchivos/register.html", modelo);
        });

        app.post("/register", CrudController::procesarRegistro);

    }

    // Método para iniciar la consola H2
    private static void startH2Console() throws SQLException {
        try {
            Server.createTcpServer("-tcpAllowOthers", "-tcpPort", "9092", "-tcpDaemon").start();
            Server.createWebServer("-webPort", "8082", "-tcpAllowOthers").start();
            System.out.println("Consola H2 disponible en: http://localhost:8082");
        } catch (SQLException e) {
            System.out.println("Error al iniciar la consola H2: " + e.getMessage());
            throw e;
        }
    }
}
