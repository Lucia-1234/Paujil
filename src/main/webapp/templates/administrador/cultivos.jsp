<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.cultivo, java.util.List, java.util.Map"%>
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

    <%-- Mensajes de estado --%>
    <% String status = request.getParameter("status"); %>
    <% if ("success".equals(status) || "eliminado".equals(status) || "registroEliminado".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i>
            <%= "eliminado".equals(status) ? "Cultivo eliminado correctamente."
              : "registroEliminado".equals(status) ? "Registro eliminado correctamente."
              : "Operación realizada correctamente." %>
        </div>
    <% } else if ("error".equals(status)) { %>
        <div class="feedback-message feedback-message--error">
            <i class="fa-solid fa-circle-exclamation"></i> Ocurrió un error. Intente nuevamente.
        </div>
    <% } %>

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
           class="list-header__back" aria-label="Volver al menú">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Gestión de Cultivos</h1>
    </header>

    <div class="panel">
        <section class="crop-list">
<%
    List<cultivo> lista = (List<cultivo>) request.getAttribute("listaCultivos");
    Map<Integer, Integer> contadores = (Map<Integer, Integer>) request.getAttribute("contadoresHistorial");
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
            Integer conteo = (contadores != null) ? contadores.get(c.getIdCultivo()) : 0;
            String nomEsc  = c.getNombreCultivo().replace("'", "\\'");
            String tipoEsc = c.getTipoCultivo() != null ? c.getTipoCultivo().replace("'", "\\'") : "";
            String cosecha = c.getFechaCosecha() != null ? c.getFechaCosecha().toString() : "";
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
                    <div class="crop-card__actions">
                        <button class="btn btn--history"
                                onclick="toggleHistorial(this, '<%= c.getIdCultivo() %>')">
                            <i class="fa-solid fa-clock-rotate-left"></i>
                            Historial (<%= conteo != null ? conteo : 0 %>)
                        </button>
                        <button class="btn btn--edit"
                                data-id="<%= c.getIdCultivo() %>"
                                data-nombre="<%= nomEsc %>"
                                data-tipo="<%= tipoEsc %>"
                                data-siembra="<%= c.getFechaSiembra() %>"
                                data-cosecha="<%= cosecha %>"
                                onclick="abrirModalEditar(
                                    this.dataset.id,
                                    this.dataset.nombre,
                                    this.dataset.tipo,
                                    this.dataset.siembra,
                                    this.dataset.cosecha)">
                            <i class="fa-solid fa-pen"></i> Editar
                        </button>
                        <button class="btn btn--delete"
                                data-id="<%= c.getIdCultivo() %>"
                                onclick="abrirModalEliminar(this.dataset.id)">
                            <i class="fa-solid fa-trash"></i> Eliminar
                        </button>
                        <button class="btn btn--add"
                                onclick="abrirModalRegistro('<%= c.getIdCultivo() %>')">
                            <i class="fa-solid fa-plus"></i> Agregar registro
                        </button>
                    </div>
                </div>
            </article>

            <%-- Fila expandible del historial --%>
            <div id="historial-<%= c.getIdCultivo() %>" class="history-panel" style="display:none;">
                <div id="history-content-<%= c.getIdCultivo() %>" class="history-content">
                    <p class="no-data">Cargando...</p>
                </div>
            </div>
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

<%-- ═══════════════ MODALES ═══════════════ --%>

<%-- Modal Agregar / Editar Cultivo --%>
<div id="modalEditar" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true" aria-labelledby="modalTitulo">
    <div class="modal-content">
        <button type="button" class="modal-close" onclick="cerrarModal('modalEditar')" aria-label="Cerrar">&times;</button>
        <h2 id="modalTitulo">Editar Cultivo</h2>
        <form action="${pageContext.request.contextPath}/ServletCultivo" method="POST">
            <input type="hidden" id="editId" name="id">
            <label>Nombre del cultivo</label>
            <input type="text" id="editNombre" name="nombreCultivo" required>
            <label>Tipo</label>
            <input type="text" id="editTipo" name="tipoCultivo">
            <label>Fecha de siembra</label>
            <input type="date" id="editSiembra" name="fechaSiembra" required>
            <label>Fecha de cosecha</label>
            <input type="date" id="editCosecha" name="fechaCosecha">
            <button type="submit" class="btn--save-form">
                <i class="fa-solid fa-floppy-disk"></i> Guardar cambios
            </button>
        </form>
    </div>
</div>

<%-- Modal Confirmación Eliminación --%>
<div id="modalConfirmacion" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
    <div class="confirm-modal">
        <div class="confirm-modal__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
        <p class="confirm-modal__text">¿Seguro que deseas<br>eliminar este cultivo?</p>
        <div class="confirm-modal__actions">
            <button type="button" class="btn btn--cancel" onclick="cerrarModal('modalConfirmacion')">Cancelar</button>
            <button type="button" class="btn--confirm-delete" onclick="ejecutarEliminacion()">Eliminar</button>
        </div>
    </div>
</div>

<%-- Modal Agregar Registro Histórico --%>
<div id="modalRegistro" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
    <div class="modal-content">
        <button type="button" class="modal-close" onclick="cerrarModal('modalRegistro')" aria-label="Cerrar">&times;</button>
        <h2>Agregar registro de labor</h2>
        <form action="${pageContext.request.contextPath}/ServletLabor" method="POST">
            <input type="hidden" id="regIdCultivo" name="idCultivo">
            <label>Labor realizada *</label>
            <textarea name="descripcionTrabajo" required maxlength="1000" rows="3"></textarea>
            <label>Fecha de inicio *</label>
            <input type="date" name="fechaInicio" required>
            <label>Fecha de finalización *</label>
            <input type="date" name="fechaFinalizo" required>
            <label>Observaciones</label>
            <textarea name="observaciones" rows="2" maxlength="500"></textarea>
            <button type="submit" class="btn--save-form">
                <i class="fa-solid fa-floppy-disk"></i> Guardar registro
            </button>
        </form>
    </div>
</div>
            
<%-- Modal confirmación eliminar registro --%>
<div id="modalConfirmacionRegistro" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
    <div class="confirm-modal">
        <div class="confirm-modal__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
        <p class="confirm-modal__text">¿Seguro que deseas<br>eliminar este registro?</p>
        <div class="confirm-modal__actions">
            <button type="button" class="btn btn--cancel"
                    onclick="cerrarModal('modalConfirmacionRegistro')">Cancelar</button>
            <button type="button" class="btn--confirm-delete"
                    onclick="ejecutarEliminacionRegistro()">Eliminar</button>
        </div>
    </div>
</div>

<script>window._ctxPath = '${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
</body>
</html>