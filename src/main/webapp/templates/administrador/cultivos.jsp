<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.dao.CultivoDao, com.paujil.modelo.cultivo, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/cultivo.css">
    <title>Gestión de Cultivos - Finca El Paujil</title>
</head>
<body>
<main class="container">

    <%-- Mensajes de estado --%>
    <% String status = request.getParameter("status"); %>
    <% if ("success".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Operación realizada correctamente.
        </div>
    <% } else if ("error".equals(status)) { %>
        <div class="feedback-message feedback-message--error">
            <i class="fa-solid fa-circle-exclamation"></i> Ocurrió un error. Intente nuevamente.
        </div>
    <% } else if ("eliminado".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Cultivo eliminado correctamente.
        </div>
    <% } %>

    <section class="crop-list">
        <%
            CultivoDao dao = new CultivoDao();
            List<cultivo> lista = dao.listarCultivos();
            if (lista.isEmpty()) {
        %>
            <article class="crop-card">
                <p class="no-data">No hay cultivos registrados.</p>
            </article>
        <%
            } else {
                for (cultivo c : lista) {
                    String nomEsc  = c.getNombreCultivo().replace("'", "\\'");
                    String tipoEsc = c.getTipoCultivo() != null
                                     ? c.getTipoCultivo().replace("'", "\\'") : "";
                    String cosecha = c.getFechaCosecha() != null
                                     ? c.getFechaCosecha().toString() : "";
        %>
            <article class="crop-card">
                <div class="crop-card__content">
                    <h2 class="crop-card__title"><%= c.getNombreCultivo() %></h2>
                    <p class="crop-card__description">
                        Tipo: <%= c.getTipoCultivo() != null ? c.getTipoCultivo() : "—" %>
                    </p>
                    <p class="crop-card__dates">
                        Siembra: <%= c.getFechaSiembra() %> |
                        Cosecha: <%= !cosecha.isEmpty() ? cosecha : "Por definir" %>
                    </p>
                    <div class="crop-card__actions">
                        <%-- data-* para evitar escape manual de comillas en onclick --%>
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
                        <a href="${pageContext.request.contextPath}/templates/administrador/registro.jsp?idCultivo=<%= c.getIdCultivo() %>"
                           class="btn btn--add">
                            <i class="fa-solid fa-plus"></i> Agregar registro
                        </a>
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
</main>

<%-- ══════════════════════════════════════════
     MODALES — sin JavaScript inline
     ══════════════════════════════════════ --%>

<%-- Modal Registro / Edición --%>
<div id="modalEditar" class="modal-overlay" style="display:none;">
    <div class="modal-content">
        <button type="button" class="modal-close" onclick="cerrarModal('modalEditar')">&times;</button>
        <h2 id="modalTitulo">Editar Cultivo</h2>
        <form action="${pageContext.request.contextPath}/ServletCultivo" method="POST">
            <input type="hidden" id="editId" name="id">
            <label>Nombre</label>
            <input type="text" id="editNombre" name="nombreCultivo" required>
            <label>Tipo</label>
            <input type="text" id="editTipo" name="tipoCultivo">
            <label>Fecha Siembra</label>
            <input type="date" id="editSiembra" name="fechaSiembra" required>
            <label>Fecha Cosecha</label>
            <input type="date" id="editCosecha" name="fechaCosecha">
            <button type="submit" class="btn btn--new">
                <i class="fa-solid fa-floppy-disk"></i> Guardar cambios
            </button>
        </form>
    </div>
</div>

<%-- Modal Confirmación Eliminación --%>
<div id="modalConfirmacion" class="modal-overlay" style="display:none;">
    <div class="modal-content">
        <h2>¿Seguro que deseas eliminar?</h2>
        <p>Esta acción no se puede deshacer.</p>
        <button type="button" class="btn" onclick="cerrarModal('modalConfirmacion')">Cancelar</button>
        <button type="button" class="btn btn--delete" onclick="ejecutarEliminacion()">
            Confirmar eliminación
        </button>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
</body>
</html>
>