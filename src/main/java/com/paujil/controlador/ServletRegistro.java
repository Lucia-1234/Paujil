package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.Rol;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ServletRegistro — capa de seguridad mínima en backend.
 *
 * Responsabilidades tras la migración de validaciones al frontend:
 *
 *  1. Sanitización básica (null-check, trim, longitudes máximas).
 *  2. Verificación de tipos críticos (formato de fecha, resolución de enum Rol).
 *  3. Duplicados en BD (correo, teléfono) — imposible de verificar en cliente.
 *  4. Hash de contraseña y persistencia transaccional.
 *
 *
 * Estas reglas se ejecutan en el cliente antes del envío, pero se mantiene aquí
 * una guardia de seguridad contra peticiones maliciosas o manipuladas que
 * lleguen sin pasar por el formulario (ej. curl, Burp Suite, scripts).
 */
@WebServlet("/ServletRegistro")
public class ServletRegistro extends HttpServlet {

    // Límites de longitud máxima: defensa contra payloads oversized y ataques de BD
    private static final int MAX_NOMBRE    = 80;
    private static final int MAX_CORREO    = 120;
    private static final int MAX_TELEFONO  = 10;
    private static final int MAX_DIRECCION = 120;
    private static final int MAX_PASS      = 100;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── 1. Lectura de parámetros ──────────────────────────────────────────
        String nombre    = request.getParameter("txtNombre");
        String correo    = request.getParameter("txtEmail");
        String telefono  = request.getParameter("txtTelefono");
        String fechaNac  = request.getParameter("txtFechaNacimiento");
        String direccion = request.getParameter("txtDireccion");
        String rolStr    = request.getParameter("txtRol");
        String pass      = request.getParameter("txtContrasena");
        // La confirmación solo existe para validación del cliente; no se persiste ni se re-verifica aquí
        // porque una petición legítima que pasó por el formulario ya fue validada en JS.

        // ── 2. Sanitización: trim y normalización ─────────────────────────────
        // Estas operaciones son defensivas contra entradas manipuladas, no lógica de negocio.
        nombre    = sanitizar(nombre);
        correo    = sanitizar(correo);
        telefono  = sanitizar(telefono);
        fechaNac  = sanitizar(fechaNac);
        direccion = sanitizar(direccion);
        rolStr    = sanitizar(rolStr);
        pass      = (pass != null) ? pass.trim() : null;  // contraseña: solo trim, no toLowerCase

        if (correo != null) correo = correo.toLowerCase();
        if (rolStr != null) rolStr = rolStr.toLowerCase();

        // ── 3. Guardias de seguridad (anti-bypass) ────────────────────────────
        // Estas comprobaciones rechazan peticiones que no pasaron por el validador JS.
        // Son intencionalmente genéricas: no revelan cuál campo falló (previene enumeración).

        // 3a. Presencia de campos obligatorios
        if (estaVacioONulo(nombre)    || excedeLongitud(nombre,    MAX_NOMBRE)
         || estaVacioONulo(correo)    || excedeLongitud(correo,    MAX_CORREO)
         || estaVacioONulo(telefono)  || excedeLongitud(telefono,  MAX_TELEFONO)
         || estaVacioONulo(fechaNac)
         || estaVacioONulo(direccion) || excedeLongitud(direccion, MAX_DIRECCION)
         || estaVacioONulo(rolStr)
         || estaVacioONulo(pass)      || excedeLongitud(pass,      MAX_PASS)) {

            enviarError("Solicitud inválida. Verifica todos los campos.", request, response);
            return;
        }

        // 3b. Teléfono: solo dígitos (defensa contra inyecciones en el campo numérico)
        if (!telefono.matches("\\d{1,10}")) {
            enviarError("Solicitud inválida. Verifica todos los campos.", request, response);
            return;
        }

        // 3c. Resolución del enum Rol (falla si se envía un valor no reconocido)
        Rol rol;
        try {
            rol = Rol.desde(rolStr);
        } catch (IllegalArgumentException e) {
            // Valor de rol manipulado; puede indicar tampering del formulario
            enviarError("Solicitud inválida. Verifica todos los campos.", request, response);
            return;
        }

        // 3d. Parseo de fecha: Date.valueOf espera estrictamente "yyyy-MM-dd"
        java.sql.Date fechaSql;
        try {
            fechaSql = java.sql.Date.valueOf(fechaNac);
        } catch (IllegalArgumentException e) {
            enviarError("Solicitud inválida. Verifica todos los campos.", request, response);
            return;
        }

        // ── 4. Verificaciones que solo el servidor puede hacer ─────────────────
        // Estas NO pueden migrarse al cliente porque requieren consultar la BD.

        UsuarioDao dao = new UsuarioDao();

        if (dao.correoExiste(correo)) {
            // Mensaje específico aquí sí es válido: el usuario necesita saber que debe usar otro correo
            enviarError("El correo electrónico ya está registrado.", request, response);
            return;
        }

        if (dao.telefonoExiste(telefono)) {
            enviarError("El número de teléfono ya está registrado.", request, response);
            return;
        }

        // ── 5. Hash de contraseña y persistencia ──────────────────────────────
        // BCrypt: nunca se almacena la contraseña en texto plano
        String passHash = com.paujil.utils.Seguridad.encriptar(pass);

        boolean exito = dao.registrarUsuarioCompleto(
                nombre,
                correo,
                telefono,
                fechaSql,
                direccion,
                passHash,
                rol.getIdBd()
        );

        if (exito) {
            // El usuario se crea en estado 'Pendiente'; requiere aprobación del administrador
            response.sendRedirect(request.getContextPath()
                    + "/templates/login.jsp?registro=success");
        } else {
            enviarError("Error al guardar. Intenta nuevamente más tarde.", request, response);
        }
    }

    // ── Helpers privados ───────────────────────────────────────────────────────

    /**
     * Devuelve el valor con trim aplicado, o null si el parámetro es nulo.
     * No convierte case; cada llamador decide si necesita toLowerCase.
     */
    private String sanitizar(String valor) {
        return (valor != null) ? valor.trim() : null;
    }

    /** Verdadero si el valor es null o solo espacios (post-trim). */
    private boolean estaVacioONulo(String valor) {
        return valor == null || valor.isEmpty();
    }

    /** Verdadero si la longitud supera el límite máximo permitido. */
    private boolean excedeLongitud(String valor, int max) {
        return valor != null && valor.length() > max;
    }

    /**
     * Redirige al formulario con el mensaje de error y repuebla los campos.
     * Usa forward para conservar los valores ingresados en el scope de request.
     */
    private void enviarError(String mensaje, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        request.setAttribute("mensaje",   mensaje);
        request.setAttribute("nombre",    request.getParameter("txtNombre"));
        request.setAttribute("email",     request.getParameter("txtEmail"));
        request.setAttribute("telefono",  request.getParameter("txtTelefono"));
        request.setAttribute("direccion", request.getParameter("txtDireccion"));
        request.setAttribute("fecha",     request.getParameter("txtFechaNacimiento"));
        request.setAttribute("rol",       request.getParameter("txtRol"));

        request.getRequestDispatcher("/templates/registro_usuario.jsp")
               .forward(request, response);
    }
}