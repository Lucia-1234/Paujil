/**
 * validaciones.js
 * Módulo de validación frontend — Finca El Paujil
 *
 * Estrategia:
 *  - Funciones puras de validación (sin efectos secundarios).
 *  - Funciones de UI para mostrar/ocultar errores junto al campo.
 *  - Event listeners (change, blur, input, submit) que unen ambas capas.
 *
 * Reglas de negocio:
 *  - Fecha de inicio  >= hoy.
 *  - Fecha de fin     >= fecha de inicio.
 *  - Correo con formato válido.
 *  - Teléfono: exactamente 10 dígitos.
 *  - Contraseña segura: ≥8 chars, mayúscula, minúscula, dígito y símbolo.
 *  - Campos obligatorios no vacíos.
 *  - Nombre: 2–80 caracteres.
 *  - Dirección: 5–120 caracteres.
 */

'use strict';

/* =========================================================
   1. FUNCIONES PURAS DE VALIDACIÓN
   ========================================================= */

/**
 * Devuelve true si el valor es null, undefined o solo espacios.
 * @param {string|null|undefined} valor
 * @returns {boolean}
 */
function estaVacio(valor) {
    return valor == null || String(valor).trim() === '';
}

/**
 * Valida longitud mínima y máxima de un texto (ignora espacios extremos).
 * @param {string} valor
 * @param {number} min
 * @param {number} max
 * @returns {boolean}
 */
function enRango(valor, min, max) {
    const len = String(valor).trim().length;
    return len >= min && len <= max;
}

/**
 * Valida formato de correo electrónico.
 * Regex alineada con la del backend (validador.java → esCorreoValido).
 * @param {string} correo
 * @returns {boolean}
 */
function esCorreoValido(correo) {
    const regex = /^[A-Za-z0-9+_.\-]+@[A-Za-z0-9.\-]+\.[A-Za-z]{2,}$/;
    return regex.test(String(correo).trim());
}

/**
 * Valida que el teléfono tenga exactamente 10 dígitos.
 * Coincide con la validación del backend (\\d{10}).
 * @param {string} telefono
 * @returns {boolean}
 */
function esTelefonoValido(telefono) {
    return /^\d{10}$/.test(String(telefono).trim());
}

/**
 * Contraseña segura: ≥8 caracteres, al menos una mayúscula, una minúscula,
 * un dígito y un símbolo. Sincronizado con validador.java → esContrasenaSegura.
 * @param {string} pass
 * @returns {boolean}
 */
function esContrasenaSegura(pass) {
    const regex = /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\.\-_*]).{8,}$/;
    return regex.test(String(pass));
}

/**
 * Verifica que dos contraseñas sean idénticas.
 * @param {string} pass
 * @param {string} confirmacion
 * @returns {boolean}
 */
function contrasenaCoincide(pass, confirmacion) {
    return pass === confirmacion;
}

/**
 * Valida que una fecha (string "yyyy-MM-dd") sea >= hoy.
 * Regla de negocio: fecha de inicio no puede ser anterior a la fecha actual.
 * @param {string} fechaStr
 * @returns {boolean}
 */
function fechaNoEsAnteriorAHoy(fechaStr) {
    if (estaVacio(fechaStr)) return false;
    const fecha = new Date(fechaStr + 'T00:00:00');  // fuerza interpretación local
    const hoy   = new Date();
    hoy.setHours(0, 0, 0, 0);
    return fecha >= hoy;
}

/**
 * Valida que fechaFin sea >= fechaInicio.
 * Regla de negocio: la fecha de finalización no puede ser anterior a la de inicio.
 * @param {string} fechaInicioStr
 * @param {string} fechaFinStr
 * @returns {boolean}
 */
function fechaFinNoEsAnteriorAInicio(fechaInicioStr, fechaFinStr) {
    if (estaVacio(fechaInicioStr) || estaVacio(fechaFinStr)) return false;
    const inicio = new Date(fechaInicioStr + 'T00:00:00');
    const fin    = new Date(fechaFinStr    + 'T00:00:00');
    return fin >= inicio;
}

/**
 * Verifica que el usuario sea mayor de edad (>= 18 años).
 * Espejo de validador.java → esMayorDeEdad.
 * @param {string} fechaNacStr  Formato "yyyy-MM-dd"
 * @returns {boolean}
 */
