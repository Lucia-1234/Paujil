/* ==========================================================================
   script.js — Gestor centralizado de interactividad | Finca El Paujil
   --------------------------------------------------------------------------
   MÓDULOS:
     1. MODALES           — apertura / cierre genérico
     2. CULTIVOS          — modal editar, agregar, eliminar
     3. BIOPREPARADOS     — modal editar, agregar, eliminar + ingredientes
     4. USUARIOS          — modal desactivar, eliminar, denegar (pendientes)
     5. HISTORIAL         — fetch JSON + render tabla de registros de labor
     6. TRABAJOS          — modal eliminar trabajo
     7. REGISTROS HIST.   — modal eliminar registro histórico
     8. FILTRO USUARIOS   — tabla con filtro por estado

   NOTA DE VALIDACIONES:
     La validación de formularios fue migrada completamente al frontend en:
       - validaciones.js          → funciones puras + helpers UI + registro
       - validaciones-cultivos.js → cultivos y registros de labor
       - validaciones-asignar.js  → formulario de asignar trabajo
     Este archivo ya no contiene ninguna lógica de validación.
     El orden de carga en cada JSP debe ser:
       1. validaciones.js
       2. validaciones-cultivos.js  (solo en vistas de cultivos)
       3. script.js

   NOTA SOBRE TIPOS DE TRABAJO:
     La gestión de tipos de trabajo (crear/editar/eliminar) vive ahora
     completa en tipos-trabajo.js, cargado solo en las vistas que la
     necesitan (listar_trabajos.jsp y asignar_trabajos.jsp). Se sacó de
     este archivo para no duplicar `abrirModalTipoTrabajo` con dos
     comportamientos distintos en el mismo nombre de función global.
   ========================================================================== */

'use strict';

/* ══════════════════════════════════════════════════════════════════════════
   1. MÓDULO: MODALES — apertura / cierre genérico
   ══════════════════════════════════════════════════════════════════════ */

window.abrirModal = function (id) {
    const modal = document.getElementById(id);
    if (modal) modal.style.display = 'flex';
};

window.cerrarModal = function (id) {
    const modal = document.getElementById(id);
    if (modal) modal.style.display = 'none';
};

document.addEventListener('click', function (e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.style.display = 'none';
        _urlAccionConfirmacion = null;
    }
});

document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay').forEach(function (m) {
            m.style.display = 'none';
        });
        _urlAccionConfirmacion = null;
    }
});


/* ══════════════════════════════════════════════════════════════════════════
   2. MÓDULO: CULTIVOS — modal editar, agregar, eliminar
   ══════════════════════════════════════════════════════════════════════ */

/**
 * Abre el modal de cultivo en modo AGREGAR.
 * Limpia los campos y resetea los estados de validación antes de abrir,
 * para que el modal no muestre residuos de un uso anterior.
 */
window.abrirModalAgregar = function () {
    _setVal('editId',      '');
    _setVal('editNombre',  '');
    _setVal('editTipo',    '');
    _setVal('editSiembra', '');
    _setVal('editCosecha', '');
    _setText('modalTitulo', 'Agregar Cultivo');

    // limpiarEstadosForm() definida en validaciones-cultivos.js
    const formCultivo = document.getElementById('formCultivo');
    if (formCultivo && typeof limpiarEstadosForm === 'function') {
        limpiarEstadosForm(formCultivo);
    }

    abrirModal('modalEditar');
};

/**
 * Abre el modal de cultivo en modo EDITAR precargando los datos.
 * @param {string} id
 * @param {string} nombre
 * @param {string} tipo
 * @param {string} siembra  - YYYY-MM-DD
 * @param {string} cosecha  - YYYY-MM-DD (puede ser vacío)
 */
window.abrirModalEditar = function (id, nombre, tipo, siembra, cosecha) {
    _setVal('editId',      id);
    _setVal('editNombre',  nombre);
    _setVal('editTipo',    tipo);
    _setVal('editSiembra', siembra);
    _setVal('editCosecha', cosecha);
    _setText('modalTitulo', 'Editar Cultivo');

    const formCultivo = document.getElementById('formCultivo');
    if (formCultivo && typeof limpiarEstadosForm === 'function') {
        limpiarEstadosForm(formCultivo);
    }

    abrirModal('modalEditar');
};

