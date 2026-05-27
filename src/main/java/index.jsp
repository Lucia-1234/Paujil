<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bienvenido - Finca El Paujil</title>
       <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/Styles.css">
</head>
<body>

    <main class="welcome-brand">
        <img src="${pageContext.request.contextPath}/static/IMG/logo_paujil.png" alt="EL PAUJIL" class="welcome-brand__logo" onerror="this.style.display='none';">
    </main>

    <section class="welcome-actions">
        <h1 class="welcome-actions__title">¡Bienvenido a la finca el Pauji!</h1>
        
        <nav class="welcome-actions__menu">
            <a href="${pageContext.request.contextPath}/templates/login.jsp?rol=1" class="btn-role">Administrador</a>
            <a href="${pageContext.request.contextPath}/templates/login.jsp?rol=2" class="btn-role">Trabajador</a>
        </nav>

        <a href="${pageContext.request.contextPath}/templates/registro_usuario.jsp" class="welcome-actions__link">Crear cuenta</a>
    </section>

</body>
</html>