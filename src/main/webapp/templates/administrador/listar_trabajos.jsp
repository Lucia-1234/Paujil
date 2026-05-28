<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.trabajo, com.paujil.dao.TrabajoDao, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/Styles.css">
    <title>Gestión de Trabajos - Finca El Paujil</title>
</head>
<body>
    <main class="container">
        <header class="list-header">
            <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
               class="list-header__back">
                <i class="fa-solid fa-arrow-left"></i> Volver
            </a>
            <h1 class="list-header__title">Trabajos asignados</h1>
            <a href="${pageContext.request.contextPath}/templates/administrador/agregar_trabajo.jsp"
               class="btn btn--new">
                <i class="fa-solid fa-circle-plus"></i> Agregar trabajo
            </a>
        </header>

        <%-- Mensajes de estado --%>
        <% String status = request.getParameter("status"); %>
        <% if ("success".equals(status)) { %>
            <div class="feedback-message feedback-message--ok">Trabajo registrado correctamente.</div>
        <% } else if ("eliminado".equals(status)) { %>
            <div class="feedback-message feedback-message--ok">Trabajo eliminado correctamente.</div>
        <% } else if ("error".equals(status)) { %>
            <div class="feedback-message feedback-message--error">Ocurrió un error. Intente nuevamente.</div>
        <% } %>

        <div class="job-list">
            <%
                TrabajoDao dao = new TrabajoDao();
                // CORRECCIÓN: el método correcto es listarTrabajosCompletos(), no listarTrabajos()
                List<trabajo> lista = dao.listarTrabajosCompletos();
                if (lista.isEmpty()) {
            %>
                <p class="no-data">No hay trabajos registrados.</p>
            <%
                } else {
                    for (trabajo t : lista) {
            %>
                <article class="job-card">
                    <div class="job-card__info">
                        <h2 class="job-card__title"><%= t.getNombre() %></h2>
                        <p class="job-card__desc"><%= t.getDescripcion() %></p>
                        <div class="job-card__meta">
                            <span><i class="fa-solid fa-calendar"></i> <%= t.getFechaAsignacion() %></span>
                            <span><i class="fa-solid fa-seedling"></i> <%= t.getNombreCultivo() != null ? t.getNombreCultivo() : "—" %></span>
                            <span><i class="fa-solid fa-user"></i> <%= t.getNombreUsuario() != null ? t.getNombreUsuario() : "—" %></span>
                        </div>
                    </div>
                    <div class="job-card__actions">
                        <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=eliminar&id=<%= t.getId() %>"
                           class="btn btn--delete"
                           onclick="return confirm('¿Seguro que deseas eliminar este trabajo?')">
                            <i class="fa-solid fa-trash"></i> Eliminar
                        </a>
                    </div>
                </article>
            <%
                    }
                }
            %>
        </div>
    </main>

    <script src="${pageContext.request.contextPath}/static/JS/scripts.js"></script>
</body>
</html>