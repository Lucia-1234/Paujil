<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.asignacion, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
    <title>Monitoreo de Trabajos - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
           class="list-header__back">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Gestión de Trabajos</h1>
        <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=prepararCreacion"
           class="btn btn--new" style="margin-left:auto;">
            <i class="fa-solid fa-circle-plus"></i> Asignar trabajo
        </a>
    </header>

    <%-- Feedback --%>
    <% String status = request.getParameter("status");
       if ("success".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Trabajo asignado correctamente.
        </div>
    <% } else if ("eliminado".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Trabajo eliminado correctamente.
        </div>
    <% } else if ("error".equals(status)) { %>
        <div class="feedback-message feedback-message--error">
            <i class="fa-solid fa-circle-exclamation"></i> Ocurrió un error. Intenta de nuevo.
        </div>
    <% } %>

    <div class="filtros-estado">
        <span><i class="fa-solid fa-filter"></i> Filtrar:</span>
        <button class="filtro-btn seleccionado" data-filtro="todos">Todos</button>
        <button class="filtro-btn" data-filtro="Pendiente">Pendientes</button>
        <button class="filtro-btn" data-filtro="En proceso">En proceso</button>
        <button class="filtro-btn" data-filtro="Finalizado">Finalizados</button>
    </div>

    <div class="panel">
        <div class="job-list">
            <%
                List<asignacion> lista = (List<asignacion>) request.getAttribute("listaAsignaciones");
                if (lista == null || lista.isEmpty()) {
            %>
                <p class="no-data">
                    <i class="fa-solid fa-circle-info"></i> No hay trabajos registrados.
                </p>
            <%
                } else {
                    for (asignacion a : lista) {
            %>
                <article class="job-card" data-estado="<%= a.getEstadoTrabajo() %>">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                        <h2 class="job-card__title" style="margin:0;"><%= a.getNombreTrabajo() %></h2>
                        <span class="badge
                            <%= "Finalizado".equals(a.getEstadoTrabajo()) ? "badge--activo"
                              : "En proceso".equals(a.getEstadoTrabajo()) ? "badge--enproceso"
                              : "badge--pendiente" %>">
                            <%= a.getEstadoTrabajo() %>
                        </span>
                    </div>

                    <div class="job-card__info" style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:10px;">
                        <p><strong><i class="fa-solid fa-tag"></i> Tipo:</strong> <%= a.getNombreTipoTrabajo() %></p>
                        <p><strong><i class="fa-solid fa-user"></i> Encargado:</strong> <%= a.getNombreUsuario() %></p>
                        <p><strong><i class="fa-solid fa-seedling"></i> Cultivo:</strong> <%= a.getNombreCultivo() %></p>
                        <p><strong><i class="fa-solid fa-calendar"></i> Asignado:</strong> <%= a.getFechaAsignacion() %></p>
                        <% if (a.getFechaInicio() != null) { %>
                            <p><strong><i class="fa-solid fa-play"></i> Iniciado:</strong> <%= a.getFechaInicio() %></p>
                        <% } %>
                        <% if (a.getFechaFinalizacion() != null) { %>
                            <p><strong><i class="fa-solid fa-check-double"></i> Terminado:</strong> <%= a.getFechaFinalizacion() %></p>
                        <% } %>
                    </div>

                    <p class="job-card__desc" style="border-top:1px solid #eee; padding-top:10px;">
                        <strong>Descripción:</strong> <%= a.getDescripcionTrabajo() %>
                    </p>

                    <% if (a.getObservaciones() != null && !a.getObservaciones().isEmpty()) { %>
                    <div class="obs-container" style="margin-top:10px; padding:10px; background:#f9f9f9; border-left:4px solid var(--color-brand-green); border-radius:4px;">
                        <strong><i class="fa-solid fa-pen-to-square"></i> Observaciones:</strong>
                        <p style="margin:5px 0 0 0; color:#555;"><%= a.getObservaciones() %></p>
                    </div>
                    <% } %>

                    <div class="job-card__actions" style="margin-top:10px;">
                        <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=eliminar&id=<%= a.getIdTrabajo() %>"
                           class="btn btn--delete"
                           onclick="return confirm('¿Eliminar este trabajo y todas sus asignaciones?')">
                            <i class="fa-solid fa-trash"></i> Eliminar
                        </a>
                    </div>
                </article>
            <%
                    }
                }
            %>
        </div>
    </div>
</main>
<script src="${pageContext.request.contextPath}/static/js/filtros-trabajos.js"></script>
</body>
</html>