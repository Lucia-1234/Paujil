<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.trabajo, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
    <title>Trabajos Asignados - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <%
        String status = request.getParameter("status");
        if ("success".equals(status)) {
    %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Acción realizada correctamente.
        </div>
    <% } else if ("error".equals(status)) { %>
        <div class="feedback-message feedback-message--error">
            <i class="fa-solid fa-circle-exclamation"></i> Ocurrió un error. Intente nuevamente.
        </div>
    <% } %>

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
           class="list-header__back" aria-label="Volver al menú">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Trabajos asignados</h1>
        <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=prepararCreacion"
           class="btn btn--new" style="margin-left:auto;">
            <i class="fa-solid fa-circle-plus"></i> Agregar trabajo
        </a>
    </header>

    <div class="panel">
        <div class="job-list">
            <%
                List<trabajo> lista = (List<trabajo>) request.getAttribute("listaTrabajos");
                if (lista == null || lista.isEmpty()) {
            %>
                <p class="no-data">
                    <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                    No hay trabajos registrados.
                </p>
            <%
                } else {
                    for (trabajo t : lista) {
            %>
                <article class="job-card">
                    <h2 class="job-card__title"><%= t.getNombre() %></h2>
                    <p class="job-card__desc"><%= t.getDescripcion() %></p>
                    <div class="job-card__info">
                        <div class="job-card__meta">
                            <i class="fa-solid fa-calendar" style="color:var(--color-brand-green);margin-right:4px;"></i>
                            <%= t.getFechaAsignacion() %>
                        </div>
                        <div class="job-card__meta">
                            <i class="fa-solid fa-seedling" style="color:var(--color-brand-green);margin-right:4px;"></i>
                            <%= t.getNombreCultivo() != null ? t.getNombreCultivo() : "—" %>
                        </div>
                        <div class="job-card__meta">
                            <i class="fa-solid fa-user" style="color:var(--color-brand-green);margin-right:4px;"></i>
                            <%= t.getNombreUsuario() != null ? t.getNombreUsuario() : "—" %>
                        </div>
                    </div>
                    <div class="job-card__actions">
                        <button class="btn btn--delete"
                                data-id="<%= t.getId() %>"
                                onclick="abrirModalEliminarTrabajo(this.dataset.id)">
                            <i class="fa-solid fa-trash"></i> Eliminar
                        </button>
                    </div>
                </article>
            <%
                    }
                }
            %>
        </div>
    </div>
</main>

<%-- Modal Confirmación Eliminación --%>
<div id="modalConfirmacionTrabajo" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
    <div class="confirm-modal">
        <div class="confirm-modal__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
        <p class="confirm-modal__text">¿Eliminar este<br>trabajo?</p>
        <div class="confirm-modal__actions">
            <button type="button" class="btn btn--cancel" onclick="cerrarModal('modalConfirmacionTrabajo')">Cancelar</button>
            <button type="button" class="btn--confirm-delete" onclick="ejecutarEliminacionTrabajo()">Eliminar</button>
        </div>
    </div>
</div>
<script>window._ctxPath = '${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
</body>
</html>
