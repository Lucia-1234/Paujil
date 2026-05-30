<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.trabajo, java.util.List"%>
<%
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }

    List<trabajo> lista = (List<trabajo>) request.getAttribute("listaMisTrabajos");
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

    <%-- Mensajes de feedback --%>
    <% if ("finalizado".equals(status)) { %>
        <p class="alert alert--ok">Trabajo marcado como finalizado.</p>
    <% } else if ("guardado".equals(status)) { %>
        <p class="alert alert--ok">Avance guardado correctamente.</p>
    <% } else if ("error".equals(status)) { %>
        <p class="alert alert--error">Ocurrió un error. Intenta de nuevo.</p>
    <% } %>

    <% if (lista.isEmpty()) { %>
        <p class="empty-msg">No tienes tareas pendientes en este momento.</p>
    <% } else {
        for (trabajo t : lista) {
            boolean finalizado = (t.getFechaFinalizacion() != null);
    %>
        <article class="bio-card <%= finalizado ? "bio-card--done" : "" %>">
            <div class="bio-card__header">
                <h2 class="bio-card__title"><%= t.getNombre() %></h2>
                <span class="badge <%= finalizado ? "badge--done" : "badge--pending" %>">
                    <%= finalizado ? "Finalizado" : "Pendiente" %>
                </span>
            </div>

            <p><strong>Cultivo:</strong> <%= t.getNombreCultivo() != null ? t.getNombreCultivo() : "N/A" %></p>
            <p><strong>Descripción:</strong> <%= t.getDescripcion() %></p>
            <p><small>Asignación: <strong><%= t.getFechaAsignacion() %></strong></small></p>

            <% if (finalizado) { %>
                <p class="status-done">✅ Finalizado el: <%= t.getFechaFinalizacion() %></p>
                <% if (t.getObservaciones() != null && !t.getObservaciones().isEmpty()) { %>
                    <p><strong>Observaciones:</strong> <%= t.getObservaciones() %></p>
                <% } %>
            <% } else { %>
                <form action="${pageContext.request.contextPath}/ServletTrabajo" method="POST">
                    <input type="hidden" name="accion" value="actualizarEstado">
                    <input type="hidden" name="idTrabajo" value="<%= t.getId() %>">

                    <label for="obs_<%= t.getId() %>">Observaciones:</label>
                    <textarea id="obs_<%= t.getId() %>" name="observaciones" rows="3"
                        placeholder="Escribe tus notas aquí..."><%= t.getObservaciones() != null ? t.getObservaciones() : "" %></textarea>

                    <div class="actions">
                        <button type="submit" name="btnAccion" value="guardar"
                                class="btn">Guardar avance</button>
                        <button type="submit" name="btnAccion" value="finalizar"
                                class="btn btn--ok"
                                onclick="return confirm('¿Confirmas que este trabajo está finalizado?')">Finalizar</button>
                    </div>
                </form>
            <% } %>
        </article>
    <%
        } // fin for
    } // fin else
    %>

</main>
</body>
</html>