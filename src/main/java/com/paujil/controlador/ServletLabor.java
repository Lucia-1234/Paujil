package com.paujil.controlador;

import com.paujil.dao.RegistroTrabajoDao;
import com.paujil.modelo.registros;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;

/**
 * ServletLabor — capa de seguridad mínima en backend.
 *
 * Responsabilidades tras la migración de validaciones al frontend:
 *
 *  1. Autenticación: verifica sesión activa antes de cualquier operación.
 *  2. Sanitización: null-check, trim, longitudes máximas.
 *  3. Parseo defensivo de tipos (Integer, Date) — rechazo genérico si fallan.
 *  4. Persistencia vía DAO.
 *
 * Lo que ya NO hace este servlet:
 *  - Validar que fechaInicio >= hoy              → validaciones-cultivos.js → validarFechaInicio()
 *  - Validar que fechaFinalizo >= fechaInicio     → validaciones-cultivos.js → validarFechaFin()
 *  - Validar longitud de descripción (UX)        → validaciones-cultivos.js → validarTextoRequerido()
 *  - Validar campos obligatorios vacíos (UX)     → validaciones-cultivos.js → validarFormLabor()
 *
 * Se conserva una guardia anti-bypass para fechaInicio > fechaFinalizo porque
 * protege la integridad de la BD ante peticiones que no pasaron por el formulario.
 */
@WebServlet("/ServletLabor")
public class ServletLabor extends HttpServlet {

    // Límites de longitud máxima: defensa contra payloads oversized
    private static final int MAX_DESCRIPCION   = 1000;
    private static final int MAX_OBSERVACIONES =  500;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── 1. Autenticación ─────────────────────────────────────────────────
        // false evita crear sesión fantasma; si no hay sesión el usuario no está autenticado
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
            return;
        }
        int idUsuario = (Integer) session.getAttribute("idUsuario");

        // ── 2. Lectura de parámetros ─────────────────────────────────────────
        String idCultivoStr     = request.getParameter("idCultivo");
        String descripcion      = sanitizar(request.getParameter("descripcionTrabajo"));
        String fechaInicioStr   = sanitizar(request.getParameter("fechaInicio"));
        String fechaFinalizoStr = sanitizar(request.getParameter("fechaFinalizo"));
        // Campo opcional; se almacenará como null si viene vacío
        String observaciones    = sanitizar(request.getParameter("observaciones"));

        // ── 3. Guardias de seguridad (anti-bypass) ───────────────────────────
        // Rechaza peticiones que no pasaron por el validador JS (curl, Burp, scripts).
        // Mensajes genéricos intencionalmente: no revelan qué campo falló.

        // 3a. Presencia de campos obligatorios y longitudes máximas
        if (estaVacioONulo(idCultivoStr)
         || estaVacioONulo(descripcion)      || excedeLongitud(descripcion,   MAX_DESCRIPCION)
         || estaVacioONulo(fechaInicioStr)
         || estaVacioONulo(fechaFinalizoStr)
         || excedeLongitud(observaciones,    MAX_OBSERVACIONES)) {

            reenviarConError("Solicitud inválida. Verifica todos los campos.",
                    idCultivoStr, request, response);
            return;
        }

        // 3b. Parseo defensivo del ID de cultivo
        int idCultivo;
        try {
            idCultivo = Integer.parseInt(idCultivoStr);
        } catch (NumberFormatException e) {
            // Valor no numérico indica manipulación del formulario
            reenviarConError("Solicitud inválida. Verifica todos los campos.",
                    idCultivoStr, request, response);
            return;
        }

        // 3c. Parseo defensivo de fechas: Date.valueOf espera estrictamente "yyyy-MM-dd"
        Date fechaInicio, fechaFinalizo;
        try {
            fechaInicio   = Date.valueOf(fechaInicioStr);
            fechaFinalizo = Date.valueOf(fechaFinalizoStr);
        } catch (IllegalArgumentException e) {
            reenviarConError("Solicitud inválida. Verifica todos los campos.",
                    idCultivoStr, request, response);
            return;
        }

        // 3d. Guardia de integridad: fechaInicio no puede ser posterior a fechaFinalizo.
        // Esta es la única regla de negocio que se mantiene en el backend porque:
        //  a) Es trivial de verificar.
        //  b) Una inversión de fechas corrompería los datos de historial de labor.
        //  c) Protege contra bypass directo de la validación JS.
        if (fechaInicio.after(fechaFinalizo)) {
            reenviarConError("Solicitud inválida. Verifica todos los campos.",
                    idCultivoStr, request, response);
            return;
        }

        // ── 4. Persistencia ──────────────────────────────────────────────────
        // Observaciones vacías se convierten a null para distinguirlas de texto real en BD
        registros reg = new registros(
            idCultivo,
            descripcion,
            idUsuario,
            fechaInicio,
            fechaFinalizo,
            estaVacioONulo(observaciones) ? null : observaciones
        );

        boolean exito = new RegistroTrabajoDao().registrarLabor(reg);

        // Redirect-After-POST: evita duplicar el registro al recargar la página
        String status = exito ? "registrado" : "error";
        response.sendRedirect(request.getContextPath()
                + "/ServletCultivo?status=" + status + "&idCultivoActivo=" + idCultivo);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    /** Aplica trim o devuelve null si el parámetro es nulo. */
    private String sanitizar(String valor) {
        return (valor != null) ? valor.trim() : null;
    }

    /** Verdadero si el valor es null o cadena vacía (post-trim). */
    private boolean estaVacioONulo(String valor) {
        return valor == null || valor.isEmpty();
    }

    /** Verdadero si la longitud supera el límite máximo. Tolera null. */
    private boolean excedeLongitud(String valor, int max) {
        return valor != null && valor.length() > max;
    }

    /**
     * Forward con mensaje de error.
     * Usa forward (no redirect) para que el mensaje sobreviva en el scope de request.
     * Recarga el listado de cultivos para que la vista tenga contexto completo.
     */
    private void reenviarConError(String mensaje, String idCultivo,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        request.setAttribute("idCultivo",    idCultivo);
        // Recarga el listado para que cultivos.jsp pueda renderizar la tabla junto al error
        request.setAttribute("listaCultivos",
                new com.paujil.dao.CultivoDao().listarCultivos());
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }
}