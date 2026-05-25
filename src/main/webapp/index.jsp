<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bienvenido - Finca El Paujil</title>
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
        }

        /* Sección Izquierda: El Logotipo y el Degradado Corporativo */
        .welcome-brand {
            flex: 1;
            background: linear-gradient(135deg, #a646c0 0%, #76c065 100%);
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            color: white;
            padding: 20px;
        }

        .welcome-brand__logo {
            max-width: 180px;
            height: auto;
            margin-bottom: 15px;
            /* Si no tienes la imagen separada, puedes usar un icono temporal de FontAwesome o Boxicons */
        }

        /* Sección Derecha: Botonera de Roles */
        .welcome-actions {
            flex: 1.2;
            background-color: #ffffff;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            padding: 40px;
        }

        .welcome-actions__title {
            font-size: 2rem;
            color: #000000;
            margin-bottom: 40px;
            font-weight: bold;
            text-align: center;
        }

        .welcome-actions__menu {
            display: flex;
            flex-direction: column;
            gap: 20px;
            width: 100%;
            max-width: 320px;
        }

        /* Estilo exacto de los botones negros redondeados de tu imagen */
        .btn-role {
            display: block;
            width: 100%;
            background-color: #000000;
            color: #ffffff;
            text-decoration: none;
            text-align: center;
            padding: 15px 20px;
            font-size: 1.1rem;
            border-radius: 50px; /* Redondeado completo estilo cápsula */
            font-weight: 500;
            transition: background-color 0.3s ease, transform 0.2s ease;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }

        .btn-role:hover {
            background-color: #222222;
            transform: translateY(-1px);
        }

        /* Enlace inferior de registro */
        .welcome-actions__link {
            margin-top: 50px;
            color: #333333;
            text-decoration: none;
            font-size: 1rem;
            transition: color 0.2s;
        }

        .welcome-actions__link:hover {
            text-decoration: underline;
            color: #a646c0;
        }

        /* Responsivo para celulares */
        @media (max-width: 768px) {
            body {
                flex-direction: column;
            }
            .welcome-brand {
                flex: 0 0 30%;
            }
            .welcome-actions {
                flex: 1;
                padding: 20px;
            }
            .welcome-actions__title {
                font-size: 1.5rem;
                margin-bottom: 25px;
            }
        }
    </style>
</head>
<body>

    <main class="welcome-brand">
        <img src="static/IMG/logo_paujil.png" alt="EL PAUJIL" class="welcome-brand__logo" onerror="this.style.display='none';">
    </main>

    <section class="welcome-actions">
        <h1 class="welcome-actions__title">¡Bienvenido a la finca el Pauji!</h1>
        
        <nav class="welcome-actions__menu">
            <a href="templates/login.jsp?rol=1" class="btn-role">Administrador</a>
            <a href="templates/login.jsp?rol=2" class="btn-role">Trabajador</a>
        </nav>

        <a href="templates/registro_usuario.jsp" class="welcome-actions__link">Crear cuenta</a>
    </section>

</body>
</html>