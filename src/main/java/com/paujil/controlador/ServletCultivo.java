package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.dao.RegistroTrabajoDao;
import com.paujil.modelo.cultivo;
import com.paujil.modelo.registros;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ServletCultivo — capa de seguridad mínima en backend.
 *
 * Responsabilidades tras la migración de validaciones al frontend:
 *
 *  GET:
 *   - Autenticación/autorización por rol (sin cambios).
 *   - Sanitización de parámetros de ruta (id numérico).
 *   - Delegación a DAO y reenvío a vistas.
 *
 *  POST:
 *   1. Sanitización: null-check, trim, longitudes máximas.
 *   2. Parseo defensivo de tipos (Date, Integer) — rechazo genérico si fallan.
 *   3. Persistencia vía DAO.
 *
 * Lo que ya NO hace este servlet:
 *  - Validar que fechaSiembra >= hoy              → validaciones-cultivos.js → validarFormCultivo()
 *  - Validar que fechaCosecha >= fechaSiembra     → validaciones-cultivos.js → validarFormCultivo()
 *  - Validar que nombre/tipo tengan cierta long.  → validaciones-cultivos.js → validarTextoRequerido()
 *  - Validar fechaInicio/fechaFinalizo de labores → validaciones-cultivos.js → validarFormLabor()
 */
@WebServlet("/ServletCultivo")
public class ServletCultivo extends HttpServlet {

    // Límites de longitud máxima: defensa contra payloads oversized
    private static final int MAX_NOMBRE = 80;
    private static final int MAX_TIPO   = 60;

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        CultivoDao dao = new CultivoDao();

        // Vista del TRABAJADOR: solo lectura
        if ("verTrabajador".equals(accion)) {
            if (!verificarSesionUsuario(request, response)) return;
            request.setAttribute("listaCultivos", dao.listarCultivos());
            request.getRequestDispatcher("/templates/trabajador/cultivos_trabajador.jsp")
                   .forward(request, response);
            return;
        }

        // El resto de acciones requieren rol de administrador
        if (!verificarSesionAdmin(request, response)) return;

        // ── Historial HTML ──
        if ("historial".equals(accion)) {
            String idStr = request.getParameter("id");
            if (estaVacio(idStr)) {
                response.sendRedirect(request.getContextPath() + "/ServletCultivo");
                return;
            }
            try {
                int idCultivo = Integer.parseInt(idStr);
                RegistroTrabajoDao registroDao = new RegistroTrabajoDao();
                request.setAttribute("historialCultivo", registroDao.listarPorCultivo(idCultivo));
                request.setAttribute("idCultivoActivo",  idCultivo);
            } catch (NumberFormatException e) {
                // ID no numérico: se carga solo el listado, sin historial
            }
            request.setAttribute("listaCultivos", dao.listarCultivos());
            request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
                   .forward(request, response);
            return;
        }

