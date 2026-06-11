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
    <title>Monitoreo de Trabajos - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp" class="list-header__back">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Monitoreo de Trabajos</h1>
        <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=prepararCreacion" class="btn btn--new" style="margin-left:auto;">
            <i class="fa-solid fa-circle-plus"></i> Asignar trabajo
        </a>
    </header>

    <%-- Filtros para el Administrador --%>
    <div class="filtros-estado" id="filtrosEstado">
        <span><i class="fa-solid fa-filter"></i> Filtrar:</span>
        <button class="filtro-btn seleccionado" data-filtro="todos">Todos</button>
        <button class="filtro-btn" data-filtro="pendiente">Pendientes</button>
        <button class="filtro-btn" data-filtro="finalizado">Finalizados</button>
    </div>

    <div class="panel">
        <div class="job-list">
            <%
                List<trabajo> lista = (List<trabajo>) request.getAttribute("listaTrabajos");
                if (lista == null || lista.isEmpty()) {
            %>
                <p class="no-data"><i class="fa-solid fa-circle-info"></i> No hay trabajos registrados actualmente.</p>
            <%
                } else {
                    for (trabajo t : lista) {
                        boolean finalizado = (t.getFechaFinalizacion() != null);
            %>
                <article class="job-card" data-estado="<%= finalizado ? "finalizado" : "pendiente" %>">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                        <h2 class="job-card__title" style="margin:0;"><%= t.getNombre() %></h2>
                        <span class="badge <%= finalizado ? "badge--activo" : "badge--pendiente" %>">
                            <%= finalizado ? "Finalizado" : "Pendiente" %>
                        </span>
                    </div>

                    <div class="job-card__info" style="display:grid; grid-template-columns: 1fr 1fr; gap:10px; margin-bottom:10px;">
                        <p><strong><i class="fa-solid fa-user"></i> Encargado:</strong> <%= t.getNombreUsuario() %></p>
                        <p><strong><i class="fa-solid fa-seedling"></i> Cultivo:</strong> <%= t.getNombreCultivo() %></p>
                        <p><strong><i class="fa-solid fa-calendar"></i> Asignado:</strong> <%= t.getFechaAsignacion() %></p>
                        <% if (finalizado) { %>
                            <p><strong><i class="fa-solid fa-check-double"></i> Terminado:</strong> <%= t.getFechaFinalizacion() %></p>
                        <% } %>
                    </div>

                    <p class="job-card__desc" style="border-top:1px solid #eee; padding-top:10px;">
                        <strong>Descripción:</strong> <%= t.getDescripcion() %>
                    </p>

                    <div class="obs-container" style="margin-top:10px; padding:10px; background:#f9f9f9; border-left:4px solid var(--color-brand-green); border-radius:4px;">
                        <strong><i class="fa-solid fa-pen-to-square"></i> Comentarios del trabajador:</strong>
                        <p style="margin:5px 0 0 0; color: #555;">
                            <%= (t.getObservaciones() != null && !t.getObservaciones().isEmpty()) ? t.getObservaciones() : "Sin observaciones aún." %>
                        </p>
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