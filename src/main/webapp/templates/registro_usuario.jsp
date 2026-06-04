<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/registro.css">
    <title>Registro - Finca El Paujil</title>
</head>
<body class="register-page">

    <header class="register-header">
        <h1 class="register-header__title">Crear cuenta</h1>
        <img src="${pageContext.request.contextPath}/static/IMG/logo_paujil.png"
             alt="Logo Finca El Paujil"
             class="register-header__logo"
             onerror="this.style.display='none';">
    </header>

    <main class="register-card">

        <%--
            Mensajes del backend: solo se muestran para errores de seguridad o BD
            (correo/teléfono duplicado, fallo al guardar). La lógica de negocio
            ya no genera estos mensajes porque el frontend la intercepta primero.
        --%>
        <% String msg = (String) request.getAttribute("mensaje");
           if (msg != null) { %>
            <div class="error-message" role="alert">
                <i class="fa-solid fa-circle-exclamation"></i> <%= msg %>
            </div>
        <% } %>

        <form id="formRegistro"
              action="${pageContext.request.contextPath}/ServletRegistro"
              method="POST"
              novalidate>

            <%-- Nombre completo --%>
            <div class="register-card__group">
                <i class="fa-solid fa-user register-card__icon"></i>
                <input type="text"
                       id="txtNombre"
                       name="txtNombre"
                       class="register-card__input"
                       placeholder="Nombre completo"
                       value="${not empty nombre ? nombre : ''}"
                       autocomplete="name"
                       maxlength="80"
                       aria-required="true">
            </div>

            <%-- Correo electrónico --%>
            <div class="register-card__group">
                <i class="fa-solid fa-envelope register-card__icon"></i>
                <input type="email"
                       id="txtEmail"
                       name="txtEmail"
                       class="register-card__input"
                       placeholder="Correo electrónico"
                       value="${not empty email ? email : ''}"
                       autocomplete="email"
                       maxlength="120"
                       aria-required="true">
            </div>

            <%-- Teléfono --%>
            <div class="register-card__group">
                <i class="fa-solid fa-phone register-card__icon"></i>
                <input type="tel"
                       id="txtTelefono"
                       name="txtTelefono"
                       class="register-card__input"
                       placeholder="Teléfono (10 dígitos)"
                       value="${not empty telefono ? telefono : ''}"
                       autocomplete="tel"
                       maxlength="10"
                       inputmode="numeric"
                       aria-required="true">
            </div>

            <%-- Fecha de nacimiento --%>
            <div class="register-card__group">
                <i class="fa-solid fa-calendar-days register-card__icon"></i>
                <input type="date"
                       id="txtFechaNacimiento"
                       name="txtFechaNacimiento"
                       class="register-card__input"
                       value="${not empty fecha ? fecha : ''}"
                       aria-required="true">
            </div>

            <%-- Dirección --%>
            <div class="register-card__group">
                <i class="fa-solid fa-location-dot register-card__icon"></i>
                <input type="text"
                       id="txtDireccion"
                       name="txtDireccion"
                       class="register-card__input"
                       placeholder="Dirección"
                       value="${not empty direccion ? direccion : ''}"
                       autocomplete="street-address"
                       maxlength="120"
                       aria-required="true">
            </div>

            <%-- Rol --%>
            <div class="register-card__group register-card__group--select">
                <i class="fa-solid fa-user-gear register-card__icon"></i>
                <select id="txtRol"
                        name="txtRol"
                        class="register-card__select"
                        aria-required="true">
                    <option value="" disabled
                        ${''.equals(pageContext.request.getAttribute("rol")) || pageContext.request.getAttribute("rol") == null ? "selected" : ""}>
                        Selecciona tu rol
                    </option>
                    <option value="trabajador"
                        ${"trabajador".equals(pageContext.request.getAttribute("rol")) ? "selected" : ""}>
                        Trabajador
                    </option>
                    <option value="administrador"
                        ${"administrador".equals(pageContext.request.getAttribute("rol")) ? "selected" : ""}>
                        Administrador
                    </option>
                </select>
            </div>

            <%-- Contraseña --%>
            <div class="register-card__group">
                <i class="fa-solid fa-lock register-card__icon"></i>
                <input type="password"
                       id="txtContrasena"
                       name="txtContrasena"
                       class="register-card__input"
                       placeholder="Contraseña"
                       autocomplete="new-password"
                       maxlength="100"
                       aria-required="true">
            </div>

            <%-- Confirmar contraseña --%>
            <div class="register-card__group">
                <i class="fa-solid fa-shield-halved register-card__icon"></i>
                <input type="password"
                       id="txtConfirmarContrasena"
                       name="txtConfirmarContrasena"
                       class="register-card__input"
                       placeholder="Confirmar contraseña"
                       autocomplete="new-password"
                       maxlength="100"
                       aria-required="true">
            </div>

            <button type="submit" class="register-card__button">Registrarse</button>
        </form>

        <p style="text-align:center; margin-top:16px; font-size:var(--font-size-sm); color:rgba(255,255,255,.75);">
            ¿Ya tienes cuenta?
            <a href="${pageContext.request.contextPath}/templates/login.jsp"
               style="color:var(--color-white); font-weight:600;">Iniciar sesión</a>
        </p>
    </main>

    <%-- El script se carga al final del body para garantizar que el DOM esté listo --%>
    <script src="${pageContext.request.contextPath}/static/js/validaciones.js"></script>
</body>
</html>

