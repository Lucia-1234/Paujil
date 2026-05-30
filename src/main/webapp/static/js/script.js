/* ==========================================================================
   script.js — Gestor centralizado de interactividad | Finca El Paujil
   --------------------------------------------------------------------------
   MÓDULOS:
     1. MODALES        — apertura / cierre genérico
     2. CULTIVOS       — modal editar, agregar, eliminar
     3. BIOPREPARADOS  — modal editar, agregar, eliminar + ingredientes dinámicos
     4. USUARIOS       — modal desactivar, eliminar, denegar (pendientes)
     5. REGISTRO       — validación del formulario de registro
   ========================================================================== */

'use strict';

/* ══════════════════════════════════════════════════════════════════════════
   1. MÓDULO: MODALES
   ══════════════════════════════════════════════════════════════════════ */

/**
 * Abre un modal por su ID.
 * @param {string} id - ID del elemento .modal-overlay
 */
window.abrirModal = function (id) {
    const modal = document.getElementById(id);
    if (modal) modal.style.display = 'flex';
};

/**
 * Cierra un modal por su ID.
 * @param {string} id - ID del elemento .modal-overlay
 */
window.cerrarModal = function (id) {
    const modal = document.getElementById(id);
    if (modal) modal.style.display = 'none';
};

/** Cierra cualquier modal al hacer clic en el fondo oscuro. */
document.addEventListener('click', function (e) {
    if (e.target.classList.contains('modal-overlay')) {
        e.target.style.display = 'none';
        _urlAccionConfirmacion = null;
    }
});

/** Cierra cualquier modal con la tecla Escape. */
document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        document.querySelectorAll('.modal-overlay').forEach(function (m) {
            m.style.display = 'none';
        });
        _urlAccionConfirmacion = null;
    }
});


/* ══════════════════════════════════════════════════════════════════════════
   2. MÓDULO: CULTIVOS
   ══════════════════════════════════════════════════════════════════════ */

/** Abre el modal de cultivo en modo AGREGAR (limpia todos los campos). */
window.abrirModalAgregar = function () {
    _setVal('editId',      '');
    _setVal('editNombre',  '');
    _setVal('editTipo',    '');
    _setVal('editSiembra', '');
    _setVal('editCosecha', '');
    _setText('modalTitulo', 'Agregar Cultivo');
    abrirModal('modalEditar');
};

/**
 * Abre el modal de cultivo en modo EDITAR precargando los datos.
 * @param {string} id
 * @param {string} nombre
 * @param {string} tipo
 * @param {string} siembra  - formato YYYY-MM-DD
 * @param {string} cosecha  - formato YYYY-MM-DD (puede ser vacío)
 */
window.abrirModalEditar = function (id, nombre, tipo, siembra, cosecha) {
    _setVal('editId',      id);
    _setVal('editNombre',  nombre);
    _setVal('editTipo',    tipo);
    _setVal('editSiembra', siembra);
    _setVal('editCosecha', cosecha);
    _setText('modalTitulo', 'Editar Cultivo');
    abrirModal('modalEditar');
};

/** @type {string|null} URL de eliminación de cultivo pendiente de confirmar */
let _idCultivoEliminar = null;

/**
 * Abre el modal de confirmación de eliminación de cultivo.
 * @param {string} id
 */
window.abrirModalEliminar = function (id) {
    _idCultivoEliminar = id;
    abrirModal('modalConfirmacion');
};

/** Navega a la URL de eliminación de cultivo tras confirmar. */
window.ejecutarEliminacion = function () {
    if (_idCultivoEliminar) {
        window.location.href = 'ServletCultivo?accion=eliminar&id=' + _idCultivoEliminar;
    }
};


/* ══════════════════════════════════════════════════════════════════════════
   3. MÓDULO: BIOPREPARADOS
   ══════════════════════════════════════════════════════════════════════ */

