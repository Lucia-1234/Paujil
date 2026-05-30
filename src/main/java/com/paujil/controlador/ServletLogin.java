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

@WebServlet("/ServletLogin")
public class ServletLogin extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lee las credenciales y el rol enviados desde el formulario de login
        String correoInput     = request.getParameter("txtUsuario");
        String contrasenaInput = request.getParameter("txtContrasena");
        // El rol seleccionado en el formulario restringe la busqueda a un perfil especifico
        String rolSeleccionado = request.getParameter("txtRol");

        // trim() elimina espacios accidentales; toLowerCase() garantiza comparacion sin importar mayusculas
        if (correoInput     != null) correoInput     = correoInput.trim().toLowerCase();
        // La contrasena solo se normaliza en espacios; no se convierte a minusculas para respetar el hash
        if (contrasenaInput != null) contrasenaInput = contrasenaInput.trim();
        if (rolSeleccionado != null) rolSeleccionado = rolSeleccionado.trim().toLowerCase();

        // La contrasena se excluye del WHERE a proposito: bcrypt requiere verificacion en memoria,
        // no comparacion directa en BD, lo que ademas previene timing attacks
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena_usuario, u.estado_usuario, r.nombre_rol, c.direccion_correo, t.numero_telefono " +
                     "FROM usuarios u " +
                     // INNER JOIN garantiza que solo usuarios con correo registrado sean candidatos
                     "INNER JOIN correos c ON u.id_usuario = c.id_usuario " +
                     "INNER JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                     // INNER JOIN al rol filtra en BD usuarios sin el rol solicitado
                     "INNER JOIN roles r ON ur.id_rol = r.id_rol " +
                     // LEFT JOIN permite que el telefono sea opcional sin excluir al usuario del resultado
                     "LEFT JOIN telefonos t ON u.id_usuario = t.id_usuario " +
                     // TRIM/LOWER en BD espeja la normalizacion del input para garantizar coincidencia exacta
                     "WHERE TRIM(LOWER(c.direccion_correo)) = ? " +
                     "AND TRIM(LOWER(r.nombre_rol)) = ? " +
                     // Filtra usuarios inactivos o pendientes antes de llegar a la verificacion de contrasena
                     "AND u.estado_usuario = 'Activo'";

        // try-with-resources cierra conexion y statement automaticamente aunque ocurra una excepcion
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Parametros posicionales previenen inyeccion SQL al tratar los valores como literales
            ps.setString(1, correoInput);
            ps.setString(2, rolSeleccionado);

            try (ResultSet rs = ps.executeQuery()) {
                // rs.next() avanza al primer resultado; si no hay filas, las credenciales no coinciden
                if (rs.next()) {
                    // El hash se extrae de BD para compararlo en memoria; nunca se loguea ni se transmite
                    String hashEnBD = rs.getString("contrasena_usuario");

                    // BCrypt recomputa el hash del input y lo compara con el almacenado;
                    // su coste computacional configurable lo hace resistente a fuerza bruta
                    if (Seguridad.verificar(contrasenaInput, hashEnBD)) {

                        // Invalida la sesion previa para prevenir session fixation attacks
                        HttpSession oldSession = request.getSession(false);
                        if (oldSession != null) {
                            // Destruye el ID de sesion anterior antes de crear uno nuevo
                            oldSession.invalidate();
                        }
                        // true fuerza la creacion de una sesion nueva con ID regenerado
                        HttpSession session = request.getSession(true);

                        // Almacena los datos de identidad que filtros y servlets consultaran
                        // en cada solicitud para autorizar o denegar acceso a recursos protegidos
                        session.setAttribute("idUsuario",       rs.getInt("id_usuario"));
                        session.setAttribute("nombreUsuario",   rs.getString("nombre_usuario"));
                        // rolUsuario es el atributo clave que consultan los filtros de seguridad
                        session.setAttribute("rolUsuario",      rs.getString("nombre_rol"));
                        session.setAttribute("telefonoUsuario", rs.getString("numero_telefono"));

                        // Enrutamiento por rol: cada perfil tiene su propio espacio de trabajo
                        if ("administrador".equalsIgnoreCase(rs.getString("nombre_rol"))) {
                            response.sendRedirect(request.getContextPath() + "/templates/administrador/menu_administrador.jsp");
                        } else {
                            response.sendRedirect(request.getContextPath() + "/templates/trabajador/menu_trabajador.jsp");
                        }
                    } else {
                        // si el correo existe en el sistema (previene enumeracion de usuarios)
                        response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=1");
                    }
                } else {
                    // Usuario inexistente, inactivo o con rol incorrecto; error generico intencional
                    response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=1");
                }
            }
        } catch (Exception e) {
            // Loguea el detalle tecnico internamente sin exponerlo al usuario;
            // "error=fatal" senala un fallo de infraestructura, distinto al de credenciales
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=fatal");
        }
    }
}