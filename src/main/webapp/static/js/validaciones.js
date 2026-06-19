/**
 * validaciones.js
 * Módulo central de validación para el sistema de Finca El Paujil.
 */

'use strict'; // Activa el modo estricto para mayor seguridad y evitar errores de sintaxis.

/* =========================================================
   1. FUNCIONES PURAS DE VALIDACIÓN (Lógica centralizada)
   ========================================================= */

// Verifica si un campo es nulo o está compuesto solo por espacios en blanco.
function estaVacio(valor) {
    return valor == null || String(valor).trim() === '';
}

// Valida si la longitud de un texto está dentro de los límites (min, max) definidos.
function enRango(valor, min, max) {
    const len = String(valor).trim().length;
    return len >= min && len <= max;
}

// Usa Regex para validar que solo contenga letras, espacios y acentos.
function esNombreValido(nombre) {
    const regex = /^[A-Za-zÁÉÍÓÚÑÜáéíóúñü\s]+$/;
    return regex.test(String(nombre).trim());
}

// Valida formato de email estándar mediante expresión regular.
function esCorreoValido(correo) {
    const regex = /^[A-Za-z0-9+_.\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$/;
    return regex.test(String(correo).trim());
}

// Asegura que el número de teléfono contenga estrictamente 10 dígitos.
function esTelefonoValido(telefono) {
    return /^\d{10}$/.test(String(telefono).trim());
}

// Verifica complejidad de contraseña: ≥8 chars, mayúscula, minúscula, número y símbolo.
function esContrasenaSegura(pass) {
    const regex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\.\-_*]).{8,}$/;
    return regex.test(String(pass));
}

// Compara dos strings para validar la coincidencia de contraseñas.
function contrasenaCoincide(pass, confirmacion) {
    return pass === confirmacion;
}

// Calcula la edad basándose en la fecha de nacimiento ingresada en formato "yyyy-MM-dd".
function esMayorDeEdad(fechaNacStr) {
    if (estaVacio(fechaNacStr)) return false;
    const nacimiento = new Date(fechaNacStr + 'T00:00:00');
    const hoy        = new Date();
    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const cumpleMes = nacimiento.getMonth() - hoy.getMonth();
    const cumpleDia = nacimiento.getDate()  - hoy.getDate();
    if (cumpleMes > 0 || (cumpleMes === 0 && cumpleDia > 0)) edad--; // Ajuste si aún no cumple años.
    return edad >= 18;
}

// Valida que la fecha indicada no sea anterior al día actual.
function fechaNoEsAnteriorAHoy(fechaStr) {
    if (estaVacio(fechaStr)) return false;
    const fecha = new Date(fechaStr + 'T00:00:00');
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0); // Normaliza para comparar solo fechas (sin horas).
    return fecha >= hoy;
}

// Valida que una fecha fin sea igual o posterior a la fecha inicio.
function fechaFinNoEsAnteriorAInicio(inicioStr, finStr) {
    if (estaVacio(inicioStr) || estaVacio(finStr)) return true;
    const inicio = new Date(inicioStr + 'T00:00:00');
    const fin = new Date(finStr + 'T00:00:00');
    return fin >= inicio;
}

/* =========================================================
   2. HELPERS DE UI (Manipulación del DOM)
   ========================================================= */

// Muestra el mensaje de error junto al campo, creando el elemento si no existe.
function mostrarError(campo, mensaje) {
    campo.classList.add('campo-error'); // Aplica clase CSS visual.
    campo.classList.remove('campo-ok');
    campo.setAttribute('aria-invalid', 'true'); // Mejora la accesibilidad.

    const idError = 'error-' + (campo.id || campo.name);
    let span = document.getElementById(idError);
    if (!span) { // Si no existe el contenedor del mensaje, lo crea dinámicamente.
        span = document.createElement('span');
        span.id        = idError;
        span.className = 'mensaje-error';
        span.setAttribute('role', 'alert');
        const grupo = campo.closest('.register-card__group') || campo.parentNode;
        grupo.insertAdjacentElement('afterend', span);
    }
    span.textContent = mensaje;
    span.style.display = 'block';
}

// Limpia el estado de error y restablece el estilo visual normal.
function limpiarError(campo) {
    campo.classList.remove('campo-error');
    campo.classList.add('campo-ok');
    campo.setAttribute('aria-invalid', 'false');

    const idError = 'error-' + (campo.id || campo.name);
    const span    = document.getElementById(idError);
    if (span) {
        span.textContent = '';
        span.style.display = 'none';
    }
}

// Elimina cualquier indicativo visual de error o éxito.
function resetearCampo(campo) {
    campo.classList.remove('campo-error', 'campo-ok');
    campo.removeAttribute('aria-invalid');
    const idError = 'error-' + (campo.id || campo.name);
    const span    = document.getElementById(idError);
    if (span) {
        span.textContent = '';
        span.style.display = 'none';
    }
}

/* =========================================================
   3. FUNCIONES DE VALIDACIÓN POR CAMPO (Puente Lógica-UI)
   ========================================================= */

// Valida el campo nombre llamando a las funciones puras y controlando la UI.
function validarNombre(campo) {
    const valor = campo.value.trim();
    if (estaVacio(valor)) { mostrarError(campo, 'El nombre completo es obligatorio.'); return false; }
    if (!enRango(valor, 2, 80)) { mostrarError(campo, 'El nombre completo debe tener entre 2 y 80 caracteres.'); return false; }
    if (!esNombreValido(valor)) { mostrarError(campo, 'El nombre no puede contener números.'); return false; }
    limpiarError(campo); return true;
}

// Repite la estructura para correo, teléfono, fecha, contraseña y confirmación.
function validarCorreo(campo) { /* ... lógica similar ... */ }
function validarTelefono(campo) { /* ... lógica similar ... */ }
function validarFechaNacimiento(campo) { /* ... lógica similar ... */ }
function validarContrasena(campo) { /* ... lógica similar ... */ }
function validarConfirmacion(campoConf, campoPass) { /* ... lógica similar ... */ }
function validarSelect(campo, etiqueta) { /* ... lógica similar ... */ }

/* =========================================================
   4. INICIALIZACIÓN Y EVENTOS
   ========================================================= */

// Espera a que el DOM esté listo para adjuntar los listeners.
document.addEventListener('DOMContentLoaded', function () {
    const formRegistro = document.getElementById('formRegistro');
    if (!formRegistro) return;

    // Asignación de listeners (blur para validar cuando el usuario sale del input).
    const cNombre = formRegistro.querySelector('[name="txtNombre"]');
    if (cNombre) cNombre.addEventListener('blur', () => validarNombre(cNombre));

    // Lógica del evento submit (previene envío si hay errores).
    formRegistro.addEventListener('submit', function (e) {
        const resultados = [ /* Array de todas las funciones de validación */ ];
        const todosValidos = resultados.every(Boolean); // Comprueba que todo sea true.

        if (!todosValidos) {
            e.preventDefault(); // Detiene el envío al servidor.
            const primerError = formRegistro.querySelector('.campo-error');
            if (primerError) {
                // Enfoca y centra visualmente el primer campo que tiene error.
                primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                primerError.focus();
            }
        }
    });
});