/** Abre el modal de biopreparado en modo AGREGAR. */
window.abrirModalBioAgregar = function () {
    _setVal('bioId',               '');
    _setVal('bioNombre',           '');
    _setVal('bioDescripcion',      '');
    _setVal('bioPrecio',           '');
    _setVal('bioFechaCreacion',    '');
    _setVal('bioFechaVencimiento', '');
    _setVal('bioPreparacion',      '');
    _setText('modalBioTitulo', 'Agregar Biopreparado');
    _limpiarIngredientes();
    abrirModal('modalBio');
};

/**
 * Abre el modal de biopreparado en modo EDITAR.
 * @param {string} id
 * @param {string} nombre
 * @param {string} descripcion
 * @param {string} precio
 * @param {string} fCreacion    - YYYY-MM-DD
 * @param {string} fVencimiento - YYYY-MM-DD
 * @param {string} preparacion
 */
window.abrirModalBioEditar = function (id, nombre, descripcion, precio,
                                       fCreacion, fVencimiento, preparacion) {
    _setVal('bioId',               id);
    _setVal('bioNombre',           nombre);
    _setVal('bioDescripcion',      descripcion.replace(/\\n/g, '\n'));
    _setVal('bioPrecio',           precio);
    _setVal('bioFechaCreacion',    fCreacion);
    _setVal('bioFechaVencimiento', fVencimiento);
    _setVal('bioPreparacion',      preparacion.replace(/\\n/g, '\n'));
    _setText('modalBioTitulo', 'Editar Biopreparado');
    _limpiarIngredientes();
    abrirModal('modalBio');
};

/** @type {string|null} ID del biopreparado pendiente de eliminar */
let _idBioEliminar = null;

/**
 * Abre el modal de confirmación de eliminación de biopreparado.
 * @param {string} id
 */
window.abrirModalBioEliminar = function (id) {
    _idBioEliminar = id;
    abrirModal('modalConfirmacionBio');
};

/** Navega a la URL de eliminación de biopreparado tras confirmar. */
window.ejecutarEliminacionBio = function () {
    if (_idBioEliminar) {
        window.location.href = 'ServletBiopreparado?accion=eliminar&id=' + _idBioEliminar;
    }
};

/**
 * Agrega una fila de ingrediente al contenedor dinámico dentro del modal.
 * Los inputs usan los nombres de array que lee el servlet:
 *   nombresIng[], cantidadesIng[], unidadesIng[]
 */
window.agregarIngrediente = function (nombre = '', cantidad = '', unidad = '') {
    const contenedor = document.getElementById('contenedorIngredientes');
    const fila = document.createElement('div');
    fila.className = 'bio-modal__ingredient-row';
    fila.innerHTML = `
        <input type="text" name="nombresIng[]" placeholder="Ingrediente" value="${nombre}" required>
        <input type="number" name="cantidadesIng[]" placeholder="Cant." value="${cantidad}" step="0.01" required>
        <input type="text" name="unidadesIng[]" placeholder="Unidad" value="${unidad}" required>
        <button type="button" class="bio-modal__remove-ingredient" 
                onclick="this.parentElement.remove()">
            <i class="fa-solid fa-circle-minus"></i>
        </button>
    `;
    contenedor.appendChild(fila);
    // Auto-scroll al final del contenedor de ingredientes
    contenedor.scrollTop = contenedor.scrollHeight;
};

/** Vacía el contenedor de ingredientes del modal. */
function _limpiarIngredientes() {
    const c = document.getElementById('contenedorIngredientes');
    if (c) c.innerHTML = '';
}


/* ══════════════════════════════════════════════════════════════════════════
   4. MÓDULO: GESTIÓN DE USUARIOS
   ══════════════════════════════════════════════════════════════════════ */

/** URL que se ejecutará al confirmar cualquier acción de usuario */
let _urlAccionConfirmacion = null;

