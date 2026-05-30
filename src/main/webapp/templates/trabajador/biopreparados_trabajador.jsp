<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.biopreparado, com.paujil.modelo.biopreparado.ingredienteBio, java.util.List"%>
<%
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }

    List<biopreparado> lista = (List<biopreparado>) request.getAttribute("listaBiopreparados");
    if (lista == null) {
        response.sendRedirect(request.getContextPath() + "/ServletBiopreparado?accion=verTrabajador");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Biopreparados - Finca El Paujil</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/biopreparados.css">
</head>
<body>
<main class="container">

    <div class="list-header">
        <h1 class="list-header__title">Biopreparados</h1>
        <a href="${pageContext.request.contextPath}/templates/trabajador/menu_trabajador.jsp"
           class="btn btn--secondary">← Volver al menú</a>
    </div>

    <% if (lista.isEmpty()) { %>
        <p class="empty-msg">No hay biopreparados registrados en este momento.</p>
    <% } else {
        for (biopreparado b : lista) { %>
        <article class="bio-card">
            <div class="bio-card__header">
                <h2 class="bio-card__title"><%= b.getNombre() %></h2>
                <% if (b.getPrecio() > 0) { %>
                    <span class="badge badge--pending">$<%= String.format("%.2f", b.getPrecio()) %></span>
                <% } %>
            </div>

            <% if (b.getDescripcion() != null && !b.getDescripcion().isEmpty()) { %>
                <p><strong>Descripción:</strong> <%= b.getDescripcion() %></p>
            <% } %>

            <p><strong>Fecha de creación:</strong> <%= b.getFechaCreacion() %></p>
            <p><strong>Fecha de vencimiento:</strong> <%= b.getFechaVencimiento() %></p>

            <% if (b.getPreparacion() != null && !b.getPreparacion().isEmpty()) { %>
                <details>
                    <summary><strong>Modo de preparación</strong></summary>
                    <p class="preparacion-texto"><%= b.getPreparacion() %></p>
                </details>
            <% } %>

            <% if (b.getIngredientes() != null && !b.getIngredientes().isEmpty()) { %>
                <details>
                    <summary><strong>Ingredientes (<%= b.getIngredientes().size() %>)</strong></summary>
                    <ul class="ingredientes-lista">
                        <% for (ingredienteBio ing : b.getIngredientes()) { %>
                            <li>
                                <%= ing.getNombre() %>
                                <% if (ing.getCantidad() > 0) { %>
                                    — <%= ing.getCantidad() %> <%= ing.getUnidad() %>
                                <% } %>
                            </li>
                        <% } %>
                    </ul>
                </details>
            <% } %>
        </article>
    <%  }
    } %>

</main>
</body>
</html>

