package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.dao.TrabajoDao;
import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.trabajo;
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

@WebServlet("/ServletTrabajo")
public class ServletTrabajo extends HttpServlet {

    // Instancia compartida entre peticiones; TrabajoDao debe ser stateless para que esto sea seguro
    private final TrabajoDao trabajoDao = new TrabajoDao();

    //  GET 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // El parametro "accion" actua como discriminador de rutas dentro del mismo endpoint GET
        String accion = request.getParameter("accion");
        // Valor por defecto para que una URL sin parametros muestre el listado principal
        if (accion == null) accion = "listar";

        switch (accion) {

            //  Acciones de ADMINISTRADOR 

            case "listar":
                if (!verificarSesionAdmin(request, response)) return;
                // Consulta enriquecida que trae datos relacionados (usuario, cultivo) en una sola llamada
                List<trabajo> lista = trabajoDao.listarTrabajosCompletos();
                request.setAttribute("listaTrabajos", lista);
                request.getRequestDispatcher("/templates/administrador/listar_trabajos.jsp")
                       .forward(request, response);
                break;

            case "eliminar":
                if (!verificarSesionAdmin(request, response)) return;
                String idStr = request.getParameter("id");
                // Aborta si no se recibio ID para evitar una eliminacion sin objetivo
                if (estaVacio(idStr)) {
                    response.sendRedirect("ServletTrabajo?accion=listar&status=error");
                    return;
                }
                try {
                    int id = Integer.parseInt(idStr);
                    boolean eliminado = trabajoDao.eliminarTrabajo(id);
                    // El parametro status permite que la vista siguiente muestre retroalimentacion
                    response.sendRedirect("ServletTrabajo?accion=listar&status="
                            + (eliminado ? "eliminado" : "error"));
                } catch (NumberFormatException e) {
                    // ID no numerico indica manipulacion del parametro; se redirige sin eliminar
                    response.sendRedirect("ServletTrabajo?accion=listar&status=error");
                }
                break;

            case "prepararCreacion":
                if (!verificarSesionAdmin(request, response)) return;
                // Precarga los selects del formulario para que el JSP no haga consultas directas a BD
                request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
                // Solo usuarios activos son asignables; los inactivos o pendientes se excluyen
                request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos());
                request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
                       .forward(request, response);
                break;

            //  Acciones de TRABAJADOR 

            case "misTrabajos":
                if (!verificarSesionUsuario(request, response)) return;
                // getSession(false) es seguro aqui porque verificarSesionUsuario ya garantiza que existe
                int idUsuario = (int) request.getSession(false).getAttribute("idUsuario");
                // Filtra los trabajos por el usuario autenticado; cada trabajador ve solo los suyos
                request.setAttribute("listaMisTrabajos",
                        trabajoDao.listarTrabajosPorUsuario(idUsuario));
                request.getRequestDispatcher("/templates/trabajador/trabajos_asignados.jsp")
                       .forward(request, response);
                break;

            case "finalizados":
                if (!verificarSesionUsuario(request, response)) return;
                int idUserFin = (int) request.getSession(false).getAttribute("idUsuario");
                // Vista historica separada de "misTrabajos" para no mezclar trabajos activos y cerrados
                request.setAttribute("listaFinalizados",
                        trabajoDao.listarTrabajosFinalizadosPorUsuario(idUserFin));
                request.getRequestDispatcher("/templates/trabajador/trabajos_finalizados.jsp")
                       .forward(request, response);
                break;
        }
    }

    //  POST 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        // Cadena vacia como valor por defecto evita NullPointerException en el switch
        if (accion == null) accion = "";

        switch (accion) {

            case "registrar":
                // Solo el admin puede crear nuevas asignaciones de trabajo
                if (!verificarSesionAdmin(request, response)) return;
                registrarTrabajo(request, response);
                break;

            case "actualizarEstado":
                // El trabajador actualiza el progreso o finaliza su propio trabajo asignado
                if (!verificarSesionUsuario(request, response)) return;
                actualizarEstado(request, response);
                break;

            default:
                // Accion desconocida: se trata como intento de registro y exige rol admin
                if (!verificarSesionAdmin(request, response)) return;
                registrarTrabajo(request, response);
        }
    }

    //  Helper: registrar trabajo (admin) 
    private void registrarTrabajo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nombre       = request.getParameter("nombreTrabajo");
        String desc         = request.getParameter("descripcion");
        String fechaStr     = request.getParameter("fechaAsignacion");
        String idCultivoStr = request.getParameter("idCultivo");
        // idUsuario identifica al trabajador al que se asignara el trabajo
        String idUsuarioStr = request.getParameter("idUsuario");

        // Todos los campos son necesarios para construir una asignacion valida
        if (estaVacio(nombre) || estaVacio(desc) || estaVacio(fechaStr)
                || estaVacio(idCultivoStr) || estaVacio(idUsuarioStr)) {
            enviarError("Todos los campos son obligatorios.", request, response);
            return;
        }

        try {
            int idCultivo        = Integer.parseInt(idCultivoStr);
            int idUsuario        = Integer.parseInt(idUsuarioStr);
            // Date.valueOf espera "yyyy-MM-dd"; lanza IllegalArgumentException ante otro formato
            Date fechaAsignacion = Date.valueOf(fechaStr);

            // El objeto trabajo encapsula solo los datos propios; las relaciones se pasan por separado al DAO
            trabajo t = new trabajo(nombre.trim(), desc.trim(), fechaAsignacion);
            // registrarTrabajoCompleto maneja la insercion atomica en tablas relacionadas
            boolean ok = trabajoDao.registrarTrabajoCompleto(t, idCultivo, idUsuario);

            if (ok) {
                // Redirect-After-POST evita reenvio del formulario al recargar la pagina
                response.sendRedirect("ServletTrabajo?accion=listar&status=success");
            } else {
                enviarError("Error al guardar en base de datos.", request, response);
            }
        } catch (Exception e) {
            // Captura tanto NumberFormatException de los IDs como IllegalArgumentException de la fecha
            enviarError("Datos invalidos: " + e.getMessage(), request, response);
        }
    }

    //  Helper: actualizar estado (trabajador) 
    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idTrabajoStr  = request.getParameter("idTrabajo");
        // Observaciones son opcionales; el DAO debe tolerar null o cadena vacia
        String observaciones = request.getParameter("observaciones");
        // btnAccion diferencia entre guardar progreso parcial y cerrar definitivamente el trabajo
        String btnAccion     = request.getParameter("btnAccion");

        if (estaVacio(idTrabajoStr)) {
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error");
            return;
        }

        try {
            int idTrabajo = Integer.parseInt(idTrabajoStr);
            // true indica cierre definitivo del trabajo; false guarda progreso sin cerrarlo
            boolean finalizar = "finalizar".equals(btnAccion);
            boolean ok = trabajoDao.actualizarEstadoTrabajo(idTrabajo, observaciones, finalizar);
            // El status distingue entre guardado parcial y finalizacion para que la vista muestre el mensaje correcto
            String status = ok ? (finalizar ? "finalizado" : "guardado") : "error";
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=" + status);
        } catch (NumberFormatException e) {
            // ID malformado; puede indicar manipulacion del formulario
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error");
        }
    }

    //  Helper: reenviar a formulario con error 

    // forward preserva el mensaje en el scope de request; un redirect lo perderia
    private void enviarError(String mensaje, HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException {
        // El JSP lee este atributo para mostrar el aviso de error al usuario
        request.setAttribute("mensajeError", mensaje);
        // Recarga los selects para que el formulario permanezca funcional tras el error
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos());
        request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
               .forward(request, response);
    }
}