package com.paujil.utils;

import com.paujil.modelo.Rol;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

public final class ServletUtils {

    private ServletUtils() {}

    // Valida si un campo de formulario llego vacio, nulo o solo con espacios; previene procesar datos invalidos antes de llegar al DAO
    public static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    // Extrae y sanea un parametro HTTP: retorna null si es ausente o en blanco, y elimina espacios extremos si tiene contenido util
    public static String obtenerParam(HttpServletRequest req, String nombre) {
        String v = req.getParameter(nombre);
        // Retorna el valor limpio solo si existe y tiene contenido real; null evita que el Servlet procese cadenas vacias como valores validos
        return (v != null && !v.isBlank()) ? v.trim() : null;
    }

    // Parsea un parametro HTTP a Integer de forma segura; Optional.empty() senaliza ausencia o formato invalido sin lanzar excepcion al Servlet llamador
    public static Optional<Integer> obtenerParamEntero(HttpServletRequest req, String nombre) {
        try {
            return Optional.of(Integer.parseInt(req.getParameter(nombre)));
        } catch (Exception e) {
            // Captura NumberFormatException y NullPointerException; retorna vacio en lugar de propagar el error al flujo principal del Servlet
            return Optional.empty();
        }
    }

    // Convierte un String con formato ISO (yyyy-MM-dd) a java.sql.Date; Optional.empty() protege contra fechas mal formadas sin interrumpir el Servlet
    public static Optional<java.sql.Date> parsearFecha(String fechaStr) {
        try {
            // valueOf lanza IllegalArgumentException si el formato no es exactamente yyyy-MM-dd, lo que captura el catch
            return Optional.of(java.sql.Date.valueOf(fechaStr));
        } catch (Exception e) {
            // Retorna vacio para que el Servlet pueda mostrar un error de validacion sin necesidad de manejar una excepcion
            return Optional.empty();
        }
    }

    // Fachada semantica que verifica sesion activa y exige especificamente el rol ADMINISTRADOR; simplifica la guardia de seguridad en Servlets de administracion
    public static boolean verificarSesionAdmin(HttpServletRequest req,
                                               HttpServletResponse res) throws IOException {
        return verificarSesion(req, res, Rol.ADMINISTRADOR);
    }

    // Fachada que verifica unicamente que exista una sesion activa sin importar el rol; usada en recursos accesibles a cualquier usuario autenticado
    public static boolean verificarSesionUsuario(HttpServletRequest req,
                                                 HttpServletResponse res) throws IOException {
        // Pasa null como rolRequerido para omitir la verificacion de rol en el metodo central
        return verificarSesion(req, res, null);
    }

    // Metodo central de control de acceso: verifica sesion activa y opcionalmente valida el rol; redirige al login si la sesion es invalida o el rol no coincide
    public static boolean verificarSesion(HttpServletRequest req,
                                          HttpServletResponse res,
                                          Rol rolRequerido) throws IOException {
        // false impide crear una sesion nueva si no existe; evitar crear sesiones vacias es critico para no falsear la validacion
        HttpSession session = req.getSession(false);
        // La sesion es valida si existe y contiene el atributo idUsuario que se asigna solo tras autenticacion exitosa
        boolean valida = session != null && session.getAttribute("idUsuario") != null;

        // Solo verifica el rol si la sesion base es valida y el recurso exige un rol especifico
        if (valida && rolRequerido != null) {
            // Recupera el nombre del rol almacenado en sesion durante el login del usuario
            String rolSesion = (String) session.getAttribute("rolUsuario");
            // Compara ignorando mayusculas para tolerar variaciones de capitalizacion entre la BD y el enum
            valida = rolRequerido.getNombre().equalsIgnoreCase(rolSesion);
        }

        // Si la sesion es invalida o el rol no coincide, redirige al login con parametro de error para mostrar mensaje en la vista
        if (!valida) {
            res.sendRedirect(req.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
        }
        return valida;
    }

    // Centraliza el reenvio a una vista con mensaje de error; preserva valores del formulario para que el usuario no los pierda al corregir un campo invalido
    public static void reenviarConError(String mensaje, String vista,
                                        HttpServletRequest req, HttpServletResponse res,
                                        String... camposAPreservar)
            throws ServletException, IOException {
        // Inyecta el mensaje de error como atributo de request para que la JSP destino lo muestre en pantalla
        req.setAttribute("mensajeError", mensaje);
        // Itera los nombres de campos que deben repoblarse; permite que el formulario en la JSP muestre los valores ya ingresados por el usuario
        for (String campo : camposAPreservar) {
            // Transfiere cada parametro del request como atributo para que la JSP lo recupere con ${campo} sin nueva solicitud HTTP
            req.setAttribute(campo, req.getParameter(campo));
        }
        // Reenvio interno al Servlet contenedor sin redireccion HTTP; preserva el request original con todos los atributos inyectados
        req.getRequestDispatcher(vista).forward(req, res);
    }
}