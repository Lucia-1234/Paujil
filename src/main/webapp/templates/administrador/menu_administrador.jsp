<%-- Arriba del todo, tu lógica de seguridad --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="dao.UsuarioDao, modelo.usuario, java.util.List"%>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/Styles.css">.

<!DOCTYPE html>
<html lang="es">
<head>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/Styles.css">
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
                <a href="asignar_rol.jsp" class="dashboard-grid__item">Asignar rol</a>
                <a href="asignar_trabajos.jsp" class="dashboard-grid__item">Asignar trabajos</a>
                
                <a href="administrar_usuarios.jsp" class="dashboard-grid__item">Administrar usuarios</a>
                
                <a href="cultivos.jsp" class="dashboard-grid__item">Cultivos</a>
                <a href="insumos.jsp" class="dashboard-grid__item">Insumos</a>
                <a href="biopreparados.jsp" class="dashboard-grid__item">Biopreparados</a>
                <a href="gestion_trabajos.jsp" class="dashboard-grid__item">Gestión de trabajos</a>
                <a href="Perfil.jsp" class="dashboard-grid__item">Perfil</a>

            </nav> 
        </section>
    </main>
</body>
</html>ml>