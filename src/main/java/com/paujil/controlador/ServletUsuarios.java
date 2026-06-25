package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;


/**
 * ServletUsuarios: Controlador encargado de la gestión administrativa de cuentas de usuario.
 * Su rol es recibir peticiones, validar permisos y coordinar con UsuarioDao para persistir cambios.
 */
@WebServlet("/ServletUsuario") // Mapea este controlador a la URL "/ServletUsuario".
public class ServletUsuarios extends HttpServlet { // Extiende HttpServlet para procesar protocolos web.

    // ── Guarda de sesión ──────────────────────────────────────────────────────────

    /**
     * Valida si el usuario actual tiene permisos de administrador.
     * @return true si la sesión es válida y el usuario es administrador.
     */
    private boolean sesionAdminValida(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession session = req.getSession(false); // Recupera la sesión sin crear una nueva.
        // Valida: sesión no nula, ID presente y rol específicamente "administrador".
        if (session == null
                || session.getAttribute("idUsuario") == null
                || !"administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario"))) {
            // Si la validación falla, redirige al login con un error.
            res.sendRedirect(req.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
            return false; // Retorna falso para detener la ejecución en el método que invoca a este.
        }
        return true; // Acceso autorizado.
    }

    // ── GET: Procesamiento de lecturas y acciones mediante URL ───────────────────
    @Override 
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (!sesionAdminValida(req, res)) return; // Filtro de seguridad inicial.

        String accion = req.getParameter("accion"); // Obtiene el parámetro para la lógica de bifurcación.
        String idStr  = req.getParameter("id"); // Obtiene el ID del usuario objetivo.
        UsuarioDao dao = new UsuarioDao(); // Instancia el DAO para operaciones de BD.

        if (accion != null) { // Si hay una acción definida, entra al selector.
            switch (accion) { // Estructura de control basada en la acción recibida.

                case "activar": // Caso: cambia estado de usuario a 'Activo'.
                    cambiarEstado(dao, idStr, "Activo", res, req);
                    return;

                case "desactivar": // Caso: cambia estado a 'Inactivo'.
                    cambiarEstado(dao, idStr, "Inactivo", res, req);
                    return;

                case "aprobar": // Caso: aprueba registro pendiente a 'Activo'.
                    cambiarEstado(dao, idStr, "Activo", res, req);
                    return;

                case "denegar": // Caso: elimina registro pendiente.
                    eliminar(dao, idStr, res, req);
                    return;

                case "eliminar": // Caso: elimina usuario activo/inactivo.
                    eliminar(dao, idStr, res, req);
                    return;

                case "pendientes": // Caso: filtra solo usuarios pendientes.
                    List<usuario> pendientes = dao.listarUsuariosPendientes(); // Llama al DAO para obtener pendientes.
                    req.setAttribute("listaUsuarios", pendientes); // Carga la lista en el request.
                    req.setAttribute("vista", "pendientes"); // Define la vista para el JSP.
                    req.getRequestDispatcher("/templates/administrador/gestion_usuarios.jsp")
                       .forward(req, res); // Envía a la vista.
                    return;

                default: break; // Si no coincide, continua hacia la carga por defecto.
            }
        }

        // Vista por defecto: carga la lista completa de usuarios.
        List<usuario> todos = dao.listarTodosLosUsuarios(); // Obtiene todos los usuarios.
        req.setAttribute("listaUsuarios", todos); // Envía los datos.
        req.setAttribute("vista", "todos"); // Define la vista como general.
        req.getRequestDispatcher("/templates/administrador/gestion_usuarios.jsp")
           .forward(req, res); // Despacha la petición a la página JSP.
    }

    // ── POST: Actualización de datos mediante formularios ───────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (!sesionAdminValida(req, res)) return; // Revalida acceso para métodos de escritura.

        req.setCharacterEncoding("UTF-8"); // Define codificación para caracteres especiales.

        String idStr  = req.getParameter("id"); // Obtiene el ID del usuario.
        String estado = req.getParameter("estadoUsuario"); // Obtiene el nuevo estado.

        if (idStr == null || idStr.isBlank()) { // Validación de presencia del ID.
            res.sendRedirect("ServletUsuario?status=error"); // Redirección por datos incompletos.
            return;
        }

        try {
            int id = Integer.parseInt(idStr); // Convierte a entero.
            UsuarioDao dao = new UsuarioDao(); // Prepara acceso a datos.
            boolean ok = false; // Inicializa bandera de operación.

            if (estado != null && !estado.isBlank()) { // Valida si el estado es válido.
                ok = dao.actualizarEstado(id, estado.trim()); // Ejecuta actualización en BD.
            }

            res.sendRedirect("ServletUsuario?status=" + (ok ? "success" : "error")); // Retorno con estado.
        } catch (NumberFormatException e) { // Captura error si el ID no es numérico.
            res.sendRedirect("ServletUsuario?status=error"); // Redirección por error de formato.
        }
    }

    // ── Helpers: Métodos privados reutilizables ─────────────────────────────────

    // Centraliza la lógica de cambio de estado para evitar repetir código en cada case.
    private void cambiarEstado(UsuarioDao dao, String idStr, String nuevoEstado,
                               HttpServletResponse res, HttpServletRequest req)
            throws IOException {
        if (idStr != null && !idStr.isBlank()) { // Verifica ID.
            try {
                dao.actualizarEstado(Integer.parseInt(idStr), nuevoEstado); // Invoca DAO.
            } catch (NumberFormatException ignored) { } // Ignora si hay error en el ID.
        }
        // Determina la redirección basándose en la vista donde estaba el admin.
        String vista = req.getParameter("vista");
        String redirect = "pendientes".equals(vista)
                ? "ServletUsuario?accion=pendientes&status=success"
                : "ServletUsuario?status=success";
        res.sendRedirect(redirect); // Ejecuta la redirección.
    }

    // Centraliza la lógica de eliminación para asegurar consistencia en la respuesta.
    //Recibe el objeto dao (para interactuar con la base de datos), el idStr (el ID del usuario a eliminar como cadena), 
    //y los objetos res y req (necesarios para redirigir al usuario tras la operación).
    private void eliminar(UsuarioDao dao, String idStr,
                        HttpServletResponse res, HttpServletRequest req)
          throws IOException {
     //Verifica que el ID recibido no sea nulo ni esté vacío (o contenga solo espacios) para evitar errores.
      if (idStr != null && !idStr.isBlank()) {
          try {
              //Convierte el texto recibido a un número entero. Si la conversión falla 
              //(por ejemplo, si el ID no es un número), el código salta al bloque catch
              int id = Integer.parseInt(idStr);

              // Bloquea si el trabajador tiene asignaciones activas.
              //Consulta a la base de datos si el usuario tiene trabajo pendiente.
              if (dao.tieneAsignacionesActivas(id)) {
                  //Si tiene asignaciones, el código decide a dónde redirigir al usuario. Usa un operador ternario (? :) 
                  //para verificar si el usuario venía desde la vista "pendientes".
                  String vista = req.getParameter("vista");
                  String redirect = "pendientes".equals(vista)
                      ? "ServletUsuario?accion=pendientes&status=error&motivo=asignaciones_activas"
                      : "ServletUsuario?status=error&motivo=asignaciones_activas";
                  res.sendRedirect(redirect);
                  return; // Detiene la ejecución, no elimina.
              }
              //Solo se llega a este punto si el usuario no tiene asignaciones activas.
              dao.eliminarUsuario(id); // Solo llega aquí si no tiene asignaciones activas.
              //Si el ID no era un número válido, el código simplemente ignora el error y salta al final.
          } catch (NumberFormatException ignored) {}
      }
      //el código redirige al usuario a la página de origen, pasando un parámetro status=success 
      //para que el usuario reciba una confirmación visual de que la eliminación fue exitosa.
      String vista = req.getParameter("vista");
      String redirect = "pendientes".equals(vista)
              ? "ServletUsuario?accion=pendientes&status=success"
              : "ServletUsuario?status=success";
      res.sendRedirect(redirect);
  }
}