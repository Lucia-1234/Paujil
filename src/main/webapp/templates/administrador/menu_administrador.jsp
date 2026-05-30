<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("idUsuario") == null || !"administrador".equalsIgnoreCase((String)session.getAttribute("rolUsuario"))) {
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
    <title>Panel Administrador | Finca El Paujil</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/reset.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/cards.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/button.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/menu.css">
</head>
<body class="admin-dashboard">
    <div class="layout-wrapper">
        <header class="admin-header">
            <h1 class="admin-header__title">Panel de Control</h1>
            <a href="${pageContext.request.contextPath}/ServletLogout" 
               class="btn btn--danger btn--small" 
               onclick="return confirm('¿Cerrar sesión?')">Cerrar sesión</a>
        </header>

        <main class="admin-panel">
            <div class="profile-card profile-card--horizontal">
                <img src="${pageContext.request.contextPath}/static/IMG/admin.png" alt="Perfil" class="profile-card__img">
                <div class="profile-card__info">
                    <p class="profile-card__role">Administrador</p>
                    <p class="profile-card__name"><%= (nombreUsuario != null) ? nombreUsuario : "Usuario" %></p>
                </div>
            </div>

            <nav class="dashboard-grid">
                <%-- Cada enlace ahora usa la clase modular 'card dashboard-card' --%>
                <a href="${pageContext.request.contextPath}/GestionarRoles" class="card dashboard-card">
                    <span class="dashboard-card__label">Asignar Rol</span>
                </a>
                <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=prepararCreacion" class="card dashboard-card">
                    <span class="dashboard-card__label">Asignar Trabajo</span>
                </a>
                <a href="${pageContext.request.contextPath}/ServletUsuario?accion=listar" class="card dashboard-card">
                    <span class="dashboard-card__label">Usuarios</span>
                </a>
                <a href="${pageContext.request.contextPath}/ServletCultivo" class="card dashboard-card">
                    <span class="dashboard-card__label">Cultivos</span>
                </a>
                <a href="${pageContext.request.contextPath}/ServletBiopreparado" class="card dashboard-card">
                    <span class="dashboard-card__label">Biopreparados</span>
                </a>
                <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=listar" class="card dashboard-card">
                    <span class="dashboard-card__label">Gestión Trabajos</span>
                </a>
            </nav>
        </main>
    </div>
</body>
</html>