/**
 * validaciones-cultivos.js
 * Módulo de validación frontend para Cultivos y Labores.
 */

'use strict'; // Activa el modo estricto para evitar errores comunes de sintaxis en JS.

/* =========================================================
   1. UTILIDADES DE FECHA
   ========================================================= */

/** Genera la fecha actual en formato ISO (YYYY-MM-DD). */
function hoyISO() {
    const hoy  = new Date(); // Crea una nueva instancia con la fecha y hora actual del sistema.
    const yyyy = hoy.getFullYear(); // Extrae el año de la fecha.
    const mm   = String(hoy.getMonth() + 1).padStart(2, '0'); // Obtiene el mes (0-11), suma 1 y formatea con 2 dígitos rellenando con '0'.
    const dd   = String(hoy.getDate()).padStart(2, '0'); // Obtiene el día del mes y formatea con 2 dígitos rellenando con '0'.
    return `${yyyy}-${mm}-${dd}`; // Retorna la fecha concatenada en formato string ISO.
}

/** Compara si una fecha es igual o posterior a la fecha actual. */
function fechaNoEsAnteriorAHoy(fechaISO) {
    return fechaISO >= hoyISO(); // Compara alfabéticamente el string de fecha contra la fecha de hoy.
}

/** Verifica que la fecha fin sea igual o posterior a la fecha inicio. */
function fechaFinNoEsAnteriorAInicio(inicioISO, finISO) {
    return finISO >= inicioISO; // Retorna true si la fecha fin es mayor o igual a la de inicio.
}

/* =========================================================
   2. HELPERS DE ERROR (Interfaz de Usuario)
   ========================================================= */

/** Muestra un mensaje de error y marca el campo visualmente. */
function mostrarErrorFecha(campo, mensaje) {
    campo.classList.add('campo-error'); // Añade la clase CSS para mostrar el campo con estilo de error.
    campo.classList.remove('campo-ok'); // Elimina la clase CSS de estilo correcto si existía.
    campo.setAttribute('aria-invalid', 'true'); // Define el estado de error para tecnologías de asistencia (accesibilidad).

    const spanId = 'error-' + campo.id; // Construye el ID del elemento span de error basado en el ID del input.
    let span = document.getElementById(spanId); // Busca si ya existe un elemento de error para este input.

    if (!span) { // Si el elemento span de error no existe...
        span = document.createElement('span'); // Crea un nuevo nodo de tipo span.
        span.id = spanId; // Le asigna el ID construido anteriormente.
        span.className = 'error-fecha'; // Le aplica la clase CSS para el estilo de los mensajes de error.
        span.setAttribute('role', 'alert'); // Define el rol semántico para que los lectores de pantalla lo anuncien.
        campo.insertAdjacentElement('afterend', span); // Inserta el span en el DOM inmediatamente después del input.
    }

    if (span.textContent !== mensaje) { // Si el mensaje del span es distinto al nuevo error...
        span.textContent = mensaje; // Actualiza el texto con el nuevo mensaje.
        span.style.animation = 'none'; // Detiene la animación CSS si estaba corriendo.
        void span.offsetHeight; // Fuerza un reflow (re-render) para permitir el reinicio de la animación.
        span.style.animation = ''; // Restaura la animación CSS.
    }
    span.style.display = 'block'; // Asegura que el span sea visible.
}

/** Limpia el estado de error y marca el campo como correcto. */
function limpiarErrorFecha(campo) {
    campo.classList.remove('campo-error'); // Elimina la clase de estilo de error.
    campo.classList.add('campo-ok'); // Añade la clase de estilo de éxito.
    campo.removeAttribute('aria-invalid'); // Elimina el atributo de invalidación.

    const span = document.getElementById('error-' + campo.id); // Busca el span de error asociado.
    if (span) { // Si el span existe en el DOM...
        span.textContent = ''; // Limpia el texto del mensaje.
        span.style.display = 'none'; // Oculta el elemento en la interfaz.
    }
}

