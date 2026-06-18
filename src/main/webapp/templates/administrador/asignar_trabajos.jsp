<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.cultivo, com.paujil.modelo.usuario,
                com.paujil.modelo.tipoTrabajo, java.util.List"%>
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
        <form id="formAsignarTrabajo" class="job-form"
              action="${pageContext.request.contextPath}/ServletTrabajo" method="POST"
              novalidate>
            <input type="hidden" name="accion" value="registrar">

            <%-- Cultivo --%>
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
                <span id="error-idCultivo" class="mensaje-error" role="alert" style="display:none;"></span>
            </div>

            <%-- Trabajador --%>
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
                <span id="error-idUsuario" class="mensaje-error" role="alert" style="display:none;"></span>
            </div>

            <%-- Tipo de trabajo --%>
            <div class="job-form__row">
                <label class="job-form__label" for="idTipoTrabajo">
                    <i class="fa-solid fa-tag" style="margin-right:4px;color:var(--color-brand-green);"></i> Tipo de trabajo
                </label>
                <div style="display:flex; gap:10px; align-items:flex-start; flex-direction:column; width:100%;">
                    <div style="display:flex; gap:10px; align-items:center; width:100%;">
                        <select id="idTipoTrabajo" name="idTipoTrabajo" class="job-form__select" required style="flex:1;">
                            <option value="">— Seleccione un tipo —</option>
                            <%
                                List<tipoTrabajo> tipos = (List<tipoTrabajo>) request.getAttribute("listaTiposTrabajo");
                                if (tipos != null) {
                                    for (tipoTrabajo tp : tipos) {
                            %>
                                <option value="<%= tp.getIdTipoTrabajo() %>"><%= tp.getNombreTipo() %></option>
                            <%      }
                                }
                            %>
                        </select>
                        <button type="button" class="btn btn--edit btn--sm" onclick="abrirModalTipoTrabajo()">
                            <i class="fa-solid fa-plus"></i> Nuevo tipo
                        </button>
                    </div>
                    <span id="error-idTipoTrabajo" class="mensaje-error" role="alert" style="display:none;"></span>
                </div>
            </div>

            <%-- Descripción --%>
            <div class="job-form__row">
                <label class="job-form__label" for="descripcion">
                    <i class="fa-solid fa-align-left" style="margin-right:4px;color:var(--color-brand-green);"></i> Descripción
                </label>
                <textarea id="descripcion" name="descripcion" class="job-form__textarea"
                          placeholder="Describe las tareas a realizar... (mínimo 10 caracteres)"
                          maxlength="500" required></textarea>
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <span id="error-descripcion" class="mensaje-error" role="alert" style="display:none;"></span>
                    <span id="contadorDesc" style="font-size:var(--font-size-xs, 11px);
                          color:var(--color-text-secondary); margin-left:auto;">0 / 500</span>
                </div>
            </div>

            <%-- Fecha de asignación --%>
            <div class="job-form__row">
                <label class="job-form__label" for="fechaAsignacion">
                    <i class="fa-solid fa-calendar-days" style="margin-right:4px;color:var(--color-brand-green);"></i> Fecha de asignación
                </label>
                <input type="date" id="fechaAsignacion" name="fechaAsignacion"
                       class="job-form__input" required>
                <span id="error-fechaAsignacion" class="mensaje-error" role="alert" style="display:none;"></span>
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

    <%-- Modal: nuevo tipo de trabajo --%>
    <div id="modalTipoTrabajo" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
        <div class="confirm-modal">
            <div class="confirm-modal__icon"><i class="fa-solid fa-tag"></i></div>
            <p class="confirm-modal__text">Crear nuevo tipo de trabajo</p>

            <div id="errorTipoTrabajo" class="feedback-message feedback-message--error"
                 style="display:none; margin-bottom:12px;"></div>

            <input type="text" id="inputNombreTipo" class="job-form__input"
                   placeholder="Ej: Riego, Poda, Fertilización..." maxlength="50"
                   style="margin-bottom:4px;">
            <div style="display:flex; justify-content:space-between; margin-bottom:16px;">
                <span id="errorInlineNombreTipo" style="font-size:12px; color:#c0392b; display:none;"></span>
                <span id="contadorTipo" style="font-size:11px; color:var(--color-text-secondary); margin-left:auto;">0 / 50</span>
            </div>

            <div class="confirm-modal__actions">
                <button class="btn btn--cancel" onclick="cerrarModal('modalTipoTrabajo')">Cancelar</button>
                <button class="btn--confirm-delete" style="background:var(--color-brand-green);"
                        onclick="ejecutarCrearTipoTrabajo()">
                    <i class="fa-solid fa-floppy-disk"></i> Guardar
                </button>
            </div>
        </div>
    </div>

</main>

<script src="${pageContext.request.contextPath}/static/js/validaciones.js"></script>
<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
<script src="${pageContext.request.contextPath}/static/js/validaciones-asignar.js"></script>
</body>
</html>
