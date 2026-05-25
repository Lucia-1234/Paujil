
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List, com.paujil.modelo.usuario, com.paujil.dao.UsuarioDao"%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Asignar Roles</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/Styles.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
</head>
<body>
    <main class="role-manager">
        <header class="role-manager__header">
            <a href="menu_administrador.jsp" class="role-manager__back-link">
                <i class="fa-solid fa-arrow-left-long"></i>
            </a>
            <h1 class="role-manager__title">Asignar rol</h1>
        </header>

        <section class="role-manager__content">
            <ul class="request-list">
                <%
                    UsuarioDao dao = new UsuarioDao();
                    List<usuario> lista = dao.listarUsuariosPendientes();
                    
                    if (lista != null && !lista.isEmpty()) {
                        for (usuario u : lista) {
                %>
                            <li class="request-item" id="user-<%= u.getIdUsuario() %>">
                                <div class="request-item__user-info">
                                    <div class="request-item__avatar"><i class="fa-solid fa-user"></i></div>
                                    <div class="request-item__details">
                                        <h2 class="request-item__name"><%= u.getNombre() %></h2>
                                        <p class="request-item__type">ID: <%= u.getIdUsuario() %></p>
                                    </div>
                                </div>
                                
                                <div class="request-item__actions">
                                    <button type="button" class="btn btn--accept" 
                                            onclick="gestionarUsuario(<%= u.getIdUsuario() %>, 'aceptar')">
                                        Aceptar
                                    </button>

                                    <button type="button" class="btn btn--deny" style="background-color: #dc3545;"
                                            onclick="gestionarUsuario(<%= u.getIdUsuario() %>, 'denegar')">
                                        Denegar
                                    </button>
                                </div>
                            </li>
                <%
                        } 
                    } else {
                %>
                        <li style="color:white; padding: 20px; text-align: center;">No hay usuarios pendientes.</li>
                <%
                    } 
                %>
            </ul>
        </section>
    </main>

    <script src="${pageContext.request.contextPath}/static/JS/scripts.js"></script>
</body>
</html>