/**
 * Abre el modal de confirmación genérico de usuario y guarda la URL destino.
 * @param {string} modalId     - ID del modal a abrir
 * @param {string} textoId     - ID del párrafo donde se escribe el texto dinámico
 * @param {string} texto       - Mensaje descriptivo de la acción
 * @param {string} url         - URL a la que navegar al confirmar
 */
function _abrirModalUsuario(modalId, textoId, texto, url) {
    _urlAccionConfirmacion = url;
    const p = document.getElementById(textoId);
    if (p) p.textContent = texto;
    abrirModal(modalId);
}

/**
 * Abre el modal para DESACTIVAR un usuario activo.
 * @param {string} id
 * @param {string} nombre
 */
window.abrirModalDesactivar = function (id, nombre) {
    _abrirModalUsuario(
        'modalDesactivar',
        'textoDesactivar',
        '"' + nombre + '" no podrá iniciar sesión hasta que sea reactivado.',
        'ServletUsuario?accion=desactivar&id=' + id
    );
};

/**
 * Abre el modal para ELIMINAR un usuario activo.
 * @param {string} id
 * @param {string} nombre
 */
window.abrirModalEliminarUsuario = function (id, nombre) {
    _abrirModalUsuario(
        'modalEliminarUsuario',
        'textoEliminarUsuario',
        'Se eliminará permanentemente el usuario "' + nombre + '" y todos sus datos.',
        'ServletUsuario?accion=eliminar&id=' + id
    );
};

/**
 * Abre el modal para DENEGAR un usuario pendiente.
 * @param {string} id
 * @param {string} nombre
 */
window.abrirModalDenegar = function (id, nombre) {
    _abrirModalUsuario(
        'modalDenegar',
        'textoDenegar',
        'Se rechazará el registro de "' + nombre + '" y se eliminarán sus datos del sistema.',
        'ServletUsuario?accion=denegar&id=' + id + '&vista=pendientes'
    );
};

/** Navega a la URL de confirmación (compartida por todos los modales de usuario). */
window.ejecutarAccionUsuario = function () {
    if (_urlAccionConfirmacion) {
        window.location.href = _urlAccionConfirmacion;
    }
};


/* ══════════════════════════════════════════════════════════════════════════
   5. MÓDULO: FORMULARIO DE REGISTRO
   ══════════════════════════════════════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', function () {
    const form     = document.getElementById('formRegistro');
    const feedback = document.getElementById('mensaje-feedback');
    if (!form) return;

    form.addEventListener('submit', function (e) {
        feedback.style.display = 'none';
        feedback.className = 'feedback-message';

        const pass1     = document.getElementById('txtContrasena').value;
        const pass2     = document.getElementById('txtConfirmarContrasena').value;
        const telefono  = document.getElementById('txtTelefono').value;

        if (pass1 !== pass2) {
            e.preventDefault();
            _mostrarErrorRegistro(feedback, 'Las contraseñas no coinciden.');
            return;
        }
        if (!/^\d{10}$/.test(telefono)) {
            e.preventDefault();
            _mostrarErrorRegistro(feedback, 'El teléfono debe tener exactamente 10 dígitos.');
        }
    });
});

function _mostrarErrorRegistro(el, mensaje) {
    el.textContent = mensaje;
    el.style.display = 'block';
    el.classList.add('feedback-message--error');
    window.scrollTo({ top: 0, behavior: 'smooth' });
}


/* ══════════════════════════════════════════════════════════════════════════
   UTILIDADES INTERNAS (privadas, prefijo _)
   ══════════════════════════════════════════════════════════════════════ */

/** Asigna value a un input/textarea por ID (no falla si no existe). */
function _setVal(id, value) {
    const el = document.getElementById(id);
    if (el) el.value = value;
}

/** Asigna textContent a un elemento por ID (no falla si no existe). */
function _setText(id, text) {
    const el = document.getElementById(id);
    if (el) el.textContent = text;
}



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