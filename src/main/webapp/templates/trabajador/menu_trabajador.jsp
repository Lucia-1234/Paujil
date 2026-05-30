<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }
    String nombreUsuario = (String) session.getAttribute("nombreUsuario");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Trabajador - Finca El Paujil</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/menu.css">
</head>
<body>
    <div class="trabaja-layout">
        <header class="trabaja-header">
            <h1 class="trabaja-header__title">Trabajador</h1>
            <a href="${pageContext.request.contextPath}/ServletLogout"
               class="btn btn--danger"
               onclick="return confirm('¿Cerrar sesión?')">Cerrar sesión</a>
        </header>
        <main class="trabaja-panel">
            <section class="trabaja-panel__container">
                <div class="profile-card">
                    <div class="profile-card__image-container">
                        <img src="${pageContext.request.contextPath}/static/IMG/trabajador.png"
                             alt="Perfil" class="profile-card__img">
                    </div>
                    <% if (nombreUsuario != null) { %>
                        <p class="profile-card__name"><%= nombreUsuario %></p>
                    <% } %>
                </div>
                <nav class="dashboard-grid">
                    <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=misTrabajos"
                       class="dashboard-grid__item">Trabajos asignados</a>
                    <a href="${pageContext.request.contextPath}/ServletCultivo?accion=verTrabajador"
                       class="dashboard-grid__item">Cultivos</a>
                    <a href="${pageContext.request.contextPath}/ServletBiopreparado?accion=verTrabajador"
                       class="dashboard-grid__item">Recetario de biopreparados</a>
                    <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=finalizados"
                       class="dashboard-grid__item">Trabajos completados</a>
                </nav>
            </section>
        </main>
    </div>
</body>
</html>