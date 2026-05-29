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
        /* ── Pestañas principales ── */
        .tabs { display:flex; gap:var(--spacing-sm); margin-bottom:var(--spacing-lg); flex-wrap:wrap; }
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

        /* ── Filtros de estado (cliente-side) ── */
        .filtros-estado {
            display: flex;
            gap: var(--spacing-xs);
            margin-bottom: var(--spacing-md);
            align-items: center;
            flex-wrap: wrap;
        }
        .filtros-estado span {
            font-size: var(--font-size-sm);
            color: var(--color-text-muted);
            font-weight: 600;
            margin-right: 4px;
        }
        .filtro-btn {
            padding: 4px 14px;
            border-radius: var(--radius-pill);
            border: 1.5px solid currentColor;
            background: transparent;
            font-size: var(--font-size-sm);
            font-weight: 600;
            cursor: pointer;
            transition: background 0.15s, color 0.15s;
        }
        .filtro-btn[data-filtro="todos"]    { color: #555; border-color: #aaa; }
        .filtro-btn[data-filtro="Activo"]   { color: var(--color-dark-green); border-color: var(--color-dark-green); }
        .filtro-btn[data-filtro="Inactivo"] { color: var(--color-action-red); border-color: var(--color-action-red); }

        .filtro-btn.seleccionado[data-filtro="todos"]    { background:#555;    color:#fff; }
        .filtro-btn.seleccionado[data-filtro="Activo"]   { background:var(--color-dark-green); color:#fff; }
        .filtro-btn.seleccionado[data-filtro="Inactivo"] { background:var(--color-action-red);  color:#fff; }

        /* Contador de resultados visibles */
        .contador-resultados {
            font-size: var(--font-size-sm);
            color: var(--color-text-muted);
            margin-left: auto;
        }

        /* ── Badges ── */
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

        /* ── Tabla ── */
        .usuario-table th {
            background: var(--color-brand-green);
            color: var(--color-white);
            padding: var(--spacing-sm) var(--spacing-md);
            text-align: left;
        }
        .usuario-table td { padding: var(--spacing-sm) var(--spacing-md); }
        .usuario-table tr:nth-child(even) { background: var(--color-light-green-bg); }
        .usuario-table tr.fila-oculta { display: none; }

        .acciones { display:flex; gap:var(--spacing-xs); flex-wrap:wrap; }

        /* Fila especial cuando no hay resultados tras filtrar */
        .fila-sin-resultados { display: none; }
        .fila-sin-resultados td {
            text-align: center;
            padding: var(--spacing-lg);
            color: var(--color-text-muted);
            font-style: italic;
        }
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
        if (vista == null) vista = "todos";
        boolean esPendientes = "pendientes".equals(vista);
    %>

    <%-- Pestañas principales --%>
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

    <%-- Filtros de estado (solo se muestran en la vista "todos") --%>
    <% if (!esPendientes) { %>
    <div class="filtros-estado" id="filtrosEstado">
        <span><i class="fa-solid fa-filter"></i> Filtrar:</span>
        <button class="filtro-btn seleccionado" data-filtro="todos">
            Todos
        </button>
        <button class="filtro-btn" data-filtro="Activo">
            <i class="fa-solid fa-circle-check"></i> Activos
        </button>
        <button class="filtro-btn" data-filtro="Inactivo">
            <i class="fa-solid fa-circle-pause"></i> Inactivos
        </button>
        <span class="contador-resultados" id="contadorResultados"></span>
    </div>
    <% } %>

    <%-- Tabla --%>
    <%
        List<usuario> lista = (List<usuario>) request.getAttribute("listaUsuarios");
        if (lista == null || lista.isEmpty()) {
    %>
        <p class="no-data">
            <i class="fa-solid fa-circle-info"></i>
            No hay usuarios <%= esPendientes ? "pendientes" : "registrados" %> en este momento.
        </p>
    <% } else { %>
        <div style="overflow-x:auto;">
        <table class="table usuario-table" id="tablaUsuarios" style="width:100%; border-collapse:collapse;">
            <thead>
                <tr>
                    <th>#</th>
                    <th>Nombre</th>
                    <th>Correo</th>
                    <th>Teléfono</th>
                    <th>Rol</th>
                    <% if (esPendientes) { %>
                        <th>F. Nacimiento</th>
                    <% } else { %>
                        <th>Estado</th>
                    <% } %>
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
                <tr id="usuario-<%= u.getIdUsuario() %>"
                    data-estado="<%= estadoU %>">
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
                                    data-id="<%= u.getIdUsuario() %>"
                                    data-nombre="<%= nomEsc %>"
                                    onclick="abrirModalDenegar(this.dataset.id, this.dataset.nombre)">
                                <i class="fa-solid fa-user-xmark"></i> Denegar
                            </button>
                        <% } else { %>
                            <%-- Botón Activar / Desactivar según estado actual --%>
                            <% if ("Inactivo".equals(estadoU)) { %>
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
            <%-- Fila que aparece cuando ningún usuario coincide con el filtro --%>
            <tr class="fila-sin-resultados" id="filaSinResultados">
                <td colspan="7">
                    <i class="fa-solid fa-circle-info"></i>
                    No hay usuarios con ese estado.
                </td>
            </tr>
            </tbody>
        </table>
        </div>
    <% } %>
</main>

<%-- ══════════════════════════════════════════
     MODALES
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

<%-- Modal Activar --%>
<div id="modalActivar" class="modal-overlay" style="display:none;">
    <div class="modal-content">
        <h2>¿Activar usuario?</h2>
        <p id="textoActivar"></p>
        <div class="acciones" style="justify-content:flex-end; margin-top:12px;">
            <button class="btn" onclick="cerrarModal('modalActivar')">Cancelar</button>
            <button class="btn btn--edit" onclick="ejecutarAccionUsuario()">Activar</button>
        </div>
    </div>
</div>

<%-- Modal Eliminar usuario --%>
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

<%-- ── Lógica de filtrado cliente-side ── --%>
<script>
(function () {
    const filtros    = document.querySelectorAll('.filtro-btn');
    const tabla      = document.getElementById('tablaUsuarios');
    const sinResults = document.getElementById('filaSinResultados');
    const contador   = document.getElementById('contadorResultados');

    if (!filtros.length || !tabla) return;

    function actualizarContador(visibles, total) {
        if (contador) {
            contador.textContent = visibles === total
                ? total + ' usuario' + (total !== 1 ? 's' : '')
                : visibles + ' de ' + total + ' usuario' + (total !== 1 ? 's' : '');
        }
    }

    function aplicarFiltro(filtro) {
        const filas = tabla.querySelectorAll('tbody tr[data-estado]');
        let visibles = 0;

        filas.forEach(function (fila) {
            const estado = fila.getAttribute('data-estado');
            const mostrar = filtro === 'todos' || estado === filtro;
            fila.classList.toggle('fila-oculta', !mostrar);
            if (mostrar) visibles++;
        });

        // Mostrar aviso si ninguna fila pasa el filtro
        if (sinResults) {
            sinResults.style.display = visibles === 0 ? 'table-row' : 'none';
        }

        actualizarContador(visibles, filas.length);
    }

    // Inicializar con "todos" y mostrar contador
    aplicarFiltro('todos');

    filtros.forEach(function (btn) {
        btn.addEventListener('click', function () {
            filtros.forEach(function (b) { b.classList.remove('seleccionado'); });
            btn.classList.add('seleccionado');
            aplicarFiltro(btn.getAttribute('data-filtro'));
        });
    });
})();
</script>
</body>
</html>