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

import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/ServletCultivo")
public class ServletCultivo extends HttpServlet {

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Discriminador de rutas: centraliza en un solo servlet múltiples operaciones sobre cultivos
        String accion = request.getParameter("accion");
        CultivoDao dao = new CultivoDao();

        // ── Vista del TRABAJADOR: solo lectura ─────────────────────────────
        if ("verTrabajador".equals(accion)) {
            if (!verificarSesionUsuario(request, response)) return;
            request.setAttribute("listaCultivos", dao.listarCultivos());
            request.getRequestDispatcher("/templates/trabajador/cultivos_trabajador.jsp")
                   .forward(request, response);
            return;
        }

        // ── Todo lo demás requiere rol ADMINISTRADOR ───────────────────────
        if (!verificarSesionAdmin(request, response)) return;

        // ── Historial HTML: carga registros de trabajo de un cultivo específico ──
        if ("historial".equals(accion)) {
            // Segunda verificación redundante; defensiva ante refactorizaciones futuras que muevan este bloque
            if (!verificarSesionAdmin(request, response)) return;
            String idStr = request.getParameter("id");
            if (estaVacio(idStr)) {
                response.sendRedirect(request.getContextPath() + "/ServletCultivo");
                return;
            }
            try {
                int idCultivo = Integer.parseInt(idStr);
                RegistroTrabajoDao registroDao = new RegistroTrabajoDao();
                request.setAttribute("historialCultivo", registroDao.listarPorCultivo(idCultivo));
                // Permite que la JSP resalte o expanda el cultivo activo en la tabla
                request.setAttribute("idCultivoActivo", idCultivo);
            } catch (NumberFormatException e) {
                // ID no numérico: se omite el historial y la vista renderiza solo el listado normal
            }
            request.setAttribute("listaCultivos", dao.listarCultivos());
            request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
                   .forward(request, response);
            return;
        }

