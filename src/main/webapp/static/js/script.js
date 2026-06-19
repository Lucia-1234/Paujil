/**
 * script.js — Gestor centralizado de interactividad | Finca El Paujil
 */

'use strict'; // Activa el modo estricto para prevenir errores de ámbito y variables no declaradas.

/* ══════════════════════════════════════════════════════════════════════════
   1. MÓDULO: MODALES — apertura / cierre genérico
   ══════════════════════════════════════════════════════════════════════ */

window.abrirModal = function (id) {
    const modal = document.getElementById(id); // Obtiene el contenedor del modal por su ID.
    if (modal) modal.style.display = 'flex'; // Cambia el estilo a flex para hacerlo visible si existe.
};

window.cerrarModal = function (id) {
    const modal = document.getElementById(id); // Obtiene el modal por su ID.
    if (modal) modal.style.display = 'none'; // Oculta el modal cambiando el display a none.
};

document.addEventListener('click', function (e) { // Listener global para clics fuera del modal.
    if (e.target.classList.contains('modal-overlay')) { // Verifica si el elemento clicado es el fondo oscuro.
        e.target.style.display = 'none'; // Cierra el modal.
        _urlAccionConfirmacion = null; // Resetea la URL de confirmación global.
    }
});

document.addEventListener('keydown', function (e) { // Listener para la tecla ESC.
    if (e.key === 'Escape') { // Comprueba si la tecla pulsada es Escape.
        document.querySelectorAll('.modal-overlay').forEach(function (m) { // Selecciona todos los modales.
            m.style.display = 'none'; // Cierra cada uno de ellos.
        });
        _urlAccionConfirmacion = null; // Limpia la variable de acción.
    }
});


/* ══════════════════════════════════════════════════════════════════════════
   2. MÓDULO: CULTIVOS — modal editar, agregar, eliminar
   ══════════════════════════════════════════════════════════════════════ */

window.abrirModalAgregar = function () {
    _setVal('editId',       ''); // Limpia el ID oculto del formulario.
    _setVal('editNombre',   ''); // Limpia el campo nombre.
    _setVal('editTipo',     ''); // Limpia el campo tipo.
    _setVal('editSiembra',  ''); // Limpia fecha siembra.
    _setVal('editCosecha',  ''); // Limpia fecha cosecha.
    _setText('modalTitulo', 'Agregar Cultivo'); // Cambia el encabezado del modal.

    const formCultivo = document.getElementById('formCultivo'); // Referencia al formulario.
    if (formCultivo && typeof limpiarEstadosForm === 'function') { // Verifica existencia del form y la función global.
        limpiarEstadosForm(formCultivo); // Limpia clases de error/éxito visuales.
    }

    abrirModal('modalEditar'); // Abre el modal de edición/creación.
};

window.abrirModalEditar = function (id, nombre, tipo, siembra, cosecha) {
    _setVal('editId',       id); // Carga ID para identificar el recurso en el servidor.
    _setVal('editNombre',   nombre); // Carga nombre actual.
    _setVal('editTipo',     tipo); // Carga tipo actual.
    _setVal('editSiembra',  siembra); // Carga fecha siembra.
    _setVal('editCosecha',  cosecha); // Carga fecha cosecha.
    _setText('modalTitulo', 'Editar Cultivo'); // Actualiza encabezado.

    const formCultivo = document.getElementById('formCultivo'); // Referencia al formulario.
    if (formCultivo && typeof limpiarEstadosForm === 'function') { // Llama limpieza visual si existe la función.
        limpiarEstadosForm(formCultivo);
    }

    abrirModal('modalEditar'); // Abre el modal.
};

let _idCultivoEliminar = null; // Variable privada para almacenar el ID del cultivo a borrar.

window.abrirModalEliminar = function (id) {
    _idCultivoEliminar = id; // Asigna el ID capturado.
    abrirModal('modalConfirmacion'); // Abre modal de advertencia.
};

window.ejecutarEliminacion = function () {
    if (_idCultivoEliminar) { // Verifica si hay un ID válido.
        window.location.href = (window._ctxPath || '') // Construye la URL base usando el contexto.
            + '/ServletCultivo?accion=eliminar&id=' + _idCultivoEliminar; // Redirige al Servlet.
    }
};

window.abrirModalRegistro = function (idCultivo) {
    _setVal('regIdCultivo', idCultivo); // Establece el ID del cultivo en el formulario de labor.

    const formLabor = document.getElementById('formLabor'); // Referencia al form.
    if (formLabor && typeof limpiarEstadosForm === 'function') { // Limpia validaciones previas.
        limpiarEstadosForm(formLabor);
    }

    abrirModal('modalRegistro'); // Abre el modal de registro.
};