        // ── Historial JSON (consumido por fetch/AJAX) ──
        if ("historialJson".equals(accion)) {
            response.setContentType("application/json; charset=UTF-8");
            String idStr = request.getParameter("id");
            if (estaVacio(idStr)) {
                response.getWriter().write("[]");
                return;
            }
            try {
                int idCultivo = Integer.parseInt(idStr);
                List<registros> lista = new RegistroTrabajoDao().listarPorCultivo(idCultivo);
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < lista.size(); i++) {
                    registros r = lista.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"idTrabajoRealizado\":").append(r.getIdTrabajoRealizado()).append(",")
                        .append("\"descripcionTrabajo\":\"").append(escaparJson(r.getDescripcionTrabajo())).append("\",")
                        .append("\"nombreUsuario\":\"").append(escaparJson(r.getNombreUsuario())).append("\",")
                        .append("\"fechaInicio\":\"").append(r.getFechaInicio()).append("\",")
                        .append("\"fechaFinalizo\":\"").append(r.getFechaFinalizo()).append("\",")
                        .append("\"observaciones\":").append(r.getObservaciones() != null
                            ? "\"" + escaparJson(r.getObservaciones()) + "\""
                            : "null")
                        .append("}");
                }
                json.append("]");
                response.getWriter().write(json.toString());
            } catch (NumberFormatException e) {
                response.getWriter().write("[]");
            }
            return;
        }

        // ── Eliminar cultivo ──
        if ("eliminar".equals(accion)) {
            String idStr = request.getParameter("id");
            if (!estaVacio(idStr)) {
                try {
                    dao.eliminarCultivo(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            response.sendRedirect(request.getContextPath() + "/ServletCultivo?status=eliminado");
            return;
        }

        // ── Eliminar registro de trabajo ──
        if ("eliminarRegistro".equals(accion)) {
            String idStr        = request.getParameter("id");
            String idCultivoStr = request.getParameter("idCultivo");
            if (!estaVacio(idStr)) {
                try {
                    new RegistroTrabajoDao().eliminarLabor(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            String redirect = request.getContextPath() + "/ServletCultivo?status=registroEliminado";
            if (!estaVacio(idCultivoStr)) redirect += "&idCultivoActivo=" + idCultivoStr;
            response.sendRedirect(redirect);
            return;
        }

        // ── Caso por defecto: listado admin ──
        List<cultivo> lista = dao.listarCultivos();
        RegistroTrabajoDao registroDao = new RegistroTrabajoDao();
        Map<Integer, Integer> contadoresHistorial = new HashMap<>();
        for (cultivo c : lista) {
            contadoresHistorial.put(
                c.getIdCultivo(),
                registroDao.contarPorCultivo(c.getIdCultivo())
            );
        }
        request.setAttribute("listaCultivos",        lista);
        request.setAttribute("contadoresHistorial",  contadoresHistorial);
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }

    // ── POST: solo ADMINISTRADOR ──────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesionAdmin(request, response)) return;

        String idStr       = request.getParameter("id");
        String nombre      = sanitizar(request.getParameter("nombreCultivo"));
        String tipo        = sanitizar(request.getParameter("tipoCultivo"));
        String fSiembraStr = sanitizar(request.getParameter("fechaSiembra"));
        String fCosechaStr = sanitizar(request.getParameter("fechaCosecha"));

        // ── Guardia de seguridad mínima ──────────────────────────────────────
        // Rechaza peticiones que no pasaron por el validador JS (bypass, curl, etc.).
        // El mensaje es genérico intencionalmente para no revelar qué falló.

        // Campos obligatorios y longitudes
        if (estaVacio(nombre)    || excedeLongitud(nombre, MAX_NOMBRE)
         || excedeLongitud(tipo, MAX_TIPO)
         || estaVacio(fSiembraStr)) {
            reenviarAdminConError("Solicitud inválida. Verifica todos los campos.",
                                  request, response);
            return;
        }

        // Parseo defensivo de fechas: Date.valueOf espera "yyyy-MM-dd"
        Date fSiembra;
        Date fCosecha = null;
        try {
            fSiembra = Date.valueOf(fSiembraStr);
            if (!estaVacio(fCosechaStr)) {
                fCosecha = Date.valueOf(fCosechaStr);
                // Verifica que cosecha no sea anterior a siembra (anti-bypass del validador JS)
                if (fCosecha.toLocalDate().isBefore(fSiembra.toLocalDate())) {
                    reenviarAdminConError("Solicitud inválida. Verifica todos los campos.",
                                          request, response);
                    return;
                }
            }
        } catch (IllegalArgumentException e) {
            reenviarAdminConError("Solicitud inválida. Verifica todos los campos.",
                                  request, response);
            return;
        }

        // ── Persistencia ─────────────────────────────────────────────────────
        CultivoDao dao = new CultivoDao();
        if (!estaVacio(idStr)) {
            try {
                dao.actualizarCultivo(Integer.parseInt(idStr),
                                      nombre, tipo != null ? tipo : "",
                                      fSiembra, fCosecha);
            } catch (NumberFormatException e) {
                reenviarAdminConError("Solicitud inválida. Verifica todos los campos.",
                                      request, response);
                return;
            }
        } else {
            dao.registrarCultivo(new cultivo(nombre, tipo != null ? tipo : "", fSiembra, fCosecha));
        }

        // Redirect-After-POST: evita reenvío del formulario al recargar
        response.sendRedirect("ServletCultivo");
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    /** Aplica trim o devuelve null si el parámetro es nulo. */
    private String sanitizar(String valor) {
        return (valor != null) ? valor.trim() : null;
    }

    /** Verdadero si la longitud supera el límite máximo. */
    private boolean excedeLongitud(String valor, int max) {
        return valor != null && valor.length() > max;
    }

    /** Escapa caracteres especiales para serialización JSON manual segura. */
    private String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Forward con mensaje de error; recarga el listado para que la vista tenga contexto. */
    private void reenviarAdminConError(String mensaje,
                                       HttpServletRequest request,
                                       HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }
}