/* ══════════════════════════════════════════════════════════════════════════
   MÓDULO: LOTES — modal editar, agregar, eliminar
   ══════════════════════════════════════════════════════════════════════ */

window.abrirModalAgregarLote = function () {
    _setVal('loteId',     ''); // Limpia el ID oculto para que el servlet opere como INSERT.
    _setVal('loteNombre', ''); // Limpia el campo nombre.
    _setText('modalTituloLote', 'Agregar Lote'); // Cambia el encabezado del modal.
    const formLote = document.getElementById('formLote'); // Referencia al formulario.
    if (formLote && typeof limpiarEstadosForm === 'function') { // Verifica existencia del form y la función global.
        limpiarEstadosForm(formLote); // Limpia clases de error/éxito visuales.
    }
    abrirModal('modalEditarLote'); // Abre el modal de edición/creación.
};

window.abrirModalEditarLote = function (id, nombre) {
    _setVal('loteId',     id); // Carga ID para identificar el recurso en el servidor (UPDATE).
    _setVal('loteNombre', nombre); // Carga nombre actual.
    _setText('modalTituloLote', 'Editar Lote'); // Actualiza el encabezado.
    const formLote = document.getElementById('formLote'); // Referencia al formulario.
    if (formLote && typeof limpiarEstadosForm === 'function') { // Llama limpieza visual si existe la función.
        limpiarEstadosForm(formLote);
    }
    abrirModal('modalEditarLote'); // Abre el modal.
};

let _idLoteEliminar = null; // Variable privada para almacenar el ID del lote a borrar.
window.abrirModalEliminarLote = function (id) {
    _idLoteEliminar = id; // Asigna el ID capturado.
    abrirModal('modalConfirmacionLote'); // Abre modal de advertencia.
};

window.ejecutarEliminacionLote = function () {
    if (_idLoteEliminar) { // Verifica si hay un ID válido.
        window.location.href = (window._ctxPath || '') // Construye la URL base usando el contexto.
            + '/ServletLote?accion=eliminar&id=' + _idLoteEliminar; // Redirige al Servlet con acción eliminar.
    }
};