/** Neutraliza el estado visual de un campo. */
function neutralizarCampo(campo) {
    campo.classList.remove('campo-error', 'campo-ok'); // Quita ambos estados posibles.
    campo.removeAttribute('aria-invalid'); // Quita el atributo de error.
    const span = document.getElementById('error-' + campo.id); // Busca el span asociado.
    if (span) { // Si existe el span...
        span.textContent = ''; // Limpia el texto.
        span.style.display = 'none'; // Lo oculta.
    }
}

/* =========================================================
   3. VALIDACIÓN — Formulario Cultivo
   ========================================================= */

function validarNombreCultivo(cNombre) {
    const valor = cNombre.value.trim(); // Obtiene el valor y elimina espacios laterales.
    if (estaVacio(valor)) { // Valida si el valor es vacío.
        mostrarErrorFecha(cNombre, 'El nombre del cultivo es obligatorio.'); // Llama al helper de error.
        return false; // Retorna falso para detener el flujo.
    }
    if (!enRango(valor, 2, 80)) { // Valida longitud de caracteres.
        mostrarErrorFecha(cNombre, 'El nombre debe tener entre 2 y 80 caracteres.');
        return false;
    }
    limpiarErrorFecha(cNombre); // Limpia el error si es válido.
    return true; // Retorna éxito.
}

function validarTipoCultivo(cTipo) {
    if (estaVacio(cTipo.value)) { // Si es opcional y está vacío...
        limpiarErrorFecha(cTipo); // Limpia cualquier estado previo y permite continuar.
        return true;
    }
    if (!enRango(cTipo.value, 2, 60)) { // Valida longitud.
        mostrarErrorFecha(cTipo, 'El tipo debe tener entre 2 y 60 caracteres.');
        return false;
    }
    limpiarErrorFecha(cTipo); // Limpia el error si es válido.
    return true;
}

function validarSiembra(cSiembra, esEdicion) {
    if (estaVacio(cSiembra.value)) { // Verifica obligatoriedad.
        mostrarErrorFecha(cSiembra, 'La fecha de siembra es obligatoria.');
        return false;
    }
    if (!esEdicion && !fechaNoEsAnteriorAHoy(cSiembra.value)) { // Si es nuevo registro, valida fecha actual.
        mostrarErrorFecha(cSiembra, 'La fecha de siembra no puede ser anterior a hoy.');
        return false;
    }
    const anio = new Date(cSiembra.value).getFullYear(); // Extrae año de la fecha.
    if (isNaN(anio) || anio < 1900 || anio > 9999) { // Valida límites del año.
        mostrarErrorFecha(cSiembra, 'El año de la fecha de siembra no es válido.');
        return false;
    }
    limpiarErrorFecha(cSiembra); // Limpia error.
    return true;
}

function validarCosecha(cCosecha, cSiembra) {
    if (estaVacio(cCosecha.value)) { // Si opcional, permite vacío.
        limpiarErrorFecha(cCosecha);
        return true;
    }
    if (!estaVacio(cSiembra.value) && !fechaFinNoEsAnteriorAInicio(cSiembra.value, cCosecha.value)) { // Valida rango fechas.
        mostrarErrorFecha(cCosecha, 'La fecha de cosecha no puede ser anterior a la de siembra.');
        return false;
    }
    limpiarErrorFecha(cCosecha); // Limpia error.
    return true;
}

function validarFormCultivo() {
    const form = document.getElementById('formCultivo'); // Busca el formulario por ID.
    const cNombre = form.querySelector('[name="nombreCultivo"]'); // Selecciona campo nombre.
    const cTipo = form.querySelector('[name="tipoCultivo"]'); // Selecciona campo tipo.
    const cSiembra = form.querySelector('[name="fechaSiembra"]'); // Selecciona campo siembra.
    const cCosecha = form.querySelector('[name="fechaCosecha"]'); // Selecciona campo cosecha.
    const esEdicion = !estaVacio(document.getElementById('editId').value); // Verifica si el hidden input tiene ID (es edicion).

    let valido = true; // Flag de estado.
    if (!validarNombreCultivo(cNombre)) valido = false; // Valida y actualiza estado.
    if (!validarTipoCultivo(cTipo)) valido = false; // Valida y actualiza estado.
    if (!validarSiembra(cSiembra, esEdicion)) valido = false; // Valida y actualiza estado.
    if (!validarCosecha(cCosecha, cSiembra)) valido = false; // Valida y actualiza estado.

    if (!valido) enfocarPrimerError(form); // Si es inválido, enfoca el primer error encontrado.
    return valido; // Retorna el estado final.
}

