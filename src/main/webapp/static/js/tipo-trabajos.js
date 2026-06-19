/* ==========================================================================
   tipos-trabajo.js — Gestión de tipos de trabajo (crear / editar / eliminar)
   Vistas: listar_trabajos.jsp  y  asignar_trabajos.jsp
   --------------------------------------------------------------------------
   Reutiliza abrirModal / cerrarModal de script.js.
   No usa frameworks. Toda petición va por fetch a ServletTrabajo (POST).

   CORRECCIONES respecto a la versión anterior:
     1. _refrescarTablaTipos actualiza también el select #filtroTipo (en
        listar_trabajos.jsp) llamando a window.refreshFiltroTipoSelect si
        la función está disponible (solo existe cuando filtros-trabajos.js
        fue cargado en la misma página).
     2. La función ejecutarGuardarTipo es ahora el único punto de entrada
        para crear y editar, en línea con ambos JSP que la invocan.
   ========================================================================== */

'use strict';

let _modoModalTipo      = 'crear';   // 'crear' | 'editar'
let _idTipoEliminar     = null;
let _nombreTipoEliminar = null;

/* ════════════════════════════════════════════════════════
   ABRIR MODALES
   ════════════════════════════════════════════════════════ */

window.abrirModalTipoTrabajo = function () {
    _modoModalTipo = 'crear';
    _setTextoTituloModal('Crear nuevo tipo de trabajo');
    _setValModal('idTipoEditar', '');
    _setValModal('inputNombreTipo', '');
    _setTextoContador('0 / 50');
    _limpiarErroresModalTipo();
    abrirModal('modalTipoTrabajo');
    _enfocarInputNombre();
};

window.abrirModalEditarTipo = function (id, nombreActual) {
    _modoModalTipo = 'editar';
    _setTextoTituloModal('Editar tipo de trabajo');
    _setValModal('idTipoEditar', id);
    _setValModal('inputNombreTipo', nombreActual);
    _setTextoContador(nombreActual.length + ' / 50');
    _limpiarErroresModalTipo();
    abrirModal('modalTipoTrabajo');
    setTimeout(function () {
        const input = document.getElementById('inputNombreTipo');
        if (input) { input.focus(); input.select(); }
    }, 50);
};

window.abrirModalEliminarTipo = function (id, nombre) {
    _idTipoEliminar     = id;
    _nombreTipoEliminar = nombre;
    const textoEl = document.getElementById('textoEliminarTipo');
    if (textoEl) textoEl.textContent = '¿Eliminar el tipo de trabajo "' + nombre + '"?';
    const errBox = document.getElementById('errorEliminarTipo');
    if (errBox) { errBox.style.display = 'none'; errBox.textContent = ''; }
    abrirModal('modalConfirmacionTipo');
};

/* ── Helpers internos de UI ── */
function _setTextoTituloModal(texto) {
    const el = document.getElementById('tituloModalTipo');
    if (el) el.textContent = texto;
}

function _setValModal(id, valor) {
    const el = document.getElementById(id);
    if (el) el.value = valor;
}

function _setTextoContador(texto) {
    const el = document.getElementById('contadorTipo');
    if (el) el.textContent = texto;
}

function _enfocarInputNombre() {
    setTimeout(function () {
        const input = document.getElementById('inputNombreTipo');
        if (input) input.focus();
    }, 50);
}

function _limpiarErroresModalTipo() {
    const errBox    = document.getElementById('errorTipoTrabajo');
    const errInline = document.getElementById('errorInlineNombreTipo');
    const input     = document.getElementById('inputNombreTipo');
    if (errBox)    { errBox.style.display = 'none'; errBox.textContent = ''; }
    if (errInline) { errInline.style.display = 'none'; errInline.textContent = ''; }
    if (input)     { input.classList.remove('campo-error', 'campo-ok'); }
}

/* ════════════════════════════════════════════════════════
   VALIDACIÓN EN VIVO DEL INPUT
   ════════════════════════════════════════════════════════ */

function _validarNombreTipo(valor) {
    const v = valor.trim();
    if (v.length === 0)  return 'El nombre no puede estar vacío.';
    if (v.length < 3)    return 'Mínimo 3 caracteres.';
    if (v.length > 50)   return 'Máximo 50 caracteres.';
    if (!/^[\p{L}\s\-]+$/u.test(v)) return 'Solo letras, espacios y guiones.';
    return null;
}

document.addEventListener('DOMContentLoaded', function () {
    const input     = document.getElementById('inputNombreTipo');
    const contador  = document.getElementById('contadorTipo');
    const errInline = document.getElementById('errorInlineNombreTipo');

    if (input) {
        input.addEventListener('input', function () {
            const len = input.value.length;
            if (contador) {
                contador.textContent = len + ' / 50';
                contador.style.color = len > 45 ? '#c0392b' : 'var(--color-text-secondary)';
            }
            const error = _validarNombreTipo(input.value);
            if (error) {
                input.classList.add('campo-error');
                input.classList.remove('campo-ok');
                if (errInline) { errInline.textContent = error; errInline.style.display = 'inline'; }
            } else {
                input.classList.remove('campo-error');
                input.classList.add('campo-ok');
                if (errInline) { errInline.textContent = ''; errInline.style.display = 'none'; }
            }
        });

        input.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                ejecutarGuardarTipo();
            }
        });
    }
});

/* ════════════════════════════════════════════════════════
   GUARDAR (crear o editar según _modoModalTipo)
   ════════════════════════════════════════════════════════ */

