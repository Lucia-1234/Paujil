<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return; // Detiene la carga del resto del JSP
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Panel Administrador - Finca El Paujil</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/CSS/Styles.css">
</head>
<body>
    
    <header class="admin-header">
        <h1 class="admin-header__title">Administrador</h1>
    </header>

    <main class="admin-panel">
        <section class="admin-panel__container">
            
            <div class="profile-card">
                <div class="profile-card__image-container">
                    <img src="${pageContext.request.contextPath}/static/IMG/download 4.png" alt="Perfil" class="profile-card__img">
                </div>
            </div>

            <nav class="dashboard-grid">
                <a href="${pageContext.request.contextPath}/asignar_rol.jsp" class="dashboard-grid__item">Asignar rol</a>
                <a href="${pageContext.request.contextPath}/asignar_trabajos.jsp" class="dashboard-grid__item">Asignar trabajos</a>
                <a href="${pageContext.request.contextPath}/administrar_usuarios.jsp" class="dashboard-grid__item">Administrar usuarios</a>
                <a href="${pageContext.request.contextPath}/cultivos.jsp" class="dashboard-grid__item">Cultivos</a>
                <a href="${pageContext.request.contextPath}/insumos.jsp" class="dashboard-grid__item">Insumos</a>
                <a href="${pageContext.request.contextPath}/biopreparados.jsp" class="dashboard-grid__item">Biopreparados</a>
                <a href="${pageContext.request.contextPath}/gestion_trabajos.jsp" class="dashboard-grid__item">Gestión de trabajos</a>
                <a href="${pageContext.request.contextPath}/Perfil.jsp" class="dashboard-grid__item">Perfil</a>
            </nav> 
            
        </section>
    </main>

</body>
</html>