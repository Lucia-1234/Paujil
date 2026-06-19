package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.dao.TipoTrabajoDao;
import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.tipoTrabajo;
import com.paujil.modelo.trabajo;
import com.paujil.servicio.AsignacionServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;

/**
 * ServletTrabajo: Clase controladora encargada de orquestar el flujo de las labores agrícolas.
 * Responsabilidad: Interceptar peticiones HTTP, coordinar la lógica con AsignacionServicio y dirigir la vista.
 */
@WebServlet("/ServletTrabajo") // Define el endpoint URL que este servlet atenderá en el contenedor web.
public class ServletTrabajo extends HttpServlet { // Define la clase como un componente ejecutable de tipo Servlet.

    // Instancia persistente del servicio de asignaciones, centralizando la lógica de negocio fuera del servlet.
    private final AsignacionServicio servicio = new AsignacionServicio(); 

    // Inicio del método doGet: Maneja exclusivamente peticiones de lectura (visualización de vistas).
    @Override 
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { 

        String accion = request.getParameter("accion"); // Extrae el parámetro 'accion' para bifurcar la lógica.
        if (accion == null) accion = "listar"; // Define una ruta por defecto si el usuario no especifica acción.

        switch (accion) { // Estructura de control principal para gestionar las distintas vistas del sistema.

            case "listar": // Caso administrativo: recupera toda la lista de trabajos para auditoría.
                if (!verificarSesionAdmin(request, response)) return; // Guardia de acceso: solo admins pueden ver el listado global.
                request.setAttribute("listaAsignaciones", servicio.listarTodas()); // Inyecta la lista en el request.
                request.setAttribute("listaTiposTrabajo", new TipoTrabajoDao().listarTipos()); // Inyecta catálogo de tipos.
                request.getRequestDispatcher("/templates/administrador/listar_trabajos.jsp").forward(request, response); // Renderiza la vista JSP.
                break; // Finaliza ejecución del bloque case.

            case "eliminar": // Caso administrativo: elimina una labor del sistema.
                if (!verificarSesionAdmin(request, response)) return; // Guardia de acceso.
                String idStr = request.getParameter("id"); // Obtiene el ID enviado por URL.
                if (estaVacio(idStr)) { response.sendRedirect("ServletTrabajo?accion=listar&status=error"); return; } // Valida que el ID exista.
                try {
                    boolean eliminado = servicio.eliminarTrabajo(Integer.parseInt(idStr)); // Ejecuta lógica de eliminación en servicio.
                    response.sendRedirect("ServletTrabajo?accion=listar&status=" + (eliminado ? "eliminado" : "error")); // Redirige con feedback.
                } catch (NumberFormatException e) { response.sendRedirect("ServletTrabajo?accion=listar&status=error"); } // Maneja ID no numérico.
                break;

            case "prepararCreacion": // Caso administrativo: prepara el formulario de alta de trabajos.
                if (!verificarSesionAdmin(request, response)) return; // Guardia de acceso.
                request.setAttribute("listaCultivos", new CultivoDao().listarCultivos()); // Carga cultivos para el selector.
                request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos()); // Carga trabajadores para asignación.
                request.setAttribute("listaTiposTrabajo", new TipoTrabajoDao().listarTipos()); // Carga tipos disponibles.
                request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp").forward(request, response); // Muestra formulario.
                break;

            case "misTrabajos": // Caso trabajador: consulta sus tareas asignadas.
                if (!verificarSesionUsuario(request, response)) return; // Valida que haya una sesión iniciada.
                int idUsuario = (int) request.getSession(false).getAttribute("idUsuario"); // Obtiene ID desde la sesión actual.
                request.setAttribute("listaMisTrabajos", servicio.listarPorUsuario(idUsuario, null)); // Filtra tareas por trabajador.
                request.getRequestDispatcher("/templates/trabajador/trabajos_asignados.jsp").forward(request, response); // Muestra la vista.
                break;

            case "pendientes": // Caso trabajador: filtro de tareas por ejecutar.
                if (!verificarSesionUsuario(request, response)) return; // Seguridad de sesión.
                int idUserPend = (int) request.getSession(false).getAttribute("idUsuario"); // Recupera ID de usuario.
                request.setAttribute("listaPendientes", servicio.listarPorUsuario(idUserPend, "Pendiente")); // Consulta tareas con estado 'Pendiente'.
                request.getRequestDispatcher("/templates/trabajador/trabajos_pendientes.jsp").forward(request, response); // Muestra vista.
                break;

            case "finalizados": // Caso trabajador: consulta tareas completadas.
                if (!verificarSesionUsuario(request, response)) return; // Seguridad de sesión.
                int idUserFin = (int) request.getSession(false).getAttribute("idUsuario"); // Recupera ID de usuario.
                request.setAttribute("listaFinalizados", servicio.listarPorUsuario(idUserFin, "Finalizado")); // Consulta tareas con estado 'Finalizado'.
                request.getRequestDispatcher("/templates/trabajador/trabajos_finalizados.jsp").forward(request, response); // Muestra vista.
                break;

            default: response.sendRedirect("ServletTrabajo?accion=listar"); // Redirección por seguridad ante parámetros desconocidos.
        }
    }

    // Inicio del método doPost: Maneja la escritura, actualización y eliminación de datos en BD.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String accion = request.getParameter("accion"); // Captura acción de escritura.
        if (accion == null) accion = ""; // Inicializa si es nulo para evitar NullPointerException.

        switch (accion) { // Selección de lógica según la acción enviada por formulario o AJAX.
            case "registrar": // Ejecuta guardado de nuevo trabajo.
                if (!verificarSesionAdmin(request, response)) return; // Control de acceso administrativo.
                registrarTrabajo(request, response); // Delega a método privado de registro.
                break;
            case "actualizarEstado": // Ejecuta cambio de estado de una labor (iniciar/finalizar).
                if (!verificarSesionUsuario(request, response)) return; // Control de acceso general.
                actualizarEstado(request, response); // Delega a método privado de estado.
                break;
            case "crearTipo": // Ejecuta creación de tipos (AJAX).
                if (!verificarSesionAdmin(request, response)) return;
                crearTipo(request, response); // Delega a método JSON de creación.
                break;
            case "editarTipo": // Ejecuta modificación de tipos (AJAX).
                if (!verificarSesionAdmin(request, response)) return;
                editarTipo(request, response); // Delega a método JSON de edición.
                break;
            case "eliminarTipo": // Ejecuta borrado de tipos (AJAX).
                if (!verificarSesionAdmin(request, response)) return;
                eliminarTipo(request, response); // Delega a método JSON de eliminación.
                break;
            default: response.sendRedirect("ServletTrabajo?accion=listar"); // Acción por defecto.
        }
    }

    // Helper: Registrar Trabajo. Valida inputs y llama a la capa de persistencia.
    private void registrarTrabajo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String desc = request.getParameter("descripcion"); // Obtiene descripción de la labor.
        String fechaStr = request.getParameter("fechaAsignacion"); // Obtiene fecha de inicio.
        String idCultivoStr = request.getParameter("idCultivo"); // Obtiene ID del cultivo relacionado.
        String idUsuarioStr = request.getParameter("idUsuario"); // Obtiene ID del trabajador.
        String idTipoTrabajoStr = request.getParameter("idTipoTrabajo"); // Obtiene el tipo de labor.

        // Validación defensiva: verifica que ningún campo vital esté ausente.
        if (estaVacio(desc) || estaVacio(fechaStr) || estaVacio(idCultivoStr) || estaVacio(idUsuarioStr) || estaVacio(idTipoTrabajoStr)) {
            enviarError("Todos los campos son obligatorios.", request, response); // Retorna error al usuario.
            return; // Termina ejecución.
        }

        // Validación de lógica: verifica longitud de la descripción (protección de BD).
        if (desc.trim().length() < 10 || desc.trim().length() > 500) {
            enviarError("La descripción debe estar entre 10 y 500 caracteres.", request, response);
            return;
        }

        try {
            int idCultivo = Integer.parseInt(idCultivoStr); // Parsea ID a entero.
            int idUsuario = Integer.parseInt(idUsuarioStr); // Parsea ID a entero.
            int idTipoTrabajo = Integer.parseInt(idTipoTrabajoStr); // Parsea ID a entero.
            Date fechaAsignacion = Date.valueOf(fechaStr); // Parsea String a Date SQL.
            trabajo t = new trabajo(desc.trim(), idTipoTrabajo); // Instancia objeto de modelo trabajo.
            boolean ok = servicio.registrarTrabajoCompleto(t, idCultivo, idUsuario, fechaAsignacion); // Persiste en BD.
            if (ok) response.sendRedirect("ServletTrabajo?accion=listar&status=success"); // Feedback positivo.
            else enviarError("Error al guardar en base de datos.", request, response); // Feedback negativo.
        } catch (IllegalArgumentException e) { // Captura error si parseo falla.
            enviarError("Datos inválidos.", request, response);
        }
    }

    // Helper: Actualizar Estado. Permite al trabajador avanzar el flujo de tareas.
    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String idAsignacionStr = request.getParameter("idAsignacion"); // Obtiene ID de la tarea.
        String observaciones = request.getParameter("observaciones"); // Notas adicionales del trabajador.
        String btnAccion = request.getParameter("btnAccion"); // Identifica si el usuario hizo clic en 'iniciar' o 'finalizar'.

        if (estaVacio(idAsignacionStr)) { response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error"); return; }
        if (observaciones != null && observaciones.trim().length() > 500) { response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error_obs"); return; }

        try {
            int idAsignacion = Integer.parseInt(idAsignacionStr); // Parsea ID de la labor.
            String nuevoEstado = "iniciar".equals(btnAccion) ? "En proceso" : "finalizar".equals(btnAccion) ? "Finalizado" : null;
            boolean ok = servicio.actualizarEstado(idAsignacion, observaciones, nuevoEstado); // Actualiza estado en BD.
            String status = ok ? (nuevoEstado != null ? nuevoEstado.toLowerCase().replace(" ", "_") : "guardado") : "error";
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=" + status); // Redirige con estado.
        } catch (NumberFormatException e) { response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error"); }
    }

    // Helper: Manejador de errores administrativos. Re-pobla los combos para no perder información en pantalla.
    private void enviarError(String mensaje, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje); // Envía mensaje de error.
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos()); // Repuebla lista.
        request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos()); // Repuebla lista.
        request.setAttribute("listaTiposTrabajo", new TipoTrabajoDao().listarTipos()); // Repuebla lista.
        request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp").forward(request, response); // Vuelve al formulario.
    }

    // Helper: Crea nuevo tipo de trabajo (AJAX).
    private void crearTipo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8"); // Define formato JSON.
        String nombre = request.getParameter("nombreTipo"); // Obtiene nombre.
        if (estaVacio(nombre) || nombre.trim().length() < 3 || nombre.trim().length() > 50 || !nombre.matches("[\\p{L}\\s\\-]+")) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"Nombre inválido.\"}"); // Valida formato vía Regex.
            return;
        }
        TipoTrabajoDao dao = new TipoTrabajoDao();
        if (!dao.registrarTipo(nombre.trim())) response.getWriter().write("{\"ok\":false,\"mensaje\":\"Error al guardar.\"}");
        else response.getWriter().write(construirJsonTipos(dao.listarTipos())); // Devuelve lista actualizada.
    }

    // Helper: Edita tipo de trabajo existente (AJAX).
    private void editarTipo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String idStr = request.getParameter("id");
        String nombre = request.getParameter("nombreTipo");
        if (estaVacio(idStr) || estaVacio(nombre)) { response.getWriter().write("{\"ok\":false,\"mensaje\":\"Datos inválidos.\"}"); return; }
        try {
            int id = Integer.parseInt(idStr);
            TipoTrabajoDao dao = new TipoTrabajoDao();
            if (!dao.editarTipo(id, nombre.trim())) response.getWriter().write("{\"ok\":false,\"mensaje\":\"Error al editar.\"}");
            else response.getWriter().write(construirJsonTipos(dao.listarTipos())); // Devuelve lista actualizada.
        } catch (NumberFormatException e) { response.getWriter().write("{\"ok\":false,\"mensaje\":\"ID inválido.\"}"); }
    }

    // Helper: Elimina tipo de trabajo (AJAX) con validación de integridad referencial.
    private void eliminarTipo(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String idStr = request.getParameter("id");
        try {
            int id = Integer.parseInt(idStr);
            TipoTrabajoDao dao = new TipoTrabajoDao();
            if (dao.contarTrabajosPorTipo(id) > 0) response.getWriter().write("{\"ok\":false,\"mensaje\":\"Tipo en uso.\"}"); // Evita borrado si tiene dependencias.
            else if (!dao.eliminarTipo(id)) response.getWriter().write("{\"ok\":false,\"mensaje\":\"Error al eliminar.\"}");
            else response.getWriter().write(construirJsonTipos(dao.listarTipos())); // Devuelve lista actualizada.
        } catch (Exception e) { response.getWriter().write("{\"ok\":false,\"mensaje\":\"Error técnico.\"}"); }
    }

    // Helper: Construye el objeto JSON.
    private String construirJsonTipos(List<tipoTrabajo> tipos) {
        StringBuilder json = new StringBuilder("{\"ok\":true,\"tipos\":["); // Abre estructura JSON.
        for (int i = 0; i < tipos.size(); i++) { // Itera elementos.
            if (i > 0) json.append(","); // Inserta comas separadoras.
            json.append("{\"id\":").append(tipos.get(i).getIdTipoTrabajo()) // Agrega ID.
                .append(",\"nombre\":\"").append(escaparJson(tipos.get(i).getNombreTipo())).append("\"}"); // Agrega nombre.
        }
        return json.append("]}").toString(); // Cierra y retorna cadena.
    }

    // Helper: Sanitización JSON. Previene ataques por inyección de caracteres especiales.
    private String escaparJson(String s) {
        return (s == null) ? "" : s.replace("\\", "\\\\").replace("\"", "\\\""); // Escapa caracteres reservados.
    }
}