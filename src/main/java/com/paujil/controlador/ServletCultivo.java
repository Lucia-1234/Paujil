package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.modelo.cultivo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/ServletCultivo") // Ajustado a /ServletCultivo como usas en el JSP
public class ServletCultivo extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
        // 1. Recibir parámetros comunes
        String idStr = request.getParameter("id"); // Este llega del campo oculto en el modal
        String nombre = request.getParameter("nombreCultivo");
        String tipo = request.getParameter("tipoCultivo");
        Date fSiembra = Date.valueOf(request.getParameter("fechaSiembra"));

        String fCosechaStr = request.getParameter("fechaCosecha");
        Date fCosecha = (fCosechaStr != null && !fCosechaStr.isEmpty()) ? Date.valueOf(fCosechaStr) : null;

        // String bioprep = request.getParameter("nombreBiopreparado"); // Lo usaremos cuando implementemos la tabla relacional

        CultivoDao dao = new CultivoDao();
        boolean operacionExitosa = false;

        // 2. Determinar si es Edición o Registro Nuevo
        if (idStr != null && !idStr.isEmpty()) {
            // ES UNA EDICIÓN (UPDATE)
            int id = Integer.parseInt(idStr);
            // Nota: Asegúrate de tener este método en tu DAO actualizado
            operacionExitosa = dao.actualizarCultivo(id, nombre, tipo, fSiembra, fCosecha);
        } else {
            // ES UN REGISTRO NUEVO (INSERT)
            cultivo c = new cultivo(nombre, tipo, fSiembra, fCosecha);
            operacionExitosa = dao.registrarCultivo(c);
        }

        // 3. Respuesta al usuario
        if (operacionExitosa) {
            response.sendRedirect("lista_cultivos.jsp?status=success");
        } else {
            response.sendRedirect("lista_cultivos.jsp?status=error");
        }
    }
    // Manejo de eliminaciones (GET)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        
        if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));
            CultivoDao dao = new CultivoDao();
            
            // Asumiendo que tienes un método eliminarCultivo en tu Dao
            boolean eliminado = dao.eliminarCultivo(id);
            
            if (eliminado) {
                response.sendRedirect("cultivos.jsp?eliminado=true");
            } else {
                response.sendRedirect("cultivos.jsp?error=true");
            }
        }
    }
}