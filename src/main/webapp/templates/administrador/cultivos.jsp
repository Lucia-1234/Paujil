<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.dao.CultivoDao, com.paujil.modelo.cultivo, java.util.List"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/estilos.css">
    <title>Gestión de Cultivos</title>
</head>
<body>
    <main class="container">
        <section class="crop-list">
            <%
                CultivoDao dao = new CultivoDao();
                List<cultivo> lista = dao.listarCultivos();
                if(lista.isEmpty()) { %>
                    <article class="crop-card"><p>No hay cultivos registrados.</p></article>
            <% } else {
                for(cultivo c : lista) { %>
                <article class="crop-card">
                    <div class="crop-card__content">
                        <h2 class="crop-card__title"><%= c.getNombreCultivo() %></h2>
                        <p class="crop-card__description">Tipo: <%= c.getTipoCultivo() %></p>
                        <p class="crop-card__dates">
                            Siembra: <%= c.getFechaSiembra() %> | Cosecha: <%= c.getFechaCosecha() %>
                        </p>
                        <div class="crop-card__actions">
                            <button class="btn btn--edit" onclick="abrirModalEditar('<%= c.getIdCultivo() %>', '<%= c.getNombreCultivo() %>', '<%= c.getTipoCultivo() %>', '<%= c.getFechaSiembra() %>', '<%= c.getFechaCosecha() %>')">Editar</button>
                            <button class="btn btn--delete" onclick="confirmarEliminar('<%= c.getIdCultivo() %>')">Eliminar</button>
                        </div>
                    </div>
                </article>
            <% }} %>
        </section>
    </main>

    <div id="modalEditar" class="modal-overlay">
        <div class="modal-content">
            <span class="modal-close" onclick="cerrarModalEditar()">&times;</span>
            <form action="${pageContext.request.contextPath}/ServletCultivo" method="POST">
                <input type="hidden" name="accion" value="editar">
                <input type="hidden" id="editId" name="id">
                
                <label>Nombre</label>
                <input type="text" id="editNombre" name="nombreCultivo" required>
                
                <label>Tipo</label>
                <input type="text" id="editTipo" name="tipoCultivo">
                
                <label>Fecha Siembra</label>
                <input type="date" id="editSiembra" name="fechaSiembra">
                
                <label>Fecha Cosecha</label>
                <input type="date" id="editCosecha" name="fechaCosecha">
                
                <button type="submit">Guardar</button>
            </form>
        </div>
    </div>

    <div id="modalConfirmacion" class="modal-overlay">
        <div class="modal-content">
            <h2>¿Seguro que deseas eliminar?</h2>
            <button onclick="cerrarModal()">Cancelar</button>
            <a id="btnConfirmarEliminar" href="#" class="btn btn--delete">Eliminar</a>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/scripts.js"></script>
</body>
</html>