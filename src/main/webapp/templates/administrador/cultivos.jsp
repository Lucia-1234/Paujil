<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.cultivo, com.paujil.modelo.lote, java.util.List, java.util.Map"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/cultivos.css">

    <title>Gestión de Cultivos - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <%-- Mensajes de estado del backend (operaciones completadas o errores de BD) --%>
    <% String status = request.getParameter("status");
        String motivo = request.getParameter("motivo"); %>
     <% if ("success".equals(status) || "eliminado".equals(status)) { %>
         <div class="feedback-message feedback-message--ok" role="status">
             <i class="fa-solid fa-circle-check"></i>
             <%= "eliminado".equals(status) ? "Cultivo eliminado correctamente." : "Operación realizada correctamente." %>
         </div>
     <% } else if ("error".equals(status)) { %>
         <div class="feedback-message feedback-message--error" role="alert">
             <i class="fa-solid fa-circle-exclamation"></i>
             <% if ("asignaciones_activas".equals(motivo)) { %>
                 No se puede eliminar este cultivo porque tiene trabajos activos
                 (Pendiente, En proceso o En revisión). Finaliza esos trabajos primero.
             <% } else { %>
                 Ocurrió un error. Intente nuevamente.
             <% } %>
         </div>
     <% } %>

    <%-- Mensaje de error del backend (validación de seguridad que pasó el servlet) --%>
    <% String mensajeError = (String) request.getAttribute("mensajeError");
       if (mensajeError != null) { %>
        <div class="feedback-message feedback-message--error alerta-servidor" role="alert">
            <i class="fa-solid fa-circle-exclamation"></i> <%= mensajeError %>
        </div>
    <% } %>

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
           class="list-header__back" aria-label="Volver al menú">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Gestión de Cultivos</h1>
        <a href="${pageContext.request.contextPath}/ServletLote"
           class="btn btn--lotes" aria-label="Ir a gestión de lotes">
            <i class="fa-solid fa-map-location-dot"></i> Gestionar Lotes
        </a>
    </header>

    <div class="panel">
        <section class="crop-list">
<%
    List<cultivo> lista = (List<cultivo>) request.getAttribute("listaCultivos");
    Map<Integer, List<lote>> lotesPorCultivo = (Map<Integer, List<lote>>) request.getAttribute("lotesPorCultivo");
    if (lista == null) lista = new java.util.ArrayList<>();
    if (lista.isEmpty()) {
%>
            <p class="no-data">
                <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                No hay cultivos registrados.
            </p>
<%
    } else {
        for (cultivo c : lista) {
            String nomEsc  = c.getNombreCultivo().replace("'", "\\'");
            String tipoEsc = c.getTipoCultivo() != null ? c.getTipoCultivo().replace("'", "\\'") : "";
            String cosecha = c.getFechaCosecha() != null ? c.getFechaCosecha().toString() : "";
            List<lote> lotesCultivo = (lotesPorCultivo != null) ? lotesPorCultivo.get(c.getIdCultivo()) : null;

            // Construye el atributo data-lotes con los IDs separados por coma, para que el
            // formulario de edición sepa qué checkboxes premarcar (ej: data-lotes="1,3,5").
            StringBuilder idsLotesAttr = new StringBuilder();
            if (lotesCultivo != null) {
                for (int i = 0; i < lotesCultivo.size(); i++) {
                    if (i > 0) idsLotesAttr.append(",");
                    idsLotesAttr.append(lotesCultivo.get(i).getIdLote());
                }
            }
%>
            <article class="crop-card">
                <div class="crop-card__content">
                    <h2 class="crop-card__title"><%= c.getNombreCultivo() %></h2>
                    <p class="crop-card__description">
                        <strong>Tipo:</strong> <%= c.getTipoCultivo() != null ? c.getTipoCultivo() : "—" %>
                    </p>
                    <p class="crop-card__dates">
                        <i class="fa-solid fa-seedling" style="color:var(--color-brand-green);"></i>
                        Siembra: <strong><%= c.getFechaSiembra() %></strong>
                        &nbsp;·&nbsp;
                        <i class="fa-solid fa-basket-shopping" style="color:var(--color-brand-green);"></i>
                        Cosecha: <strong><%= !cosecha.isEmpty() ? cosecha : "Por definir" %></strong>
                    </p>

                    <%-- Etiquetas de lotes asignados a este cultivo --%>
                    <p class="crop-card__lots">
                        <i class="fa-solid fa-map-location-dot" style="color:var(--color-brand-green);"></i>
                        Lote: 
                        <%
                            // Buscamos el nombre del lote en la lista completa que pasaste desde el Servlet
                            String nombreLote = "No asignado";
                            List<lote> todosLosLotes = (List<lote>) request.getAttribute("catalogoLotes");

                            if (todosLosLotes != null) {
                                for (lote l : todosLosLotes) {
                                    if (l.getIdLote() == c.getIdLote()) {
                                        nombreLote = l.getNombreLote();
                                        break;
                                    }
                                }
                            }
                        %>
                        <span class="lot-tag"><%= nombreLote %></span>
                    </p>

                    <div class="crop-card__actions">
                        <button class="btn btn--edit"
                            data-id="<%= c.getIdCultivo() %>"
                            data-nombre="<%= nomEsc %>"
                            data-tipo="<%= tipoEsc %>"
                            data-siembra="<%= c.getFechaSiembra() %>"
                            data-cosecha="<%= cosecha %>"
                            data-id-lote="<%= c.getIdLote() %>" 
                            onclick="abrirModalEditar(
                                this.dataset.id,
                                this.dataset.nombre,
                                this.dataset.tipo,
                                this.dataset.siembra,
                                this.dataset.cosecha,
                                this.dataset.idLote)">
                        <i class="fa-solid fa-pen"></i> Editar
                    </button>
                        <button class="btn btn--delete"
                                data-id="<%= c.getIdCultivo() %>"
                                onclick="abrirModalEliminar(this.dataset.id)">
                            <i class="fa-solid fa-trash"></i> Eliminar
                        </button>
                    </div>
                </div>
            </article>
<%
        }
    }
