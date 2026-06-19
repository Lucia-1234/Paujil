'use strict'; // Activa el modo estricto para evitar errores de sintaxis comunes.

/* ==========================================================================
   filtros-trabajos.js — Filtro de tarjetas por estado y por tipo
   ========================================================================== */

(function () { // Encapsula el código en una función autoejecutable (IIFE) para no contaminar el scope global.
    const estadoBtns = document.querySelectorAll('.filtro-estado-btn'); // Selecciona todos los botones de filtro por estado.
    const tipoBtns   = document.querySelectorAll('.filtro-tipo-btn'); // Selecciona todos los botones de filtro por tipo.

    let estadoActual = 'todos'; // Variable de control para el estado activo (inicializa en 'todos').
    let tipoActual   = 'todos'; // Variable de control para el tipo activo (inicializa en 'todos').

    /* Referencias a elementos del DOM relacionados con el acordeón de tipos */
    const acordeon       = document.getElementById('acordeonTipos'); // Contenedor principal del acordeón.
    const acordeonHeader = document.getElementById('acordeonHeader'); // Encabezado clicable.
    const acordeonBody   = document.getElementById('acordeonBody'); // Cuerpo desplegable.
    const acordeonChevron = document.getElementById('acordeonChevron'); // Icono de flecha/chevron.

    /* Referencias al panel principal de tarjetas */
    const panelTrabajos     = document.getElementById('panelTrabajos'); // Contenedor general de tarjetas.
    const jobList           = document.getElementById('jobList'); // Lista donde residen las 'job-card'.
    const sinResultadosMsg  = document.getElementById('sinResultadosFiltro'); // Mensaje de "sin coincidencias".

    /* Referencia al contenedor de filtros de tipo (punto de inserción para el acordeón) */
    const filtrosTipo = document.getElementById('filtrosTipo');

    /* ── Mover el acordeón en el DOM para que quede justo debajo de los filtros ── */
    if (acordeon && filtrosTipo && filtrosTipo.parentNode) { // Verifica la existencia de todos los elementos necesarios.
        filtrosTipo.parentNode.insertBefore(acordeon, filtrosTipo.nextSibling); // Inserta el acordeón tras los filtros de tipo.
    }

    /* Configuración inicial: Ocultar acordeón al cargar la página */
    if (acordeon) {
        acordeon.style.display = 'none'; // Aplica estilo inline para ocultar el elemento.
    }

    /** Función para mostrar visualmente el acordeón usando clases de estado */
    function mostrarAcordeon() {
        if (!acordeon) return; // Validación de existencia.
        acordeon.style.display = ''; // Limpia el estilo display (lo hace visible).
        acordeonBody.classList.add('abierto'); // Activa la clase CSS de apertura para el cuerpo.
        acordeonHeader.classList.add('abierto'); // Activa la clase CSS para el encabezado.
        acordeonChevron.classList.add('abierto'); // Rota el icono chevron.
    }

    /** Función para ocultar el acordeón y remover clases de estado */
    function ocultarAcordeon() {
        if (!acordeon) return; // Validación de existencia.
        acordeon.style.display = 'none'; // Oculta el contenedor.
        acordeonBody.classList.remove('abierto'); // Quita clase de apertura.
        acordeonHeader.classList.remove('abierto'); // Quita clase de encabezado.
        acordeonChevron.classList.remove('abierto'); // Restaura icono.
    }

    /** Función principal que compara atributos data-* con los estados actuales */
    function aplicarFiltros() {
        if (!jobList) return; // Validación de existencia de la lista.
        const cards = jobList.querySelectorAll('.job-card'); // Selecciona todas las tarjetas disponibles.
        let visibles = 0; // Contador para controlar si hay resultados.

        cards.forEach(function (card) { // Itera sobre cada tarjeta.
            const okEstado = estadoActual === 'todos' // Si estado es 'todos', cumple.
                || card.getAttribute('data-estado') === estadoActual; // O si el atributo coincide.
            const okTipo = tipoActual === 'todos' // Si tipo es 'todos', cumple.
                || card.getAttribute('data-tipo') === tipoActual; // O si el atributo coincide.
            const visible = okEstado && okTipo; // La tarjeta se muestra solo si ambos cumplen.
            card.style.display = visible ? '' : 'none'; // Aplica estilo de visualización.
            if (visible) visibles++; // Incrementa contador si se muestra.
        });

        /* Manejo del mensaje de "sin resultados" */
        if (sinResultadosMsg) {
            sinResultadosMsg.style.display = (cards.length > 0 && visibles === 0) ? '' : 'none'; // Muestra si no hay coincidencias pero hay tarjetas totales.
        }
    }

    /* Listener: Lógica para los botones de estado */
    estadoBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            estadoBtns.forEach(function (b) { b.classList.remove('seleccionado'); }); // Resetea estilo de botones.
            btn.classList.add('seleccionado'); // Marca el botón clicado.
            estadoActual = btn.getAttribute('data-filtro'); // Actualiza el estado global.
            aplicarFiltros(); // Ejecuta el filtrado.
        });
    });

    /* Listener: Lógica para los botones de tipo (con comportamiento de acordeón) */
    tipoBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            const tipoClickeado = btn.getAttribute('data-tipo'); // Identifica el tipo seleccionado.

            /* Lógica de Toggle: Si el usuario pulsa "Todos" y ya está activo, cierra el acordeón */
            if (tipoClickeado === 'todos' && tipoActual === 'todos' && acordeon && acordeon.style.display !== 'none') {
                ocultarAcordeon(); // Cierra el acordeón.
                if (panelTrabajos) panelTrabajos.style.display = ''; // Muestra las tarjetas.
                aplicarFiltros(); // Re-aplica filtros actuales.
                return; // Salida anticipada.
            }

            tipoBtns.forEach(function (b) { b.classList.remove('seleccionado'); }); // Resetea botones de tipo.
            btn.classList.add('seleccionado'); // Marca botón activo.
            tipoActual = tipoClickeado; // Actualiza el tipo global.

            if (tipoActual === 'todos') {
                /* Si elige "Todos", despliega el acordeón de categorías y oculta el panel general */
                mostrarAcordeon();
                if (panelTrabajos) panelTrabajos.style.display = 'none';
            } else {
                /* Si elige otro tipo, oculta el acordeón y filtra las tarjetas */
                ocultarAcordeon();
                if (panelTrabajos) panelTrabajos.style.display = '';
                aplicarFiltros();
            }
        });
    });
})();