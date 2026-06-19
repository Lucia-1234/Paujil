<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.asignacion, java.util.List"%>
<%
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }
    List<asignacion> lista = (List<asignacion>) request.getAttribute("listaMisTrabajos");
    if (lista == null) {
        response.sendRedirect(request.getContextPath() + "/ServletTrabajo?accion=misTrabajos");
        return;
    }
    String status = request.getParameter("status");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
    <title>Mis Trabajos - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/trabajador/menu_trabajador.jsp"
           class="list-header__back" aria-label="Volver al menú">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Mis trabajos asignados</h1>
    </header>

    <% if ("Finalizado".equals(status) || "finalizado".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Trabajo marcado como finalizado.
        </div>
    <% } else if ("en_proceso".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Trabajo iniciado.
        </div>
    <% } else if ("guardado".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Avance guardado correctamente.
        </div>
    <% } else if ("error".equals(status)) { %>
        <div class="feedback-message feedback-message--error">
            <i class="fa-solid fa-circle-exclamation"></i> Ocurrió un error. Intenta de nuevo.
        </div>
    <% } %>

    <%-- Filtro por ESTADO --%>
    <div class="filtros-estado">
        <span><i class="fa-solid fa-filter"></i> Estado:</span>
        <button class="filtro-btn filtro-estado-btn seleccionado" data-filtro="todos">Todos</button>
        <button class="filtro-btn filtro-estado-btn" data-filtro="Pendiente">Pendientes</button>
        <button class="filtro-btn filtro-estado-btn" data-filtro="En proceso">En proceso</button>
        <button class="filtro-btn filtro-estado-btn" data-filtro="Finalizado">Finalizados</button>
    </div>

    <div class="panel" id="panelTrabajos">
        <div class="job-list" id="jobList">
            <% if (lista.isEmpty()) { %>
                <p class="no-data">
                    <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                    No tienes tareas asignadas en este momento.
                </p>
            <% } else {
                for (asignacion a : lista) {
                    boolean finalizado = "Finalizado".equals(a.getEstadoTrabajo());
                    boolean enProceso  = "En proceso".equals(a.getEstadoTrabajo());
                    String tipoEsc = a.getNombreTipoTrabajo() != null
                                     ? a.getNombreTipoTrabajo().replace("\"", "&quot;")
                                     : "";
            %>
                <article class="job-card"
                         data-estado="<%= a.getEstadoTrabajo() %>"
                         data-tipo="<%= tipoEsc %>">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:var(--spacing-sm);">
                        <h2 class="job-card__title" style="margin-bottom:0;">
                            <i class="fa-solid fa-tag" style="margin-right:4px;"></i>
                            <%= a.getNombreTipoTrabajo() %>
                        </h2>
                        <span class="job-status <%= finalizado ? "job-status--done" : enProceso ? "job-status--inprogress" : "job-status--pending" %>">
                            <%= a.getEstadoTrabajo() %>
                        </span>
                    </div>

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
                                <i class="fa-solid fa-circle-check" style="color:var(--color-action-green);margin-right:4px;"></i>
                                Finalizado: <%= a.getFechaFinalizacion() %>
                            </div>
                        <% } %>
                    </div>

                    <p class="job-card__desc" style="margin-top:var(--spacing-sm);">
                        <%= a.getDescripcionTrabajo() %>
                    </p>

                    <% if (finalizado) { %>
                        <% if (a.getObservaciones() != null && !a.getObservaciones().isEmpty()) { %>
                            <div class="obs-container" style="margin-top:var(--spacing-sm); padding:10px;
                                 background:#f9f9f9; border-left:4px solid var(--color-brand-green); border-radius:4px;">
                                <strong><i class="fa-solid fa-pen-to-square"></i> Observaciones:</strong>
                                <p style="margin:5px 0 0 0; color:#555;"><%= a.getObservaciones() %></p>
                            </div>
                        <% } %>
                    <% } else { %>
                        <form action="${pageContext.request.contextPath}/ServletTrabajo" method="POST"
                              style="margin-top:var(--spacing-md);">
                            <input type="hidden" name="accion"       value="actualizarEstado">
                            <input type="hidden" name="idAsignacion" value="<%= a.getId() %>">

                            <div class="job-form__row" style="grid-template-columns:1fr; gap:6px;">
                                <label class="job-form__label" for="obs_<%= a.getId() %>">
                                    <i class="fa-solid fa-pen-to-square" style="margin-right:4px;"></i> Observaciones
                                </label>
                                <textarea id="obs_<%= a.getId() %>" name="observaciones"
                                          class="job-form__textarea" rows="3"
                                          placeholder="Escribe tus notas o avances aquí..."><%= a.getObservaciones() != null ? a.getObservaciones() : "" %></textarea>
                            </div>

                            <div class="job-card__actions" style="margin-top:var(--spacing-sm);">
                                <% if (!enProceso) { %>
                                    <button type="submit" name="btnAccion" value="iniciar"
                                            class="btn btn--edit">
                                        <i class="fa-solid fa-play"></i> Iniciar
                                    </button>
                                <% } %>
                                <button type="submit" name="btnAccion" value="guardar"
                                        class="btn btn--edit">
                                    <i class="fa-solid fa-floppy-disk"></i> Guardar avance
                                </button>
                                <button type="submit" name="btnAccion" value="finalizar"
                                        class="btn btn--accept"
                                        onclick="return confirm('¿Confirmas que este trabajo está finalizado?')">
                                    <i class="fa-solid fa-circle-check"></i> Finalizar
                                </button>
                            </div>
                        </form>
                    <% } %>
                </article>
            <%
                }
            } %>
        </div>
        <p class="no-data" id="sinResultadosFiltro" style="display:none;">
            <i class="fa-solid fa-circle-info"></i> No hay trabajos que coincidan con el filtro seleccionado.
        </p>
    </div>

</main>
<script src="${pageContext.request.contextPath}/static/js/filtros-trabajos.js"></script>
</body>
</html>
