/* ==========================================================================
   tipos-trabajo.js — Gestión de tipos de trabajo (crear / editar / eliminar)
   ========================================================================== */

'use strict'; // Activa el modo estricto para evitar errores de sintaxis y variables no declaradas.

let _modoModalTipo     = 'crear';   // Define si el modal operará para 'crear' o 'editar'.
let _idTipoEliminar     = null;     // Variable global para almacenar el ID que se va a borrar.
let _nombreTipoEliminar = null;     // Almacena el nombre del tipo para mostrarlo en el mensaje de alerta.

/* ════════════════════════════════════════════════════════
   ABRIR MODALES
   ════════════════════════════════════════════════════════ */

window.abrirModalTipoTrabajo = function () { // Lógica para abrir el modal en modo creación.
    _modoModalTipo = 'crear';               // Cambia el estado interno al modo crear.
    _setTextoTituloModal('Crear nuevo tipo de trabajo'); // Actualiza el título del modal.
    _setValModal('idTipoEditar', '');        // Limpia el campo oculto de ID.
    _setValModal('inputNombreTipo', '');     // Vacía el input de nombre.
    _setTextoContador('0 / 50');            // Resetea el contador de caracteres.
    _limpiarErroresModalTipo();             // Elimina estados de error previos.
    abrirModal('modalTipoTrabajo');         // Llama a la función genérica de apertura.
    _enfocarInputNombre();                  // Pone el foco en el input para escribir de inmediato.
};

window.abrirModalEditarTipo = function (id, nombreActual) { // Lógica para modo edición.
    _modoModalTipo = 'editar';              // Cambia el estado interno al modo editar.
    _setTextoTituloModal('Editar tipo de trabajo'); // Actualiza el título a modo edición.
    _setValModal('idTipoEditar', id);       // Carga el ID del registro a editar.
    _setValModal('inputNombreTipo', nombreActual); // Pre-carga el nombre existente.
    _setTextoContador(nombreActual.length + ' / 50'); // Muestra el conteo de caracteres actual.
    _limpiarErroresModalTipo();             // Limpia validaciones anteriores.
    abrirModal('modalTipoTrabajo');         // Abre el modal.
    setTimeout(function () {                // Temporizador para asegurar que el input sea seleccionable.
        const input = document.getElementById('inputNombreTipo');
        if (input) { input.focus(); input.select(); } // Enfoca y selecciona el texto.
    }, 50);
};

window.abrirModalEliminarTipo = function (id, nombre) { // Prepara el modal de confirmación.
    _idTipoEliminar     = id;               // Guarda el ID del elemento a eliminar.
    _nombreTipoEliminar = nombre;           // Guarda el nombre del elemento a eliminar.
    const textoEl = document.getElementById('textoEliminarTipo'); // Busca el párrafo informativo.
    if (textoEl) textoEl.textContent = '¿Eliminar el tipo de trabajo "' + nombre + '"?'; // Actualiza mensaje.
    const errBox = document.getElementById('errorEliminarTipo'); // Busca la caja de error.
    if (errBox) { errBox.style.display = 'none'; errBox.textContent = ''; } // Resetea error.
    abrirModal('modalConfirmacionTipo');    // Abre modal de eliminación.
};

/* ── Helpers internos de UI ── */
function _setTextoTituloModal(texto) { const el = document.getElementById('tituloModalTipo'); if (el) el.textContent = texto; }
function _setValModal(id, valor) { const el = document.getElementById(id); if (el) el.value = valor; }
function _setTextoContador(texto) { const el = document.getElementById('contadorTipo'); if (el) el.textContent = texto; }

function _enfocarInputNombre() { // Función para asegurar que el cursor esté en el campo correcto.
    setTimeout(function () {
        const input = document.getElementById('inputNombreTipo');
        if (input) input.focus();
    }, 50);
}

function _limpiarErroresModalTipo() { // Función para limpiar la interfaz de errores.
    const errBox    = document.getElementById('errorTipoTrabajo');
    const errInline = document.getElementById('errorInlineNombreTipo');
    const input     = document.getElementById('inputNombreTipo');
    if (errBox)    { errBox.style.display = 'none'; errBox.textContent = ''; }
    if (errInline) { errInline.style.display = 'none'; errInline.textContent = ''; }
    if (input)     { input.classList.remove('campo-error', 'campo-ok'); }
}

/* ════════════════════════════════════════════════════════
   VALIDACIÓN EN VIVO
   ════════════════════════════════════════════════════════ */

function _validarNombreTipo(valor) { // Lógica de validación pura.
    const v = valor.trim(); // Elimina espacios.
    if (v.length === 0)  return 'El nombre no puede estar vacío.';
    if (v.length < 3)    return 'Mínimo 3 caracteres.';
    if (v.length > 50)   return 'Máximo 50 caracteres.';
    if (!/^[\p{L}\s\-]+$/u.test(v)) return 'Solo letras, espacios y guiones.'; // Regex.
    return null; // Retorna null si es válido.
}

