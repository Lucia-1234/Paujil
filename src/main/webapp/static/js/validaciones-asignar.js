/**
 * validaciones-asignar.js
 * Validaciones frontend para la vista asignar_trabajos.jsp
 */

'use strict'; // Modo estricto: evita errores de variables globales no declaradas.

/* ════════════════════════════════════════════════════════
   HELPERS LOCALES: Centralizan el manejo del DOM y los errores.
   ════════════════════════════════════════════════════════ */

function _mostrarSpan(spanId, msg) { // Muestra el mensaje de error en el span específico.
    const span = document.getElementById(spanId);
    if (!span) return;
    span.textContent = msg; // Asigna el texto del error.
    span.style.display = 'block'; // Hace visible el mensaje.
}

function _limpiarSpan(spanId) { // Oculta el mensaje de error al corregir.
    const span = document.getElementById(spanId);
    if (!span) return;
    span.textContent = '';
    span.style.display = 'none'; // Oculta el elemento en el DOM.
}

function _marcarError(campo, spanId, msg) { // Aplica estilo visual de error (borde rojo).
    campo.classList.add('campo-error');
    campo.classList.remove('campo-ok');
    _mostrarSpan(spanId, msg);
}

function _marcarOk(campo, spanId) { // Aplica estilo visual de éxito (borde verde).
    campo.classList.remove('campo-error');
    campo.classList.add('campo-ok');
    _limpiarSpan(spanId);
}

/* ════════════════════════════════════════════════════════
   VALIDACIONES POR CAMPO
   ════════════════════════════════════════════════════════ */

function validarSelectCultivo() { // Valida que se haya seleccionado un cultivo en el combo.
    const campo = document.getElementById('idCultivo');
    if (!campo) return true; // Si el campo no existe en la página, retorna true para no bloquear.
    if (!campo.value) { // Verifica si el valor está vacío.
        _marcarError(campo, 'error-idCultivo', 'Selecciona un cultivo.');
        return false;
    }
    _marcarOk(campo, 'error-idCultivo');
    return true;
}

function validarSelectUsuario() { // Valida selección de trabajador.
    const campo = document.getElementById('idUsuario');
    if (!campo) return true;
    if (!campo.value) {
        _marcarError(campo, 'error-idUsuario', 'Selecciona un trabajador.');
        return false;
    }
    _marcarOk(campo, 'error-idUsuario');
    return true;
}

function validarSelectTipo() { // Valida selección del tipo de trabajo.
    const campo = document.getElementById('idTipoTrabajo');
    if (!campo) return true;
    if (!campo.value) {
        _marcarError(campo, 'error-idTipoTrabajo', 'Selecciona un tipo de trabajo.');
        return false;
    }
    _marcarOk(campo, 'error-idTipoTrabajo');
    return true;
}

function validarDescripcion() { // Valida longitud de la descripción.
    const campo = document.getElementById('descripcion');
    if (!campo) return true;
    const val = campo.value.trim(); // Elimina espacios en blanco.
    if (val.length === 0) {
        _marcarError(campo, 'error-descripcion', 'La descripción es obligatoria.');
        return false;
    }
    if (val.length < 10) { // Regla: mínimo 10 caracteres.
        _marcarError(campo, 'error-descripcion', 'La descripción debe tener al menos 10 caracteres.');
        return false;
    }
    if (val.length > 500) { // Regla: máximo 500 caracteres.
        _marcarError(campo, 'error-descripcion', 'La descripción no puede superar 500 caracteres.');
        return false;
    }
    _marcarOk(campo, 'error-descripcion');
    return true;
}

function validarFechaAsignacion() { // Valida que la fecha no sea pasada.
    const campo = document.getElementById('fechaAsignacion');
    if (!campo) return true;
    const val = campo.value;
    if (!val) {
        _marcarError(campo, 'error-fechaAsignacion', 'La fecha de asignación es obligatoria.');
        return false;
    }
    const seleccionada = new Date(val + 'T00:00:00'); // Convierte string a fecha.
    const hoy = new Date(); // Obtiene la fecha actual.
    hoy.setHours(0, 0, 0, 0); // Normaliza para comparar solo días.
    if (seleccionada < hoy) {
        _marcarError(campo, 'error-fechaAsignacion', 'La fecha no puede ser anterior a hoy.');
        return false;
    }
    _marcarOk(campo, 'error-fechaAsignacion');
    return true;
}

/* ════════════════════════════════════════════════════════
   VALIDACIÓN MODAL EN TIEMPO REAL
   ════════════════════════════════════════════════════════ */

function validarInputNombreTipo() {
    const input = document.getElementById('inputNombreTipo');
    const spanError = document.getElementById('errorInlineNombreTipo');
    if (!input) return false;
    const val = input.value.trim();

    // Lógica de validación con Regex para caracteres permitidos.
    if (val.length === 0) { /* ... */ return false; }
    if (val.length < 3)   { /* ... */ return false; }
    if (!/^[\p{L}\s\-]+$/u.test(val)) { // Regex: Letras, espacios y guiones solamente.
        /* ... */ return false;
    }
    return true;
}

/* ════════════════════════════════════════════════════════
   INICIALIZACIÓN Y EVENTOS
   ════════════════════════════════════════════════════════ */

document.addEventListener('DOMContentLoaded', function () {
    // Configura los eventos de input, change y submit al cargar el DOM.
    const form = document.getElementById('formAsignarTrabajo');
    if (!form) return;

    form.addEventListener('submit', function (e) { // Valida todo el formulario al presionar Enviar.
        const resultados = [
            validarSelectCultivo(),
            validarSelectUsuario(),
            validarSelectTipo(),
            validarDescripcion(),
            validarFechaAsignacion()
        ];

        const todosValidos = resultados.every(Boolean); // Comprueba si todas las funciones devolvieron true.

        if (!todosValidos) {
            e.preventDefault(); // Detiene el envío del formulario si algo falló.
            const primerError = form.querySelector('.campo-error'); // Encuentra el primer campo fallido.
            if (primerError) {
                primerError.scrollIntoView({ behavior: 'smooth', block: 'center' }); // Centra la vista en el error.
                primerError.focus(); // Enfoca el campo fallido.
            }
        }
    });
});

/* ════════════════════════════════════════════════════════
   MONKEY PATCHING (Sobreescritura de función de script.js)
   ════════════════════════════════════════════════════════ */

(function () {
    const _originalAbrir = window.abrirModalTipoTrabajo; // Guarda la referencia original.
    window.abrirModalTipoTrabajo = function () { // Extiende la función original.
        // Aquí se inyecta lógica de limpieza previa para garantizar un estado limpio en cada uso.
        if (_originalAbrir) _originalAbrir(); 
    };
})();