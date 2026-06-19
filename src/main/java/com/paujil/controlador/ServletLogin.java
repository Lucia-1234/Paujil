package com.paujil.controlador;

import com.paujil.utils.Seguridad;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import paujil.basedatos.clase_Conexion;

@WebServlet("/ServletLogin") // Registra este componente en el contenedor web para manejar las peticiones de inicio de sesión.
public class ServletLogin extends HttpServlet { // Define la clase como un servlet estándar para procesar solicitudes HTTP.

    @Override // Indica que sobreescribiremos el método doPost, diseñado para recibir datos sensibles vía POST.
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { // Define la firma del método que procesa el formulario.

        // Captura el correo, la contraseña y el rol desde los campos del formulario HTML.
        String correoInput     = request.getParameter("txtUsuario"); 
        String contrasenaInput = request.getParameter("txtContrasena"); 
        String rolSeleccionado = request.getParameter("txtRol"); 

        // Sanitización: elimina espacios accidentales y normaliza el correo a minúsculas para evitar inconsistencias en la base de datos.
        if (correoInput     != null) correoInput     = correoInput.trim().toLowerCase(); 
        // Normaliza solo espacios en la contraseña; se mantiene el case para no alterar el valor del hash original.
        if (contrasenaInput != null) contrasenaInput = contrasenaInput.trim(); 
        // Normaliza el rol seleccionado para asegurar una comparación de texto precisa.
        if (rolSeleccionado != null) rolSeleccionado = rolSeleccionado.trim().toLowerCase(); 

        // Declaración de consulta SQL utilizando JOINs para consolidar datos de usuario, roles y contactos en una sola llamada.
        // La consulta es deliberadamente selectiva, filtrando por estado 'Activo' antes de cualquier validación.
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena_usuario, u.estado_usuario, r.nombre_rol, c.direccion_correo, t.numero_telefono " +
                     "FROM usuarios u " +
                     "INNER JOIN correos c ON u.id_usuario = c.id_usuario " +
                     "INNER JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                     "INNER JOIN roles r ON ur.id_rol = r.id_rol " +
                     "LEFT JOIN telefonos t ON u.id_usuario = t.id_usuario " +
                     "WHERE TRIM(LOWER(c.direccion_correo)) = ? " +
                     "AND TRIM(LOWER(r.nombre_rol)) = ? " +
                     "AND u.estado_usuario = 'Activo'";

        // Try-with-resources garantiza que la conexión y el statement se cierren automáticamente, liberando memoria del servidor.
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asignación de parámetros posicionales: esto desactiva cualquier intento de Inyección SQL.
            ps.setString(1, correoInput); 
            ps.setString(2, rolSeleccionado); 

            // Ejecuta la consulta preparada y almacena el resultado en un ResultSet.
            try (ResultSet rs = ps.executeQuery()) {
                // Si rs.next() es true, encontramos un usuario activo que coincide con el correo y el rol.
                if (rs.next()) {
                    // Recupera el hash de contraseña almacenado en BD (nunca se compara texto plano).
                    String hashEnBD = rs.getString("contrasena_usuario");

                    // Utiliza BCrypt para verificar si la contraseña ingresada coincide con el hash almacenado en memoria.
                    if (Seguridad.verificar(contrasenaInput, hashEnBD)) {

                        // Seguridad: invalidamos cualquier sesión existente para prevenir ataques de "Session Fixation".
                        HttpSession oldSession = request.getSession(false); 
                        if (oldSession != null) { 
                            oldSession.invalidate(); // Elimina la sesión antigua.
                        }
                        // Crea una sesión totalmente nueva y regenera el ID de sesión.
                        HttpSession session = request.getSession(true); 

                        // Almacena los datos de perfil en la sesión para persistir la identidad durante la navegación.
                        session.setAttribute("idUsuario",       rs.getInt("id_usuario")); 
                        session.setAttribute("nombreUsuario",   rs.getString("nombre_usuario")); 
                        session.setAttribute("rolUsuario",      rs.getString("nombre_rol")); 
                        session.setAttribute("telefonoUsuario", rs.getString("numero_telefono")); 

                        // Lógica de enrutamiento: redirige al usuario al panel correcto según su rol.
                        if ("administrador".equalsIgnoreCase(rs.getString("nombre_rol"))) {
                            response.sendRedirect(request.getContextPath() + "/templates/administrador/menu_administrador.jsp"); 
                        } else {
                            response.sendRedirect(request.getContextPath() + "/templates/trabajador/menu_trabajador.jsp"); 
                        }
                    } else {
                        // Credenciales incorrectas: redirige al login con error genérico para no dar pistas de qué falló.
                        response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=1"); 
                    }
                } else {
                    // Usuario no encontrado o inactivo: error genérico por seguridad.
                    response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=1"); 
                }
            }
        } catch (Exception e) {
            // Manejo de errores: logueamos internamente el fallo y notificamos al usuario de un error genérico (fatal).
            e.printStackTrace(); 
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=fatal"); 
        }
    }
}