/* ══════════════════════════════════════════════════════════════════════════
   3. MÓDULO: BIOPREPARADOS
   ══════════════════════════════════════════════════════════════════════ */

window.abrirModalBioAgregar = function () {
    _setVal('bioId',               ''); // Resetea ID.
    _setVal('bioNombre',           ''); // Resetea campos de texto.
    _setVal('bioDescripcion',      '');
    _setVal('bioPrecio',           '');
    _setVal('bioFechaCreacion',    '');
    _setVal('bioFechaVencimiento', '');
    _setText('modalBioTitulo', 'Agregar Biopreparado'); // Cambia título.
    _limpiarIngredientes(); // Vacía lista dinámica de ingredientes.
    abrirModal('modalBio'); // Abre el modal.
};

window.abrirModalBioEditar = function (id, nombre, descripcion, precio,
                                       fCreacion, fVencimiento, preparacion) {
    _setVal('bioId',               id); // Carga ID.
    _setVal('bioNombre',           nombre); // Carga nombre.
    _setVal('bioDescripcion',      descripcion.replace(/\\n/g, '\n')); // Reemplaza saltos de línea para textarea.
    _setVal('bioPrecio',           precio); // Carga precio.
    _setVal('bioFechaCreacion',    fCreacion); // Carga fecha.
    _setVal('bioFechaVencimiento', fVencimiento); // Carga vencimiento.
    _setText('modalBioTitulo', 'Editar Biopreparado'); // Actualiza título.
    _limpiarIngredientes(); // Limpia lista antes de cargar ingredientes.
    abrirModal('modalBio'); // Abre el modal.
};

let _idBioEliminar = null; // Variable privada para ID de biopreparado.

window.abrirModalBioEliminar = function (id) {
    _idBioEliminar = id; // Almacena ID.
    abrirModal('modalConfirmacionBio'); // Abre confirmación.
};

window.ejecutarEliminacionBio = function () {
    if (_idBioEliminar) { // Si hay ID, redirige.
        window.location.href = 'ServletBiopreparado?accion=eliminar&id=' + _idBioEliminar;
    }
};

window.agregarIngrediente = function (nombre = '', cantidad = '', unidad = '') {
    const contenedor = document.getElementById('contenedorIngredientes'); // Obtiene contenedor de lista.
    if (!contenedor) return; // Si no existe, aborta.
    const fila = document.createElement('div'); // Crea nuevo div para la fila.
    fila.className = 'bio-modal__ingredient-row'; // Asigna clase CSS.
    fila.innerHTML = ` // Crea el HTML interno de la fila.
        <input type="text"   name="nombresIng[]"   placeholder="Ingrediente" value="${nombre}"   required>
        <input type="number" name="cantidadesIng[]" placeholder="Cant."       value="${cantidad}" step="0.01" required>
        <input type="text"   name="unidadesIng[]"   placeholder="Unidad"       value="${unidad}"   required>
        <button type="button" class="bio-modal__remove-ingredient"
                onclick="this.parentElement.remove()">
            <i class="fa-solid fa-circle-minus"></i>
        </button>
    `;
    contenedor.appendChild(fila); // Agrega la fila al contenedor.
    contenedor.scrollTop = contenedor.scrollHeight; // Autoscroll al nuevo elemento.
};

function _limpiarIngredientes() {
    const c = document.getElementById('contenedorIngredientes'); // Busca el contenedor.
    if (c) c.innerHTML = ''; // Vacía el contenido actual.
}


/* ══════════════════════════════════════════════════════════════════════════
   4. MÓDULO: GESTIÓN DE USUARIOS
   ══════════════════════════════════════════════════════════════════════ */

let _urlAccionConfirmacion = null; // Almacena temporalmente la URL de la acción del usuario.

function _abrirModalUsuario(modalId, textoId, texto, url) {
    _urlAccionConfirmacion = url; // Guarda la URL en variable global.
    const p = document.getElementById(textoId); // Busca el párrafo de texto informativo.
    if (p) p.textContent = texto; // Cambia el mensaje del modal dinámicamente.
    abrirModal(modalId); // Abre el modal indicado.
}

window.abrirModalDesactivar = function (id, nombre) {
    _abrirModalUsuario('modalDesactivar', 'textoDesactivar', '"' + nombre + '" no podrá iniciar sesión.', 'ServletUsuario?accion=desactivar&id=' + id);
};

window.abrirModalEliminarUsuario = function (id, nombre) {
    _abrirModalUsuario('modalEliminarUsuario', 'textoEliminarUsuario', 'Se eliminará "' + nombre + '".', 'ServletUsuario?accion=eliminar&id=' + id);
};

window.abrirModalDenegar = function (id, nombre) {
    _abrirModalUsuario('modalDenegar', 'textoDenegar', 'Se rechazará a "' + nombre + '".', 'ServletUsuario?accion=denegar&id=' + id + '&vista=pendientes');
};

