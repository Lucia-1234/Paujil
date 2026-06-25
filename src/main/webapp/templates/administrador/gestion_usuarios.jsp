<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.usuario, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/views/trabajos.css">
    <title>Gestión de Usuarios - Finca El Paujil</title>
    <style>
        
    </style>
</head>
<body>
<main class="page-wrapper">

    <%
        String status = request.getParameter("status");
        String motivo = request.getParameter("motivo");
        if ("success".equals(status)) {
    %>
        <div class="feedback-message feedback-message--ok">
            <i class="fa-solid fa-circle-check"></i> Operación realizada correctamente.
        </div>
    <% } else if ("error".equals(status)) { %>
        <div class="feedback-message feedback-message--error">
            <i class="fa-solid fa-circle-exclamation"></i>
            <% if ("asignaciones_activas".equals(motivo)) { %>
                No se puede eliminar este trabajador porque tiene trabajos activos
                (Pendiente, En proceso o En revisión). Finaliza o reasigna sus trabajos primero.
            <% } else { %>
                Ocurrió un error. Intente nuevamente.
            <% } %>
        </div>
    <% } %>

    <%
        String vista = (String) request.getAttribute("vista");
        if (vista == null) vista = "todos";
        boolean esPendientes = "pendientes".equals(vista);
    %>

    <header class="list-header">
        <a href="${pageContext.request.contextPath}/templates/administrador/menu_administrador.jsp"
           class="list-header__back" aria-label="Volver al menú">
            <i class="fa-solid fa-arrow-left-long"></i>
        </a>
        <h1 class="list-header__title">Gestión de Usuarios</h1>
    </header>

    <%-- Pestañas --%>
    <div class="tabs">
        <a href="${pageContext.request.contextPath}/ServletUsuario">
            <button class="tab-btn <%= !esPendientes ? "activo" : "" %>">
                <i class="fa-solid fa-users"></i> Usuarios
            </button>
        </a>
        <a href="${pageContext.request.contextPath}/ServletUsuario?accion=pendientes">
            <button class="tab-btn <%= esPendientes ? "activo" : "" %>">
                <i class="fa-solid fa-user-clock"></i> Pendientes de aprobación
            </button>
        </a>
    </div>

    <% if (!esPendientes) { %>
    <div class="filtros-estado" id="filtrosEstado">
        <span><i class="fa-solid fa-filter"></i> Filtrar:</span>
        <button class="filtro-btn seleccionado" data-filtro="todos">Todos</button>
        <button class="filtro-btn" data-filtro="Activo">
            <i class="fa-solid fa-circle-check"></i> Activos
        </button>
        <button class="filtro-btn" data-filtro="Inactivo">
            <i class="fa-solid fa-circle-pause"></i> Inactivos
        </button>
        <span class="contador-resultados" id="contadorResultados"></span>
    </div>
    <% } %>

    <%
        List<usuario> lista = (List<usuario>) request.getAttribute("listaUsuarios");
        if (lista == null || lista.isEmpty()) {
    %>
        <p class="no-data">
            <i class="fa-solid fa-circle-info" style="margin-right:6px;"></i>
            No hay usuarios <%= esPendientes ? "pendientes" : "registrados" %> en este momento.
        </p>
    <% } else { %>
        <div class="user-table-wrapper" style="overflow-x:auto;">
        <table class="user-table usuario-table" id="tablaUsuarios">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Teléfono</th>
                    <th>Rol</th>
                    <% if (esPendientes) { %><th>F. Nacimiento</th><% } else { %><th>Estado</th><% } %>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
            <% int num = 1; for (usuario u : lista) {
                   String nomEsc   = u.getNombre().replace("'", "\\'");
                   String estadoU  = u.getEstado() != null ? u.getEstado() : "";
                   String badgeCss = "Activo".equals(estadoU)   ? "badge--activo"
                                   : "Inactivo".equals(estadoU) ? "badge--inactivo"
                                   : "badge--pendiente";
            %>
                <tr id="usuario-<%= u.getIdUsuario() %>" data-estado="<%= estadoU %>">
                    <td><%= num++ %></td>
                    <td><strong><%= u.getNombre() %></strong></td>
                    <td><%= u.getCorreo()   != null ? u.getCorreo()   : "—" %></td>
                    <td><%= u.getTelefono() != null ? u.getTelefono() : "—" %></td>
                    <td><%= u.getRol()      != null ? u.getRol()      : "—" %></td>
                    <% if (esPendientes) { %>
                        <td><%= u.getFechaNacimiento() != null ? u.getFechaNacimiento() : "—" %></td>
                    <% } else { %>
                        <td>
                            <span class="badge <%= badgeCss %>">
                                <%= !estadoU.isEmpty() ? estadoU : "—" %>
                            </span>
                        </td>
                    <% } %>
                    <td>
                        <div class="acciones">
                        <% if (esPendientes) { %>
                            <a href="${pageContext.request.contextPath}/ServletUsuario?accion=aprobar&id=<%= u.getIdUsuario() %>&vista=pendientes"
                               class="btn btn--edit btn--sm">
                                <i class="fa-solid fa-user-check"></i> Aprobar
                            </a>
                            <button class="btn btn--delete btn--sm"
                                    data-id="<%= u.getIdUsuario() %>" data-nombre="<%= nomEsc %>"
                                    onclick="abrirModalDenegar(this.dataset.id, this.dataset.nombre)">
                                <i class="fa-solid fa-user-xmark"></i> Denegar
                            </button>
                        <% } else { %>
                            <% if ("Inactivo".equals(estadoU)) { %>
                                <a href="${pageContext.request.contextPath}/ServletUsuario?accion=activar&id=<%= u.getIdUsuario() %>"
                                   class="btn btn--edit btn--sm">
                                    <i class="fa-solid fa-circle-check"></i> Activar
                                </a>
                            <% } else { %>
                                <button class="btn btn--sm"
                                        style="background:var(--color-text-muted);color:#fff;"
                                        data-id="<%= u.getIdUsuario() %>" data-nombre="<%= nomEsc %>"
                                        onclick="abrirModalDesactivar(this.dataset.id, this.dataset.nombre)">
                                    <i class="fa-solid fa-circle-pause"></i> Desactivar
                                </button>
                            <% } %>
                            <button class="btn btn--delete btn--sm"
                                    data-id="<%= u.getIdUsuario() %>" data-nombre="<%= nomEsc %>"
                                    onclick="abrirModalEliminarUsuario(this.dataset.id, this.dataset.nombre)">
                                <i class="fa-solid fa-trash"></i> Eliminar
                            </button>
                        <% } %>
                        </div>
                    </td>
                </tr>
            <% } %>
            <tr class="fila-sin-resultados" id="filaSinResultados">
                <td colspan="7"><i class="fa-solid fa-circle-info"></i> No hay usuarios con ese estado.</td>
            </tr>
            </tbody>
        </table>
        </div>
    <% } %>

