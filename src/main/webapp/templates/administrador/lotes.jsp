<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.lote, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/lotes.css">

    <title>Gestión de Lotes - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <%-- Mensajes de estado del backend --%>
    <% String status = request.getParameter("status"); %>
    <% if ("success".equals(status) || "eliminado".equals(status)) { %>
        <div class="feedback-message feedback-message--ok" role="status">
            <i class="fa-solid fa-circle-check"></i>
            <%= "eliminado".equals(status) ? "Lote eliminado correctamente." : "Operación realizada correctamente." %>
        </div>
    <% } else if ("error".equals(status)) { %>
        <div class="feedback-message feedback-message--error" role="alert">
            <i class="fa-solid fa-circle-exclamation"></i> Ocurrió un error. Intente nuevamente.
        </div>
    <% } %>

    <%-- Mensaje de error del backend (validación de seguridad que pasó el servlet) --%>
    <% String mensajeError = (String) request.getAttribute("mensajeError");
       if (mensajeError != null) { %>
        <div class="feedback-message feedback-message--error" role="alert">
            <i class="fa-solid fa-circle-exclamation"></i> <%= mensajeError %>
        </div>
    <% } %>

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/ServletCultivo"
           class="list-header__back" aria-label="Volver a cultivos">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Gestión de Lotes</h1>
    </header>

    <div class="panel">
        <section class="lot-list">
<%
    List<lote> lista = (List<lote>) request.getAttribute("listaLotes");
    if (lista == null) lista = new java.util.ArrayList<>();
    if (lista.isEmpty()) {
%>
            <p class="no-data">
                <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                No hay lotes registrados.
            </p>
<%
    } else {
        for (lote l : lista) {
            String nomEsc = l.getNombreLote().replace("'", "\\'");
%>
            <article class="lot-card">
                <div class="lot-card__content">
                    <div class="lot-card__icon">
                        <i class="fa-solid fa-map-location-dot"></i>
                    </div>
                    <div class="lot-card__info">
                        <h2 class="lot-card__title"><%= l.getNombreLote() %></h2>
                    </div>
                    <div class="lot-card__actions">
                        <button class="btn btn--edit"
                                data-id="<%= l.getIdLote() %>"
                                data-nombre="<%= nomEsc %>"
                                onclick="abrirModalEditarLote(this.dataset.id, this.dataset.nombre)">
                            <i class="fa-solid fa-pen"></i> Editar
                        </button>
                        <button class="btn btn--delete"
                                data-id="<%= l.getIdLote() %>"
                                onclick="abrirModalEliminarLote(this.dataset.id)">
                            <i class="fa-solid fa-trash"></i> Eliminar
                        </button>
                    </div>
                </div>
            </article>
<%
        }
    }
%>
            <div class="lot-list__footer">
                <button type="button" class="btn btn--new" onclick="abrirModalAgregarLote()">
                    <i class="fa-solid fa-circle-plus"></i> Agregar lote
                </button>
            </div>
        </section>
    </div>
</main>

<%-- ═══════════════════════════════════════════════════════
     MODALES
     ═══════════════════════════════════════════════════════ --%>

<%-- ── Modal Agregar / Editar Lote ── --%>
<div id="modalEditarLote" class="modal-overlay" style="display:none;"
     role="dialog" aria-modal="true" aria-labelledby="modalTituloLote">
    <div class="modal-content">
        <button type="button" class="modal-close"
                onclick="cerrarModal('modalEditarLote')" aria-label="Cerrar">&times;</button>
        <h2 id="modalTituloLote">Editar Lote</h2>

        <form id="formLote"
              action="${pageContext.request.contextPath}/ServletLote"
              method="POST"
              novalidate>

            <input type="hidden" id="loteId" name="id">

            <label for="loteNombre">Nombre del lote *</label>
            <input type="text"
                   id="loteNombre"
                   name="nombreLote"
                   maxlength="80"
                   aria-required="true">
            <span id="error-loteNombre" class="error-fecha" role="alert" style="display:none;"></span>

            <button type="submit" class="btn--save-form">
                <i class="fa-solid fa-floppy-disk"></i> Guardar cambios
            </button>
        </form>
    </div>
</div>

<%-- ── Modal Confirmación Eliminación Lote ── --%>
<div id="modalConfirmacionLote" class="modal-overlay" style="display:none;"
     role="dialog" aria-modal="true">
    <div class="confirm-modal">
        <div class="confirm-modal__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
        <p class="confirm-modal__text">¿Seguro que deseas<br>eliminar este lote?</p>
        <p class="confirm-modal__subtext">
            Se eliminará también su asignación en todos los cultivos que lo tengan.
        </p>
        <div class="confirm-modal__actions">
            <button type="button" class="btn btn--cancel"
                    onclick="cerrarModal('modalConfirmacionLote')">Cancelar</button>
            <button type="button" class="btn--confirm-delete"
                    onclick="ejecutarEliminacionLote()">Eliminar</button>
        </div>
    </div>
</div>

<script>window._ctxPath = '${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/static/js/validaciones.js"></script>
<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
<script src="${pageContext.request.contextPath}/static/js/script_lotes.js"></script>
</body>
</html>
