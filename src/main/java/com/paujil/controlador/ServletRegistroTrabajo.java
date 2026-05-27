package com.paujil.controlador;

import com.paujil.dao.RegistroTrabajoDao;
import com.paujil.modelo.registros;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/ServletLabor")
public class ServletRegistroTrabajo extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Recibir parámetros
        int idCultivo = Integer.parseInt(request.getParameter("idCultivo"));
        String dd = request.getParameter("dd");
        String mm = request.getParameter("mm");
        String aa = request.getParameter("aa");
        String labor = request.getParameter("labor");
        String responsable = request.getParameter("responsable");
        String comentarios = request.getParameter("comentarios");

        // 2. Construir la fecha (Formato esperado por SQL: yyyy-mm-dd)
        String fechaStr = aa + "-" + mm + "-" + dd;
        Date fechaSql = Date.valueOf(fechaStr);

        // 3. Crear el objeto
        registros reg = new registros();
        reg.setIdCultivo(idCultivo);
        reg.setFecha(fechaSql);
        reg.setLabor(labor);
        reg.setResponsable(responsable);
        reg.setComentarios(comentarios);

        // 4. Guardar en BD (Necesitas crear el método registrar en LaborDao)
        RegistroTrabajoDao dao = new RegistroTrabajoDao();
        boolean exito = dao.registrarLabor(reg);

        if (exito) {
            response.sendRedirect("cultivos.jsp?status=success");
        } else {
            response.sendRedirect("agregar_trabajos.jsp?idCultivo=" + idCultivo + "&status=error");
        }
    }
}