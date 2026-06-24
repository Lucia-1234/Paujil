<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.asignacion, com.paujil.modelo.tipoTrabajo, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
    <title>Monitoreo de Trabajos - Finca El Paujil</title>
</head>
<body>
<main class="page-wrapper">

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
           class="list-header__back">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Gestión de Trabajos</h1>
        <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=prepararCreacion"
           class="btn btn--new" style="margin-left:auto;">
            <i class="fa-solid fa-circle-plus"></i> Asignar trabajo
        </a>
    </header>

    <%-- Feedback --%>
    <% String status = request.getParameter("status");
       if ("success".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Trabajo asignado correctamente.
        </div>
    <% } else if ("eliminado".equals(status)) { %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Trabajo eliminado correctamente.
        </div>
    <% } else if ("error".equals(status)) { %>
        <div class="feedback-message feedback-message--error">
            <i class="fa-solid fa-circle-exclamation"></i> Ocurrió un error. Intenta de nuevo.
        </div>
    <% } %>

    <%--
        ══════════════════════════════════════════════════════
        ACORDEÓN: Tipos de trabajo (colapsado por defecto)
        Al hacer clic en la cabecera se expande/colapsa.
        Contiene la tabla de tipos + botón "Nuevo tipo".
        ══════════════════════════════════════════════════════
    --%>
    <div class="tipos-acordeon" id="acordeonTipos">
        <%-- Cabecera clicable --%>
        <div class="tipos-acordeon__header" id="acordeonHeader" onclick="toggleAcordeonTipos(event)">
            <span class="tipos-acordeon__header-left">
                <i class="fa-solid fa-tags"></i> Tipos de trabajo
            </span>
            <span class="tipos-acordeon__header-right">
                <button type="button" class="btn-nuevo-tipo" onclick="abrirModalTipoTrabajo()">
                    <i class="fa-solid fa-plus"></i> Nuevo tipo
                </button>
                <i class="fa-solid fa-chevron-down tipos-acordeon__chevron" id="acordeonChevron"></i>
            </span>
        </div>

        <%-- Cuerpo expandible --%>
        <div class="tipos-acordeon__body" id="acordeonBody">
            <table class="tipos-acordeon__table" id="tablaTipos">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th class="acciones-td">Acciones</th>
                    </tr>
                </thead>
                <tbody id="tbodyTipos">
                    <%
                        List<tipoTrabajo> tipos = (List<tipoTrabajo>) request.getAttribute("listaTiposTrabajo");
                        if (tipos == null || tipos.isEmpty()) {
                    %>
                        <tr id="filaSinTipos">
                            <td colspan="2" class="no-tipos-msg">Sin tipos de trabajo registrados.</td>
                        </tr>
                    <%
                        } else {
                            for (tipoTrabajo tp : tipos) {
                    %>
                        <tr id="tipo-fila-<%= tp.getIdTipoTrabajo() %>">
                            <td id="tipo-nombre-<%= tp.getIdTipoTrabajo() %>"><%= tp.getNombreTipo() %></td>
                            <td class="acciones-td">
                                <button type="button" class="btn-tipo-edit"
                                        onclick="abrirModalEditarTipo(<%= tp.getIdTipoTrabajo() %>, '<%= tp.getNombreTipo().replace("'", "\\'") %>')">
                                    <i class="fa-solid fa-pen"></i>
                                </button>
                                <button type="button" class="btn-tipo-del"
                                        onclick="abrirModalEliminarTipo(<%= tp.getIdTipoTrabajo() %>, '<%= tp.getNombreTipo().replace("'", "\\'") %>')">
                                    <i class="fa-solid fa-trash"></i>
                                </button>
                            </td>
                        </tr>
                    <%
                            }
                        }
                    %>
                </tbody>
            </table>
        </div>
    </div>

    <%-- Filtro por ESTADO --%>
    <div class="filtros-estado">
        <span><i class="fa-solid fa-filter"></i> Estado:</span>
        <button class="filtro-btn filtro-estado-btn seleccionado" data-filtro="todos">Todos</button>
        <button class="filtro-btn filtro-estado-btn" data-filtro="Pendiente">Pendientes</button>
        <button class="filtro-btn filtro-estado-btn" data-filtro="En proceso">En proceso</button>
        <button class="filtro-btn filtro-estado-btn" data-filtro="Finalizado">Finalizados</button>
    </div>

    <%-- Filtro por TIPO DE TRABAJO --%>
    <%
        List<tipoTrabajo> tiposParaFiltro = (List<tipoTrabajo>) request.getAttribute("listaTiposTrabajo");
    %>
    <div class="filtros-estado" id="filtrosTipo" style="margin-top:6px;">
        <span><i class="fa-solid fa-tag"></i> Tipo:</span>
        <button class="filtro-btn filtro-tipo-btn seleccionado" data-tipo="todos">Todos</button>
    </div>

    <div class="panel" id="panelTrabajos">
        <div class="job-list" id="jobList">
            <%
                List<asignacion> lista = (List<asignacion>) request.getAttribute("listaAsignaciones");
                if (lista == null || lista.isEmpty()) {
            %>
                <p class="no-data">
                    <i class="fa-solid fa-circle-info"></i> No hay trabajos registrados.
                </p>
            <%
                } else {
                    for (asignacion a : lista) {
                        String nombreTipoEsc = a.getNombreTipoTrabajo() != null
                                               ? a.getNombreTipoTrabajo().replace("\"", "&quot;")
                                               : "";
                        // Construye etiqueta "Cultivo — Lote" para mostrar junto al nombre del cultivo.
                        String etiquetaCultivo = a.getNombreCultivo() != null ? a.getNombreCultivo() : "";
                        if (a.getNombreLote() != null && !a.getNombreLote().isEmpty()) {
                            etiquetaCultivo += " &mdash; " + a.getNombreLote();
                        }
            %>
                <article class="job-card"
                         data-estado="<%= a.getEstadoTrabajo() %>"
                         data-tipo="<%= nombreTipoEsc %>">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:10px;">
                        <h2 class="job-card__title" style="margin:0;">
                            <i class="fa-solid fa-tag"></i> <%= a.getNombreTipoTrabajo() %>
                        </h2>
                        <span class="badge
                            <%= "Finalizado".equals(a.getEstadoTrabajo()) ? "badge--activo"
                              : "En proceso".equals(a.getEstadoTrabajo()) ? "badge--enproceso"
                              : "badge--pendiente" %>">
                            <%= a.getEstadoTrabajo() %>
                        </span>
                    </div>

                    <div class="job-card__info" style="display:grid; grid-template-columns:1fr 1fr; gap:10px; margin-bottom:10px;">
                        <p><strong><i class="fa-solid fa-user"></i> Encargado:</strong> <%= a.getNombreUsuario() %></p>
                        <p><strong><i class="fa-solid fa-seedling"></i> Cultivo:</strong> <%= etiquetaCultivo %></p>
                        <p><strong><i class="fa-solid fa-calendar"></i> Asignado:</strong> <%= a.getFechaAsignacion() %></p>
                        <% if (a.getFechaInicio() != null) { %>
                            <p><strong><i class="fa-solid fa-play"></i> Iniciado:</strong> <%= a.getFechaInicio() %></p>
                        <% } %>
                        <% if (a.getFechaFinalizacion() != null) { %>
                            <p><strong><i class="fa-solid fa-check-double"></i> Terminado:</strong> <%= a.getFechaFinalizacion() %></p>
                        <% } %>
                    </div>

                    <p class="job-card__desc" style="border-top:1px solid #eee; padding-top:10px;">
                        <strong>Descripción:</strong> <%= a.getDescripcionTrabajo() %>
                    </p>

                    <% if (a.getObservaciones() != null && !a.getObservaciones().isEmpty()) { %>
                    <div class="obs-container" style="margin-top:10px; padding:10px; background:#f9f9f9; border-left:4px solid var(--color-brand-green); border-radius:4px;">
                        <strong><i class="fa-solid fa-pen-to-square"></i> Observaciones:</strong>
                        <p style="margin:5px 0 0 0; color:#555;"><%= a.getObservaciones() %></p>
                    </div>
                    <% } %>

                    <div class="job-card__actions" style="margin-top:10px;">
                        <a href="${pageContext.request.contextPath}/ServletTrabajo?accion=eliminar&id=<%= a.getIdTrabajo() %>"
                           class="btn btn--delete"
                           onclick="return confirm('¿Eliminar este trabajo y todas sus asignaciones?')">
                            <i class="fa-solid fa-trash"></i> Eliminar
                        </a>
                    </div>
                </article>
            <%
                    }
                }
            %>
        </div>
        <p class="no-data" id="sinResultadosFiltro" style="display:none;">
            <i class="fa-solid fa-circle-info"></i> No hay trabajos que coincidan con el filtro seleccionado.
        </p>
    </div>

    <%-- Modal: nuevo / editar tipo de trabajo --%>
    <div id="modalTipoTrabajo" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
        <div class="confirm-modal">
            <div class="confirm-modal__icon"><i class="fa-solid fa-tag"></i></div>
            <p class="confirm-modal__text" id="tituloModalTipo">Crear nuevo tipo de trabajo</p>

            <div id="errorTipoTrabajo" class="feedback-message feedback-message--error"
                 style="display:none; margin-bottom:12px;"></div>

            <input type="hidden" id="idTipoEditar" value="">
            <input type="text" id="inputNombreTipo" class="job-form__input"
                   placeholder="Ej: Riego, Poda, Fertilización..." maxlength="50"
                   style="margin-bottom:4px;">
            <div style="display:flex; justify-content:space-between; margin-bottom:16px;">
                <span id="errorInlineNombreTipo" style="font-size:12px; color:#c0392b; display:none;"></span>
                <span id="contadorTipo" style="font-size:11px; color:var(--color-text-secondary); margin-left:auto;">0 / 50</span>
            </div>

            <div class="confirm-modal__actions">
                <button class="btn btn--cancel" onclick="cerrarModal('modalTipoTrabajo')">Cancelar</button>
                <button class="btn--confirm-delete" id="btnGuardarTipo" style="background:var(--color-brand-green);"
                        onclick="ejecutarGuardarTipo()">
                    <i class="fa-solid fa-floppy-disk"></i> Guardar
                </button>
            </div>
        </div>
    </div>

    <%-- Modal: confirmar eliminación de tipo de trabajo --%>
    <div id="modalConfirmacionTipo" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
        <div class="confirm-modal">
            <div class="confirm-modal__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
            <p class="confirm-modal__text" id="textoEliminarTipo">¿Eliminar este tipo de trabajo?</p>

            <div id="errorEliminarTipo" class="feedback-message feedback-message--error"
                 style="display:none; margin-bottom:12px;"></div>

            <div class="confirm-modal__actions">
                <button class="btn btn--cancel" onclick="cerrarModal('modalConfirmacionTipo')">Cancelar</button>
                <button class="btn--confirm-delete" onclick="ejecutarEliminacionTipo()">
                    <i class="fa-solid fa-trash"></i> Eliminar
                </button>
            </div>
        </div>
    </div>

</main>

<script>
    /* ── Acordeón de tipos de trabajo ── */
    function toggleAcordeonTipos(e) {
        /* Evitar que el click en el botón "Nuevo tipo" propague */
        if (e.target.closest('.btn-nuevo-tipo')) return;
        var header  = document.getElementById('acordeonHeader');
        var body    = document.getElementById('acordeonBody');
        var chevron = document.getElementById('acordeonChevron');
        var abierto = body.classList.contains('abierto');
        if (abierto) {
            body.classList.remove('abierto');
            header.classList.remove('abierto');
            chevron.classList.remove('abierto');
        } else {
            body.classList.add('abierto');
            header.classList.add('abierto');
            chevron.classList.add('abierto');
        }
    }
</script>

<script>window._ctxPath = '${pageContext.request.contextPath}';</script>
<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
<script src="${pageContext.request.contextPath}/static/js/tipo-trabajos.js"></script>
<script src="${pageContext.request.contextPath}/static/js/filtros-trabajos.js"></script>
</body>
</html>
