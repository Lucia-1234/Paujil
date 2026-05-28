<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/Styles.css">
    <title>Registro - Finca El Paujil</title>
</head>
<body class="register-page">

    <header class="register-header">
        <h1 class="register-header__title">Registro</h1>
        <img src="${pageContext.request.contextPath}/static/IMG/logo_paujil.png" alt="Logo" class="register-header__logo" onerror="this.style.display='none';">
    </header>

    <main class="register-card">
        <%-- Contenedor para mensajes de error de JS o Backend --%>
        <div id="mensaje-feedback" class="feedback-message" style="display: none;"></div>

        <form id="formRegistro" action="${pageContext.request.contextPath}/ServletRegistro" method="POST">
            
            <div class="register-card__group">
                <i class="fa-solid fa-user register-card__icon"></i>
                <input type="text" name="txtNombre" class="register-card__input" placeholder="Nombre completo" required>
            </div>

            <div class="register-card__group">
                <i class="fa-solid fa-envelope register-card__icon"></i>
                <input type="email" name="txtEmail" class="register-card__input" placeholder="Correo electrónico" required>
            </div>

            <div class="register-card__group">
                <i class="fa-solid fa-phone register-card__icon"></i>
                <input type="tel" name="txtTelefono" id="txtTelefono" class="register-card__input" placeholder="Teléfono (10 dígitos)" required>
            </div>

            <div class="register-card__group">
                <i class="fa-solid fa-calendar-days register-card__icon"></i>
                <input type="date" name="txtFechaNacimiento" class="register-card__input" required>
            </div>

            <div class="register-card__group">
                <i class="fa-solid fa-house-chimney register-card__icon"></i>
                <input type="text" name="txtDireccion" class="register-card__input" placeholder="Dirección" required>
            </div>

            <div class="register-card__group register-card__group--select">
                <i class="fa-solid fa-user-gear register-card__icon"></i>
                <select name="txtRol" class="register-card__select" required>
                    <option value="" disabled selected>Selecciona tu rol</option>
                    <option value="trabajador">Trabajador</option>
                    <option value="administrador">Administrador</option>
                </select>
            </div>

            <div class="register-card__group">
                <i class="fa-solid fa-lock register-card__icon"></i>
                <input type="password" name="txtContrasena" id="txtContrasena" class="register-card__input" placeholder="Contraseña" required>
            </div>

            <div class="register-card__group">
                <i class="fa-solid fa-shield-halved register-card__icon"></i>
                <input type="password" name="txtConfirmarContrasena" id="txtConfirmarContrasena" class="register-card__input" placeholder="Confirmar contraseña" required>
            </div>

            <button type="submit" class="register-card__button">Registrarse</button>
        </form>
        
        <% String msg = (String) request.getAttribute("mensaje");
        if (msg != null) { %>
         <div class="feedback-message" style="display:block; color:red;">
             <%= msg %>
         </div>
        <% } %>
    </main>

    <script src="${pageContext.request.contextPath}/static/js/scripts.js"></script>
</body>
</html>