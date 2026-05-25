package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.modelo.cultivo;
import java.io.IOException;
import java.math.BigDecimal;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ServletCultivo")
public class ServletCultivo extends HttpServlet {

    // Maneja inserciones (Agregar) y actualizaciones (Editar)
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        CultivoDao dao = new CultivoDao();
        String URL_VISTA = request.getContextPath() + "/cultivos.jsp";

        if ("registrar".equals(accion)) {
            String nombre = request.getParameter("txtNombreCultivo");
            String tipo = request.getParameter("txtTipoCultivo");
            java.sql.Date siembra = java.sql.Date.valueOf(request.getParameter("txtFechaSiembra"));
            
            String cosechaStr = request.getParameter("txtFechaCosecha");
            java.sql.Date cosecha = (cosechaStr != null && !cosechaStr.isEmpty()) ? java.sql.Date.valueOf(cosechaStr) : null;
            BigDecimal area = new BigDecimal(request.getParameter("txtArea"));

            // Variables de tablas relacionales
            int idLote = Integer.parseInt(request.getParameter("cmbLote"));
            int idUsuario = Integer.parseInt(request.getParameter("cmbUsuario"));
            int idInsumo = Integer.parseInt(request.getParameter("cmbInsumo"));

            cultivo nuevo = new cultivo(nombre, tipo, siembra, cosecha, area);
            int idGenerado = dao.registrarCultivoBase(nuevo);

            if (idGenerado > 0) {
                dao.asignarLote(idGenerado, idLote);
                dao.asignarTrabajador(idGenerado, idUsuario);
                dao.asignarInsumo(idGenerado, idInsumo);
                response.sendRedirect(URL_VISTA + "?exito=registrado");
            } else {
                response.sendRedirect(URL_VISTA + "?error=db");
            }

        } else if ("editar".equals(accion)) {
            int idCultivo = Integer.parseInt(request.getParameter("txtIdCultivo"));
            String nombre = request.getParameter("txtNombreCultivo");
            String tipo = request.getParameter("txtTipoCultivo");
            java.sql.Date siembra = java.sql.Date.valueOf(request.getParameter("txtFechaSiembra"));
            
            String cosechaStr = request.getParameter("txtFechaCosecha");
            java.sql.Date cosecha = (cosechaStr != null && !cosechaStr.isEmpty()) ? java.sql.Date.valueOf(cosechaStr) : null;
            BigDecimal area = new BigDecimal(request.getParameter("txtArea"));

            cultivo editado = new cultivo(nombre, tipo, siembra, cosecha, area);
            editado.setIdCultivo(idCultivo);

            if (dao.actualizar(editado)) {
                response.sendRedirect(URL_VISTA + "?exito=actualizado");
            } else {
                response.sendRedirect(URL_VISTA + "?error=db");
            }
        }
    }

    // Maneja las eliminaciones rápidas que viajan por el enlace href
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String accion = request.getParameter("accion");
        String idStr = request.getParameter("id");
        CultivoDao dao = new CultivoDao();
        String URL_VISTA = request.getContextPath() + "/cultivos.jsp";

        if ("eliminar".equals(accion) && idStr != null) {
            if (dao.eliminar(Integer.parseInt(idStr))) {
                response.sendRedirect(URL_VISTA + "?exito=eliminado");
            } else {
                response.sendRedirect(URL_VISTA + "?error=db");
            }
        }
    }
}