package Controladores;

import Modelos.Compra;
import Modelos.Producto;
import Modelos.User;
import Servicios.ProductServices;
import Servicios.PurchaseServices;
import Servicios.UserServices;
import io.javalin.http.Context;
import org.jasypt.util.text.BasicTextEncryptor;

import jakarta.servlet.http.Cookie;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrudController {
    private static UserServices userServices = UserServices.getRama();
    private static ProductServices productServices = ProductServices.getInstance();
    private static PurchaseServices purchaseServices = PurchaseServices.getInstance();

    // Inicializar encriptador de Jasypt
    private static BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
    static {
        textEncryptor.setPasswordCharArray("clave-secreta".toCharArray()); // Puedes usar una clave más segura aquí
    }

    // Listar todos los usuarios
    public static void ListarUsuarios(Context ctx) throws Exception {
        List<User> userList = userServices.getAllUsers();
        ctx.json(userList);
    }

    // Listar todos los productos
    public static void listarProductos(Context ctx) throws Exception {
        List<Producto> productoLista = productServices.getAllProducts();
        ctx.json(productoLista);
    }

    // Listar todas las compras
    public static void listarCompras(Context ctx) throws Exception {
        List<Compra> purchaseLista = purchaseServices.getAllPurchases();
        ctx.json(purchaseLista);
    }

    // Mostrar formulario de creación de usuarios
    public static void FormCrearUser(Context ctx) throws Exception {
        Map<String, Object> modelo = new HashMap<>();
        modelo.put("titulo", "Formulario Creación Usuario");
        modelo.put("accion", "/addCart");
        modelo.put("funda", (ProductServices.getCarrito() != null) ? ProductServices.countCarrito() : 0);

        ctx.render("/shoppy/NuevosArchivos/authentication-register.html", modelo);
    }

    // Procesar la creación de un nuevo usuario
    public static void procesarCreacionUser(Context ctx) throws Exception {
        String name = ctx.formParam("name");
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");

        User temp = new User(username, name, password);
        userServices.addUser(temp);

        ctx.redirect("/templates/index.html");
    }

    // Manejar el proceso de login con cookies encriptadas
    public static void login(Context ctx) throws Exception {
        String username = ctx.formParam("username");
        String password = ctx.formParam("password");
        String rememberMe = ctx.formParam("rememberMe");

        User user = userServices.getUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            // Guardar el usuario en la sesión
            ctx.sessionAttribute("user", user);

            // Si el usuario marcó "Recordarme", crear una cookie encriptada
            if ("on".equals(rememberMe)) {
                String encryptedUsername = textEncryptor.encrypt(username);
                Cookie cookie = new Cookie("user", encryptedUsername);
                cookie.setMaxAge(7 * 24 * 60 * 60); // Duración de una semana en segundos
                cookie.setPath("/");
                ctx.res().addCookie(cookie);
            }

            ctx.redirect("/templates/index.html");
        } else {
            ctx.status(401).result("Credenciales incorrectas");
        }
    }

    // Verificar si el usuario está logueado mediante la cookie encriptada
    public static void checkLogin(Context ctx) {
        Cookie[] cookies = ctx.req().getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("user")) {
                    try {
                        // Desencriptar la cookie
                        String decryptedUsername = textEncryptor.decrypt(cookie.getValue());
                        User user = userServices.getUserByUsername(decryptedUsername);
                        if (user != null) {
                            ctx.sessionAttribute("user", user);
                        }
                    } catch (Exception e) {
                        // Si falla la desencriptación, eliminar la cookie
                        Cookie invalidCookie = new Cookie("user", null);
                        invalidCookie.setMaxAge(0); // Expira inmediatamente
                        invalidCookie.setPath("/");
                        ctx.res().addCookie(invalidCookie);
                    }
                }
            }
        }
    }

    // Procesar la creación de productos
    public static void procesarCreacionProducto(Context ctx) throws Exception {
        String nombre = ctx.formParam("productNombre");
        double precio = Double.parseDouble(ctx.formParam("price"));
        int stock = Integer.parseInt(ctx.formParam("stock"));

        Producto temp = new Producto(nombre, precio, stock);
        ProductServices.addProduct(temp);
        ctx.redirect("/templates/index.html");
    }

    // Procesar la creación de facturas
    public static void procesarCreacionFactura(Context ctx) throws Exception {
        String nombre = ctx.formParam("nombre");
        List<Producto> factura = productServices.getCarrito();

        Compra temp = new Compra(nombre, factura);
        PurchaseServices.addPurchase(temp);
        ctx.redirect("/");
    }

    // Procesar el borrado de un producto
    public static void procesarBorrarProducto(Context ctx) throws Exception {
        String nombre = ctx.formParam("productNombre");
        productServices.deleteProduct(nombre);
        ctx.redirect("/");
    }

    // Procesar el borrado de un producto del carrito
    public static void procesarBorrarProductoCarrito(Context ctx) throws Exception {
        String nombre = ctx.formParam("productNombre");
        productServices.deleteFromCarrito(nombre);
        ctx.redirect("/");
    }

    // Procesar el logout (cerrar sesión y eliminar cookie encriptada)
    public static void logout(Context ctx) {
        ctx.req().getSession().invalidate(); // Invalida la sesión

        // Elimina la cookie encriptada de usuario
        Cookie cookie = new Cookie("user", null);
        cookie.setMaxAge(0); // Expira inmediatamente
        cookie.setPath("/");
        ctx.res().addCookie(cookie);

        ctx.redirect("/login.html");
    }

    public static void procesarRegistro(Context ctx) throws Exception {
        String username = ctx.formParam("username");
        String name = ctx.formParam("name");
        String password = ctx.formParam("password");

        // Verifica si el nombre de usuario ya existe
        User existingUser = UserServices.getRama().getUserByUsername(username);
        if (existingUser == null) {
            // Crear el nuevo usuario y guardarlo en la base de datos
            User newUser = new User(username, name, password);
            UserServices.getRama().addUser(newUser);
            ctx.redirect("/login");  // Redirigir al login después del registro
        } else {
            ctx.status(400).result("El nombre de usuario ya existe");
        }
    }

}
