package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.validador;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ServletRegistro")
public class ServletRegistro extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── 1. Recolección y saneamiento ──────────────────────────────────────
        String nombre    = request.getParameter("txtNombre");
        String correo    = request.getParameter("txtEmail");
        String telefono  = request.getParameter("txtTelefono");
        String fechaNac  = request.getParameter("txtFechaNacimiento");
        String direccion = request.getParameter("txtDireccion");
        String rolStr    = request.getParameter("txtRol");       // "trabajador" | "administrador"
        String pass      = request.getParameter("txtContrasena");
        String passConf  = request.getParameter("txtConfirmarContrasena");

        // Saneamiento básico
        if (correo   != null) correo   = correo.trim().toLowerCase();
        if (telefono != null) telefono = telefono.trim();
        if (rolStr   != null) rolStr   = rolStr.trim().toLowerCase();

        // ── 2. Validaciones ───────────────────────────────────────────────────

        // 2a. Campos obligatorios vacíos
        if (estaVacio(nombre) || estaVacio(correo) || estaVacio(telefono)
                || estaVacio(fechaNac) || estaVacio(direccion)
                || estaVacio(rolStr)   || estaVacio(pass)) {
            enviarError("Todos los campos son obligatorios.", request, response);
            return;
        }

        // 2b. Contraseñas coinciden
        if (!pass.equals(passConf)) {
            enviarError("Las contraseñas no coinciden.", request, response);
            return;
        }

        // 2c. Contraseña segura (delegado al validador existente)
        if (!validador.esContrasenaSegura(pass)) {
            enviarError("La contraseña no es segura (mínimo 8 caracteres, "
                    + "mayúsculas, minúsculas, número y símbolo).", request, response);
            return;
        }

        // 2d. Teléfono: exactamente 10 dígitos
        if (!telefono.matches("\\d{10}")) {
            enviarError("El teléfono debe tener exactamente 10 dígitos.", request, response);
            return;
        }

        // 2e. Convertir nombre de rol a id_rol numérico
        //     Debe coincidir con los datos reales de la tabla 'roles' en BD.
        //     Si cambias los roles, actualiza este mapeo.
        int idRol;
        switch (rolStr) {
            case "trabajador":     idRol = 1; break;
            case "administrador":  idRol = 2; break;
            default:
                enviarError("Rol no válido.", request, response);
                return;
        }

        // 2f. Fecha de nacimiento con formato válido
        java.sql.Date fechaSql;
        try {
            fechaSql = java.sql.Date.valueOf(fechaNac); // Espera "yyyy-MM-dd"
        } catch (IllegalArgumentException e) {
            enviarError("Formato de fecha inválido.", request, response);
            return;
        }

        // ── 3. Verificar duplicados antes de insertar ─────────────────────────
        UsuarioDao dao = new UsuarioDao();

        if (dao.correoExiste(correo)) {
            enviarError("El correo electrónico ya está registrado.", request, response);
            return;
        }
        if (dao.telefonoExiste(telefono)) {
            enviarError("El número de teléfono ya está registrado.", request, response);
            return;
        }

        // ── 4. Cifrar contraseña ANTES de persistir ───────────────────────────
        String passHash = com.paujil.utils.Seguridad.encriptar(pass);

        // ── 5. Persistir con transacción en el DAO ────────────────────────────
        boolean exito = dao.registrarUsuarioCompleto(
                nombre,
                correo,
                telefono,
                fechaSql,
                direccion,
                passHash,
                idRol
        );

        // ── 6. Respuesta ──────────────────────────────────────────────────────
        if (exito) {
            // El usuario queda en estado 'Pendiente'; el admin lo activará.
            response.sendRedirect(request.getContextPath()
                    + "/templates/login.jsp?registro=success");
        } else {
            enviarError("Error al guardar en base de datos. Intente nuevamente.", request, response);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void enviarError(String mensaje, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
                // Guardamos el mensaje de error
            request.setAttribute("mensaje", mensaje);

            // Guardamos los valores para que el usuario no tenga que volver a escribirlos
            request.setAttribute("nombre", request.getParameter("txtNombre"));
            request.setAttribute("email", request.getParameter("txtEmail"));
            request.setAttribute("telefono", request.getParameter("txtTelefono"));
            request.setAttribute("direccion", request.getParameter("txtDireccion"));
            request.setAttribute("fecha", request.getParameter("txtFechaNacimiento"));
            // ... repetir para los campos que quieras preservar

            request.getRequestDispatcher("/templates/registro_usuario.jsp")
                   .forward(request, response);
    }
}