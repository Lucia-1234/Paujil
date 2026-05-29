<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.trabajo, java.util.List"%>
<%
    // Guarda de sesión
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }
    String status = request.getParameter("status");
%>

<!DOCTYPE html>
<html lang="es">
<head>
   <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
</head>
<body>
<main class="container">
    <div class="list-header">
        <h1 class="list-header__title">Mis trabajos asignados</h1>
        <a href="${pageContext.request.contextPath}/templates/trabajador/menu_trabajador.jsp" class="btn btn--secondary">← Volver al menú</a>
    </div>

    <%-- Mensajes de feedback --%>
    <% if ("finalizado".equals(status)) { %>
        <p class="alert alert--ok">Trabajo marcado como finalizado.</p>
    <% } else if ("guardado".equals(status)) { %>
        <p class="alert alert--ok">Avance guardado correctamente.</p>
    <% } %>

    <%
        // AQUÍ ESTÁ LA ÚNICA DECLARACIÓN DE 'lista'
        List<trabajo> lista = (List<trabajo>) request.getAttribute("listaMisTrabajos");
        
        if (lista == null || lista.isEmpty()) {
    %>
        <p class="empty-msg">No tienes tareas pendientes en este momento.</p>
    <%
        } else {
            for (trabajo t : lista) {
                boolean finalizado = (t.getFechaFinalizacion() != null);
    %>
                <%
            }
        }
    %>
</main>
</body>
</html>

