<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.asignacion, java.util.List"%>
<%
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }
    List<asignacion> lista = (List<asignacion>) request.getAttribute("listaFinalizados");
    if (lista == null) {
        response.sendRedirect(request.getContextPath() + "/ServletTrabajo?accion=finalizados");
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
           class="list-header__back" aria-label="Volver a mis trabajos">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Historial de trabajos finalizados</h1>
    </header>

    <div class="panel">
        <div class="job-list">
            <% if (lista.isEmpty()) { %>
                <p class="no-data">
                    <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                    No tienes trabajos finalizados actualmente.
                </p>
            <% } else {
                for (asignacion a : lista) { %>
                <article class="job-card">

                    <%-- Cabecera: nombre (tipo) + badge --%>
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:var(--spacing-sm);">
                        <%--
                            CORRECCIÓN 8: getNombreTrabajo() eliminado — columna inexistente en BD.
                            Se usa getNombreTipoTrabajo() como identificador del trabajo.
                        --%>
                        <h2 class="job-card__title" style="margin-bottom:0;">
                            <i class="fa-solid fa-tag" style="margin-right:4px;"></i>
                            <%= a.getNombreTipoTrabajo() %>
                        </h2>
                        <span class="job-status job-status--done">
                            <i class="fa-solid fa-circle-check" style="margin-right:4px;"></i>Finalizado
                        </span>
                    </div>

                    <%-- Meta --%>
                    <div class="job-card__info">
                        <div class="job-card__meta">
                            <i class="fa-solid fa-seedling" style="color:var(--color-brand-green);margin-right:4px;"></i>
                            <%= a.getNombreCultivo() != null ? a.getNombreCultivo() : "N/A" %>
                        </div>
                        <div class="job-card__meta">
                            <i class="fa-solid fa-calendar" style="color:var(--color-brand-green);margin-right:4px;"></i>
                            Asignado: <%= a.getFechaAsignacion() %>
                        </div>
                        <% if (a.getFechaInicio() != null) { %>
                        <div class="job-card__meta">
                            <i class="fa-solid fa-play" style="color:var(--color-brand-green);margin-right:4px;"></i>
                            Iniciado: <%= a.getFechaInicio() %>
                        </div>
                        <% } %>
                        <% if (a.getFechaFinalizacion() != null) { %>
                        <div class="job-card__meta">
                            <i class="fa-solid fa-calendar-check" style="color:var(--color-action-green);margin-right:4px;"></i>
                            Finalizado: <strong><%= a.getFechaFinalizacion() %></strong>
                        </div>
                        <% } %>
                    </div>

                    <%-- Descripción --%>
                    <p class="job-card__desc" style="margin-top:var(--spacing-sm);">
                        <%= a.getDescripcionTrabajo() %>
                    </p>

                    <%-- Observaciones --%>
                    <% if (a.getObservaciones() != null && !a.getObservaciones().isEmpty()) { %>
                        <div class="obs-container" style="margin-top:var(--spacing-sm); padding:10px;
                             background:#f9f9f9; border-left:4px solid var(--color-brand-green); border-radius:4px;">
                            <strong><i class="fa-solid fa-pen-to-square"></i> Observaciones:</strong>
                            <p style="margin:5px 0 0 0; color:#555;"><%= a.getObservaciones() %></p>
                        </div>
                    <% } else { %>
                        <p class="job-card__desc" style="margin-top:var(--spacing-sm); font-style:italic; color:#999;">
                            Sin observaciones registradas.
                        </p>
                    <% } %>

                </article>
            <% } } %>
        </div>
    </div>

</main>
</body>
</html>