window.ejecutarGuardarTipo = function () {
    const input  = document.getElementById('inputNombreTipo');
    const errBox = document.getElementById('errorTipoTrabajo');
    const nombre = input ? input.value.trim() : '';

    function mostrarError(msg) {
        if (errBox) { errBox.textContent = msg; errBox.style.display = 'block'; }
    }

    const errorValidacion = _validarNombreTipo(nombre);
    if (errorValidacion) {
        mostrarError(errorValidacion);
        return;
    }

    const params = new URLSearchParams();
    params.append('nombreTipo', nombre);

    if (_modoModalTipo === 'editar') {
        const id = document.getElementById('idTipoEditar')
                   ? document.getElementById('idTipoEditar').value
                   : '';
        if (!id) {
            mostrarError('No se pudo determinar el tipo a editar.');
            return;
        }
        params.append('accion', 'editarTipo');
        params.append('id', id);
    } else {
        params.append('accion', 'crearTipo');
    }

    fetch((window._ctxPath || '') + '/ServletTrabajo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        if (!data.ok) {
            mostrarError(data.mensaje || 'No se pudo guardar el tipo de trabajo.');
            return;
        }
        _refrescarTablaTipos(data.tipos);
        cerrarModal('modalTipoTrabajo');
    })
    .catch(function () {
        mostrarError('Error de conexión con el servidor.');
    });
};

/* ════════════════════════════════════════════════════════
   ELIMINAR
   ════════════════════════════════════════════════════════ */

window.ejecutarEliminacionTipo = function () {
    if (!_idTipoEliminar) return;

    const params = new URLSearchParams();
    params.append('accion', 'eliminarTipo');
    params.append('id', _idTipoEliminar);

    fetch((window._ctxPath || '') + '/ServletTrabajo', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        if (!data.ok) {
            const errBox = document.getElementById('errorEliminarTipo');
            if (errBox) {
                errBox.textContent = data.mensaje || 'No se pudo eliminar el tipo.';
                errBox.style.display = 'block';
            }
            return;
        }
        _refrescarTablaTipos(data.tipos);
        cerrarModal('modalConfirmacionTipo');
        _idTipoEliminar     = null;
        _nombreTipoEliminar = null;
    })
    .catch(function () {
        const errBox = document.getElementById('errorEliminarTipo');
        if (errBox) {
            errBox.textContent = 'Error de conexión con el servidor.';
            errBox.style.display = 'block';
        }
    });
};

/* ════════════════════════════════════════════════════════
   REFRESCAR TABLA + SELECT DE TIPO
   Tras crear / editar / eliminar:
     1. Actualiza la tabla de tipos en listar_trabajos.jsp
     2. Actualiza el select #idTipoTrabajo del formulario (si existe en la página)
     3. Sincroniza el select #filtroTipo de la barra de filtros (solo en
        listar_trabajos.jsp; la función solo existe si filtros-trabajos.js
        fue cargado en esta misma página).
   ════════════════════════════════════════════════════════ */

function _refrescarTablaTipos(tipos) {
    // ── 1. Tabla de tipos ──
    const tbody = document.getElementById('tbodyTipos');
    if (tbody) {
        if (!tipos || tipos.length === 0) {
            tbody.innerHTML = '<tr id="filaSinTipos"><td colspan="2" class="no-data">'
                + 'Sin tipos de trabajo registrados.</td></tr>';
        } else {
            let html = '';
            tipos.forEach(function (t) {
                const nombreEsc = _escHtml(t.nombre);
                const nombreJs  = t.nombre.replace(/\\/g, '\\\\').replace(/'/g, "\\'");
                html += '<tr id="tipo-fila-' + t.id + '">'
                    + '<td id="tipo-nombre-' + t.id + '">' + nombreEsc + '</td>'
                    + '<td>'
                    + '<button type="button" class="btn btn--edit" style="padding:4px 10px;font-size:12px;"'
                    + ' onclick="abrirModalEditarTipo(' + t.id + ', \'' + nombreJs + '\')">'
                    + '<i class="fa-solid fa-pen"></i></button> '
                    + '<button type="button" class="btn btn--delete" style="padding:4px 10px;font-size:12px;"'
                    + ' onclick="abrirModalEliminarTipo(' + t.id + ', \'' + nombreJs + '\')">'
                    + '<i class="fa-solid fa-trash"></i></button>'
                    + '</td>'
                    + '</tr>';
            });
            tbody.innerHTML = html;
        }
    }

    // ── 2. Select del formulario de asignar trabajo (#idTipoTrabajo) ──
    const selectForm = document.getElementById('idTipoTrabajo');
    if (selectForm) {
        const valorPrevio = selectForm.value;
        selectForm.innerHTML = '<option value="">— Seleccione un tipo —</option>';
        if (tipos && tipos.length > 0) {
            tipos.forEach(function (t) {
                const opt = document.createElement('option');
                opt.value       = t.id;
                opt.textContent = t.nombre;
                selectForm.appendChild(opt);
            });
        }
        // Restaurar selección previa si el tipo sigue existiendo
        const siqueExiste = Array.from(selectForm.options)
            .some(function (o) { return o.value === valorPrevio; });
        selectForm.value = siqueExiste ? valorPrevio : '';
    }

    // ── 3. Select del filtro de tipo (#filtroTipo) ──
    // Solo disponible en listar_trabajos.jsp donde filtros-trabajos.js expone
    // window.refreshFiltroTipoSelect. Se llama de forma condicional para que
    // este archivo funcione también en asignar_trabajos.jsp sin errores.
    if (typeof window.refreshFiltroTipoSelect === 'function') {
        window.refreshFiltroTipoSelect(tipos);
    }
}

function _escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}