let _idCultivoEliminar = null;

window.abrirModalEliminar = function (id) {
    _idCultivoEliminar = id;
    abrirModal('modalConfirmacion');
};

window.ejecutarEliminacion = function () {
    if (_idCultivoEliminar) {
        window.location.href = (window._ctxPath || '')
            + '/ServletCultivo?accion=eliminar&id=' + _idCultivoEliminar;
    }
};

/**
 * Abre el modal de registro de labor asociando el cultivo activo.
 * Resetea los estados de validación del formulario antes de abrir.
 * @param {string} idCultivo
 */
window.abrirModalRegistro = function (idCultivo) {
    _setVal('regIdCultivo', idCultivo);

    const formLabor = document.getElementById('formLabor');
    if (formLabor && typeof limpiarEstadosForm === 'function') {
        limpiarEstadosForm(formLabor);
    }

    abrirModal('modalRegistro');
};


/* ══════════════════════════════════════════════════════════════════════════
   3. MÓDULO: BIOPREPARADOS
   ══════════════════════════════════════════════════════════════════════ */

window.abrirModalBioAgregar = function () {
    _setVal('bioId',               '');
    _setVal('bioNombre',           '');
    _setVal('bioDescripcion',      '');
    _setVal('bioPrecio',           '');
    _setVal('bioFechaCreacion',    '');
    _setVal('bioFechaVencimiento', '');
    _setText('modalBioTitulo', 'Agregar Biopreparado');
    _limpiarIngredientes();
    abrirModal('modalBio');
};

window.abrirModalBioEditar = function (id, nombre, descripcion, precio,
                                       fCreacion, fVencimiento, preparacion) {
    _setVal('bioId',               id);
    _setVal('bioNombre',           nombre);
    _setVal('bioDescripcion',      descripcion.replace(/\\n/g, '\n'));
    _setVal('bioPrecio',           precio);
    _setVal('bioFechaCreacion',    fCreacion);
    _setVal('bioFechaVencimiento', fVencimiento);
    _setText('modalBioTitulo', 'Editar Biopreparado');
    _limpiarIngredientes();
    abrirModal('modalBio');
};

let _idBioEliminar = null;

window.abrirModalBioEliminar = function (id) {
    _idBioEliminar = id;
    abrirModal('modalConfirmacionBio');
};

window.ejecutarEliminacionBio = function () {
    if (_idBioEliminar) {
        window.location.href = 'ServletBiopreparado?accion=eliminar&id=' + _idBioEliminar;
    }
};

window.agregarIngrediente = function (nombre = '', cantidad = '', unidad = '') {
    const contenedor = document.getElementById('contenedorIngredientes');
    if (!contenedor) return;
    const fila = document.createElement('div');
    fila.className = 'bio-modal__ingredient-row';
    fila.innerHTML = `
        <input type="text"   name="nombresIng[]"   placeholder="Ingrediente" value="${nombre}"   required>
        <input type="number" name="cantidadesIng[]" placeholder="Cant."       value="${cantidad}" step="0.01" required>
        <input type="text"   name="unidadesIng[]"   placeholder="Unidad"      value="${unidad}"   required>
        <button type="button" class="bio-modal__remove-ingredient"
                onclick="this.parentElement.remove()">
            <i class="fa-solid fa-circle-minus"></i>
        </button>
    `;
    contenedor.appendChild(fila);
    contenedor.scrollTop = contenedor.scrollHeight;
};

function _limpiarIngredientes() {
    const c = document.getElementById('contenedorIngredientes');
    if (c) c.innerHTML = '';
}


/* ══════════════════════════════════════════════════════════════════════════
   4. MÓDULO: GESTIÓN DE USUARIOS
   ══════════════════════════════════════════════════════════════════════ */

let _urlAccionConfirmacion = null;

function _abrirModalUsuario(modalId, textoId, texto, url) {
    _urlAccionConfirmacion = url;
    const p = document.getElementById(textoId);
    if (p) p.textContent = texto;
    abrirModal(modalId);
}