/* =========================================================
   4. VALIDACIÓN — Formulario Labor
   ========================================================= */

function validarDescripcionLabor(cDesc) {
    const valor = cDesc.value.trim(); // Limpia valor.
    if (!valor) { // Valida presencia de texto.
        mostrarErrorFecha(cDesc, 'La descripción de la labor es obligatoria.');
        return false;
    }
    if (valor.length < 5) { // Valida longitud mínima.
        mostrarErrorFecha(cDesc, 'La descripción debe tener al menos 5 caracteres.');
        return false;
    }
    if (valor.length > 1000) { // Valida longitud máxima.
        mostrarErrorFecha(cDesc, 'La descripción no puede superar los 1000 caracteres.');
        return false;
    }
    limpiarErrorFecha(cDesc); // Limpia error.
    return true;
}

function validarFechaInicioLabor(cInicio) {
    if (estaVacio(cInicio.value)) { // Valida obligatoriedad.
        mostrarErrorFecha(cInicio, 'La fecha de inicio es obligatoria.');
        return false;
    }
    if (!fechaNoEsAnteriorAHoy(cInicio.value)) { // Valida fecha no pasada.
        mostrarErrorFecha(cInicio, 'La fecha de inicio no puede ser anterior a hoy.');
        return false;
    }
    limpiarErrorFecha(cInicio); // Limpia error.
    return true;
}

function validarFechaFinLabor(cFin, cInicio) {
    if (estaVacio(cFin.value)) { // Valida obligatoriedad.
        mostrarErrorFecha(cFin, 'La fecha de finalización es obligatoria.');
        return false;
    }
    if (!estaVacio(cInicio.value) && !fechaFinNoEsAnteriorAInicio(cInicio.value, cFin.value)) { // Valida orden cronológico.
        mostrarErrorFecha(cFin, 'La fecha de finalización no puede ser anterior a la de inicio.');
        return false;
    }
    limpiarErrorFecha(cFin); // Limpia error.
    return true;
}

function validarObservacionesLabor(cObs) {
    if (!estaVacio(cObs.value) && cObs.value.trim().length > 500) { // Valida longitud opcional.
        mostrarErrorFecha(cObs, 'Las observaciones no pueden superar los 500 caracteres.');
        return false;
    }
    limpiarErrorFecha(cObs); // Limpia error.
    return true;
}

function validarFormLabor() {
    const form = document.getElementById('formLabor'); // Busca form labor.
    const cDesc = form.querySelector('[name="descripcionTrabajo"]'); // Busca campo descripción.
    const cInicio = form.querySelector('[name="fechaInicio"]'); // Busca campo inicio.
    const cFin = form.querySelector('[name="fechaFinalizo"]'); // Busca campo fin.
    const cObs = form.querySelector('[name="observaciones"]'); // Busca campo obs.

    let valido = true; // Flag estado.
    if (!validarDescripcionLabor(cDesc)) valido = false; // Valida campo.
    if (!validarFechaInicioLabor(cInicio)) valido = false; // Valida campo.
    if (!validarFechaFinLabor(cFin, cInicio)) valido = false; // Valida campo.
    if (!validarObservacionesLabor(cObs)) valido = false; // Valida campo.

    if (!valido) enfocarPrimerError(form); // Si falla, enfoca.
    return valido; // Retorna.
}

/* =========================================================
   5. LISTENERS Y UI
   ========================================================= */

