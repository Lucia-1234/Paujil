/**
 * validaciones-asignar.js
 * Validaciones frontend para la vista asignar_trabajos.jsp
 *
 * Cubre:
 *  - Select de cultivo, trabajador y tipo de trabajo (obligatorios).
 *  - Textarea de descripción (10–500 caracteres).
 *  - Input de fecha (obligatoria, no puede ser anterior a hoy).
 *  - Input del modal "nuevo tipo de trabajo" (3–50 chars, solo letras/espacios/guión).
 *
 * Depende de: validaciones.js (funciones mostrarError / limpiarError / estaVacio).
 * No usa frameworks.
 */

'use strict';

/* ════════════════════════════════════════════════════════
   HELPERS LOCALES
   ════════════════════════════════════════════════════════ */

/**
 * Muestra un mensaje de error en un <span> ya existente en el HTML
 * (no crea elementos dinámicos para los campos del formulario principal).
 */
function _mostrarSpan(spanId, msg) {
    const span = document.getElementById(spanId);
    if (!span) return;
    span.textContent = msg;
    span.style.display = 'block';
}

function _limpiarSpan(spanId) {
    const span = document.getElementById(spanId);
    if (!span) return;
    span.textContent = '';
    span.style.display = 'none';
}

function _marcarError(campo, spanId, msg) {
    campo.classList.add('campo-error');
    campo.classList.remove('campo-ok');
    _mostrarSpan(spanId, msg);
}

function _marcarOk(campo, spanId) {
    campo.classList.remove('campo-error');
    campo.classList.add('campo-ok');
    _limpiarSpan(spanId);
}

/* ════════════════════════════════════════════════════════
   FUNCIONES DE VALIDACIÓN POR CAMPO
   ════════════════════════════════════════════════════════ */

function validarSelectCultivo() {
    const campo = document.getElementById('idCultivo');
    if (!campo) return true;
    if (!campo.value) {
        _marcarError(campo, 'error-idCultivo', 'Selecciona un cultivo.');
        return false;
    }
    _marcarOk(campo, 'error-idCultivo');
    return true;
}

function validarSelectUsuario() {
    const campo = document.getElementById('idUsuario');
    if (!campo) return true;
    if (!campo.value) {
        _marcarError(campo, 'error-idUsuario', 'Selecciona un trabajador.');
        return false;
    }
    _marcarOk(campo, 'error-idUsuario');
    return true;
}

function validarSelectTipo() {
    const campo = document.getElementById('idTipoTrabajo');
    if (!campo) return true;
    if (!campo.value) {
        _marcarError(campo, 'error-idTipoTrabajo', 'Selecciona un tipo de trabajo.');
        return false;
    }
    _marcarOk(campo, 'error-idTipoTrabajo');
    return true;
}

function validarDescripcion() {
    const campo = document.getElementById('descripcion');
    if (!campo) return true;
    const val = campo.value.trim();
    if (val.length === 0) {
        _marcarError(campo, 'error-descripcion', 'La descripción es obligatoria.');
        return false;
    }
    if (val.length < 10) {
        _marcarError(campo, 'error-descripcion', 'La descripción debe tener al menos 10 caracteres.');
        return false;
    }
    if (val.length > 500) {
        _marcarError(campo, 'error-descripcion', 'La descripción no puede superar 500 caracteres.');
        return false;
    }
    _marcarOk(campo, 'error-descripcion');
    return true;
}

function validarFechaAsignacion() {
    const campo = document.getElementById('fechaAsignacion');
    if (!campo) return true;
    const val = campo.value;
    if (!val) {
        _marcarError(campo, 'error-fechaAsignacion', 'La fecha de asignación es obligatoria.');
        return false;
    }
    // No puede ser anterior a hoy
    const seleccionada = new Date(val + 'T00:00:00');
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    if (seleccionada < hoy) {
        _marcarError(campo, 'error-fechaAsignacion', 'La fecha no puede ser anterior a hoy.');
        return false;
    }
    _marcarOk(campo, 'error-fechaAsignacion');
    return true;
}

/* ════════════════════════════════════════════════════════
   VALIDACIÓN DEL INPUT DEL MODAL (en tiempo real)
   ════════════════════════════════════════════════════════ */

/**
 * Valida el input del modal "nuevo tipo de trabajo".
 * Retorna true si es válido, false si no.
 * Muestra error en el span inline del modal.
 */
