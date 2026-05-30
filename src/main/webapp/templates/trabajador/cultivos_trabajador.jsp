<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.cultivo, java.util.List"%>
<%
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }

    List<cultivo> lista = (List<cultivo>) request.getAttribute("listaCultivos");
    if (lista == null) {
        response.sendRedirect(request.getContextPath() + "/ServletCultivo?accion=verTrabajador");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Cultivos - Finca El Paujil</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/cultivos.css">
</head>
<body>
<main class="container">

    <div class="list-header">
        <h1 class="list-header__title">Cultivos activos</h1>
        <a href="${pageContext.request.contextPath}/templates/trabajador/menu_trabajador.jsp"
           class="btn btn--secondary">← Volver al menú</a>
    </div>

    <% if (lista.isEmpty()) { %>
        <p class="empty-msg">No hay cultivos registrados en este momento.</p>
    <% } else {
        for (cultivo c : lista) { %>
        <article class="bio-card">
            <div class="bio-card__header">
                <h2 class="bio-card__title"><%= c.getNombreCultivo() %></h2>
                <span class="badge badge--pending"><%= c.getTipoCultivo() != null ? c.getTipoCultivo() : "Sin tipo" %></span>
            </div>

            <p><strong>Fecha de siembra:</strong> <%= c.getFechaSiembra() %></p>

            <% if (c.getFechaCosecha() != null) { %>
                <p><strong>Fecha estimada de cosecha:</strong> <%= c.getFechaCosecha() %></p>
            <% } else { %>
                <p><strong>Fecha estimada de cosecha:</strong> <em>No definida</em></p>
            <% } %>
        </article>
    <%  }
    } %>

</main>
</body>
</html>