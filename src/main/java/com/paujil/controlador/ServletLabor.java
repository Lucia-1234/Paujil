package com.paujil.controlador;

import com.paujil.dao.RegistroTrabajoDao;
import com.paujil.modelo.registros;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;

@WebServlet("/ServletLabor")
public class ServletLabor extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // false evita crear sesion fantasma; si no hay sesion el usuario no esta autenticado
        HttpSession session = request.getSession(false);
        // Tanto admin como trabajador pueden registrar labores; solo se exige identidad activa
        if (session == null || session.getAttribute("idUsuario") == null) {
            // Redirige al login sin exponer el recurso solicitado
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
            return;
        }
        // Recupera el ID del usuario autenticado para asociarlo al registro de trabajo
        int idUsuario = (Integer) session.getAttribute("idUsuario");

        // Lee los parametros del formulario antes de cualquier validacion
        String idCultivoStr     = request.getParameter("idCultivo");
        String descripcion      = request.getParameter("descripcionTrabajo");
        String fechaInicioStr   = request.getParameter("fechaInicio");
        // fechaFinalizo puede ser igual a fechaInicio si el trabajo dura un solo dia
        String fechaFinalizoStr = request.getParameter("fechaFinalizo");
        // Campo opcional; se almacenara como null si viene vacio
        String observaciones    = request.getParameter("observaciones");

        // Todos los campos excepto observaciones son obligatorios para construir el registro
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

        // Limite de 1000 caracteres previene desbordamiento en la columna de BD y abuso de almacenamiento
        if (descripcion.length() > 1000) {
            reenviarConError("La descripcion no puede superar 1000 caracteres.",
                    idCultivoStr, request, response);
            return;
        }

        Date fechaInicio, fechaFinalizo;
        try {
            // trim() elimina espacios que algunos navegadores insertan al enviar inputs de tipo date
            fechaInicio   = Date.valueOf(fechaInicioStr.trim());
            fechaFinalizo = Date.valueOf(fechaFinalizoStr.trim());
        } catch (IllegalArgumentException e) {
            // Date.valueOf lanza esta excepcion ante cualquier formato distinto a yyyy-MM-dd
            reenviarConError("Formato de fecha invalido. Use yyyy-MM-dd.",
                    idCultivoStr, request, response);
            return;
        }

        // Regla de negocio: un trabajo no puede finalizar antes de haber comenzado
        if (fechaInicio.after(fechaFinalizo)) {
            reenviarConError("La fecha de inicio no puede ser posterior a la fecha de finalizacion.",
                    idCultivoStr, request, response);
            return;
        }

        // Convierte observaciones vacias a null para diferenciarlas de texto real en BD
        registros reg = new registros(
            idCultivo, descripcion.trim(), idUsuario,
            fechaInicio, fechaFinalizo,
            (observaciones != null && !observaciones.trim().isEmpty())
                ? observaciones.trim() : null
        );
        // Delega la persistencia al DAO; el boolean indica exito o fallo de la operacion en BD
        boolean exito = new RegistroTrabajoDao().registrarLabor(reg);

        // Patron Redirect-After-POST evita que recargar la pagina duplique el registro
        String status = exito ? "registrado" : "error";
        // idCultivoActivo permite que la vista destino expanda automaticamente el historial correcto
        response.sendRedirect(request.getContextPath()
                + "/ServletCultivo?status=" + status + "&idCultivoActivo=" + idCultivo);
    }

    // Centraliza la verificacion de vacios para evitar repetir la condicion null + trim en cada campo
    private boolean estaVacio(String v) {
        return v == null || v.trim().isEmpty();
    }

    // Usa forward para que el mensaje de error permanezca en el scope de request,
    // ya que no sobrevive una redireccion HTTP
    private void reenviarConError(String mensaje, String idCultivo,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // El JSP lee este atributo para renderizar el aviso de error al usuario
        request.setAttribute("mensajeError", mensaje);
        // Preserva el ID del cultivo para que el formulario de la vista recupere el contexto correcto
        request.setAttribute("idCultivo", idCultivo);
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }
}