function esMayorDeEdad(fechaNacStr) {
    if (estaVacio(fechaNacStr)) return false;
    const nacimiento = new Date(fechaNacStr + 'T00:00:00');
    const hoy        = new Date();
    // Calcula la edad restando años y ajustando si aún no llegó el cumpleaños
    let edad = hoy.getFullYear() - nacimiento.getFullYear();
    const cumpleMes = nacimiento.getMonth() - hoy.getMonth();
    const cumpleDia = nacimiento.getDate()  - hoy.getDate();
    if (cumpleMes > 0 || (cumpleMes === 0 && cumpleDia > 0)) edad--;
    return edad >= 18;
}


/* =========================================================
   2. HELPERS DE UI: mostrar y limpiar errores
   ========================================================= */

/**
 * Muestra un mensaje de error debajo del campo indicado.
 * Si ya existe un span de error para ese campo, lo reutiliza.
 * Añade la clase CSS "campo-error" al input/select para estilo visual.
 *
 * @param {HTMLElement} campo   - El input o select afectado.
 * @param {string}      mensaje - Texto descriptivo del error.
 */
function mostrarError(campo, mensaje) {
    campo.classList.add('campo-error');
    campo.classList.remove('campo-ok');
    campo.setAttribute('aria-invalid', 'true');

    // Busca o crea el span de error asociado al campo
    const idError = 'error-' + (campo.id || campo.name);
    let span = document.getElementById(idError);
    if (!span) {
        span = document.createElement('span');
        span.id        = idError;
        span.className = 'mensaje-error';
        span.setAttribute('role', 'alert');
        // Inserta el span inmediatamente después del campo (o de su contenedor de grupo)
        const grupo = campo.closest('.register-card__group') || campo.parentNode;
        grupo.insertAdjacentElement('afterend', span);
    }
    span.textContent = mensaje;
    span.style.display = 'block';
}

/**
 * Elimina el estado de error de un campo y oculta su mensaje.
 * Añade la clase CSS "campo-ok" para retroalimentación positiva.
 *
 * @param {HTMLElement} campo
 */
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

/**
 * Limpia los estados visuales sin añadir "campo-ok"
 * (usado al resetear un campo dependiente).
 *
 * @param {HTMLElement} campo
 */
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
   3. FUNCIONES DE VALIDACIÓN POR CAMPO (unen lógica + UI)
   ========================================================= */

/**
 * Valida un campo de texto obligatorio con rango de caracteres.
 * @param {HTMLInputElement} campo
 * @param {string}           etiqueta  - Nombre legible del campo para el mensaje.
 * @param {number}           min
 * @param {number}           max
 * @returns {boolean}
 */
function validarTextoRequerido(campo, etiqueta, min, max) {
    const valor = campo.value.trim();
    if (estaVacio(valor)) {
        mostrarError(campo, `${etiqueta} es obligatorio.`);
        return false;
    }
    if (!enRango(valor, min, max)) {
        mostrarError(campo, `${etiqueta} debe tener entre ${min} y ${max} caracteres.`);
        return false;
    }
    limpiarError(campo);
    return true;
}

/**
 * Valida el campo de correo electrónico.
 * @param {HTMLInputElement} campo
 * @returns {boolean}
 */
function validarCorreo(campo) {
    const valor = campo.value.trim();
    if (estaVacio(valor)) {
        mostrarError(campo, 'El correo electrónico es obligatorio.');
        return false;
    }
    if (!esCorreoValido(valor)) {
        mostrarError(campo, 'Ingresa un correo válido (ej. usuario@dominio.com).');
        return false;
    }
    limpiarError(campo);
    return true;
}

/**
 * Valida el campo de teléfono (exactamente 10 dígitos).
 * @param {HTMLInputElement} campo
 * @returns {boolean}
 */
function validarTelefono(campo) {
    const valor = campo.value.trim();
    if (estaVacio(valor)) {
        mostrarError(campo, 'El teléfono es obligatorio.');
        return false;
    }
    if (!esTelefonoValido(valor)) {
        mostrarError(campo, 'El teléfono debe tener exactamente 10 dígitos numéricos.');
        return false;
    }
    limpiarError(campo);
    return true;
}

/**
 * Valida la fecha de nacimiento: obligatoria y mayor de edad.
 * @param {HTMLInputElement} campo
 * @returns {boolean}
 */
function validarFechaNacimiento(campo) {
    const valor = campo.value;
    if (estaVacio(valor)) {
        mostrarError(campo, 'La fecha de nacimiento es obligatoria.');
        return false;
    }
    if (!esMayorDeEdad(valor)) {
        mostrarError(campo, 'Debes ser mayor de 18 años para registrarte.');
        return false;
    }
    limpiarError(campo);
    return true;
}

