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
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/cultivos.css">
    <title>Cultivos - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/trabajador/menu_trabajador.jsp"
           class="list-header__back" aria-label="Volver al menú">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Cultivos activos</h1>
    </header>

    <div class="panel">
        <section class="crop-list">
            <% if (lista.isEmpty()) { %>
                <p class="no-data">
                    <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                    No hay cultivos registrados en este momento.
                </p>
            <% } else {
                for (cultivo c : lista) { %>
                <article class="crop-card">
                    <div class="crop-card__content">
                        <h2 class="crop-card__title"><%= c.getNombreCultivo() %></h2>
                        <% if (c.getTipoCultivo() != null) { %>
                            <p class="crop-card__description">
                                <strong>Tipo:</strong> <%= c.getTipoCultivo() %>
                            </p>
                        <% } %>
                        <p class="crop-card__dates">
                            <i class="fa-solid fa-seedling" style="color:var(--color-brand-green);"></i>
                            Siembra: <strong><%= c.getFechaSiembra() %></strong>
                        </p>
                        <p class="crop-card__dates">
                            <i class="fa-solid fa-basket-shopping" style="color:var(--color-brand-green);"></i>
                            Cosecha estimada:
                            <strong>
                                <%= c.getFechaCosecha() != null ? c.getFechaCosecha() : "No definida" %>
                            </strong>
                        </p>
                    </div>
                </article>
            <%  }
            } %>
        </section>
    </div>

</main>
</body>
</html>
