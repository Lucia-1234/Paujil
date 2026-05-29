<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.paujil.modelo.usuario, java.util.List"%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/base.css">
    <title>Gestión de Usuarios - Finca El Paujil</title>
    <style>
        .tabs { display:flex; gap:var(--spacing-sm); margin-bottom:var(--spacing-lg); }
        .tab-btn {
            padding: var(--spacing-sm) var(--spacing-md);
            border: 2px solid var(--color-brand-green);
            border-radius: var(--radius-pill);
            background: transparent;
            color: var(--color-brand-green);
            font-weight: 600;
            cursor: pointer;
            transition: background var(--transition-base), color var(--transition-base);
        }
        .tab-btn.activo, .tab-btn:hover {
            background: var(--color-brand-green);
            color: var(--color-white);
        }
        .badge {
            display: inline-block;
            padding: 2px 8px;
            border-radius: var(--radius-pill);
            font-size: var(--font-size-xs);
            font-weight: 700;
        }
        .badge--activo    { background:#d6f0de; color:var(--color-dark-green); }
        .badge--inactivo  { background:#fde8e8; color:var(--color-action-red); }
        .badge--pendiente { background:#fff3cd; color:#856404; }
        .usuario-table th {
            background: var(--color-brand-green);
            color: var(--color-white);
            padding: var(--spacing-sm) var(--spacing-md);
            text-align: left;
        }
        .usuario-table td { padding: var(--spacing-sm) var(--spacing-md); }
        .usuario-table tr:nth-child(even) { background: var(--color-light-green-bg); }
        .acciones { display:flex; gap:var(--spacing-xs); flex-wrap:wrap; }
    </style>
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
    <% } %>

    <%
        String vista = (String) request.getAttribute("vista");
        if (vista == null) vista = "activos";
        boolean esPendientes = "pendientes".equals(vista);
    %>

    <%-- Pestañas --%>
    <div class="tabs">
        <a href="${pageContext.request.contextPath}/ServletUsuario">
            <button class="tab-btn <%= "activos".equals(vista) ? "activo" : "" %>">
                <i class="fa-solid fa-users"></i> Usuarios activos
            </button>
        </a>
        <a href="${pageContext.request.contextPath}/ServletUsuario?accion=pendientes">
            <button class="tab-btn <%= "pendientes".equals(vista) ? "activo" : "" %>">
                <i class="fa-solid fa-user-clock"></i> Pendientes de aprobación
            </button>
        </a>
    </div>

    <%-- Tabla --%>
    <%
        List<usuario> lista = (List<usuario>) request.getAttribute("listaUsuarios");
        if (lista == null || lista.isEmpty()) {
    %>
        <p class="no-data">
            <i class="fa-solid fa-circle-info"></i>
            No hay usuarios <%= esPendientes ? "pendientes" : "activos" %> en este momento.
        </p>
    <% } else { %>
        <div style="overflow-x:auto;">
        <table class="table usuario-table" style="width:100%; border-collapse:collapse;">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Teléfono</th>
                    <th>Rol</th>
                    <% if (!esPendientes) { %><th>Estado</th><th>F. Nacimiento</th><% } %>
                    <th>Acciones</th>
                </tr>
            </thead>
            <tbody>
            <% int num = 1; for (usuario u : lista) {
                   String nomEsc = u.getNombre().replace("'", "\\'");
            %>
                <tr id="usuario-<%= u.getIdUsuario() %>">
                    <td><%= num++ %></td>
                    <td><strong><%= u.getNombre() %></strong></td>
                    <td><%= u.getCorreo()   != null ? u.getCorreo()   : "—" %></td>
                    <td><%= u.getTelefono() != null ? u.getTelefono() : "—" %></td>
                    <td><%= u.getRol()      != null ? u.getRol()      : "—" %></td>

                    <% if (!esPendientes) {
                           String estado = u.getEstado();
                           String badge  = "Activo".equals(estado)   ? "badge--activo"
                                         : "Inactivo".equals(estado) ? "badge--inactivo"
                                         : "badge--pendiente";
                    %>
                        <td><span class="badge <%= badge %>"><%= estado != null ? estado : "—" %></span></td>
                        <td><%= u.getFechaNacimiento() != null ? u.getFechaNacimiento() : "—" %></td>
                    <% } %>

                    <td>
                        <div class="acciones">
                        <% if (esPendientes) { %>
                            <a href="${pageContext.request.contextPath}/ServletUsuario?accion=aprobar&id=<%= u.getIdUsuario() %>&vista=pendientes"
                               class="btn btn--edit btn--sm">
                                <i class="fa-solid fa-user-check"></i> Aprobar
                            </a>
                            <%-- Los data-* evitan escapar comillas dentro de onclick --%>
                            <button class="btn btn--delete btn--sm"
                                    data-id="<%= u.getIdUsuario() %>"
                                    data-nombre="<%= nomEsc %>"
                                    onclick="abrirModalDenegar(this.dataset.id, this.dataset.nombre)">
                                <i class="fa-solid fa-user-xmark"></i> Denegar
                            </button>
                        <% } else { %>
                            <% if ("Inactivo".equals(u.getEstado())) { %>
                                <a href="${pageContext.request.contextPath}/ServletUsuario?accion=activar&id=<%= u.getIdUsuario() %>"
                                   class="btn btn--edit btn--sm">
                                    <i class="fa-solid fa-circle-check"></i> Activar
                                </a>
                            <% } else { %>
                                <button class="btn btn--sm"
                                        style="background:var(--color-text-muted);color:#fff;"
                                        data-id="<%= u.getIdUsuario() %>"
                                        data-nombre="<%= nomEsc %>"
                                        onclick="abrirModalDesactivar(this.dataset.id, this.dataset.nombre)">
                                    <i class="fa-solid fa-circle-pause"></i> Desactivar
                                </button>
                            <% } %>
                            <button class="btn btn--delete btn--sm"
                                    data-id="<%= u.getIdUsuario() %>"
                                    data-nombre="<%= nomEsc %>"
                                    onclick="abrirModalEliminarUsuario(this.dataset.id, this.dataset.nombre)">
                                <i class="fa-solid fa-trash"></i> Eliminar
                            </button>
                        <% } %>
                        </div>
                    </td>
                </tr>
            <% } %>
            </tbody>
        </table>
        </div>
    <% } %>
</main>

<%-- ══════════════════════════════════════════
     MODALES — sin JavaScript inline
     ══════════════════════════════════════ --%>

<%-- Modal Desactivar --%>
<div id="modalDesactivar" class="modal-overlay" style="display:none;">
    <div class="modal-content">
        <h2>¿Desactivar usuario?</h2>
        <p id="textoDesactivar"></p>
        <div class="acciones" style="justify-content:flex-end; margin-top:12px;">
            <button class="btn" onclick="cerrarModal('modalDesactivar')">Cancelar</button>
            <button class="btn btn--delete" onclick="ejecutarAccionUsuario()">Desactivar</button>
        </div>
    </div>
</div>

<%-- Modal Eliminar usuario activo --%>
<div id="modalEliminarUsuario" class="modal-overlay" style="display:none;">
    <div class="modal-content">
        <h2>¿Eliminar usuario?</h2>
        <p id="textoEliminarUsuario"></p>
        <div class="acciones" style="justify-content:flex-end; margin-top:12px;">
            <button class="btn" onclick="cerrarModal('modalEliminarUsuario')">Cancelar</button>
            <button class="btn btn--delete" onclick="ejecutarAccionUsuario()">Eliminar definitivamente</button>
        </div>
    </div>
</div>

<%-- Modal Denegar pendiente --%>
<div id="modalDenegar" class="modal-overlay" style="display:none;">
    <div class="modal-content">
        <h2>¿Denegar registro?</h2>
        <p id="textoDenegar"></p>
        <div class="acciones" style="justify-content:flex-end; margin-top:12px;">
            <button class="btn" onclick="cerrarModal('modalDenegar')">Cancelar</button>
            <button class="btn btn--delete" onclick="ejecutarAccionUsuario()">Denegar y eliminar</button>
        </div>
    </div>
</div>

<script src="${pageContext.request.contextPath}/static/js/script.js"></script>
</body>
</html>