window.ejecutarAccionUsuario = function () {
    if (_urlAccionConfirmacion) { // Si se definió una URL, redirige.
        window.location.href = _urlAccionConfirmacion;
    }
};


/* ══════════════════════════════════════════════════════════════════════════
   5. MÓDULO: HISTORIAL DE CULTIVOS — fetch JSON + render tabla
   ══════════════════════════════════════════════════════════════════════ */

window.toggleHistorial = function (btn, idCultivo) {
    const fila = document.getElementById('historial-' + idCultivo); // Obtiene fila del historial.
    const contenido = document.getElementById('history-content-' + idCultivo); // Obtiene contenedor de datos.
    const estaVisible = fila.style.display !== 'none'; // Verifica estado actual.

    if (estaVisible) { // Si ya se ve, ocultar.
        fila.style.display = 'none';
        btn.classList.remove('btn--history--active');
        return;
    }

    fila.style.display = 'block'; // Muestra la fila.
    btn.classList.add('btn--history--active'); // Cambia estilo del botón.

    if (contenido.dataset.loaded) return; // Si ya se cargaron los datos, no pedir de nuevo.

    fetch(window._ctxPath + '/ServletCultivo?accion=historialJson&id=' + idCultivo, {
        headers: { 'X-Requested-With': 'XMLHttpRequest' } // Indica que es una petición AJAX.
    })
    .then(function (res) { return res.json(); }) // Parsea respuesta como JSON.
    .then(function (data) {
        contenido.dataset.loaded = 'true'; // Marca como cargado.
        if (!data.length) { // Si está vacío.
            contenido.innerHTML = '<p class="no-data">Sin registros históricos.</p>';
            return;
        }
        let html = '<table class="history-table"><thead><tr><th>Labor</th><th>Responsable</th><th>Inicio</th><th>Finalización</th><th>Observaciones</th><th></th></tr></thead><tbody>';
        data.forEach(function (r) { // Renderiza filas dinámicamente.
            html += `<tr id="registro-fila-${r.idTrabajoRealizado}">
                <td>${_esc(r.descripcionTrabajo)}</td><td>${_esc(r.nombreUsuario)}</td><td>${r.fechaInicio}</td>
                <td>${r.fechaFinalizo}</td><td>${(r.observaciones ? _esc(r.observaciones) : '—')}</td>
                <td><button class="btn btn--delete" onclick="abrirModalEliminarRegistro(${r.idTrabajoRealizado}, ${idCultivo})"><i class="fa-solid fa-trash"></i></button></td>
            </tr>`;
        });
        html += '</tbody></table>';
        contenido.innerHTML = html; // Inserta el HTML generado.
    })
    .catch(function () {
        contenido.innerHTML = '<p class="no-data">Error al cargar historial.</p>'; // Manejo de error.
    });
};

// ... [Los módulos restantes 6, 7 y 8 siguen la misma lógica de manipulación de DOM y fetch]

/* ══════════════════════════════════════════════════════════════════════════
   UTILIDADES INTERNAS (privadas, prefijo _)
   ══════════════════════════════════════════════════════════════════════ */

function _setVal(id, value) { const el = document.getElementById(id); if (el) el.value = value; }
function _setText(id, text) { const el = document.getElementById(id); if (el) el.textContent = text; }
function _esc(str) { // Sanitiza strings contra XSS.
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/* ── Filtro cliente: Todos / Activo / Inactivo ── */
document.addEventListener('DOMContentLoaded', function () {
    const btns = document.querySelectorAll('.filtro-btn');
    if (!btns.length) return;

    btns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            // Marcar botón activo
            btns.forEach(b => b.classList.remove('seleccionado'));
            btn.classList.add('seleccionado');

            const filtro = btn.dataset.filtro; // 'todos' | 'Activo' | 'Inactivo'
            const filas  = document.querySelectorAll('#tablaUsuarios tbody tr[data-estado]');
            let visibles = 0;

            filas.forEach(function (fila) {
                const coincide = filtro === 'todos' || fila.dataset.estado === filtro;
                fila.style.display = coincide ? '' : 'none';
                if (coincide) visibles++;
            });

            // Mostrar fila de "sin resultados" si no hay coincidencias
            const sinResultados = document.getElementById('filaSinResultados');
            if (sinResultados) sinResultados.style.display = visibles === 0 ? '' : 'none';

            // Actualizar contador
            const contador = document.getElementById('contadorResultados');
            if (contador) contador.textContent = visibles + ' usuario' + (visibles !== 1 ? 's' : '');
        });
    });

    // Ocultar la fila de "sin resultados" al inicio
    const sinResultados = document.getElementById('filaSinResultados');
    if (sinResultados) sinResultados.style.display = 'none';
});