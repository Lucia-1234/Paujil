/**
 * validaciones-cultivos.js
 * Módulo de validación frontend — Cultivos y Registros de Labor
 * Finca El Paujil
 *
 * Depende de validaciones.js (que debe cargarse antes en el HTML).
 * Reutiliza las funciones puras ya definidas allí:
 *   estaVacio, enRango, validarTextoRequerido
 *
 * Este archivo es la única fuente de verdad para:
 *   - fechaNoEsAnteriorAHoy
 *   - fechaFinNoEsAnteriorAInicio
 *
 * Todos los helpers de error de este módulo usan mostrarErrorFecha /
 * limpiarErrorFecha de forma consistente, incluyendo campos de texto
 * como nombre, tipo, descripción y observaciones, para garantizar que
 * los spans de error queden siempre insertados justo después del campo.
 */

'use strict';

/* =========================================================
   UTILIDADES DE FECHA (puras, sin efectos secundarios)
   ========================================================= */

/**
 * Devuelve la fecha de hoy en formato YYYY-MM-DD.
 * @returns {string}
 */
function hoyISO() {
    const hoy  = new Date();
    const yyyy = hoy.getFullYear();
    const mm   = String(hoy.getMonth() + 1).padStart(2, '0');
    const dd   = String(hoy.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

/**
 * Compara dos cadenas YYYY-MM-DD lexicográficamente.
 * @param {string} fechaISO
 * @returns {boolean} true si la fecha NO es anterior a hoy.
 */
function fechaNoEsAnteriorAHoy(fechaISO) {
    return fechaISO >= hoyISO();
}

/**
 * Verifica que fechaFin no sea anterior a fechaInicio.
 * @param {string} inicioISO
 * @param {string} finISO
 * @returns {boolean} true si fin >= inicio.
 */
function fechaFinNoEsAnteriorAInicio(inicioISO, finISO) {
    return finISO >= inicioISO;
}


/* =========================================================
   HELPERS DE ERROR
   Única familia de helpers para todo este módulo.
   Buscan el span por ID convencional: "error-{campo.id}"
   Si no existe lo crean insertándolo justo después del campo.
   ========================================================= */

/**
 * Muestra un mensaje de error junto al campo.
 * @param {HTMLElement} campo
 * @param {string}      mensaje
 */
function mostrarErrorFecha(campo, mensaje) {
    campo.classList.add('campo-error');
    campo.classList.remove('campo-ok');
    campo.setAttribute('aria-invalid', 'true');

    const spanId = 'error-' + campo.id;
    let span = document.getElementById(spanId);

    if (!span) {
        span = document.createElement('span');
        span.id = spanId;
        span.className = 'error-fecha';
        span.setAttribute('role', 'alert');
        campo.insertAdjacentElement('afterend', span);
    }

    if (span.textContent !== mensaje) {
        span.textContent = mensaje;
        span.style.animation = 'none';
        void span.offsetHeight; // fuerza reflow para reiniciar animación CSS
        span.style.animation = '';
    }
    span.style.display = 'block';
}

/**
 * Limpia el estado de error del campo y lo marca como válido.
 * @param {HTMLElement} campo
 */
function limpiarErrorFecha(campo) {
    campo.classList.remove('campo-error');
    campo.classList.add('campo-ok');
    campo.removeAttribute('aria-invalid');

    const span = document.getElementById('error-' + campo.id);
    if (span) {
        span.textContent   = '';
        span.style.display = 'none';
    }
}

/**
 * Quita toda marca visual (error/ok) sin indicar éxito.
 * Útil para limpiar el modal antes de abrirlo.
 * @param {HTMLElement} campo
 */
function neutralizarCampo(campo) {
    campo.classList.remove('campo-error', 'campo-ok');
    campo.removeAttribute('aria-invalid');
    const span = document.getElementById('error-' + campo.id);
    if (span) {
        span.textContent   = '';
        span.style.display = 'none';
    }
}


/* =========================================================
   1. VALIDACIÓN — Modal Agregar / Editar Cultivo (#formCultivo)
   ========================================================= */

/**
 * Valida el nombre del cultivo.
 * Usa mostrarErrorFecha para mantener consistencia visual en este modal.
 * @param {HTMLInputElement} cNombre
 * @returns {boolean}
 */
function validarNombreCultivo(cNombre) {
    const valor = cNombre.value.trim();
    if (estaVacio(valor)) {
        mostrarErrorFecha(cNombre, 'El nombre del cultivo es obligatorio.');
        return false;
    }
    if (!enRango(valor, 2, 80)) {
        mostrarErrorFecha(cNombre, 'El nombre debe tener entre 2 y 80 caracteres.');
        return false;
    }
    limpiarErrorFecha(cNombre);
    return true;
}

/**
 * Valida el tipo del cultivo (campo opcional).
 * Si se ingresa, debe tener entre 2 y 60 caracteres.
 * @param {HTMLInputElement} cTipo
 * @returns {boolean}
 */
function validarTipoCultivo(cTipo) {
    if (estaVacio(cTipo.value)) {
        limpiarErrorFecha(cTipo);
        return true;
    }
    if (!enRango(cTipo.value, 2, 60)) {
        mostrarErrorFecha(cTipo, 'El tipo debe tener entre 2 y 60 caracteres.');
        return false;
    }
    limpiarErrorFecha(cTipo);
    return true;
}

/**
 * Valida la fecha de siembra.
 * - En CREACIÓN: obligatoria y >= hoy.
 * - En EDICIÓN:  solo obligatoria (puede ser una fecha pasada legítima).
 * @param {HTMLInputElement} cSiembra
 * @param {boolean}          esEdicion
 * @returns {boolean}
 */
function validarSiembra(cSiembra, esEdicion) {
    if (estaVacio(cSiembra.value)) {
        mostrarErrorFecha(cSiembra, 'La fecha de siembra es obligatoria.');
        return false;
    }
    if (!esEdicion && !fechaNoEsAnteriorAHoy(cSiembra.value)) {
        mostrarErrorFecha(cSiembra, 'La fecha de siembra no puede ser anterior a hoy.');
        return false;
    }
    const anio = new Date(cSiembra.value).getFullYear();
    if (isNaN(anio) || anio < 1900 || anio > 9999) {
        mostrarErrorFecha(cSiembra, 'El año de la fecha de siembra no es válido.');
        return false;
    }
    limpiarErrorFecha(cSiembra);
    return true;
}

/**
 * Valida la fecha de cosecha (campo opcional).
 * Si tiene valor, debe ser >= fechaSiembra.
 * @param {HTMLInputElement} cCosecha
 * @param {HTMLInputElement} cSiembra
 * @returns {boolean}
 */
function validarCosecha(cCosecha, cSiembra) {
    if (estaVacio(cCosecha.value)) {
        limpiarErrorFecha(cCosecha);
        return true;
    }
    if (!estaVacio(cSiembra.value) &&
        !fechaFinNoEsAnteriorAInicio(cSiembra.value, cCosecha.value)) {
        mostrarErrorFecha(cCosecha, 'La fecha de cosecha no puede ser anterior a la de siembra.');
        return false;
    }
    limpiarErrorFecha(cCosecha);
    return true;
}

/**
 * Valida el formulario completo de cultivo (agregar o editar).
 * @returns {boolean}
 */
function validarFormCultivo() {
    const form     = document.getElementById('formCultivo');
    const cNombre  = form.querySelector('[name="nombreCultivo"]');
    const cTipo    = form.querySelector('[name="tipoCultivo"]');
    const cSiembra = form.querySelector('[name="fechaSiembra"]');
    const cCosecha = form.querySelector('[name="fechaCosecha"]');
    const esEdicion = !estaVacio(document.getElementById('editId').value);

    let valido = true;

    if (!validarNombreCultivo(cNombre))       valido = false;
    if (!validarTipoCultivo(cTipo))           valido = false;
    if (!validarSiembra(cSiembra, esEdicion)) valido = false;
    if (!validarCosecha(cCosecha, cSiembra))  valido = false;

    if (!valido) enfocarPrimerError(form);
    return valido;
}


/* =========================================================
   2. VALIDACIÓN — Modal Registro de Labor (#formLabor)
   ========================================================= */

/**
 * Valida la descripción de la labor.
 * @param {HTMLTextAreaElement} cDesc
 * @returns {boolean}
 */
function validarDescripcionLabor(cDesc) {
    const valor = cDesc.value.trim();
    if (!valor) {
        mostrarErrorFecha(cDesc, 'La descripción de la labor es obligatoria.');
        return false;
    }
    if (valor.length < 5) {
        mostrarErrorFecha(cDesc, 'La descripción debe tener al menos 5 caracteres.');
        return false;
    }
    if (valor.length > 1000) {
        mostrarErrorFecha(cDesc, 'La descripción no puede superar los 1000 caracteres.');
        return false;
    }
    limpiarErrorFecha(cDesc);
    return true;
}

/**
 * Valida la fecha de inicio de una labor: obligatoria y >= hoy.
 * @param {HTMLInputElement} cInicio
 * @returns {boolean}
 */
function validarFechaInicioLabor(cInicio) {
    if (estaVacio(cInicio.value)) {
        mostrarErrorFecha(cInicio, 'La fecha de inicio es obligatoria.');
        return false;
    }
    if (!fechaNoEsAnteriorAHoy(cInicio.value)) {
        mostrarErrorFecha(cInicio, 'La fecha de inicio no puede ser anterior a hoy.');
        return false;
    }
    limpiarErrorFecha(cInicio);
    return true;
}

/**
 * Valida la fecha de finalización de una labor: obligatoria y >= fechaInicio.
 * @param {HTMLInputElement} cFin
 * @param {HTMLInputElement} cInicio
 * @returns {boolean}
 */
function validarFechaFinLabor(cFin, cInicio) {
    if (estaVacio(cFin.value)) {
        mostrarErrorFecha(cFin, 'La fecha de finalización es obligatoria.');
        return false;
    }
    if (!estaVacio(cInicio.value) &&
        !fechaFinNoEsAnteriorAInicio(cInicio.value, cFin.value)) {
        mostrarErrorFecha(cFin, 'La fecha de finalización no puede ser anterior a la de inicio.');
        return false;
    }
    limpiarErrorFecha(cFin);
    return true;
}

/**
 * Valida las observaciones de una labor (campo opcional, máx. 500 chars).
 * @param {HTMLTextAreaElement} cObs
 * @returns {boolean}
 */
function validarObservacionesLabor(cObs) {
    if (!estaVacio(cObs.value) && cObs.value.trim().length > 500) {
        mostrarErrorFecha(cObs, 'Las observaciones no pueden superar los 500 caracteres.');
        return false;
    }
    limpiarErrorFecha(cObs);
    return true;
}

/**
 * Valida el formulario completo de registro de labor.
 * @returns {boolean}
 */
function validarFormLabor() {
    const form    = document.getElementById('formLabor');
    const cDesc   = form.querySelector('[name="descripcionTrabajo"]');
    const cInicio = form.querySelector('[name="fechaInicio"]');
    const cFin    = form.querySelector('[name="fechaFinalizo"]');
    const cObs    = form.querySelector('[name="observaciones"]');

    let valido = true;

    if (!validarDescripcionLabor(cDesc))       valido = false;
    if (!validarFechaInicioLabor(cInicio))     valido = false;
    if (!validarFechaFinLabor(cFin, cInicio))  valido = false;
    if (!validarObservacionesLabor(cObs))      valido = false;

    if (!valido) enfocarPrimerError(form);
    return valido;
}


/* =========================================================
   3. HELPERS DE UI
   ========================================================= */

/**
 * Desplaza la vista y enfoca el primer campo con error dentro del formulario.
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
 * Se llama al abrir un modal para no mostrar residuos del uso anterior.
 * @param {HTMLFormElement} form
 */
function limpiarEstadosForm(form) {
    form.querySelectorAll('.campo-error, .campo-ok').forEach(el => {
        el.classList.remove('campo-error', 'campo-ok');
        el.removeAttribute('aria-invalid');
    });
    form.querySelectorAll('.mensaje-error, .error-fecha').forEach(span => {
        span.textContent   = '';
        span.style.display = 'none';
    });
}


/* =========================================================
   4. LISTENERS — tiempo real (change, input, blur) y submit
   ========================================================= */

document.addEventListener('DOMContentLoaded', function () {

    /* ── Formulario cultivo ─────────────────────────────── */
    const formCultivo = document.getElementById('formCultivo');
    if (formCultivo) {

        const cNombre  = formCultivo.querySelector('[name="nombreCultivo"]');
        const cTipo    = formCultivo.querySelector('[name="tipoCultivo"]');
        const cSiembra = formCultivo.querySelector('[name="fechaSiembra"]');
        const cCosecha = formCultivo.querySelector('[name="fechaCosecha"]');

        if (cNombre) {
            ['input', 'blur'].forEach(ev =>
                cNombre.addEventListener(ev, () => validarNombreCultivo(cNombre)));
        }

        if (cTipo) {
            ['input', 'blur'].forEach(ev =>
                cTipo.addEventListener(ev, () => validarTipoCultivo(cTipo)));
        }

        if (cSiembra) {
            ['change', 'input'].forEach(ev =>
                cSiembra.addEventListener(ev, () => {
                    const esEdicion = !estaVacio(document.getElementById('editId').value);
                    validarSiembra(cSiembra, esEdicion);
                    if (cCosecha && !estaVacio(cCosecha.value)) {
                        validarCosecha(cCosecha, cSiembra);
                    }
                }));
        }

        if (cCosecha) {
            ['change', 'input'].forEach(ev =>
                cCosecha.addEventListener(ev, () =>
                    validarCosecha(cCosecha, cSiembra)));
        }

        formCultivo.addEventListener('submit', function (e) {
            if (!validarFormCultivo()) e.preventDefault();
        });
    }

    /* ── Formulario labor ───────────────────────────────── */
    const formLabor = document.getElementById('formLabor');
    if (formLabor) {

        const cDesc   = formLabor.querySelector('[name="descripcionTrabajo"]');
        const cInicio = formLabor.querySelector('[name="fechaInicio"]');
        const cFin    = formLabor.querySelector('[name="fechaFinalizo"]');
        const cObs    = formLabor.querySelector('[name="observaciones"]');

        if (cDesc) {
            ['input', 'blur'].forEach(ev =>
                cDesc.addEventListener(ev, () => validarDescripcionLabor(cDesc)));
        }

        if (cInicio) {
            ['change', 'input'].forEach(ev =>
                cInicio.addEventListener(ev, () => {
                    validarFechaInicioLabor(cInicio);
                    if (cFin && !estaVacio(cFin.value)) {
                        validarFechaFinLabor(cFin, cInicio);
                    } else if (cFin) {
                        neutralizarCampo(cFin);
                    }
                }));
        }

        if (cFin) {
            ['change', 'input'].forEach(ev =>
                cFin.addEventListener(ev, () =>
                    validarFechaFinLabor(cFin, cInicio)));
        }

        if (cObs) {
            ['input', 'blur'].forEach(ev =>
                cObs.addEventListener(ev, () => validarObservacionesLabor(cObs)));
        }

        formLabor.addEventListener('submit', function (e) {
            if (!validarFormLabor()) e.preventDefault();
        });
    }
});