function validarInputNombreTipo() {
    const input = document.getElementById('inputNombreTipo');
    const spanError = document.getElementById('errorInlineNombreTipo');
    const errorBlock = document.getElementById('errorTipoTrabajo');
    if (!input) return false;

    const val = input.value.trim();

    function mostrarErr(msg) {
        if (spanError) { spanError.textContent = msg; spanError.style.display = 'inline'; }
        if (errorBlock) errorBlock.style.display = 'none'; // oculta el bloque de servidor
        input.classList.add('campo-error');
        input.classList.remove('campo-ok');
    }
    function limpiarErr() {
        if (spanError) { spanError.textContent = ''; spanError.style.display = 'none'; }
        input.classList.remove('campo-error');
        input.classList.add('campo-ok');
    }

    if (val.length === 0) {
        mostrarErr('El nombre no puede estar vacío.');
        return false;
    }
    if (val.length < 3) {
        mostrarErr('Mínimo 3 caracteres.');
        return false;
    }
    if (val.length > 50) {
        mostrarErr('Máximo 50 caracteres.');
        return false;
    }
    if (!/^[\p{L}\s\-]+$/u.test(val)) {
        mostrarErr('Solo letras, espacios y guiones.');
        return false;
    }
    limpiarErr();
    return true;
}

/* ════════════════════════════════════════════════════════
   INICIALIZACIÓN
   ════════════════════════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', function () {

    /* ── Contador de caracteres: descripción ── */
    const txtDesc    = document.getElementById('descripcion');
    const cntDesc    = document.getElementById('contadorDesc');
    if (txtDesc && cntDesc) {
        txtDesc.addEventListener('input', function () {
            const len = txtDesc.value.length;
            cntDesc.textContent = len + ' / 500';
            cntDesc.style.color = len > 480 ? '#c0392b' : 'var(--color-text-secondary)';
        });
        txtDesc.addEventListener('blur', validarDescripcion);
    }

    /* ── Contador de caracteres: modal tipo ── */
    const inputTipo = document.getElementById('inputNombreTipo');
    const cntTipo   = document.getElementById('contadorTipo');
    if (inputTipo && cntTipo) {
        inputTipo.addEventListener('input', function () {
            const len = inputTipo.value.length;
            cntTipo.textContent = len + ' / 50';
            cntTipo.style.color = len > 45 ? '#c0392b' : 'var(--color-text-secondary)';
            validarInputNombreTipo();
        });
        // Permite enviar con Enter desde el modal
        inputTipo.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                ejecutarCrearTipoTrabajo();
            }
        });
    }

    /* ── Validación on-change para selects ── */
    const selCultivo = document.getElementById('idCultivo');
    const selUsuario = document.getElementById('idUsuario');
    const selTipo    = document.getElementById('idTipoTrabajo');
    const inputFecha = document.getElementById('fechaAsignacion');

    if (selCultivo) selCultivo.addEventListener('change', validarSelectCultivo);
    if (selUsuario) selUsuario.addEventListener('change', validarSelectUsuario);
    if (selTipo)    selTipo.addEventListener('change', validarSelectTipo);
    if (inputFecha) inputFecha.addEventListener('change', validarFechaAsignacion);

    /* ── Submit: valida todos antes de enviar ── */
    const form = document.getElementById('formAsignarTrabajo');
    if (!form) return;

    form.addEventListener('submit', function (e) {
        const resultados = [
            validarSelectCultivo(),
            validarSelectUsuario(),
            validarSelectTipo(),
            validarDescripcion(),
            validarFechaAsignacion()
        ];

        const todosValidos = resultados.every(Boolean);

        if (!todosValidos) {
            e.preventDefault();
            const primerError = form.querySelector('.campo-error');
            if (primerError) {
                primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                primerError.focus();
            }
        }
    });
});

/* ════════════════════════════════════════════════════════
   SOBREESCRITURA: abrirModalTipoTrabajo limpia el estado
   (complementa script.js sin duplicar)
   ════════════════════════════════════════════════════════ */

// Se ejecuta después de que script.js define la función original,
// por eso este archivo se carga al final.
(function () {
    const _originalAbrir = window.abrirModalTipoTrabajo;
    window.abrirModalTipoTrabajo = function () {
        // Limpiar el contador y el estado del input
        const cntTipo   = document.getElementById('contadorTipo');
        const inputTipo = document.getElementById('inputNombreTipo');
        const spanInline = document.getElementById('errorInlineNombreTipo');
        if (cntTipo)    cntTipo.textContent = '0 / 50';
        if (inputTipo)  { inputTipo.value = ''; inputTipo.classList.remove('campo-error', 'campo-ok'); }
        if (spanInline) { spanInline.textContent = ''; spanInline.style.display = 'none'; }
        if (_originalAbrir) _originalAbrir();
    };
})();