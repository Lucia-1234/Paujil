<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.biopreparado, com.paujil.modelo.biopreparado.ingredienteBio, java.util.List"%>
<%
    if (session.getAttribute("idUsuario") == null) {
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
        return;
    }
    List<biopreparado> lista = (List<biopreparado>) request.getAttribute("listaBiopreparados");
    if (lista == null) {
        response.sendRedirect(request.getContextPath() + "/ServletBiopreparado?accion=verTrabajador");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/biopreparados.css">
    <title>Biopreparados - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/trabajador/menu_trabajador.jsp"
           class="list-header__back" aria-label="Volver al menú">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Recetario de Biopreparados</h1>
    </header>

    <div class="panel">
        <% if (lista.isEmpty()) { %>
            <p class="no-data">
                <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                No hay biopreparados registrados en este momento.
            </p>
        <% } else {
            int num = 1;
            for (biopreparado b : lista) { %>
            <article class="bio-card">
                <div class="bio-card__content">
                    <p class="bio-card__number"><%= String.format("%02d", num++) %></p>
                    <h2 class="bio-card__title"><%= b.getNombre() %></h2>

                    <% if (b.getDescripcion() != null && !b.getDescripcion().isEmpty()) { %>
                        <p class="bio-card__description"><%= b.getDescripcion() %></p>
                    <% } %>

                    <p class="bio-card__prep-label">
                        <i class="fa-solid fa-calendar-days" style="color:var(--color-brand-green);margin-right:4px;"></i>
                        Creación: <strong><%= b.getFechaCreacion() %></strong>
                        &nbsp;·&nbsp;
                        <i class="fa-solid fa-calendar-xmark" style="color:var(--color-action-red);margin-right:4px;"></i>
                        Vence: <strong><%= b.getFechaVencimiento() %></strong>
                        <% if (b.getPrecio() > 0) { %>
                            &nbsp;·&nbsp;
                            <i class="fa-solid fa-tag" style="margin-right:4px;"></i>
                            $<strong><%= String.format("%.2f", b.getPrecio()) %></strong>
                        <% } %>
                    </p>

                    <% if (b.getPreparacion() != null && !b.getPreparacion().isEmpty()) { %>
                        <details style="margin-bottom:var(--spacing-sm);">
                            <summary class="bio-card__prep-label" style="cursor:pointer;">
                                <i class="fa-solid fa-flask" style="margin-right:4px;"></i> Modo de preparación
                            </summary>
                            <p class="bio-card__prep-text" style="margin-top:8px;"><%= b.getPreparacion() %></p>
                        </details>
                    <% } %>

                    <% if (b.getIngredientes() != null && !b.getIngredientes().isEmpty()) { %>
                        <details>
                            <summary class="bio-card__prep-label" style="cursor:pointer;">
                                <i class="fa-solid fa-list" style="margin-right:4px;"></i>
                                Ingredientes (<%= b.getIngredientes().size() %>)
                            </summary>
                            <table class="bio-card__ingredients" style="margin-top:8px;">
                                <thead>
                                    <tr>
                                        <th>Ingrediente</th>
                                        <th>Cantidad</th>
                                        <th>Unidad</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (ingredienteBio ing : b.getIngredientes()) { %>
                                        <tr>
                                            <td><%= ing.getNombre() %></td>
                                            <td><%= ing.getCantidad() > 0 ? ing.getCantidad() : "—" %></td>
                                            <td><%= ing.getUnidad() != null ? ing.getUnidad() : "—" %></td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </details>
                    <% } %>
                </div>
            </article>
        <%  }
        } %>
    </div>

</main>
</body>
</html>

