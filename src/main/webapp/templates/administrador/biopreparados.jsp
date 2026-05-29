<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.biopreparado, com.paujil.modelo.biopreparado.ingredienteBio, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/biopreparados.css">
    <title>Gestión de Biopreparados - Finca El Paujil</title>
</head>
<body>
<main class="container">

    <%-- Mensajes de estado --%>
    <%
        String status = request.getParameter("status");
        String msg    = request.getParameter("msg");
    %>
    <% if ("success".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Operación realizada correctamente.
        </div>
    <% } else if ("error".equals(status)) { %>
        <div class="feedback-message feedback-message--error">
            <i class="fa-solid fa-circle-exclamation"></i>
            <%= (msg != null && !msg.isBlank()) ? msg : "Ocurrió un error. Intente nuevamente." %>
        </div>
    <% } %>

    <%-- Listado de tarjetas --%>
    <section class="bio-list">
        <%
            List<biopreparado> lista = (List<biopreparado>) request.getAttribute("listaBiopreparados");
            if (lista == null || lista.isEmpty()) {
        %>
            <article class="bio-card">
                <p class="no-data">No hay biopreparados registrados.</p>
            </article>
        <%
            } else {
                int num = 1;
                for (biopreparado b : lista) {
                    String nomEsc  = b.getNombre().replace("'", "\\'");
                    String descEsc = b.getDescripcion() != null
                                     ? b.getDescripcion().replace("'", "\\'").replace("\n", "\\n") : "";
                    String prepEsc = b.getPreparacion() != null
                                     ? b.getPreparacion().replace("'", "\\'").replace("\n", "\\n") : "";
        %>
            <article class="bio-card">
                <div class="bio-card__content">
                    <p class="bio-card__number"><%= String.format("%02d", num++) %></p>
                    <h2 class="bio-card__title"><%= b.getNombre() %></h2>

                    <% if (b.getDescripcion() != null && !b.getDescripcion().isBlank()) { %>
                        <p class="bio-card__description"><%= b.getDescripcion() %></p>
                    <% } %>

                    <p class="bio-card__meta">
                        <span>
                            <i class="fa-solid fa-calendar-days"></i>
                            Creación: <%= b.getFechaCreacion() %> &nbsp;|&nbsp;
                            Vence: <%= b.getFechaVencimiento() %>
                        </span>
                        <% if (b.getPrecio() > 0) { %>
                            <span>
                                <i class="fa-solid fa-tag"></i>
                                Precio: $<%= String.format("%.2f", b.getPrecio()) %>
                            </span>
                        <% } %>
                    </p>

                    <% if (!b.getIngredientes().isEmpty()) { %>
                        <ul class="bio-card__ingredients">
                            <% for (ingredienteBio ing : b.getIngredientes()) { %>
                                <li>
                                    <%= ing.getNombre() %>
                                    <% if (ing.getCantidad() > 0) { %>
                                        — <%= ing.getCantidad() %>
                                        <%= ing.getUnidad() != null ? ing.getUnidad() : "" %>
                                    <% } %>
                                </li>
                            <% } %>
                        </ul>
                    <% } %>

                    <div class="bio-card__actions">
                        <%-- Los datos se pasan como data-* y script.js los lee --%>
                        <button class="btn btn--edit"
                                data-id="<%= b.getIdBiopreparado() %>"
                                data-nombre="<%= nomEsc %>"
                                data-descripcion="<%= descEsc %>"
                                data-precio="<%= b.getPrecio() %>"
                                data-creacion="<%= b.getFechaCreacion() %>"
                                data-vencimiento="<%= b.getFechaVencimiento() %>"
                                data-preparacion="<%= prepEsc %>"
                                onclick="abrirModalBioEditar(
                                    this.dataset.id,
                                    this.dataset.nombre,
                                    this.dataset.descripcion,
                                    this.dataset.precio,
                                    this.dataset.creacion,
                                    this.dataset.vencimiento,
                                    this.dataset.preparacion)">
                            <i class="fa-solid fa-pen"></i> Editar
                        </button>
                        <button class="btn btn--delete"
                                data-id="<%= b.getIdBiopreparado() %>"
                                onclick="abrirModalBioEliminar(this.dataset.id)">
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
            <button type="button" class="btn btn--new" onclick="abrirModalBioAgregar()">
                <i class="fa-solid fa-circle-plus"></i> Agregar biopreparado
            </button>
        </div>
    </section>
</main>

<%-- ══════════════════════════════════════════
     MODALES — sin JavaScript inline
     ══════════════════════════════════════ --%>

<%-- Modal Registro / Edición --%>
<div id="modalBio" class="modal-overlay" style="display:none;">
    <div class="modal-content">
        <button type="button" class="modal-close" onclick="cerrarModal('modalBio')">&times;</button>
        <h2 id="modalBioTitulo">Agregar Biopreparado</h2>

        <form action="${pageContext.request.contextPath}/ServletBiopreparado" method="POST">
            <input type="hidden" id="bioId" name="id">

            <label>Nombre *</label>
            <input type="text" id="bioNombre" name="nombreBio" required maxlength="200">

            <label>Descripción</label>
            <textarea id="bioDescripcion" name="descripcionBio" rows="3"></textarea>

            <label>Precio ($)</label>
            <input type="number" id="bioPrecio" name="precioBio" step="0.01" min="0">

            <label>Fecha de creación *</label>
            <input type="date" id="bioFechaCreacion" name="fechaCreacion" required>

            <label>Fecha de vencimiento *</label>
            <input type="date" id="bioFechaVencimiento" name="fechaVencimiento" required>

            <label>Modo de preparación</label>
            <textarea id="bioPreparacion" name="preparacionBio" rows="4"
                      placeholder="Pasos de elaboración..."></textarea>

            <label>Ingredientes</label>
            <div id="contenedorIngredientes"></div>

            <button type="button" class="bio-modal__add-ingredient" onclick="agregarIngrediente()">
                <i class="fa-solid fa-plus"></i>
            </button>

            <button type="submit" class="btn btn--new" style="margin-top:14px;">
                <i class="fa-solid fa-floppy-disk"></i> Guardar
            </button>
        </form>
    </div>
</div>

<%-- Modal Confirmación Eliminación --%>
<div id="modalConfirmacionBio" class="modal-overlay" style="display:none;">
    <div class="modal-content">
        <h2>¿Eliminar este biopreparado?</h2>
        <p>Esta acción eliminará también todos sus ingredientes y no se puede deshacer.</p>
        <button type="button" class="btn" onclick="cerrarModal('modalConfirmacionBio')">Cancelar</button>
        <button type="button" class="btn btn--delete" onclick="ejecutarEliminacionBio()">
            Confirmar eliminación
        </button>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
</body>
</html>
