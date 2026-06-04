/**
 * validaciones-cultivos.js
 * Módulo de validación frontend — Cultivos y Registros de Labor
 * Finca El Paujil
 *
 * Depende de validaciones.js (que debe cargarse antes en el HTML).
 * Reutiliza las funciones puras ya definidas allí:
 *   estaVacio, enRango, mostrarError, limpiarError, resetearCampo,
 *   validarTextoRequerido
 *
 * Implementa validaciones de fecha EXCLUSIVAMENTE en el frontend:
 *   - fechaSiembra / fechaInicio : no puede ser anterior a hoy
 *   - fechaCosecha / fechaFinalizo : no puede ser anterior a la fecha de inicio
 *
 * El backend NO valida estas reglas de negocio.
 */

'use strict';

/* =========================================================
   UTILIDADES DE FECHA (puras, sin efectos secundarios)
   ========================================================= */

/**
 * Devuelve la fecha de hoy en formato YYYY-MM-DD sin componente de hora,
 * de modo que la comparación sea solo por día calendario.
 * @returns {string}
 */
function hoyISO() {
    const hoy = new Date();
    const yyyy = hoy.getFullYear();
    const mm   = String(hoy.getMonth() + 1).padStart(2, '0');
    const dd   = String(hoy.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
}

/**
 * Compara dos cadenas YYYY-MM-DD lexicográficamente (funciona porque el
 * formato ISO 8601 es ordenable como string).
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
   HELPERS DE MENSAJES DE ERROR
   Los span de error se buscan por ID convencional:
     campo  → #error-{campo.id}
   Si no existe el span se crea y se inserta justo después del campo.
   ========================================================= */

/**
 * Muestra un mensaje de error debajo del campo con animación de entrada.
 * Crea el span si no existe aún en el DOM.
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

    // Solo actualiza y anima si el texto cambió, para no resetear la animación
    if (span.textContent !== mensaje) {
        span.textContent = mensaje;
        span.style.animation = 'none';
        // Fuerza reflow para reiniciar la animación
        void span.offsetHeight;
        span.style.animation = '';
    }
    span.style.display = 'block';
}

/**
 * Elimina el mensaje de error del campo y marca el campo como válido.
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
 * Quita toda marca visual (error/ok) sin mostrar éxito.
 * Útil al limpiar el modal antes de abrirlo.
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
 * Valida el campo fechaSiembra.
 * Reglas:
 *   - Obligatorio siempre.
 *   - En CREACIÓN: debe ser >= hoy (regla de negocio).
 *   - En EDICIÓN:  solo se exige que tenga valor (puede ser fecha pasada legítima).
 *
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
 * Valida el campo fechaCosecha (opcional).
 * Si tiene valor, debe ser >= fechaSiembra.
 *
 * @param {HTMLInputElement} cCosecha
 * @param {HTMLInputElement} cSiembra
 * @returns {boolean}
 */
function validarCosecha(cCosecha, cSiembra) {
    if (estaVacio(cCosecha.value)) {
        limpiarErrorFecha(cCosecha);
        return true;
    }
    if (!estaVacio(cSiembra.value) && !fechaFinNoEsAnteriorAInicio(cSiembra.value, cCosecha.value)) {
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

    // Nombre: obligatorio, 2–80 caracteres
    if (!validarTextoRequerido(cNombre, 'El nombre del cultivo', 2, 80)) valido = false;

    // Tipo: opcional; si se ingresa debe tener 2–60 caracteres
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

    // Fechas
    if (!validarSiembra(cSiembra, esEdicion)) valido = false;
    if (!validarCosecha(cCosecha, cSiembra))  valido = false;

    if (!valido) enfocarPrimerError(form);
    return valido;
}


/* =========================================================
   2. VALIDACIÓN — Modal Registro de Labor (#formLabor)
   ========================================================= */

/**
 * Valida el campo fechaInicio de una labor.
 * Reglas: obligatorio y >= hoy.
 *
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
 * Valida el campo fechaFinalizo de una labor.
 * Reglas: obligatorio y >= fechaInicio.
 *
 * @param {HTMLInputElement} cFin
 * @param {HTMLInputElement} cInicio
 * @returns {boolean}
 */
function validarFechaFinLabor(cFin, cInicio) {
    if (estaVacio(cFin.value)) {
        mostrarErrorFecha(cFin, 'La fecha de finalización es obligatoria.');
        return false;
    }
    if (!estaVacio(cInicio.value) && !fechaFinNoEsAnteriorAInicio(cInicio.value, cFin.value)) {
        mostrarErrorFecha(cFin, 'La fecha de finalización no puede ser anterior a la de inicio.');
        return false;
    }
    limpiarErrorFecha(cFin);
    return true;
}

/**
 * Valida el campo descripcionTrabajo del formLabor.
 * Usa mostrarErrorFecha / limpiarErrorFecha para inserción consistente,
 * garantizando que el span quede justo después del textarea en el DOM.
 *
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

    // Descripción: obligatoria, 5–1000 caracteres
    if (!validarDescripcionLabor(cDesc)) valido = false;

    // Fechas
    if (!validarFechaInicioLabor(cInicio)) valido = false;
    if (!validarFechaFinLabor(cFin, cInicio)) valido = false;

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
   4. LISTENERS — tiempo real (change + input)
   ========================================================= */

document.addEventListener('DOMContentLoaded', function () {

    /* ── Formulario cultivo ─────────────────────────────── */
    const formCultivo = document.getElementById('formCultivo');
    if (formCultivo) {

        const cNombre  = formCultivo.querySelector('[name="nombreCultivo"]');
        const cTipo    = formCultivo.querySelector('[name="tipoCultivo"]');
        const cSiembra = formCultivo.querySelector('[name="fechaSiembra"]');
        const cCosecha = formCultivo.querySelector('[name="fechaCosecha"]');

        /* Nombre */
        if (cNombre) {
            ['input', 'blur'].forEach(ev =>
                cNombre.addEventListener(ev, () =>
                    validarTextoRequerido(cNombre, 'El nombre del cultivo', 2, 80)));
        }

        /* Tipo */
        if (cTipo) {
            ['input', 'blur'].forEach(ev =>
                cTipo.addEventListener(ev, () => {
                    if (!estaVacio(cTipo.value) && !enRango(cTipo.value, 2, 60))
                        mostrarError(cTipo, 'El tipo debe tener entre 2 y 60 caracteres.');
                    else
                        limpiarError(cTipo);
                }));
        }

        /* Fecha de siembra */
        if (cSiembra) {
            ['change', 'input'].forEach(ev =>
                cSiembra.addEventListener(ev, () => {
                    const esEdicion = !estaVacio(document.getElementById('editId').value);
                    validarSiembra(cSiembra, esEdicion);
                    // Revalida cosecha si ya tiene valor
                    if (cCosecha && !estaVacio(cCosecha.value)) {
                        validarCosecha(cCosecha, cSiembra);
                    }
                }));
        }

        /* Fecha de cosecha */
        if (cCosecha) {
            ['change', 'input'].forEach(ev =>
                cCosecha.addEventListener(ev, () =>
                    validarCosecha(cCosecha, cSiembra)));
        }

        /* Submit */
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

        /* Descripción */
        if (cDesc) {
            ['input', 'blur'].forEach(ev =>
                cDesc.addEventListener(ev, () =>
                    validarDescripcionLabor(cDesc)));
        }

        /* Fecha de inicio */
        if (cInicio) {
            ['change', 'input'].forEach(ev =>
                cInicio.addEventListener(ev, () => {
                    validarFechaInicioLabor(cInicio);
                    // Revalida fin si ya tiene valor
                    if (cFin && !estaVacio(cFin.value)) {
                        validarFechaFinLabor(cFin, cInicio);
                    } else if (cFin) {
                        neutralizarCampo(cFin);
                    }
                }));
        }

        /* Fecha de finalización */
        if (cFin) {
            ['change', 'input'].forEach(ev =>
                cFin.addEventListener(ev, () =>
                    validarFechaFinLabor(cFin, cInicio)));
        }

        /* Observaciones */
        if (cObs) {
            ['input', 'blur'].forEach(ev =>
                cObs.addEventListener(ev, () => {
                    if (!estaVacio(cObs.value) && cObs.value.trim().length > 500)
                        mostrarError(cObs, 'Las observaciones no pueden superar los 500 caracteres.');
                    else
                        limpiarError(cObs);
                }));
        }

        /* Submit */
        formLabor.addEventListener('submit', function (e) {
            if (!validarFormLabor()) e.preventDefault();
        });
    }
});