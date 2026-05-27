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
                CultivoDao cultivoDao = new CultivoDao();
                // NOTA: Recuerda que eliminamos área e insumos, así que ajusta el método si es necesario
                List<cultivo> listaCultivos = cultivoDao.listarCultivos(); 
                
                if(listaCultivos.isEmpty()) {
            %>
                <article class="crop-card"><p>No hay cultivos registrados.</p></article>
            <% } else {
                for(cultivo c : listaCultivos) { %>
                <article class="crop-card">
                    <div class="crop-card__image-container">
                        <img src="img/<%= c.getNombreCultivo().toLowerCase().contains("lulo") ? "image 5.png" : "image 4.png" %>" class="crop-card__img">
                    </div>
                    <div class="crop-card__content">
                        <h2 class="crop-card__title"><%= c.getNombreCultivo() %></h2>
                        <p class="crop-card__description">Tipo: <%= c.getTipoCultivo() %></p>
                        <div class="crop-card__actions">
                            <button class="btn btn--add">Agregar registro</button>
                            <a href="formulario_cultivo.jsp?editId=<%= c.getIdCultivo() %>" class="btn btn--edit">Editar</a>
                            <a href="${pageContext.request.contextPath}/ServletCultivo?accion=eliminar&id=<%= c.getIdCultivo() %>" class="btn btn--delete">Eliminar</a>
                        </div>
                    </div>
                </article>
            <% }} %>
            <div class="crop-list__footer">
                <a href="formulario_cultivo.jsp" class="btn btn--new"><i class="fa-solid fa-circle-plus"></i> Agregar cultivo</a>
            </div>
        </section>
    </main>
    <script src="${pageContext.request.contextPath}/js/scripts.js"></script>
</body>
</html>