function enfocarPrimerError(form) {
    const primerError = form.querySelector('.campo-error'); // Busca el primer campo con clase de error.
    if (primerError) { // Si existe...
        primerError.scrollIntoView({ behavior: 'smooth', block: 'center' }); // Desplaza pantalla hacia él.
        primerError.focus(); // Coloca el foco en el campo.
    }
}

function limpiarEstadosForm(form) {
    form.querySelectorAll('.campo-error, .campo-ok').forEach(el => { // Itera sobre inputs marcados.
        el.classList.remove('campo-error', 'campo-ok'); // Quita clases.
        el.removeAttribute('aria-invalid'); // Quita estado.
    });
    form.querySelectorAll('.mensaje-error, .error-fecha').forEach(span => { // Itera sobre mensajes error.
        span.textContent = ''; // Limpia texto.
        span.style.display = 'none'; // Oculta span.
    });
}

document.addEventListener('DOMContentLoaded', function () { // Listener carga inicial.
    const formCultivo = document.getElementById('formCultivo'); // Busca formulario cultivo.
    if (formCultivo) { // Si existe...
        const cNombre = formCultivo.querySelector('[name="nombreCultivo"]'); // Busca nombre.
        const cTipo = formCultivo.querySelector('[name="tipoCultivo"]'); // Busca tipo.
        const cSiembra = formCultivo.querySelector('[name="fechaSiembra"]'); // Busca siembra.
        const cCosecha = formCultivo.querySelector('[name="fechaCosecha"]'); // Busca cosecha.

        if (cNombre) ['input', 'blur'].forEach(ev => cNombre.addEventListener(ev, () => validarNombreCultivo(cNombre)));
        if (cTipo) ['input', 'blur'].forEach(ev => cTipo.addEventListener(ev, () => validarTipoCultivo(cTipo)));
        
        if (cSiembra) ['change', 'input'].forEach(ev => cSiembra.addEventListener(ev, () => {
            const esEdicion = !estaVacio(document.getElementById('editId').value); // Detecta modo.
            validarSiembra(cSiembra, esEdicion); // Valida siembra.
            if (cCosecha && !estaVacio(cCosecha.value)) validarCosecha(cCosecha, cSiembra); // Valida cosecha.
        }));

        if (cCosecha) ['change', 'input'].forEach(ev => cCosecha.addEventListener(ev, () => validarCosecha(cCosecha, cSiembra)));

        formCultivo.addEventListener('submit', function (e) { // Listener envío.
            if (!validarFormCultivo()) e.preventDefault(); // Previene envío si inválido.
        });
    }

    const formLabor = document.getElementById('formLabor'); // Busca form labor.
    if (formLabor) { // Si existe...
        const cDesc = formLabor.querySelector('[name="descripcionTrabajo"]'); // Busca campo desc.
        const cInicio = formLabor.querySelector('[name="fechaInicio"]'); // Busca campo inicio.
        const cFin = formLabor.querySelector('[name="fechaFinalizo"]'); // Busca campo fin.
        const cObs = formLabor.querySelector('[name="observaciones"]'); // Busca campo obs.

        if (cDesc) ['input', 'blur'].forEach(ev => cDesc.addEventListener(ev, () => validarDescripcionLabor(cDesc)));
        
        if (cInicio) ['change', 'input'].forEach(ev => cInicio.addEventListener(ev, () => {
            validarFechaInicioLabor(cInicio); // Valida inicio.
            if (cFin && !estaVacio(cFin.value)) validarFechaFinLabor(cFin, cInicio); // Valida fin si ya tiene valor.
            else if (cFin) neutralizarCampo(cFin); // Si no, neutraliza.
        }));

        if (cFin) ['change', 'input'].forEach(ev => cFin.addEventListener(ev, () => validarFechaFinLabor(cFin, cInicio)));
        if (cObs) ['input', 'blur'].forEach(ev => cObs.addEventListener(ev, () => validarObservacionesLabor(cObs)));

        formLabor.addEventListener('submit', function (e) { // Listener envío.
            if (!validarFormLabor()) e.preventDefault(); // Previene si inválido.
        });
    }
});