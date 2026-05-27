<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.trabajo, com.paujil.dao.TrabajoDao, java.util.List"%>

<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="css/estilos.css">
    <title>Gestión de Trabajos</title>
</head>
<body>
    <button id="btnNuevo">Agregar Trabajo</button>

    <div class="job-list">
        <% 
            TrabajoDao dao = new TrabajoDao();
            List<trabajo> lista = dao.listarTrabajos(); 
            for(trabajo t : lista) { 
        %>
            <article class="job-card">
                <h2><%= t.getNombre() %></h2>
                <p><%= t.getDescripcion() %></p>
                <div class="job-data">
                    <span>Fecha: <%= t.getFechaAsignacion() %></span>
                </div>
                <button onclick="editarTrabajo('<%= t.getId() %>')">Editar</button>
            </article>
        <% } %>
    </div>

    <div id="modal" class="hidden">
        <form action="ServletTrabajo" method="POST">
            <input type="hidden" name="id" id="inputId">
            <input type="text" name="nombreTrabajo" placeholder="Nombre" required>
            <textarea name="descripcion" placeholder="Descripción"></textarea>
            <input type="date" name="fechaAsignacion" required>
            <button type="submit">Guardar</button>
        </form>
    </div>

    <script src="js/scripts.js"></script>
</body>
</html>