window.abrirModalDesactivar = function (id, nombre) {
    _abrirModalUsuario(
        'modalDesactivar',
        'textoDesactivar',
        '"' + nombre + '" no podrá iniciar sesión hasta que sea reactivado.',
        'ServletUsuario?accion=desactivar&id=' + id
    );
};

window.abrirModalEliminarUsuario = function (id, nombre) {
    _abrirModalUsuario(
        'modalEliminarUsuario',
        'textoEliminarUsuario',
        'Se eliminará permanentemente el usuario "' + nombre + '" y todos sus datos.',
        'ServletUsuario?accion=eliminar&id=' + id
    );
};

window.abrirModalDenegar = function (id, nombre) {
    _abrirModalUsuario(
        'modalDenegar',
        'textoDenegar',
        'Se rechazará el registro de "' + nombre + '" y se eliminarán sus datos del sistema.',
        'ServletUsuario?accion=denegar&id=' + id + '&vista=pendientes'
    );
};

window.ejecutarAccionUsuario = function () {
    if (_urlAccionConfirmacion) {
        window.location.href = _urlAccionConfirmacion;
    }
};


/* ══════════════════════════════════════════════════════════════════════════
   5. MÓDULO: HISTORIAL DE CULTIVOS — fetch JSON + render tabla
   ══════════════════════════════════════════════════════════════════════ */

window.toggleHistorial = function (btn, idCultivo) {
    const fila      = document.getElementById('historial-'       + idCultivo);
    const contenido = document.getElementById('history-content-' + idCultivo);
    const estaVisible = fila.style.display !== 'none';

    if (estaVisible) {
        fila.style.display = 'none';
        btn.classList.remove('btn--history--active');
        return;
    }

    fila.style.display = 'block';
    btn.classList.add('btn--history--active');

    if (contenido.dataset.loaded) return;

    fetch(window._ctxPath + '/ServletCultivo?accion=historialJson&id=' + idCultivo, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' }
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        contenido.dataset.loaded = 'true';
        if (!data.length) {
            contenido.innerHTML = '<p class="no-data">Sin registros históricos.</p>';
            return;
        }
        let html = '<table class="history-table"><thead>'
            + '<tr><th>Labor</th><th>Responsable</th>'
            + '<th>Inicio</th><th>Finalización</th><th>Observaciones</th><th></th></tr>'
            + '</thead><tbody>';
        data.forEach(function (r) {
            html += '<tr id="registro-fila-' + r.idTrabajoRealizado + '">'
                + '<td>' + _esc(r.descripcionTrabajo) + '</td>'
                + '<td>' + _esc(r.nombreUsuario)      + '</td>'
                + '<td>' + r.fechaInicio              + '</td>'
                + '<td>' + r.fechaFinalizo            + '</td>'
                + '<td>' + (r.observaciones ? _esc(r.observaciones) : '—') + '</td>'
                + '<td>'
                + '<button class="btn btn--delete" style="padding:4px 10px;font-size:12px;"'
                + ' onclick="abrirModalEliminarRegistro('
                + r.idTrabajoRealizado + ',' + idCultivo + ')">'
                + '<i class="fa-solid fa-trash"></i>'
                + '</button>'
                + '</td>'
                + '</tr>';
        });
        html += '</tbody></table>';
        contenido.innerHTML = html;
    })
    .catch(function () {
        contenido.innerHTML = '<p class="no-data">Error al cargar historial.</p>';
    });
};


/* ══════════════════════════════════════════════════════════════════════════
   6. MÓDULO: TRABAJOS — modal eliminar trabajo
   ══════════════════════════════════════════════════════════════════════ */

let _idTrabajoEliminar = null;

window.abrirModalEliminarTrabajo = function (id) {
    _idTrabajoEliminar = id;
    abrirModal('modalConfirmacionTrabajo');
};

window.ejecutarEliminacionTrabajo = function () {
    if (_idTrabajoEliminar) {
        window.location.href = (window._ctxPath || '')
            + '/ServletTrabajo?accion=eliminar&id=' + _idTrabajoEliminar;
    }
};


