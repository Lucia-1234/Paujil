<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.cultivo, com.paujil.modelo.usuario, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
    <title>Asignar Trabajo - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=listar"
           class="list-header__back" aria-label="Volver al listado">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Asignar trabajo</h1>
    </header>

    <% String mensajeError = (String) request.getAttribute("mensajeError");
       if (mensajeError != null) { %>
        <div class="feedback-message feedback-message--error">
            <i class="fa-solid fa-circle-exclamation"></i> <%= mensajeError %>
        </div>
    <% } %>

    <div class="job-form-wrapper">
        <form class="job-form" action="${pageContext.request.contextPath}/ServletTrabajo" method="POST">

            <div class="job-form__row">
                <label class="job-form__label" for="idCultivo">
                    <i class="fa-solid fa-seedling" style="margin-right:4px;color:var(--color-brand-green);"></i> Cultivo
                </label>
                <select id="idCultivo" name="idCultivo" class="job-form__select" required>
                    <option value="">— Seleccione un cultivo —</option>
                    <%
                        List<cultivo> cultivos = (List<cultivo>) request.getAttribute("listaCultivos");
                        if (cultivos != null) {
                            for (cultivo c : cultivos) {
                    %>
                        <option value="<%= c.getIdCultivo() %>"><%= c.getNombreCultivo() %></option>
                    <%      }
                        }
                    %>
                </select>
            </div>

            <div class="job-form__row">
                <label class="job-form__label" for="idUsuario">
                    <i class="fa-solid fa-user-hard-hat" style="margin-right:4px;color:var(--color-brand-green);"></i> Trabajador
                </label>
                <select id="idUsuario" name="idUsuario" class="job-form__select" required>
                    <option value="">— Seleccione un trabajador —</option>
                    <%
                        List<usuario> usuarios = (List<usuario>) request.getAttribute("listaUsuarios");
                        if (usuarios != null) {
                            for (usuario u : usuarios) {
                    %>
                        <option value="<%= u.getIdUsuario() %>"><%= u.getNombre() %></option>
                    <%      }
                        }
                    %>
                </select>
            </div>

            <div class="job-form__row">
                <label class="job-form__label" for="nombreTrabajo">
                    <i class="fa-solid fa-clipboard-list" style="margin-right:4px;color:var(--color-brand-green);"></i> Nombre del trabajo
                </label>
                <input type="text" id="nombreTrabajo" name="nombreTrabajo"
                       class="job-form__input" placeholder="Ej: Poda, fumigación, abono..." required>
            </div>

            <div class="job-form__row">
                <label class="job-form__label" for="descripcion">
                    <i class="fa-solid fa-align-left" style="margin-right:4px;color:var(--color-brand-green);"></i> Descripción
                </label>
                <textarea id="descripcion" name="descripcion" class="job-form__textarea"
                          placeholder="Describe las tareas a realizar..." required></textarea>
            </div>

            <div class="job-form__row">
                <label class="job-form__label" for="fechaAsignacion">
                    <i class="fa-solid fa-calendar-days" style="margin-right:4px;color:var(--color-brand-green);"></i> Fecha de asignación
                </label>
                <input type="date" id="fechaAsignacion" name="fechaAsignacion"
                       class="job-form__input" required>
            </div>

            <div class="job-form__footer">
                <button type="submit" class="btn btn--save-job">
                    <i class="fa-solid fa-floppy-disk"></i> Guardar asignación
                </button>
                <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=listar"
                   style="margin-left:16px; color:var(--color-text-secondary); font-size:var(--font-size-sm);">
                    Cancelar
                </a>
            </div>
        </form>
    </div>

</main>
</body>
</html>
