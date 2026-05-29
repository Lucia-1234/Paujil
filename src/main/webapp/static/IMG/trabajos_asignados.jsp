<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.trabajo, java.util.List"%>
<%
    // Guarda de sesión
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }
    String status = request.getParameter("status");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mis Trabajos - Finca El Paujil</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
</head>
<body>
<main class="container">

    <div class="list-header">
        <h1 class="list-header__title">Mis trabajos asignados</h1>
        <a href="${pageContext.request.contextPath}/templates/trabajador/menu_trabajador.jsp"
        class="btn btn--secondary">← Volver al menú</a>
    </div>

    <%-- Mensajes de retroalimentación --%>
    <% if ("finalizado".equals(status)) { %>
        <p class="alert alert--ok"> Trabajo marcado como finalizado.</p>
    <% } else if ("guardado".equals(status)) { %>
        <p class="alert alert--ok"> Avance guardado correctamente.</p>
    <% } else if ("error".equals(status)) { %>
        <p class="alert alert--error"> Ocurrió un error. Intenta de nuevo.</p>
    <% } %>

    <%
        List<trabajo> lista = (List<trabajo>) request.getAttribute("listaMisTrabajos");
        if (lista == null || lista.isEmpty()) {
    %>
        <p class="empty-msg">No tienes tareas pendientes en este momento.</p>
    <%
        } else {
            for (trabajo t : lista) {
                boolean finalizado = (t.getFechaFinalizacion() != null);
    %>
        <article class="bio-card <%= finalizado ? "bio-card--done" : "" %>">

            <div class="bio-card__header">
                <h2 class="bio-card__title"><%= t.getNombre() %></h2>
                <% if (finalizado) { %>
                    <span class="badge badge--done">Finalizado</span>
                <% } else { %>
                    <span class="badge badge--pending">Pendiente</span>
                <% } %>
            </div>

            <p><strong>Cultivo:</strong> <%= t.getNombreCultivo() %></p>
            <p><strong>Descripción:</strong> <%= t.getDescripcion() %></p>
            <p><small> Fecha de asignación: <strong><%= t.getFechaAsignacion() %></strong></small></p>

            <% if (finalizado) { %>
                <p><small> Finalizado el: <strong><%= t.getFechaFinalizacion() %></strong></small></p>
                <% if (t.getObservaciones() != null && !t.getObservaciones().isEmpty()) { %>
                    <p><strong>Observaciones:</strong> <%= t.getObservaciones() %></p>
                <% } %>
            <% } else { %>
                <form action="${pageContext.request.contextPath}/ServletTrabajo" method="POST">
                    <input type="hidden" name="accion"    value="actualizarEstado">
                    <input type="hidden" name="idTrabajo" value="<%= t.getId() %>">

                    <label for="obs_<%= t.getId() %>">Observaciones:</label>
                    <textarea id="obs_<%= t.getId() %>"
                            name="observaciones"
                            rows="3"
                            placeholder="Escribe tus notas sobre este trabajo..."><%= t.getObservaciones() != null ? t.getObservaciones() : "" %></textarea>

                    <div class="actions">
                        <button type="submit" name="btnAccion" value="guardar"
                                class="btn"> Guardar avance</button>
                        <button type="submit" name="btnAccion" value="finalizar"
                                class="btn btn--ok"
                                onclick="return confirm('¿Confirmas que este trabajo está finalizado?')">
                            ✅ Marcar como Finalizado
                        </button>
                    </div>
                </form>
            <% } %>

        </article>
    <%
            }
        }
    %>

</main>
</body>
</html>