/**
 * Valida la fecha de inicio de un evento/actividad.
 * Regla: no puede ser anterior a la fecha actual.
 * @param {HTMLInputElement} campo
 * @returns {boolean}
 */
function validarFechaInicio(campo) {
    const valor = campo.value;
    if (estaVacio(valor)) {
        mostrarError(campo, 'La fecha de inicio es obligatoria.');
        return false;
    }
    if (!fechaNoEsAnteriorAHoy(valor)) {
        mostrarError(campo, 'La fecha de inicio no puede ser anterior a hoy.');
        return false;
    }
    limpiarError(campo);
    return true;
}

/**
 * Valida la fecha de fin de un evento/actividad.
 * Regla: no puede ser anterior a la fecha de inicio seleccionada.
 * @param {HTMLInputElement} campoFin
 * @param {HTMLInputElement} campoInicio
 * @returns {boolean}
 */
function validarFechaFin(campoFin, campoInicio) {
    const valorFin    = campoFin.value;
    const valorInicio = campoInicio ? campoInicio.value : '';

    if (estaVacio(valorFin)) {
        mostrarError(campoFin, 'La fecha de fin es obligatoria.');
        return false;
    }
    if (!estaVacio(valorInicio) && !fechaFinNoEsAnteriorAInicio(valorInicio, valorFin)) {
        mostrarError(campoFin, 'La fecha de fin no puede ser anterior a la fecha de inicio.');
        return false;
    }
    limpiarError(campoFin);
    return true;
}

/**
 * Valida la contraseña principal.
 * @param {HTMLInputElement} campo
 * @returns {boolean}
 */
function validarContrasena(campo) {
    const valor = campo.value;
    if (estaVacio(valor)) {
        mostrarError(campo, 'La contraseña es obligatoria.');
        return false;
    }
    if (!esContrasenaSegura(valor)) {
        mostrarError(campo,
            'La contraseña debe tener mínimo 8 caracteres, ' +
            'una mayúscula, una minúscula, un número y un símbolo (@#$%^&+=!.-_*).'
        );
        return false;
    }
    limpiarError(campo);
    return true;
}

/**
 * Valida que la confirmación coincida con la contraseña.
 * @param {HTMLInputElement} campoConf
 * @param {HTMLInputElement} campoPass
 * @returns {boolean}
 */
function validarConfirmacion(campoConf, campoPass) {
    const conf = campoConf.value;
    const pass = campoPass ? campoPass.value : '';

    if (estaVacio(conf)) {
        mostrarError(campoConf, 'La confirmación de contraseña es obligatoria.');
        return false;
    }
    if (!contrasenaCoincide(pass, conf)) {
        mostrarError(campoConf, 'Las contraseñas no coinciden.');
        return false;
    }
    limpiarError(campoConf);
    return true;
}

/**
 * Valida un campo select (que tenga un valor seleccionado).
 * @param {HTMLSelectElement} campo
 * @param {string}            etiqueta
 * @returns {boolean}
 */
function validarSelect(campo, etiqueta) {
    if (estaVacio(campo.value)) {
        mostrarError(campo, `${etiqueta} es obligatorio.`);
        return false;
    }
    limpiarError(campo);
    return true;
}


/* =========================================================
   4. INICIALIZACIÓN: formulario de registro de usuario
   ========================================================= */

