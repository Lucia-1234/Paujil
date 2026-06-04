/**
 * validaciones-cultivos.js
 * Módulo de validación frontend — Cultivos y Registros de Labor
 * Finca El Paujil
 *
 * Depende de validaciones.js (que debe cargarse antes en el HTML).
 * Reutiliza las funciones puras ya definidas allí:
 *   estaVacio, enRango, fechaNoEsAnteriorAHoy, fechaFinNoEsAnteriorAInicio,
 *   mostrarError, limpiarError, resetearCampo, validarTextoRequerido,
 *   validarFechaInicio, validarFechaFin
 */

'use strict';

/* =========================================================
   1. VALIDACIÓN — Modal Agregar / Editar Cultivo (#modalEditar)
   ========================================================= */

/**
 * Valida el formulario de cultivo (agregar o editar).
 * Campos: nombreCultivo (obligatorio), tipoCultivo (opcional),
 *         fechaSiembra (obligatoria, >= hoy en creación),
 *         fechaCosecha (opcional, pero si existe debe ser >= siembra).
 *
 * @returns {boolean} true si todos los campos son válidos.
 */
function validarFormCultivo() {
    const form     = document.getElementById('formCultivo');
    const cNombre  = form.querySelector('[name="nombreCultivo"]');
    const cTipo    = form.querySelector('[name="tipoCultivo"]');
    const cSiembra = form.querySelector('[name="fechaSiembra"]');
    const cCosecha = form.querySelector('[name="fechaCosecha"]');
    const esEdicion = !estaVacio(document.getElementById('editId').value);

    let valido = true;

    // Nombre: obligatorio, 2–80 caracteres
    if (!validarTextoRequerido(cNombre, 'El nombre del cultivo', 2, 80)) valido = false;

    // Tipo: opcional, pero si se ingresa debe tener entre 2 y 60 caracteres
    if (cTipo && !estaVacio(cTipo.value)) {
        if (!enRango(cTipo.value, 2, 60)) {
            mostrarError(cTipo, 'El tipo debe tener entre 2 y 60 caracteres.');
            valido = false;
        } else {
            limpiarError(cTipo);
        }
    } else if (cTipo) {
        limpiarError(cTipo);
    }

    // Fecha de siembra:
    //   - Obligatoria siempre.
    //   - En CREACIÓN debe ser >= hoy (regla de negocio).
    //   - En EDICIÓN solo se verifica que tenga un valor (puede ser una fecha pasada legítima).
    if (estaVacio(cSiembra.value)) {
        mostrarError(cSiembra, 'La fecha de siembra es obligatoria.');
        valido = false;
    } else if (!esEdicion && !fechaNoEsAnteriorAHoy(cSiembra.value)) {
        mostrarError(cSiembra, 'La fecha de siembra no puede ser anterior a hoy.');
        valido = false;
    } else {
        limpiarError(cSiembra);
    }

    // Fecha de cosecha: opcional
    //   Si se ingresa, debe ser >= fecha de siembra.
    if (!estaVacio(cCosecha.value)) {
        if (!estaVacio(cSiembra.value) && !fechaFinNoEsAnteriorAInicio(cSiembra.value, cCosecha.value)) {
            mostrarError(cCosecha, 'La fecha de cosecha no puede ser anterior a la fecha de siembra.');
            valido = false;
        } else {
            limpiarError(cCosecha);
        }
    } else {
        limpiarError(cCosecha);
    }

    if (!valido) enfocarPrimerError(form);
    return valido;
}

/* =========================================================
   2. VALIDACIÓN — Modal Agregar Registro de Labor (#modalRegistro)
   ========================================================= */

/**
 * Valida el formulario de registro de labor.
 * Campos: descripcionTrabajo (obligatorio, ≤1000 chars),
 *         fechaInicio (obligatoria, >= hoy),
 *         fechaFinalizo (obligatoria, >= fechaInicio),
 *         observaciones (opcional, ≤500 chars).
 *
 * @returns {boolean}
 */
function validarFormLabor() {
    const form      = document.getElementById('formLabor');
    const cDesc     = form.querySelector('[name="descripcionTrabajo"]');
    const cInicio   = form.querySelector('[name="fechaInicio"]');
    const cFin      = form.querySelector('[name="fechaFinalizo"]');
    const cObs      = form.querySelector('[name="observaciones"]');

    let valido = true;

    // Descripción: obligatoria, 5–1000 caracteres
    if (!validarTextoRequerido(cDesc, 'La descripción de la labor', 5, 1000)) valido = false;

    // Fecha de inicio: obligatoria y >= hoy
    if (!validarFechaInicio(cInicio)) valido = false;

    // Fecha de fin: obligatoria y >= fecha de inicio
    if (!validarFechaFin(cFin, cInicio)) valido = false;

    // Observaciones: opcional, máx 500 caracteres
    if (cObs && !estaVacio(cObs.value) && cObs.value.trim().length > 500) {
        mostrarError(cObs, 'Las observaciones no pueden superar los 500 caracteres.');
        valido = false;
    } else if (cObs) {
        limpiarError(cObs);
    }

    if (!valido) enfocarPrimerError(form);
    return valido;
}