        // ── Historial JSON: endpoint consumido por el frontend vía fetch/AJAX ──
        if ("historialJson".equals(accion)) {
            if (!verificarSesionAdmin(request, response)) return;
            // Declara el tipo de respuesta antes de escribir en el stream para que el cliente parsee correctamente
            response.setContentType("application/json; charset=UTF-8");

            String idStr = request.getParameter("id");
            if (estaVacio(idStr)) {
                // Array vacío como contrato: el cliente puede iterar sin comprobar null
                response.getWriter().write("[]");
                return;
            }

            try {
                int idCultivo = Integer.parseInt(idStr);
                List<registros> lista = new RegistroTrabajoDao().listarPorCultivo(idCultivo);

                // Serialización manual para evitar dependencias externas (Jackson, Gson, etc.)
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < lista.size(); i++) {
                    registros r = lista.get(i);
                    // Coma separadora solo entre elementos, no tras el último (JSON estricto)
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"idTrabajoRealizado\":").append(r.getIdTrabajoRealizado()).append(",")
                        .append("\"descripcionTrabajo\":\"").append(escaparJson(r.getDescripcionTrabajo())).append("\",")
                        .append("\"nombreUsuario\":\"").append(escaparJson(r.getNombreUsuario())).append("\",")
                        .append("\"fechaInicio\":\"").append(r.getFechaInicio()).append("\",")
                        .append("\"fechaFinalizo\":\"").append(r.getFechaFinalizo()).append("\",")
                        // Campo nullable representado como null JSON, no como string "null"
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

        // ── Eliminar cultivo 
        if ("eliminar".equals(accion)) {
            String idStr = request.getParameter("id");
            boolean ok = false;
            if (!estaVacio(idStr)) {
                try {
                    dao.eliminarCultivo(Integer.parseInt(idStr));
                    // ok permanece false si eliminarCultivo no lanza excepcion pero falla silenciosamente en BD
                } catch (NumberFormatException ignored) {}
            }
            // Parametro "status" comunica el resultado a la vista siguiente sin session flash
            String status = ok ? "eliminado" : "error";
            response.sendRedirect(request.getContextPath() + "/ServletCultivo?status=" + status);
            return;
        }

        //  Eliminar registro de trabajo individual dentro de un cultivo 
        if ("eliminarRegistro".equals(accion)) {
            if (!verificarSesionAdmin(request, response)) return;
            String idStr        = request.getParameter("id");
            String idCultivoStr = request.getParameter("idCultivo");
            if (!estaVacio(idStr)) {
                try {
                    new RegistroTrabajoDao().eliminarLabor(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            // Preserva el contexto del cultivo activo para que la vista regrese al historial correcto
            String redirect = request.getContextPath() + "/ServletCultivo?status=registroEliminado";
            if (!estaVacio(idCultivoStr)) redirect += "&idCultivoActivo=" + idCultivoStr;
            response.sendRedirect(redirect);
            return;
        }

        //  Caso por defecto: listado admin con contadores de historial 
        List<cultivo> lista = dao.listarCultivos();
        RegistroTrabajoDao registroDao = new RegistroTrabajoDao();

        // Mapa idCultivo  cantidad de registros; permite mostrar badges sin consultas adicionales en la JSP
        Map<Integer, Integer> contadoresHistorial = new HashMap<>();
        for (cultivo c : lista) {
            contadoresHistorial.put(
                c.getIdCultivo(),
                registroDao.contarPorCultivo(c.getIdCultivo())
            );
        }
        request.setAttribute("listaCultivos", lista);
        request.setAttribute("contadoresHistorial", contadoresHistorial);
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }

    //  Helper: escape JSON 

    // Escapa caracteres especiales para prevenir XSS e inyeccion JSON al serializar manualmente
    private String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")   // backslash primero para no escapar los escapes siguientes
                .replace("\"", "\\\"")   // comilla doble rompería la cadena JSON
                .replace("\n", "\\n")    // saltos de línea invalidan JSON de una sola línea
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    //  POST: solo ADMINISTRADOR 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesionAdmin(request, response)) return;

        String idStr       = request.getParameter("id");
        String nombre      = request.getParameter("nombreCultivo");
        String tipo        = request.getParameter("tipoCultivo");
        String fSiembraStr = request.getParameter("fechaSiembra");
        String fCosechaStr = request.getParameter("fechaCosecha");

        // La cosecha es opcional: modela cultivos en curso sin fecha de termino definida
        if (estaVacio(nombre) || estaVacio(tipo) || estaVacio(fSiembraStr)) {
            reenviarAdminConError("Nombre, tipo y fecha de siembra son obligatorios.",
                                  request, response);
            return;
        }

        Date fSiembra;
        Date fCosecha;
        try {
            // Date.valueOf espera "yyyy-MM-dd"; cualquier otra forma lanza IllegalArgumentException
            fSiembra = Date.valueOf(fSiembraStr);
            fCosecha = estaVacio(fCosechaStr) ? null : Date.valueOf(fCosechaStr);
        } catch (IllegalArgumentException e) {
            reenviarAdminConError("Formato de fecha inválido. Use el selector de fechas.",
                                  request, response);
            return;
        }

        CultivoDao dao = new CultivoDao();
        // Presencia de "id" distingue edicion de registro existente vs. creacion de uno nuevo
        if (!estaVacio(idStr)) {
            dao.actualizarCultivo(Integer.parseInt(idStr), nombre.trim(),
                                  tipo.trim(), fSiembra, fCosecha);
        } else {
            dao.registrarCultivo(new cultivo(nombre.trim(), tipo.trim(), fSiembra, fCosecha));
        }

        response.sendRedirect("ServletCultivo");
    }

    //  Helper: reenvio con error 

    // Forward en lugar de redirect para que el mensaje de error sobreviva en el scope de request
    private void reenviarAdminConError(String mensaje,
                                       HttpServletRequest request,
                                       HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        // Recarga el listado para que la JSP pueda renderizar la tabla junto al mensaje de error
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }
}