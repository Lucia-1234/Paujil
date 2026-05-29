<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.trabajo, java.util.List"%>
<%
    // Seguridad: Asegurar que el usuario sea trabajador o admin
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }
%>
<html>
<head>
    <title>Trabajos Finalizados</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
</head>
<body>
    <div class="container">
        <h1>Historial de Trabajos Finalizados</h1>
        
        <a href="ServletTrabajo?accion=misTrabajos" class="btn-back">← Volver a pendientes</a>
        <hr>

        <%
            List<trabajo> lista = (List<trabajo>) request.getAttribute("listaFinalizados");
            
            if (lista == null || lista.isEmpty()) {
                out.print("<div class='alert'>No tienes trabajos finalizados actualmente.</div>");
            } else {
                for (trabajo t : lista) {
        %>
                <div class="card-trabajo card-done">
                    <h3><%= t.getNombre() %></h3>
                    <p><strong>Cultivo:</strong> <%= t.getNombreCultivo() != null ? t.getNombreCultivo() : "N/A" %></p>
                    <p><strong>Fecha finalización:</strong> <%= t.getFechaFinalizacion() %></p>
                    <p><strong>Observaciones:</strong> <%= t.getObservaciones() != null ? t.getObservaciones() : "Sin comentarios" %></p>
                </div>
        <%
                }
            }
        %>
    </div>
</body>
</html>