<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/login.css">
    <title>Iniciar Sesión - Finca El Paujil</title>
</head>
<body class="login-page">

    <main class="login-container">

        <a href="${pageContext.request.contextPath}/index.jsp" class="login-container__back">
            <i class="fa-solid fa-arrow-left"></i> Volver
        </a>

        <section class="login-form">
            <h1 class="login-form__title">¡Bienvenido!</h1>

            <div class="login-form__avatar-container">
                <div class="login-form__avatar">
                    <i class="fa-solid fa-user"></i>
                </div>
            </div>

            <% if(request.getParameter("error") != null) { %>
                <div class="error-message">
                    <i class="fa-solid fa-circle-exclamation"></i> Usuario o contraseña incorrectos.
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/ServletLogin" method="POST">

                <div class="login-form__group">
                    <i class="fa-solid fa-user-tag login-form__icon"></i>
                    <select name="txtRol" class="login-form__select" required>
                        <option value="" disabled selected>Selecciona tu rol</option>
                        <option value="trabajador">Trabajador</option>
                        <option value="administrador">Administrador</option>
                    </select>
                </div>

                <div class="login-form__group">
                    <i class="fa-solid fa-envelope login-form__icon"></i>
                    <input type="email" name="txtUsuario" class="login-form__input"
                           placeholder="Correo electrónico" required autocomplete="username">
                </div>

                <div class="login-form__group">
                    <i class="fa-solid fa-lock login-form__icon"></i>
                    <input type="password" name="txtContrasena" class="login-form__input"
                           placeholder="Contraseña" required autocomplete="current-password">
                </div>

                <button type="submit" class="login-form__button">Ingresar</button>
            </form>
        </section>
    </main>

</body>
</html>