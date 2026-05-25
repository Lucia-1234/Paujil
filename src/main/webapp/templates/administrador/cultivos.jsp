<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%-- IMPORTANTE: Las importaciones que hacían falta para solucionar el error de compilación --%>
<%@page import="dao.CultivoDao"%>
<%@page import="modelo.cultivo"%>
<%@page import="java.util.List"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css"> 
        <title>Cultivos</title> 
    </head>
    
    <style>
        * {
            box-sizing: border-box;
            margin: 0; 
            padding: 0; 
            font-family: Arial, sans-serif; 
        }

        body {
            background-color: var(--white);
            padding: 20px; 
        }

        /* Header */
        .header__back-btn {
            background: none;
            border: none; 
            color: var(--primary-green);
            font-size: 1.5rem;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 20px;
        } 

        /* Contenedor Principal */
        .container {
            background-color: var(--bg-green);
            padding: 40px 20px; 
            border-radius: 8px;
            max-width: 1000px;
            margin: 0 auto;
        } 

        /* Lista de Cultivos */
        .crop-list {
            display: flex;
            flex-direction: column; 
            gap: 20px;
        } 

        /* Tarjeta (Block) */
        .crop-card {
            background-color: var(--white);
            border-radius: 15px; 
            padding: 20px;
            display: flex;
            gap: 20px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        } 

        /* Elementos de la Tarjeta */
        .crop-card__image-container {
            flex: 0 0 200px;
        } 

        .crop-card__img {
            width: 100%;
            height: 150px; 
            object-fit: cover;
            border-radius: 12px;
        } 

        .crop-card__content {
            flex: 1;
        } 

        .crop-card__title {
            font-size: 1.4rem;
            margin-bottom: 10px; 
        } 

        .crop-card__description {
            font-size: 0.9rem;
            color: #555; 
            margin-bottom: 10px;
            line-height: 1.4;
        } 

        .crop-card__info p {
            font-size: 0.9rem;
            margin-bottom: 5px; 
        } 

        /* Botones (Block y Modifiers) */
        .crop-card__actions {
            display: flex;
            gap: 10px; 
            margin-top: 15px;
        } 

        .btn {
            display: inline-flex;
            align-items: center; 
            justify-content: center;
            gap: 8px; 
            padding: 8px 16px;
            border: none; 
            border-radius: 5px;
            cursor: pointer;
            font-weight: 600;
            transition: all 0.3s ease;
            text-decoration: none; /* Asegura que los enlaces parezcan botones */
        } 

        .btn--new {
            background-color: var(--light-green);
            color: var(--white); 
            border-radius: 25px; 
            padding: 10px 20px;
        } 

        .btn--new i {
            font-size: 1.2rem;
        } 

        .header__back-btn  {
            font-size: 1.4rem;
            color: var(--primary-green); 
        }

        .btn:hover {
            filter: brightness(0.9);
            transform: translateY(-1px); 
        }

        .btn--add { background-color: var(--primary-green); color: white; } 
        .btn--edit { background-color: var(--primary-green); color: white; } 
        .btn--delete { background-color: var(--action-red); color: white; } 

        @media (max-width: 768px) {
            .crop-card {
                flex-direction: column;
                align-items: center; 
                text-align: center;
            }

            .crop-card__image-container {
                flex: 0 0 auto;
                width: 100%; 
            }

            .crop-card__actions {
                flex-direction: column;
                width: 100%; 
            }

            .btn {
                width: 100%;
            } 
        }
    </style>

    <body>
    <main class="container"> 
    <section class="crop-list"> 
        
        <%
            // Instanciamos el DAO y cargamos la lista de cultivos reales de la base de datos
            CultivoDao cultivoDao = new CultivoDao();
            List<cultivo> listaCultivos = cultivoDao.listarConRelaciones();
            
            if(listaCultivos.isEmpty()) {
        %>
            <article class="crop-card">
                <div class="crop-card__content">
                    <h2 class="crop-card__title">No hay cultivos</h2>
                    <p class="crop-card__description">No se encontraron registros de siembras en el sistema de la finca.</p>
                </div>
            </article>
        <%
            } else {
                for(cultivo cultivo : listaCultivos) {
        %>
            <article class="crop-card"> 
                <div class="crop-card__image-container"> 
                    <%-- Cambia dinámicamente la imagen según el nombre del producto --%>
                    <% if(cultivo.getNombreCultivo().toLowerCase().contains("lulo")) { %>
                        <img src="img/image 5.png" alt="Lulo" class="crop-card__img"> 
                    <% } else { %>
                        <img src="img/image 4.png" alt="Cultivo" class="crop-card__img"> 
                    <% } %>
                </div> 
                
                <div class="crop-card__content"> 
                    <%-- Título dinámico que une el Nombre del Cultivo con su Lote correspondiente --%>
                    <h2 class="crop-card__title">
                        <%= cultivo.getNombreCultivo() %> - <%= (cultivo.getNombreLoteAux() != null) ? cultivo.getNombreLoteAux() : "Sin Lote" %>
                    </h2> 
                    
                    <p class="crop-card__description"> 
                        <strong>Descripcion:</strong> <%= (cultivo.getTipoCultivo() != null) ? cultivo.getTipoCultivo() : "Cultivo general registrado en el sistema." %> 
                    </p> 
      
                    <div class="crop-card__info"> 
                        <p><strong>Cantidad / Área:</strong> <%= cultivo.getArea() %> Ha</p> 
                        <%-- Trae dinámicamente el insumo vinculado en la tabla intermedia cultivo_insumo --%>
                        <p><strong>Cultivo tratado con:</strong> <%= (cultivo.getNombreInsumoAux() != null) ? cultivo.getNombreInsumoAux() : "Ninguno" %></p> 
                    </div> 
                    
                    <div class="crop-card__actions"> 
                        <%-- Botón para agregar registros operacionales individuales --%>
                        <button class="btn btn--add"> 
                            Agregar registro 
                        </button> 
                        
                        <%-- Botón Editar: Envía el ID a un formulario modal o pantalla externa de edición --%>
                        <a href="formulario_cultivo.jsp?editId=<%= cultivo.getIdCultivo() %>" class="btn btn--edit"> 
                            editar 
                        </a> 
                        
                        <%-- Botón Eliminar: Llama al Servlet pasándole la acción por GET de forma segura --%>
                        <a href="${pageContext.request.contextPath}/ServletCultivo?accion=eliminar&id=<%= cultivo.getIdCultivo() %>" 
                           class="btn btn--delete"
                           [cite_start]onclick="return confirm('¿Está seguro de que desea eliminar este cultivo y sus relaciones?');"> 
                            eliminar 
                        </a> 
                    </div> 
                </div> 
            </article> 
        <% 
                }
            }
        %>

        <div class="crop-list__footer"> 
            <a href="formulario_cultivo.jsp" class="btn btn--new"> 
                <i class="fa-solid fa-circle-plus"></i> Agregar cultivo 
            </a> 
        </div> 
    </section> 
</main> 
</body>
</html>