%>
            <div class="crop-list__footer">
                <button type="button" class="btn btn--new" onclick="abrirModalAgregar()">
                    <i class="fa-solid fa-circle-plus"></i> Agregar cultivo
                </button>
            </div>
        </section>
    </div>
</main>

<%-- ═══════════════════════════════════════════════════════
     MODALES
     ═══════════════════════════════════════════════════════ --%>

<%-- ── Modal Agregar / Editar Cultivo ── --%>
<div id="modalEditar" class="modal-overlay" style="display:none;"
     role="dialog" aria-modal="true" aria-labelledby="modalTitulo">
    <div class="modal-content">
        <button type="button" class="modal-close"
                onclick="cerrarModal('modalEditar')" aria-label="Cerrar">&times;</button>
        <h2 id="modalTitulo">Editar Cultivo</h2>

        <%--
            novalidate → desactiva validación nativa del navegador;
                         el módulo validaciones-cultivos.js toma el control.
        --%>
        <form id="formCultivo"
              action="${pageContext.request.contextPath}/ServletCultivo"
              method="POST"
              novalidate>

            <input type="hidden" id="editId" name="id">

            <label for="editNombre">Nombre del cultivo *</label>
            <input type="text"
                   id="editNombre"
                   name="nombreCultivo"
                   maxlength="80"
                   aria-required="true">
            <span id="error-editNombre" class="error-fecha" role="alert" style="display:none;"></span>

            <label for="editTipo">Tipo</label>
            <input type="text"
                   id="editTipo"
                   name="tipoCultivo"
                   maxlength="60">
            <span id="error-editTipo" class="error-fecha" role="alert" style="display:none;"></span>

            <label for="editSiembra">Fecha de siembra *</label>
            <input type="date"
                   id="editSiembra"
                   name="fechaSiembra"
                   aria-required="true">
            <span id="error-editSiembra" class="error-fecha" role="alert" style="display:none;"></span>

            <label for="editCosecha">Fecha de cosecha</label>
            <input type="date"
                   id="editCosecha"
                   name="fechaCosecha">
            <span id="error-editCosecha" class="error-fecha" role="alert" style="display:none;"></span>

            <%-- Selección de lotes: relación muchos-a-muchos vía checkboxes con el mismo "name" --%>
            <label for="editLote">Lote asignado *</label>
            <select id="editLote" name="idLote" class="form-control" required>
                <option value="">-- Seleccione un lote --</option>
            <%
                List<lote> catalogoLotes = (List<lote>) request.getAttribute("catalogoLotes");
                if (catalogoLotes != null) {
                    for (lote l : catalogoLotes) {
            %>
                        <option value="<%= l.getIdLote() %>"><%= l.getNombreLote() %></option>
            <%
                    }
                }
            %>
            </select>

            <button type="submit" class="btn--save-form">
                <i class="fa-solid fa-floppy-disk"></i> Guardar cambios
            </button>
        </form>
    </div>
</div>

<%-- ── Modal Confirmación Eliminación Cultivo ── --%>
<div id="modalConfirmacion" class="modal-overlay" style="display:none;"
     role="dialog" aria-modal="true">
    <div class="confirm-modal">
        <div class="confirm-modal__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
        <p class="confirm-modal__text">¿Seguro que deseas<br>eliminar este cultivo?</p>
        <div class="confirm-modal__actions">
            <button type="button" class="btn btn--cancel"
                    onclick="cerrarModal('modalConfirmacion')">Cancelar</button>
            <button type="button" class="btn--confirm-delete"
                    onclick="ejecutarEliminacion()">Eliminar</button>
        </div>
    </div>
</div>

<script>window._ctxPath = '${pageContext.request.contextPath}';</script>

<%--
    Orden de carga obligatorio:
    1. validaciones.js          → funciones puras y helpers UI (base compartida)
    2. validaciones-cultivos.js → validación de fechas y textos para cultivos
    3. script.js                → modales, toggles
--%>
<script src="${pageContext.request.contextPath}/static/js/validaciones.js"></script>
<script src="${pageContext.request.contextPath}/static/js/Validaciones_cultivo.js"></script>
<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
</body>
</html>
