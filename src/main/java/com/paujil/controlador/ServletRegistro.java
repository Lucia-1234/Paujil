package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.Rol;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ServletRegistro") // Mapea la ruta del servidor para procesar formularios de registro de nuevos usuarios.
public class ServletRegistro extends HttpServlet { // Define la clase como un componente web para el manejo del registro.

    // Constantes de seguridad: establecen límites estrictos para prevenir ataques de denegación de servicio por carga excesiva.
    private static final int MAX_NOMBRE    = 80;
    private static final int MAX_CORREO    = 120;
    private static final int MAX_TELEFONO  = 10;
    private static final int MAX_DIRECCION = 120;
    private static final int MAX_PASS      = 100;
    
    @Override // Sobrescribe el método POST, punto de entrada para los envíos de datos del formulario.
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── 1. Lectura de parámetros ──────────────────────────────────────────
        // Recoge los datos brutos enviados desde el formulario HTML hacia el servidor.
        String nombre    = request.getParameter("txtNombre");
        String correo    = request.getParameter("txtEmail");
        String telefono  = request.getParameter("txtTelefono");
        String fechaNac  = request.getParameter("txtFechaNacimiento");
        String direccion = request.getParameter("txtDireccion");
        String rolStr    = request.getParameter("txtRol");
        String pass      = request.getParameter("txtContrasena");

        // ── 2. Sanitización defensiva ─────────────────────────────────────────
        // Limpiamos espacios en blanco accidentales y normalizamos para evitar duplicados por formato.
        nombre    = sanitizar(nombre);
        correo    = sanitizar(correo);
        telefono  = sanitizar(telefono);
        fechaNac  = sanitizar(fechaNac);
        direccion = sanitizar(direccion);
        rolStr    = sanitizar(rolStr);
        pass      = (pass != null) ? pass.trim() : null;  // Mantenemos la contraseña con su formato original (case-sensitive).

        if (correo != null) correo = correo.toLowerCase(); // Normalizamos a minúsculas para comparaciones únicas en BD.
        if (rolStr != null) rolStr = rolStr.toLowerCase(); // Normalizamos roles para evitar errores de coincidencia.

        // ── 3. Guardias de seguridad (Anti-Bypass) ────────────────────────────
        // Verificación de campos obligatorios y límites máximos. Si algo falta, el registro se rechaza de forma genérica.
        if (estaVacioONulo(nombre)    || excedeLongitud(nombre,   MAX_NOMBRE)
          || estaVacioONulo(correo)    || excedeLongitud(correo,   MAX_CORREO)
          || estaVacioONulo(telefono)  || excedeLongitud(telefono, MAX_TELEFONO)
          || estaVacioONulo(fechaNac)
          || estaVacioONulo(direccion) || excedeLongitud(direccion, MAX_DIRECCION)
          || estaVacioONulo(rolStr)
          || estaVacioONulo(pass)      || excedeLongitud(pass,     MAX_PASS)) {

            enviarError("Solicitud inválida. Verifica todos los campos.", request, response); // Rechazo de seguridad para peticiones anómalas.
            return; // Detiene la ejecución.
        }
        
        // 3b'. Validación estricta mediante RegEx: asegura que el nombre solo contenga letras y espacios.
        if (!nombre.matches("^[A-Za-zÁÉÍÓÚÑÜáéíóúñü\\s]+$")) {
            enviarError("Solicitud inválida. Verifica todos los campos.", request, response);
            return;
        }

        // 3b. Validación estricta de teléfono: solo permite dígitos para evitar inyecciones en la capa de datos.
        if (!telefono.matches("\\d{1,10}")) {
            enviarError("Solicitud inválida. Verifica todos los campos.", request, response);
            return;
        }

        // 3c. Resolución de Roles: asegura que solo se asignen roles definidos en el sistema (evita tampering).
        Rol rol;
        try {
            rol = Rol.desde(rolStr);
        } catch (IllegalArgumentException e) {
            enviarError("Solicitud inválida. Verifica todos los campos.", request, response);
            return;
        }

        // 3d. Parseo defensivo de fecha: asegura que el formato sea "yyyy-MM-dd" antes de persistir.
        java.sql.Date fechaSql;
        try {
            fechaSql = java.sql.Date.valueOf(fechaNac);
        } catch (IllegalArgumentException e) {
            enviarError("Solicitud inválida. Verifica todos los campos.", request, response);
            return;
        }

        // ── 4. Verificaciones de lógica de negocio (BD) ──────────────────────
        UsuarioDao dao = new UsuarioDao();

        // Verificamos duplicidad en BD: el correo debe ser único para cada usuario.
        if (dao.correoExiste(correo)) {
            enviarError("El correo electrónico ya está registrado.", request, response);
            return;
        }

        // Verificamos duplicidad de teléfono para mantener la integridad de los datos de contacto.
        if (dao.telefonoExiste(telefono)) {
            enviarError("El número de teléfono ya está registrado.", request, response);
            return;
        }

        // ── 5. Hash de contraseña y persistencia ──────────────────────────────
        // Usamos BCrypt para transformar la contraseña en un hash irreversible antes de guardarla.
        String passHash = com.paujil.utils.Seguridad.encriptar(pass);

        // Registro transaccional en base de datos.
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
            // El usuario queda en estado 'Pendiente' hasta que el administrador lo apruebe.
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?registro=success");
        } else {
            enviarError("Error al guardar. Intenta nuevamente más tarde.", request, response);
        }
    }

    // --- Helpers de Sanitización ---
    private String sanitizar(String valor) { return (valor != null) ? valor.trim() : null; }
    private boolean estaVacioONulo(String valor) { return valor == null || valor.isEmpty(); }
    private boolean excedeLongitud(String valor, int max) { return valor != null && valor.length() > max; }

    private void enviarError(String mensaje, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Redirige al registro conservando los datos ingresados para mejorar la experiencia de usuario.
        request.setAttribute("mensaje", mensaje);
        request.setAttribute("nombre", request.getParameter("txtNombre"));
        request.setAttribute("email", request.getParameter("email"));
        request.getRequestDispatcher("/templates/registro_usuario.jsp").forward(request, response);
    }
}