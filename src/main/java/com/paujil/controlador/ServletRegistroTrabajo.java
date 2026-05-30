package com.paujil.controlador;

import com.paujil.dao.RegistroTrabajoDao;
import com.paujil.modelo.registros;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/ServletRegistroTrabajo")
public class ServletRegistroTrabajo extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // false evita crear sesion fantasma; ausencia de sesion implica usuario no autenticado
        HttpSession session = request.getSession(false);
        // Verifica tanto existencia de sesion como presencia del identificador de usuario
        if (session == null || session.getAttribute("idUsuario") == null) {
            // El parametro sesion_expirada permite que el login muestre un mensaje contextual
            response.sendRedirect(request.getContextPath()
                    + "/templates/login.jsp?error=sesion_expirada");
            return;
        }
        // Recupera el ID del usuario autenticado para vincularlo como autor del registro de trabajo
        int idUsuario = (int) session.getAttribute("idUsuario");

        // Lee los parametros del formulario antes de cualquier validacion
        String idCultivoStr     = request.getParameter("idCultivo");
        String descripcion      = request.getParameter("descripcionTrabajo");
        String fechaInicioStr   = request.getParameter("fechaInicio");
        // fechaFinalizo puede coincidir con fechaInicio si el trabajo ocurre en un solo dia
        String fechaFinalizoStr = request.getParameter("fechaFinalizo");
        // Campo opcional; se almacenara como null si viene vacio para distinguirlo de texto real en BD
        String observaciones    = request.getParameter("observaciones");

        // Validacion temprana sobre los campos obligatorios antes de parsear tipos de dato
        if (estaVacio(idCultivoStr) || estaVacio(descripcion)
                || estaVacio(fechaInicioStr) || estaVacio(fechaFinalizoStr)) {
            reenviarConError("Todos los campos obligatorios deben completarse.",
                    idCultivoStr, request, response);
            return;
        }

        int idCultivo;
        try {
            // Parseo explicito necesario porque los parametros HTTP siempre llegan como String
            idCultivo = Integer.parseInt(idCultivoStr);
        } catch (NumberFormatException e) {
            // Valor no numerico indica manipulacion del formulario o error del cliente
            reenviarConError("Cultivo no valido.", idCultivoStr, request, response);
            return;
        }

        Date fechaInicio;
        Date fechaFinalizo;
        try {
            // Date.valueOf espera formato estricto "yyyy-MM-dd"; el selector de fechas garantiza esto
            fechaInicio   = Date.valueOf(fechaInicioStr);
            fechaFinalizo = Date.valueOf(fechaFinalizoStr);
        } catch (IllegalArgumentException e) {
            // Formato incorrecto; puede ocurrir si el usuario edita el campo manualmente
            reenviarConError("Formato de fecha invalido. Use el selector de fechas.",
                    idCultivoStr, request, response);
            return;
        }

        // Regla de negocio: un trabajo no puede finalizar antes de haber comenzado
        if (fechaInicio.after(fechaFinalizo)) {
            reenviarConError("La fecha de inicio no puede ser posterior a la fecha de finalizacion.",
                    idCultivoStr, request, response);
            return;
        }

        // Construye el objeto de dominio con todos los datos validados y normalizados
        registros reg = new registros(
                idCultivo,
                // trim() elimina espacios sobrantes que el usuario pudo introducir accidentalmente
                descripcion.trim(),
                // idUsuario vincula el registro al autor autenticado, no a un parametro del formulario
                idUsuario,
                fechaInicio,
                fechaFinalizo,
                // Convierte observaciones vacias a null para no almacenar cadenas en blanco en BD
                (observaciones != null && !observaciones.trim().isEmpty())
                        ? observaciones.trim() : null
        );

        RegistroTrabajoDao dao = new RegistroTrabajoDao();
        // Delega la insercion al DAO; el boolean refleja si la operacion afecto al menos una fila
        boolean exito = dao.registrarLabor(reg);

        // Patron Redirect-After-POST evita que recargar la pagina duplique el registro
        if (exito) {
            // idCultivo en la URL permite que la vista destino muestre el historial del cultivo correcto
            response.sendRedirect(request.getContextPath()
                    + "/templates/trabajador/cultivos.jsp?status=success&idCultivo=" + idCultivo);
        } else {
            reenviarConError("Error al guardar en base de datos. Intente nuevamente.",
                    idCultivoStr, request, response);
        }
    }

    // Centraliza la verificacion de vacios para no repetir la condicion null + trim en cada campo
    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    // forward preserva el mensaje de error en el scope de request, que no sobrevive una redireccion HTTP
    // idCultivo viaja como atributo porque RequestDispatcher.forward() ignora query strings en la ruta destino
    private void reenviarConError(String mensaje, String idCultivo,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // El JSP lee este atributo para mostrar el aviso de error al usuario
        request.setAttribute("mensajeError", mensaje);
        // Permite que el formulario de la vista recupere el cultivo activo sin que el usuario lo reseleccione
        request.setAttribute("idCultivo", idCultivo);
        request.getRequestDispatcher("/templates/trabajador/agregar_trabajos.jsp")
               .forward(request, response);
    }
}