/* ══════════════════════════════════════════════════════════════════════════
   7. MÓDULO: REGISTROS HISTÓRICOS — eliminar registro individual
   ══════════════════════════════════════════════════════════════════════ */

let _idRegistroEliminar   = null;
let _idCultivoDelRegistro = null;

window.abrirModalEliminarRegistro = function (idRegistro, idCultivo) {
    _idRegistroEliminar   = idRegistro;
    _idCultivoDelRegistro = idCultivo;
    abrirModal('modalConfirmacionRegistro');
};

window.ejecutarEliminacionRegistro = function () {
    if (!_idRegistroEliminar) return;

    fetch((window._ctxPath || '')
            + '/ServletCultivo?accion=eliminarRegistro&id='  + _idRegistroEliminar
            + '&idCultivo=' + _idCultivoDelRegistro)
        .then(function (res) {
            if (res.ok) {
                const fila = document.getElementById('registro-fila-' + _idRegistroEliminar);
                if (fila) fila.remove();

                const contenido = document.getElementById('history-content-' + _idCultivoDelRegistro);
                const filas     = contenido ? contenido.querySelectorAll('tbody tr') : [];
                if (filas.length === 0) {
                    contenido.innerHTML = '<p class="no-data">Sin registros históricos.</p>';
                }

                const btnHistorial = document.querySelector(
                    '[onclick="toggleHistorial(this, \'' + _idCultivoDelRegistro + '\')"]'
                );
                if (btnHistorial) {
                    const match = btnHistorial.textContent.trim().match(/\((\d+)\)/);
                    if (match) {
                        const nuevo = Math.max(0, parseInt(match[1]) - 1);
                        btnHistorial.innerHTML = btnHistorial.innerHTML.replace(
                            /\(\d+\)/, '(' + nuevo + ')'
                        );
                    }
                }
            }

            cerrarModal('modalConfirmacionRegistro');
            _idRegistroEliminar   = null;
            _idCultivoDelRegistro = null;
        })
        .catch(function () {
            cerrarModal('modalConfirmacionRegistro');
            alert('Error al eliminar el registro.');
        });
};


/* ══════════════════════════════════════════════════════════════════════════
   8. MÓDULO: FILTRO DE USUARIOS — tabla con filtro por estado
   ══════════════════════════════════════════════════════════════════════ */

(function () {
    const filtros    = document.querySelectorAll('.filtro-btn');
    const tabla      = document.getElementById('tablaUsuarios');
    const sinResults = document.getElementById('filaSinResultados');
    const contador   = document.getElementById('contadorResultados');

    if (!filtros.length || !tabla) return;

    function actualizarContador(visibles, total) {
        if (contador) {
            contador.textContent = visibles === total
                ? total    + ' usuario' + (total   !== 1 ? 's' : '')
                : visibles + ' de ' + total + ' usuario' + (total !== 1 ? 's' : '');
        }
    }

    function aplicarFiltro(filtro) {
        const filas = tabla.querySelectorAll('tbody tr[data-estado]');
        let visibles = 0;
        filas.forEach(function (fila) {
            const mostrar = filtro === 'todos' || fila.getAttribute('data-estado') === filtro;
            fila.classList.toggle('fila-oculta', !mostrar);
            if (mostrar) visibles++;
        });
        if (sinResults) sinResults.style.display = visibles === 0 ? 'table-row' : 'none';
        actualizarContador(visibles, filas.length);
    }

    aplicarFiltro('todos');

    filtros.forEach(function (btn) {
        btn.addEventListener('click', function () {
            filtros.forEach(function (b) { b.classList.remove('seleccionado'); });
            btn.classList.add('seleccionado');
            aplicarFiltro(btn.getAttribute('data-filtro'));
        });
    });
})();


/* ══════════════════════════════════════════════════════════════════════════
   UTILIDADES INTERNAS (privadas, prefijo _)
   ══════════════════════════════════════════════════════════════════════ */

function _setVal(id, value) {
    const el = document.getElementById(id);
    if (el) el.value = value;
}

function _setText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}

function _esc(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}