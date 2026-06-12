package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.dao.AsignacionDao;
import com.paujil.modelo.cultivo;
import com.paujil.modelo.asignacion;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;

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
 *   - Autenticación/autorización por rol.
 *   - Sanitización de parámetros de ruta (id numérico).
 *   - Delegación a DAO y reenvío a vistas.
 *
 *  POST:
 *   1. Sanitización: null-check, trim, longitudes máximas.
 *   2. Parseo defensivo de tipos (Date, Integer) — rechazo genérico si fallan.
 *   3. Persistencia vía DAO.
 *
 * Validaciones de fecha delegadas completamente al frontend (validaciones-cultivos.js):
 *   - fechaSiembra >= hoy              → validarFormCultivo()
 *   - fechaCosecha >= fechaSiembra     → validarFormCultivo()
 *   - fechaInicio  >= hoy              → validarFormLabor()
 *   - fechaFinalizo >= fechaInicio     → validarFormLabor()
 *
 * El backend solo rechaza peticiones con campos obligatorios vacíos o que
 * no superen el parseo de tipos, con un mensaje genérico para no revelar
 * la lógica interna ante posibles bypasses (curl, Postman, etc.).
 *
 * NOTA DE MIGRACIÓN: el antiguo RegistroTrabajoDao / modelo "registros" fue
 * reemplazado por AsignacionDao / modelo "asignacion". El historial de un
 * cultivo ahora corresponde a las asignaciones (tabla asignaciones) ligadas
 * a ese cultivo, con sus datos de trabajo (JOIN trabajos) y usuario (JOIN
 * usuarios). Mapeo de campos:
 *   idTrabajoRealizado  -> a.getId()                 (id_asignacion)
 *   descripcionTrabajo  -> a.getDescripcionTrabajo()
 *   nombreUsuario       -> a.getNombreUsuario()
 *   fechaInicio         -> a.getFechaInicio()
 *   fechaFinalizo       -> a.getFechaFinalizacion()
 *   observaciones       -> a.getObservaciones()
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
                AsignacionDao asignacionDao = new AsignacionDao();
                request.setAttribute("historialCultivo", asignacionDao.listarPorCultivo(idCultivo));
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
                List<asignacion> lista = new AsignacionDao().listarPorCultivo(idCultivo);
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < lista.size(); i++) {
                    asignacion a = lista.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"idTrabajoRealizado\":").append(a.getId()).append(",")
                        .append("\"descripcionTrabajo\":\"").append(escaparJson(a.getDescripcionTrabajo())).append("\",")
                        .append("\"nombreUsuario\":\"").append(escaparJson(a.getNombreUsuario())).append("\",")
                        .append("\"fechaInicio\":\"").append(a.getFechaInicio()).append("\",")
                        .append("\"fechaFinalizo\":\"").append(a.getFechaFinalizacion()).append("\",")
                        .append("\"observaciones\":").append(a.getObservaciones() != null
                            ? "\"" + escaparJson(a.getObservaciones()) + "\""
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

        // ── Eliminar registro de trabajo (asignación) ──
        if ("eliminarRegistro".equals(accion)) {
            String idStr        = request.getParameter("id");
            String idCultivoStr = request.getParameter("idCultivo");
            if (!estaVacio(idStr)) {
                try {
                    new AsignacionDao().eliminarAsignacion(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            String redirect = request.getContextPath() + "/ServletCultivo?status=registroEliminado";
            if (!estaVacio(idCultivoStr)) redirect += "&idCultivoActivo=" + idCultivoStr;
            response.sendRedirect(redirect);
            return;
        }

        // ── Caso por defecto: listado admin ──
        List<cultivo> lista = dao.listarCultivos();
        AsignacionDao asignacionDao = new AsignacionDao();
        Map<Integer, Integer> contadoresHistorial = new HashMap<>();
        for (cultivo c : lista) {
            contadoresHistorial.put(
                c.getIdCultivo(),
                asignacionDao.contarPorCultivo(c.getIdCultivo())
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

        // ── Guardia mínima de seguridad ──────────────────────────────────────
        // Rechaza peticiones que bypasearon el validador JS (curl, Postman, etc.).
        // Solo verifica presencia de campos obligatorios y longitudes; las reglas
        // de negocio sobre fechas (>= hoy, cosecha >= siembra) son exclusivas del
        // frontend — no se replican aquí intencionalmente.

        // Campos obligatorios y longitudes máximas
        if (estaVacio(nombre)    || excedeLongitud(nombre, MAX_NOMBRE)
         || excedeLongitud(tipo, MAX_TIPO)
         || estaVacio(fSiembraStr)) {
            reenviarAdminConError("Solicitud inválida. Verifica todos los campos.",
                                  request, response);
            return;
        }

        // Parseo defensivo de fechas: Date.valueOf espera "yyyy-MM-dd"
        // No se evalúan relaciones entre fechas; eso es responsabilidad del frontend.
        Date fSiembra;
        Date fCosecha = null;
        try {
            fSiembra = Date.valueOf(fSiembraStr);
            if (!estaVacio(fCosechaStr)) {
                fCosecha = Date.valueOf(fCosechaStr);
                // Única guardia de integridad referencial que el backend conserva:
                // evita que un bypass persista cosecha anterior a siembra, lo que
                // corrompería datos sin posibilidad de corrección desde la UI.
                // El mensaje sigue siendo genérico para no exponer la lógica.
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