/* =========================================================
   3. HELPERS DE UI
   ========================================================= */

/**
 * Desplaza la vista y enfoca el primer campo con error dentro de un formulario.
 * @param {HTMLFormElement} form
 */
function enfocarPrimerError(form) {
    const primerError = form.querySelector('.campo-error');
    if (primerError) {
        primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
        primerError.focus();
    }
}

/**
 * Limpia todos los estados de validación (error/ok) de un formulario.
 * Se llama al abrir un modal para no mostrar estados del uso anterior.
 * @param {HTMLFormElement} form
 */
function limpiarEstadosForm(form) {
    form.querySelectorAll('.campo-error, .campo-ok').forEach(el => {
        el.classList.remove('campo-error', 'campo-ok');
        el.removeAttribute('aria-invalid');
    });
    form.querySelectorAll('.mensaje-error').forEach(span => {
        span.textContent  = '';
        span.style.display = 'none';
    });
}

/* =========================================================
   4. LISTENERS EN TIEMPO REAL
   ========================================================= */

document.addEventListener('DOMContentLoaded', function () {

    /* ── Formulario cultivo ── */
    const formCultivo = document.getElementById('formCultivo');
    if (formCultivo) {

        const cNombre  = formCultivo.querySelector('[name="nombreCultivo"]');
        const cTipo    = formCultivo.querySelector('[name="tipoCultivo"]');
        const cSiembra = formCultivo.querySelector('[name="fechaSiembra"]');
        const cCosecha = formCultivo.querySelector('[name="fechaCosecha"]');

        if (cNombre)
            cNombre.addEventListener('blur', () =>
                validarTextoRequerido(cNombre, 'El nombre del cultivo', 2, 80));

        if (cTipo)
            cTipo.addEventListener('blur', () => {
                if (!estaVacio(cTipo.value) && !enRango(cTipo.value, 2, 60))
                    mostrarError(cTipo, 'El tipo debe tener entre 2 y 60 caracteres.');
                else
                    limpiarError(cTipo);
            });

        if (cSiembra) {
            cSiembra.addEventListener('change', () => {
                const esEdicion = !estaVacio(document.getElementById('editId').value);
                if (estaVacio(cSiembra.value)) {
                    mostrarError(cSiembra, 'La fecha de siembra es obligatoria.');
                } else if (!esEdicion && !fechaNoEsAnteriorAHoy(cSiembra.value)) {
                    mostrarError(cSiembra, 'La fecha de siembra no puede ser anterior a hoy.');
                } else {
                    limpiarError(cSiembra);
                }
                // Si cosecha ya tiene valor, re-valida la relación siembra → cosecha
                if (cCosecha && !estaVacio(cCosecha.value)) {
                    if (!fechaFinNoEsAnteriorAInicio(cSiembra.value, cCosecha.value))
                        mostrarError(cCosecha, 'La fecha de cosecha no puede ser anterior a la fecha de siembra.');
                    else
                        limpiarError(cCosecha);
                }
            });
        }

        if (cCosecha) {
            cCosecha.addEventListener('change', () => {
                if (!estaVacio(cCosecha.value) && cSiembra && !estaVacio(cSiembra.value)) {
                    if (!fechaFinNoEsAnteriorAInicio(cSiembra.value, cCosecha.value))
                        mostrarError(cCosecha, 'La fecha de cosecha no puede ser anterior a la fecha de siembra.');
                    else
                        limpiarError(cCosecha);
                } else {
                    limpiarError(cCosecha);
                }
            });
        }

        // Submit del formulario de cultivo
        formCultivo.addEventListener('submit', function (e) {
            if (!validarFormCultivo()) e.preventDefault();
        });
    }

    /* ── Formulario labor ── */
    const formLabor = document.getElementById('formLabor');
    if (formLabor) {

        const cDesc   = formLabor.querySelector('[name="descripcionTrabajo"]');
        const cInicio = formLabor.querySelector('[name="fechaInicio"]');
        const cFin    = formLabor.querySelector('[name="fechaFinalizo"]');
        const cObs    = formLabor.querySelector('[name="observaciones"]');

        if (cDesc)
            cDesc.addEventListener('blur', () =>
                validarTextoRequerido(cDesc, 'La descripción de la labor', 5, 1000));

        if (cInicio) {
            cInicio.addEventListener('change', () => {
                validarFechaInicio(cInicio);
                if (cFin && !estaVacio(cFin.value))
                    validarFechaFin(cFin, cInicio);
                else if (cFin)
                    resetearCampo(cFin);
            });
        }

        if (cFin)
            cFin.addEventListener('change', () => validarFechaFin(cFin, cInicio));

        if (cObs)
            cObs.addEventListener('blur', () => {
                if (!estaVacio(cObs.value) && cObs.value.trim().length > 500)
                    mostrarError(cObs, 'Las observaciones no pueden superar los 500 caracteres.');
                else
                    limpiarError(cObs);
            });

        // Submit del formulario de labor
        formLabor.addEventListener('submit', function (e) {
            if (!validarFormLabor()) e.preventDefault();
        });
    }
});