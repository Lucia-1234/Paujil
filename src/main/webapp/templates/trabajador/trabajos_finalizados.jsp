<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.trabajo, java.util.List"%>
<%
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
    <title>Trabajos Finalizados - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=misTrabajos"
           class="list-header__back" aria-label="Volver a trabajos pendientes">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Historial de trabajos finalizados</h1>
    </header>

    <div class="panel">
        <div class="job-list">
            <%
                List<trabajo> lista = (List<trabajo>) request.getAttribute("listaFinalizados");
                if (lista == null || lista.isEmpty()) {
            %>
                <p class="no-data">
                    <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                    No tienes trabajos finalizados actualmente.
                </p>
            <%
                } else {
                    for (trabajo t : lista) {
            %>
                <article class="job-card">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:var(--spacing-sm);">
                        <h2 class="job-card__title" style="margin-bottom:0;"><%= t.getNombre() %></h2>
                        <span class="job-status job-status--done">
                            <i class="fa-solid fa-circle-check" style="margin-right:4px;"></i>Finalizado
                        </span>
                    </div>
                    <div class="job-card__info">
                        <div class="job-card__meta">
                            <i class="fa-solid fa-seedling" style="color:var(--color-brand-green);margin-right:4px;"></i>
                            <%= t.getNombreCultivo() != null ? t.getNombreCultivo() : "N/A" %>
                        </div>
                        <div class="job-card__meta">
                            <i class="fa-solid fa-calendar-check" style="color:var(--color-action-green);margin-right:4px;"></i>
                            Finalizado: <strong><%= t.getFechaFinalizacion() %></strong>
                        </div>
                    </div>
                    <% if (t.getObservaciones() != null && !t.getObservaciones().isEmpty()) { %>
                        <p class="job-card__desc" style="margin-top:var(--spacing-sm);">
                            <strong>Observaciones:</strong> <%= t.getObservaciones() %>
                        </p>
                    <% } else { %>
                        <p class="job-card__desc" style="margin-top:var(--spacing-sm); font-style:italic;">
                            Sin observaciones registradas.
                        </p>
                    <% } %>
                </article>
            <%
                    }
                }
            %>
        </div>
    </div>

</main>
</body>
</html>