</main>

<%-- ═══════════════ MODALES ═══════════════ --%>

<div id="modalDesactivar" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
    <div class="confirm-modal" style="background:var(--color-text-muted);">
        <div class="confirm-modal__icon"><i class="fa-solid fa-circle-pause"></i></div>
        <p class="confirm-modal__text" id="textoDesactivar"></p>
        <div class="confirm-modal__actions">
            <button class="btn btn--cancel" onclick="cerrarModal('modalDesactivar')">Cancelar</button>
            <button class="btn--confirm-delete" onclick="ejecutarAccionUsuario()">Desactivar</button>
        </div>
    </div>
</div>

<div id="modalEliminarUsuario" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
    <div class="confirm-modal">
        <div class="confirm-modal__icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
        <p class="confirm-modal__text" id="textoEliminarUsuario"></p>
        <div class="confirm-modal__actions">
            <button class="btn btn--cancel" onclick="cerrarModal('modalEliminarUsuario')">Cancelar</button>
            <button class="btn--confirm-delete" onclick="ejecutarAccionUsuario()">Eliminar definitivamente</button>
        </div>
    </div>
</div>

<div id="modalDenegar" class="modal-overlay" style="display:none;" role="dialog" aria-modal="true">
    <div class="confirm-modal">
        <div class="confirm-modal__icon"><i class="fa-solid fa-user-xmark"></i></div>
        <p class="confirm-modal__text" id="textoDenegar"></p>
        <div class="confirm-modal__actions">
            <button class="btn btn--cancel" onclick="cerrarModal('modalDenegar')">Cancelar</button>
            <button class="btn--confirm-delete" onclick="ejecutarAccionUsuario()">Denegar y eliminar</button>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
</body>
</html>