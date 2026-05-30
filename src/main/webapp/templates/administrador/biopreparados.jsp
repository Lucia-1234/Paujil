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
    <title>Biopreparados - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

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

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
           class="list-header__back" aria-label="Volver al menú">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Biopreparados</h1>
    </header>

    <div class="panel">
        <section class="bio-list">
            <%
                List<biopreparado> lista = (List<biopreparado>) request.getAttribute("listaBiopreparados");
                if (lista == null || lista.isEmpty()) {
            %>
                <p class="no-data">
                    <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
                    No hay biopreparados registrados.
                </p>
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

                        <p class="bio-card__prep-label" style="margin-bottom:var(--spacing-sm);">
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

                        <% if (!b.getIngredientes().isEmpty()) { %>
                            <table class="bio-card__ingredients">
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
                        <% } %>

                        <div class="bio-card__actions">
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
    </div>
</main>

<%-- ═══════════════ MODALES ═══════════════ --%>

<%-- Modal Registro / Edición de Biopreparado --%>
<div id="modalBio" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true" aria-labelledby="modalBioTitulo">
    <div class="modal-content">
        <button type="button" class="modal-close" onclick="cerrarModal('modalBio')" aria-label="Cerrar">&times;</button>
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

            <button type="button" style="background:none;border:none;color:var(--color-white);cursor:pointer;font-size:var(--font-size-sm);margin-top:8px;"
                    onclick="agregarIngrediente()">
                <i class="fa-solid fa-plus"></i> Añadir ingrediente
            </button>

            <button type="submit" class="btn--save-form">
                <i class="fa-solid fa-floppy-disk"></i> Guardar
            </button>
        </form>
    </div>
</div>

<%-- Modal Confirmación Eliminación --%>
<div id="modalConfirmacionBio" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
    <div class="confirm-modal">
        <div class="confirm-modal__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
        <p class="confirm-modal__text">¿Eliminar este<br>biopreparado?</p>
        <p style="font-size:var(--font-size-sm);margin-bottom:var(--spacing-md);opacity:.85;">
            Esta acción también eliminará sus ingredientes y no se puede deshacer.
        </p>
        <div class="confirm-modal__actions">
            <button type="button" class="btn btn--cancel" onclick="cerrarModal('modalConfirmacionBio')">Cancelar</button>
            <button type="button" class="btn--confirm-delete" onclick="ejecutarEliminacionBio()">Eliminar</button>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
</body>
</html>
