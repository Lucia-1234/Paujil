<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <title>Iniciar Sesión - Finca El Paujil</title>
    <style>
        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: Arial, sans-serif;
        }

        body {
            display: flex;
            height: 100vh;
            overflow: hidden;
            background-color: #f5f5f5;
        }


        /* Sección Derecha: Formulario con el Gradiente de tu Marca */
        .login-container {
            flex: 1.5;
            background: linear-gradient(180deg, #76c065 0%, #a646c0 100%);
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 40px;
            position: relative;
        }

        /* Botón discreto para regresar a la bienvenida */
        .login-container__back {
            position: absolute;
            top: 20px;
            right: 20px;
            color: rgba(255, 255, 255, 0.8);
            text-decoration: none;
            font-size: 0.9rem;
            display: flex;
            align-items: center;
            gap: 5px;
        }

        .login-container__back:hover {
            color: #ffffff;
        }

        /* Elementos del Formulario */
        .login-form {
            width: 100%;
            max-width: 360px;
            text-align: center;
        }

        .login-form__title {
            color: #ffffff;
            font-size: 2.2rem;
            font-weight: 300;
            margin-bottom: 25px;
            letter-spacing: 1px;
        }

        .login-form__avatar-container {
            margin-bottom: 30px;
        }

        .login-form__avatar {
            font-size: 6rem;
            color: rgba(255, 255, 255, 0.5);
            background: rgba(255, 255, 255, 0.15);
            padding: 20px;
            border-radius: 50%;
            width: 130px;
            height: 130px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
        }

        .login-form__group {
            position: relative;
            margin-bottom: 20px;
        }

        /* Iconos dentro de las cajas de texto */
        .login-form__icon {
            position: absolute;
            left: 15px;
            top: 50%;
            transform: translateY(-50%);
            color: rgba(255, 255, 255, 0.6);
            font-size: 1.1rem;
        }

        .login-form__input {
            width: 100%;
            padding: 14px 14px 14px 45px;
            border: none;
            background: rgba(255, 255, 255, 0.25);
            color: #ffffff;
            font-size: 1rem;
            border-radius: 6px;
            outline: none;
            transition: background 0.3s ease;
        }

        .login-form__input::placeholder {
            color: rgba(255, 255, 255, 0.7);
        }

        .login-form__input:focus {
            background: rgba(255, 255, 255, 0.35);
            box-shadow: 0 0 4px rgba(255, 255, 255, 0.4);
        }

        /* Botón de ingreso blanco e impecable */
        .login-form__button {
            width: 50%;
            padding: 12px;
            background-color: #ffffff;
            color: #333333;
            border: none;
            border-radius: 6px;
            font-size: 1rem;
            font-weight: bold;
            cursor: pointer;
            margin-top: 15px;
            transition: background-color 0.3s ease, transform 0.2s ease;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }

        .login-form__button:hover {
            background-color: #e6e6e6;
            transform: translateY(-1px);
        }

        /* Mensajes de error en caso de credenciales incorrectas */
        .error-message {
            background-color: rgba(217, 83, 79, 0.9);
            color: white;
            padding: 10px;
            border-radius: 6px;
            margin-bottom: 20px;
            font-size: 0.9rem;
        }

        /* Diseño Responsivo */
        @media (max-width: 768px) {
            
            .login-container {
                flex: 1;
                padding: 20px;
            }
        }
    </style>
</head>
<body>

    <main class="login-container">
        
        <a href="../../index.jsp" class="login-container__back">
            <i class="fa-solid fa-arrow-left"></i> Volver
        </a>

        <section class="login-form">
            <h1 class="login-form__title">¡Bienvenido!</h1>
            
            <div class="login-form__avatar-container">
                <div class="login-form__avatar">
                    <i class="fa-solid fa-user"></i>
                </div>
            </div>

            <%-- Validación visual por si el servlet detecta un error --%>
            <% if(request.getParameter("error") != null) { %>
                <div class="error-message">
                    <i class="fa-solid fa-circle-exclamation"></i> Usuario o contraseña incorrectos.
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/ServletLogin" method="POST">
                
                <input type="hidden" name="txtRol" value="<%= (request.getParameter("rol") != null) ? request.getParameter("rol") : "1" %>">

                <div class="login-form__group">
                    <i class="fa-solid fa-user login-form__icon"></i>
                    <input type="email" name="txtUsuario" class="login-form__input" placeholder="Usuario" required autocomplete="username">
                </div>

                <div class="login-form__group">
                    <i class="fa-solid fa-lock login-form__icon"></i>
                    <input type="password" name="txtContrasena" class="login-form__input" placeholder="Contraseña" required>
                </div>

                <button type="submit" class="login-form__button">Ingresar</button>
            </form>
        </section>
    </main>

</body>
</html>