document.addEventListener('DOMContentLoaded', function () {

    /* --- Formulario de registro de usuario --- */
    const formRegistro = document.getElementById('formRegistro');
    if (!formRegistro) return;  // Sale si el formulario no existe en esta vista

    // Referencias a los campos
    const cNombre   = formRegistro.querySelector('[name="txtNombre"]');
    const cEmail    = formRegistro.querySelector('[name="txtEmail"]');
    const cTelefono = formRegistro.querySelector('[name="txtTelefono"]');
    const cFecha    = formRegistro.querySelector('[name="txtFechaNacimiento"]');
    const cDir      = formRegistro.querySelector('[name="txtDireccion"]');
    const cRol      = formRegistro.querySelector('[name="txtRol"]');
    const cPass     = formRegistro.querySelector('[name="txtContrasena"]');
    const cConf     = formRegistro.querySelector('[name="txtConfirmarContrasena"]');

    // Asigna id a los campos que no lo tienen para que mostrarError pueda crear el span
    const campos = [
        [cNombre,   'txtNombre'],
        [cEmail,    'txtEmail'],
        [cTelefono, 'txtTelefono'],
        [cFecha,    'txtFechaNacimiento'],
        [cDir,      'txtDireccion'],
        [cRol,      'txtRol'],
        [cPass,     'txtContrasena'],
        [cConf,     'txtConfirmarContrasena'],
    ];
    campos.forEach(([el, id]) => { if (el && !el.id) el.id = id; });

    /* ---------- Listeners de blur (al salir del campo) ---------- */

    if (cNombre)
        cNombre.addEventListener('blur', () =>
            validarTextoRequerido(cNombre, 'El nombre completo', 2, 80));

    if (cEmail)
        cEmail.addEventListener('blur', () => validarCorreo(cEmail));

    if (cTelefono) {
        // Solo permite ingresar dígitos (input en tiempo real)
        cTelefono.addEventListener('input', function () {
            this.value = this.value.replace(/\D/g, '').slice(0, 10);
        });
        cTelefono.addEventListener('blur', () => validarTelefono(cTelefono));
    }

    if (cFecha)
        cFecha.addEventListener('change', () => validarFechaNacimiento(cFecha));

    if (cDir)
        cDir.addEventListener('blur', () =>
            validarTextoRequerido(cDir, 'La dirección', 5, 120));

    if (cRol)
        cRol.addEventListener('change', () => validarSelect(cRol, 'El rol'));

    if (cPass) {
        cPass.addEventListener('blur', () => validarContrasena(cPass));
        // Si la confirmación ya tiene texto, re-valida al cambiar la contraseña
        cPass.addEventListener('input', () => {
            if (cConf && cConf.value.length > 0)
                validarConfirmacion(cConf, cPass);
        });
    }

    if (cConf)
        cConf.addEventListener('blur', () => validarConfirmacion(cConf, cPass));

    /* ---------- Validación completa en submit ---------- */

    formRegistro.addEventListener('submit', function (e) {
        // Ejecuta todos los validadores y acumula resultados
        const resultados = [
            validarTextoRequerido(cNombre,   'El nombre completo', 2, 80),
            validarCorreo(cEmail),
            validarTelefono(cTelefono),
            validarFechaNacimiento(cFecha),
            validarTextoRequerido(cDir, 'La dirección', 5, 120),
            validarSelect(cRol, 'El rol'),
            validarContrasena(cPass),
            validarConfirmacion(cConf, cPass),
        ];

        const todosValidos = resultados.every(Boolean);

        if (!todosValidos) {
            e.preventDefault();  // Bloquea el envío al servidor

            // Desplaza al primer campo con error para que el usuario lo vea
            const primerError = formRegistro.querySelector('.campo-error');
            if (primerError) {
                primerError.scrollIntoView({ behavior: 'smooth', block: 'center' });
                primerError.focus();
            }
        }
        // Si todos son válidos, el formulario se envía normalmente al servlet
    });
});


/* =========================================================
   5. INICIALIZACIÓN: formularios con fechas de inicio/fin
      (reutilizable en cualquier vista que tenga estos campos)
   ========================================================= */

document.addEventListener('DOMContentLoaded', function () {

    const campoInicio = document.getElementById('txtFechaInicio');
    const campoFin    = document.getElementById('txtFechaFin');

    if (!campoInicio || !campoFin) return;  // Solo aplica si existen ambos campos

    // Al cambiar la fecha de inicio: valída inicio y re-valida fin si ya tiene valor
    campoInicio.addEventListener('change', function () {
        validarFechaInicio(campoInicio);
        if (!estaVacio(campoFin.value)) {
            validarFechaFin(campoFin, campoInicio);
        } else {
            // Limpia estado anterior de fin para que no quede marcado como error prematuramente
            resetearCampo(campoFin);
        }
    });

    // Al cambiar la fecha de fin: solo valida fin (inicio ya fue validado)
    campoFin.addEventListener('change', function () {
        validarFechaFin(campoFin, campoInicio);
    });

    // También valida en blur para usuarios que navegan con teclado
    campoInicio.addEventListener('blur', function () {
        validarFechaInicio(campoInicio);
    });
    campoFin.addEventListener('blur', function () {
        validarFechaFin(campoFin, campoInicio);
    });

    // Si el formulario que contiene estos campos tiene un onsubmit propio,
    // se puede llamar directamente:
    //   validarFechaInicio(campoInicio) && validarFechaFin(campoFin, campoInicio)
});