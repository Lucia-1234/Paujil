'use strict';
/* ==========================================================================
   filtros-trabajos.js — Filtro de tarjetas por estado y por tipo
   ========================================================================== */
(function () {
    const estadoBtns = document.querySelectorAll('.filtro-estado-btn');
    const tipoBtns   = document.querySelectorAll('.filtro-tipo-btn');

    let estadoActual = 'todos';
    let tipoActual   = 'todos';

    /* Referencias al acordeón de tipos (definido arriba en el JSP) */
    const acordeon         = document.getElementById('acordeonTipos');
    const acordeonHeader   = document.getElementById('acordeonHeader');
    const acordeonBody     = document.getElementById('acordeonBody');
    const acordeonChevron  = document.getElementById('acordeonChevron');

    /* Panel de "trabajos registrados" */
    const panelTrabajos      = document.getElementById('panelTrabajos');
    const jobList            = document.getElementById('jobList');
    const sinResultadosMsg   = document.getElementById('sinResultadosFiltro');

    /* Contenedor de filtros de tipo (debajo de este se insertará el acordeón) */
    const filtrosTipo = document.getElementById('filtrosTipo');

    /* ── Mover el acordeón en el DOM para que quede debajo de los filtros ── */
    if (acordeon && filtrosTipo && filtrosTipo.parentNode) {
        filtrosTipo.parentNode.insertBefore(acordeon, filtrosTipo.nextSibling);
    }

    /* Oculto por defecto: no debe verse hasta el primer click en "Todos" (tipo) */
    if (acordeon) {
        acordeon.style.display = 'none';
    }

    function mostrarAcordeon() {
        if (!acordeon) return;
        acordeon.style.display = '';
        acordeonBody.classList.add('abierto');
        acordeonHeader.classList.add('abierto');
        acordeonChevron.classList.add('abierto');
    }

    function ocultarAcordeon() {
        if (!acordeon) return;
        acordeon.style.display = 'none';
        acordeonBody.classList.remove('abierto');
        acordeonHeader.classList.remove('abierto');
        acordeonChevron.classList.remove('abierto');
    }

    function aplicarFiltros() {
        if (!jobList) return;
        const cards = jobList.querySelectorAll('.job-card');
        let visibles = 0;

        cards.forEach(function (card) {
            const okEstado = estadoActual === 'todos'
                || card.getAttribute('data-estado') === estadoActual;
            const okTipo = tipoActual === 'todos'
                || card.getAttribute('data-tipo') === tipoActual;
            const visible = okEstado && okTipo;
            card.style.display = visible ? '' : 'none';
            if (visible) visibles++;
        });

        /* Mostrar aviso si el filtro no encontró ninguna coincidencia
           (solo cuando sí hay tarjetas en el DOM; si no hay ninguna,
           ya se muestra el mensaje original "No hay trabajos registrados") */
        if (sinResultadosMsg) {
            sinResultadosMsg.style.display = (cards.length > 0 && visibles === 0) ? '' : 'none';
        }
    }

    estadoBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            estadoBtns.forEach(function (b) { b.classList.remove('seleccionado'); });
            btn.classList.add('seleccionado');
            estadoActual = btn.getAttribute('data-filtro');
            aplicarFiltros();
        });
    });

    tipoBtns.forEach(function (btn) {
        btn.addEventListener('click', function () {
            const tipoClickeado = btn.getAttribute('data-tipo');

            /* Si vuelve a darle click a "Todos" estando ya activo: alterna (toggle) */
            if (tipoClickeado === 'todos' && tipoActual === 'todos' && acordeon && acordeon.style.display !== 'none') {
                ocultarAcordeon();
                if (panelTrabajos) panelTrabajos.style.display = '';
                aplicarFiltros();
                return;
            }

            tipoBtns.forEach(function (b) { b.classList.remove('seleccionado'); });
            btn.classList.add('seleccionado');
            tipoActual = tipoClickeado;

            if (tipoActual === 'todos') {
                /* "Todos": muestra el acordeón (debajo de los filtros) y oculta el panel de tarjetas */
                mostrarAcordeon();
                if (panelTrabajos) panelTrabajos.style.display = 'none';
            } else {
                /* Cualquier otro tipo: oculta el acordeón y muestra las tarjetas filtradas */
                ocultarAcordeon();
                if (panelTrabajos) panelTrabajos.style.display = '';
                aplicarFiltros();
            }
        });
    });
})();