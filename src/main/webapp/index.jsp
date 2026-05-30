<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bienvenido - Finca El Paujil</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/index.css">
</head>
<body class="index-page">

    <aside class="welcome-brand">
        <img src="${pageContext.request.contextPath}/static/IMG/logo_paujil.png"
             alt="Logo Finca El Paujil"
             class="welcome-brand__logo"
             onerror="this.style.display='none';">
    </aside>

    <section class="welcome-actions">
        <h1 class="welcome-actions__title">¡Bienvenido a<br>Finca El Paujil!</h1>

        <nav class="welcome-actions__menu" aria-label="Acceso por rol">
            <a href="${pageContext.request.contextPath}/templates/login.jsp?rol=1"
               class="btn-role">
                <i class="fa-solid fa-user-shield" style="margin-right:8px;"></i>Administrador
            </a>
            <a href="${pageContext.request.contextPath}/templates/login.jsp?rol=2"
               class="btn-role">
                <i class="fa-solid fa-hard-hat" style="margin-right:8px;"></i>Trabajador
            </a>
        </nav>

        <a href="${pageContext.request.contextPath}/templates/registro_usuario.jsp"
           class="welcome-actions__link">¿No tienes cuenta? Crear cuenta</a>
    </section>

</body>
</html>
