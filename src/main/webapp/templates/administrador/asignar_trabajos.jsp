<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.cultivo, com.paujil.modelo.usuario, java.util.List"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
    <title>Asignar Trabajo - Finca El Paujil</title>
</head>
<body>
    <main class="container">
        <header class="list-header">
            <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=listar" class="list-header__back">← Volver</a>
            <h1 class="list-header__title">Asignar nuevo trabajo</h1>
        </header>

        <% String mensajeError = (String) request.getAttribute("mensajeError");
           if (mensajeError != null) { %>
            <div class="feedback-message feedback-message--error"><%= mensajeError %></div>
        <% } %>

        <form action="${pageContext.request.contextPath}/ServletTrabajo" method="POST">

            <label>Cultivo:</label>
            <select name="idCultivo" required>
                <option value="">-- Seleccione un cultivo --</option>
                <%
                    List<cultivo> cultivos = (List<cultivo>) request.getAttribute("listaCultivos");
                    if (cultivos != null) {
                        for (cultivo c : cultivos) {
                %>
                    <option value="<%= c.getIdCultivo() %>"><%= c.getNombreCultivo() %></option>
                <%      }
                    }
                %>
            </select>

            <label>Trabajador:</label>
            <select name="idUsuario" required>
                <option value="">-- Seleccione un trabajador --</option>
                <%
                    List<usuario> usuarios = (List<usuario>) request.getAttribute("listaUsuarios");
                    if (usuarios != null) {
                        for (usuario u : usuarios) {
                %>
                    <option value="<%= u.getIdUsuario() %>"><%= u.getNombre() %></option>
                <%      }
                    }
                %>
            </select>

            <label>Nombre del trabajo:</label>
            <input type="text" name="nombreTrabajo" required>

            <label>Descripción:</label>
            <textarea name="descripcion" required></textarea>

            <label>Fecha de asignación:</label>
            <input type="date" name="fechaAsignacion" required>

            <button type="submit" class="btn btn--new">Guardar asignación</button>
            <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=listar">Cancelar</a>
        </form>
    </main>
</body>
</html>