document.addEventListener('DOMContentLoaded', function () { // Ejecutar al cargar la página.
    const input     = document.getElementById('inputNombreTipo');
    const contador  = document.getElementById('contadorTipo');
    const errInline = document.getElementById('errorInlineNombreTipo');

    if (input) { // Añade listeners al input para validación en vivo.
        input.addEventListener('input', function () {
            const len = input.value.length;
            if (contador) {
                contador.textContent = len + ' / 50';
                contador.style.color = len > 45 ? '#c0392b' : 'var(--color-text-secondary)';
            }
            const error = _validarNombreTipo(input.value); // Valida en cada pulsación.
            if (error) { // Si hay error, actualiza estilos.
                input.classList.add('campo-error');
                input.classList.remove('campo-ok');
                if (errInline) { errInline.textContent = error; errInline.style.display = 'inline'; }
            } else { // Si es correcto, actualiza estilos.
                input.classList.remove('campo-error');
                input.classList.add('campo-ok');
                if (errInline) { errInline.textContent = ''; errInline.style.display = 'none'; }
            }
        });

        input.addEventListener('keydown', function (e) { // Permite enviar con la tecla Enter.
            if (e.key === 'Enter') { e.preventDefault(); ejecutarGuardarTipo(); }
        });
    }
});

/* ════════════════════════════════════════════════════════
   GUARDAR (CREAR / EDITAR)
   ════════════════════════════════════════════════════════ */

window.ejecutarGuardarTipo = function () { // Función principal de guardado.
    const input  = document.getElementById('inputNombreTipo');
    const errBox = document.getElementById('errorTipoTrabajo');
    const nombre = input ? input.value.trim() : '';

    function mostrarError(msg) { // Helper para mostrar errores en el modal.
        if (errBox) { errBox.textContent = msg; errBox.style.display = 'block'; }
    }

    const errorValidacion = _validarNombreTipo(nombre);
    if (errorValidacion) { mostrarError(errorValidacion); return; } // Valida final.

    const params = new URLSearchParams(); // Crea parámetros para enviar al Servlet.
    params.append('nombreTipo', nombre);

    if (_modoModalTipo === 'editar') { // Determina la acción según el modo.
        const id = document.getElementById('idTipoEditar') ? document.getElementById('idTipoEditar').value : '';
        if (!id) { mostrarError('No se pudo determinar el tipo a editar.'); return; }
        params.append('accion', 'editarTipo');
        params.append('id', id);
    } else {
        params.append('accion', 'crearTipo');
    }

    fetch((window._ctxPath || '') + '/ServletTrabajo', { // Envía datos mediante AJAX.
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
    .then(function (res) { return res.json(); }) // Procesa la respuesta JSON.
    .then(function (data) {
        if (!data.ok) { mostrarError(data.mensaje || 'Error al guardar.'); return; }
        _refrescarTablaTipos(data.tipos); // Si tiene éxito, refresca la tabla.
        cerrarModal('modalTipoTrabajo'); // Cierra el modal.
    })
    .catch(function () { mostrarError('Error de conexión.'); }); // Maneja error de red.
};

/* ════════════════════════════════════════════════════════
   ELIMINAR
   ════════════════════════════════════════════════════════ */

window.ejecutarEliminacionTipo = function () {
    if (!_idTipoEliminar) return; // Validación de ID.
    const params = new URLSearchParams();
    params.append('accion', 'eliminarTipo');
    params.append('id', _idTipoEliminar);

    fetch((window._ctxPath || '') + '/ServletTrabajo', { // Petición POST.
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params.toString()
    })
    .then(function (res) { return res.json(); })
    .then(function (data) {
        if (!data.ok) { // Manejo de errores de negocio.
            const errBox = document.getElementById('errorEliminarTipo');
            if (errBox) { errBox.textContent = data.mensaje; errBox.style.display = 'block'; }
            return;
        }
        _refrescarTablaTipos(data.tipos); // Refresca UI tras eliminar.
        cerrarModal('modalConfirmacionTipo');
        _idTipoEliminar = null; // Resetea ID.
    })
    .catch(function () { /* Manejo error red */ });
};

/* ════════════════════════════════════════════════════════
   REFRESCAR UI (Sincronización central)
   ════════════════════════════════════════════════════════ */

function _refrescarTablaTipos(tipos) { // Sincroniza tabla y selects.
    const tbody = document.getElementById('tbodyTipos');
    if (tbody) { // Renderiza filas en la tabla principal.
        if (!tipos || tipos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2">Sin tipos registrados.</td></tr>';
        } else {
            let html = '';
            tipos.forEach(function (t) {
                const nombreEsc = _escHtml(t.nombre);
                html += `<tr><td>${nombreEsc}</td><td>
                    <button class="btn btn--edit" onclick="abrirModalEditarTipo(${t.id}, '${t.nombre}')">Editar</button>
                    <button class="btn btn--delete" onclick="abrirModalEliminarTipo(${t.id}, '${t.nombre}')">Eliminar</button>
                </td></tr>`;
            });
            tbody.innerHTML = html;
        }
    }
    // Sincroniza selects en el form de asignar trabajos.
    const selectForm = document.getElementById('idTipoTrabajo');
    if (selectForm) {
        selectForm.innerHTML = '<option value="">— Seleccione —</option>';
        tipos.forEach(function (t) {
            const opt = document.createElement('option');
            opt.value = t.id; opt.textContent = t.nombre;
            selectForm.appendChild(opt);
        });
    }
    // Sincronización modular.
    if (typeof window.refreshFiltroTipoSelect === 'function') {
        window.refreshFiltroTipoSelect(tipos);
    }
}
function _escHtml(str) { return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;'); }