'use strict';

/* ==========================================================================
   filtros-trabajos.js — Filtro de tarjetas por estado | Gestión de Trabajos
   ========================================================================== */

(function () {
    const filtros = document.querySelectorAll('.filtro-btn');
    if (!filtros.length) return;

    function aplicarFiltro(filtro) {
        document.querySelectorAll('.job-card').forEach(function (card) {
            const mostrar = filtro === 'todos' || card.getAttribute('data-estado') === filtro;
            card.style.display = mostrar ? '' : 'none';
        });
    }

    filtros.forEach(function (btn) {
        btn.addEventListener('click', function () {
            filtros.forEach(function (b) { b.classList.remove('seleccionado'); });
            btn.classList.add('seleccionado');
            aplicarFiltro(btn.getAttribute('data-filtro'));
        });
    });
})();