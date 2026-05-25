<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <title>Registro - Finca El Paujil</title>
    
<%! 
    private String getErrorStyle(HttpServletRequest request, String campo) {
        // Obtenemos el atributo de forma segura
        Object obj = request.getAttribute("campoError");
        String campoError = (obj != null) ? obj.toString() : "";
        
        // Comparamos
        if (campo.equals(campoError)) {
            return "border: 2px solid #dc3545; background-color: #fff8f8;";
        }
        return "";
    }
%>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
        }

        body {
            background-color: #a646c0; /* Morado principal de tu logo */
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            padding: 20px;
        }

        .register-header {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 15px;
            margin-bottom: 25px;
            color: #ffffff;
        }

        .register-header__title {
            font-size: 2.5rem;
            font-weight: 300;
            letter-spacing: 1px;
        }

        .register-header__logo {
            height: 60px;
            width: auto;
        }

        /* Tarjeta Contenedora */
        .register-card {
            background-color: #9239a8; /* Morado oscuro para contraste */
            width: 100%;
            max-width: 500px;
            padding: 40px 35px;
            border-radius: 20px;
            box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
        }

        .register-card__group {
            position: relative;
            margin-bottom: 18px;
        }

        .register-card__icon {
            position: absolute;
            left: 15px;
            top: 50%;
            transform: translateY(-50%);
            color: #333333;
            font-size: 1.1rem;
            z-index: 2;
        }

        /* Inputs y Selectores adaptados a tu diseño */
        .register-card__input, .register-card__select {
            width: 100%;
            padding: 14px 15px 14px 45px;
            border: none;
            background-color: #f3eff5; /* Gris suave original de tus campos */
            color: #333333;
            font-size: 0.95rem;
            border-radius: 8px;
            outline: none;
            transition: background-color 0.3s;
            appearance: none; /* Elimina la flecha por defecto de los select en algunos navegadores */
        }

        /* Colocar una flecha sutil personalizada para el select */
        .register-card__group--select::after {
            content: '\f0d7';
            font-family: 'Font Awesome 5 Free';
            font-weight: 900;
            position: absolute;
            right: 15px;
            top: 50%;
            transform: translateY(-50%);
            color: #333333;
            pointer-events: none;
        }

        .register-card__input:focus, .register-card__select:focus {
            background-color: #ffffff;
            box-shadow: 0 0 5px rgba(255, 255, 255, 0.3);
        }

        .register-card__button {
            display: block;
            width: 50%;
            margin: 25px auto 0 auto;
            padding: 14px;
            background-color: #000000; /* Botón negro */
            color: #ffffff;
            font-size: 1rem;
            font-weight: bold;
            border: none;
            border-radius: 50px; /* Estilo cápsula */
            cursor: pointer;
            transition: background-color 0.3s, transform 0.2s;
        }

        .register-card__button:hover {
            background-color: #1a1a1a;
            transform: translateY(-1px);
        }

        .register-card__footer {
            text-align: center;
            margin-top: 25px;
            color: rgba(255, 255, 255, 0.8);
            font-size: 0.9rem;
        }

        .register-card__link {
            color: #ffffff;
            text-decoration: none;
            font-weight: bold;
            margin-left: 5px;
        }

        .register-card__link:hover {
            text-decoration: underline;
        }
    </style>
</head>
<body>

    <header class="register-header">
        <h1 class="register-header__title">Registro</h1>
        <img src="static/IMG/logo_paujil.png" alt="Logo" class="register-header__logo" onerror="this.style.display='none';">
    </header>

    <main class="register-card">
    <%-- Muestra el mensaje de error si existe --%>
    <% if (request.getAttribute("mensaje") != null) { %>
        <div style="background-color: #f8d7da; color: #721c24; padding: 15px; border: 1px solid #f5c6cb; margin-bottom: 20px; border-radius: 5px; font-size: 0.9rem;">
            <strong>¡Atención!</strong> <%= request.getAttribute("mensaje") %>
        </div>
    <% } %>

    <form action="${pageContext.request.contextPath}/ServletRegistro" method="POST">
        
        <div class="register-card__group">
            <i class="fa-solid fa-user register-card__icon"></i>
            <input type="text" name="txtNombre" class="register-card__input" placeholder="Nombre completo" 
                   value="${param.txtNombre}" style="<%= getErrorStyle(request, "txtNombre") %>" required>
        </div>

        <div class="register-card__group">
            <i class="fa-solid fa-envelope register-card__icon"></i>
            <input type="email" name="txtEmail" class="register-card__input" placeholder="Correo electrónico" 
                   value="${param.txtEmail}" style="<%= getErrorStyle(request, "txtEmail") %>" required>
        </div>

        <div class="register-card__group">
            <i class="fa-solid fa-phone register-card__icon"></i>
            <input type="tel" name="txtTelefono" class="register-card__input" placeholder="Teléfono (10 dígitos)" 
                   value="${param.txtTelefono}" style="<%= getErrorStyle(request, "txtTelefono") %>" required>
        </div>

        <div class="register-card__group">
            <i class="fa-solid fa-calendar-days register-card__icon"></i>
            <input type="date" name="txtFechaNacimiento" class="register-card__input" required>
        </div>

        <div class="register-card__group">
            <i class="fa-solid fa-house-chimney register-card__icon"></i>
            <input type="text" name="txtDireccion" class="register-card__input" placeholder="Dirección" 
                   value="${param.txtDireccion}" style="<%= getErrorStyle(request, "txtDireccion") %>" required>
        </div>

        <div class="register-card__group register-card__group--select">
            <i class="fa-solid fa-user-gear register-card__icon"></i>
            <select name="txtRol" class="register-card__select" required>
                <option value="" disabled ${empty param.txtRol ? 'selected' : ''}>Selecciona tu rol</option>
                <option value="trabajador" ${param.txtRol == 'trabajador' ? 'selected' : ''}>Trabajador</option>
                <option value="administrador" ${param.txtRol == 'administrador' ? 'selected' : ''}>Administrador</option>
            </select>
        </div>

        <div class="register-card__group">
            <i class="fa-solid fa-lock register-card__icon"></i>
            <input type="password" name="txtContrasena" class="register-card__input" placeholder="Contraseña" 
                   style="<%= getErrorStyle(request, "txtContrasena") %>" required>
        </div>

        <div class="register-card__group">
            <i class="fa-solid fa-shield-halved register-card__icon"></i>
            <input type="password" name="txtConfirmarContrasena" class="register-card__input" placeholder="Confirmar contraseña" 
                   style="<%= getErrorStyle(request, "txtConfirmarContrasena") %>" required>
        </div>

        <button type="submit" class="register-card__button">Registrarse</button>
    </form>
</main>
        
</body>
</html>