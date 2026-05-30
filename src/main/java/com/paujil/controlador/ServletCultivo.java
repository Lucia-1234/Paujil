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

        // El parametro "accion" centraliza multiples operaciones sobre cultivos en un solo endpoint
        String accion = request.getParameter("accion");
        CultivoDao dao = new CultivoDao();

        // ── Vista del TRABAJADOR: solo lectura ─────────────────────────────
        if ("verTrabajador".equals(accion)) {
            // Acceso limitado a sesion de usuario comun; el trabajador no puede modificar cultivos
            if (!verificarSesionUsuario(request, response)) return;
            request.setAttribute("listaCultivos", dao.listarCultivos());
            request.getRequestDispatcher("/templates/trabajador/cultivos_trabajador.jsp")
                   .forward(request, response);
            return;
        }

        // Cualquier accion distinta a "verTrabajador" requiere privilegios de administrador
        if (!verificarSesionAdmin(request, response)) return;

        // ── Historial HTML: registros de trabajo de un cultivo especifico ──
        if ("historial".equals(accion)) {
            // Segunda verificacion defensiva ante refactorizaciones que puedan mover este bloque
            if (!verificarSesionAdmin(request, response)) return;
            String idStr = request.getParameter("id");
            // Sin ID no hay cultivo objetivo; redirige al listado sin lanzar excepcion
            if (estaVacio(idStr)) {
                response.sendRedirect(request.getContextPath() + "/ServletCultivo");
                return;
            }
            try {
                int idCultivo = Integer.parseInt(idStr);
                RegistroTrabajoDao registroDao = new RegistroTrabajoDao();
                request.setAttribute("historialCultivo", registroDao.listarPorCultivo(idCultivo));
                // Permite que la JSP resalte o expanda automaticamente el cultivo activo en la tabla
                request.setAttribute("idCultivoActivo", idCultivo);
            } catch (NumberFormatException e) {
                // ID no numerico: se omite el historial y la vista renderiza solo el listado normal
            }
            // El listado siempre se carga para que la vista tenga contexto completo aunque falle el historial
            request.setAttribute("listaCultivos", dao.listarCultivos());
            request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
                   .forward(request, response);
            return;
        }

        // ── Historial JSON: endpoint consumido por el frontend via fetch/AJAX ──
        if ("historialJson".equals(accion)) {
            if (!verificarSesionAdmin(request, response)) return;
            // Declara el content-type antes de escribir en el stream para que el cliente parsee correctamente
            response.setContentType("application/json; charset=UTF-8");
            String idStr = request.getParameter("id");
            if (estaVacio(idStr)) {
                // Array vacio como contrato: el cliente puede iterar sin comprobar null
                response.getWriter().write("[]");
                return;
            }
            try {
                int idCultivo = Integer.parseInt(idStr);
                List<registros> lista = new RegistroTrabajoDao().listarPorCultivo(idCultivo);
                // Serializacion manual para evitar dependencias externas como Jackson o Gson
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < lista.size(); i++) {
                    registros r = lista.get(i);
                    // Coma separadora solo entre elementos; omitirla tras el ultimo cumple el estandar JSON
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"idTrabajoRealizado\":").append(r.getIdTrabajoRealizado()).append(",")
                        .append("\"descripcionTrabajo\":\"").append(escaparJson(r.getDescripcionTrabajo())).append("\",")
                        .append("\"nombreUsuario\":\"").append(escaparJson(r.getNombreUsuario())).append("\",")
                        .append("\"fechaInicio\":\"").append(r.getFechaInicio()).append("\",")
                        .append("\"fechaFinalizo\":\"").append(r.getFechaFinalizo()).append("\",")
                        // null JSON en lugar de string "null" para que el cliente distinga ausencia de valor
                        .append("\"observaciones\":").append(r.getObservaciones() != null
                            ? "\"" + escaparJson(r.getObservaciones()) + "\""
                            : "null")
                        .append("}");
                }
                json.append("]");
                response.getWriter().write(json.toString());
            } catch (NumberFormatException e) {
                // ID malformado: responde array vacio para no romper el parsing en el cliente
                response.getWriter().write("[]");
            }
            return;
        }

        // ── Eliminar cultivo ───────────────────────────────────────────────
        if ("eliminar".equals(accion)) {
            String idStr = request.getParameter("id");
            // ok permanece false si el ID esta vacio o si la eliminacion falla silenciosamente en BD
            boolean ok = false;
            if (!estaVacio(idStr)) {
                try {
                    dao.eliminarCultivo(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {
                    // ID no numerico: se descarta y la redireccion siguiente refresca el listado sin eliminar
                }
            }
            // El parametro "status" comunica el resultado a la vista sin necesidad de session flash
            String status = ok ? "eliminado" : "error";
            response.sendRedirect(request.getContextPath() + "/ServletCultivo?status=" + status);
            return;
        }

        // ── Eliminar registro de trabajo individual dentro de un cultivo ───
        if ("eliminarRegistro".equals(accion)) {
            if (!verificarSesionAdmin(request, response)) return;
            String idStr        = request.getParameter("id");
            // idCultivo se preserva para devolver al admin al historial correcto tras la eliminacion
            String idCultivoStr = request.getParameter("idCultivo");
            if (!estaVacio(idStr)) {
                try {
                    new RegistroTrabajoDao().eliminarLabor(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            String redirect = request.getContextPath() + "/ServletCultivo?status=registroEliminado";
            // Adjunta idCultivoActivo solo si esta disponible para que la vista expanda el historial correcto
            if (!estaVacio(idCultivoStr)) redirect += "&idCultivoActivo=" + idCultivoStr;
            response.sendRedirect(redirect);
            return;
        }

        // ── Caso por defecto: listado admin con contadores de historial ────
        List<cultivo> lista = dao.listarCultivos();
        RegistroTrabajoDao registroDao = new RegistroTrabajoDao();
        // Mapa idCultivo -> cantidad de registros; permite mostrar badges en la tabla sin consultas en la JSP
        Map<Integer, Integer> contadoresHistorial = new HashMap<>();
        for (cultivo c : lista) {
            contadoresHistorial.put(
                c.getIdCultivo(),
                // Una consulta por cultivo; aceptable para volumenes pequenos de datos
                registroDao.contarPorCultivo(c.getIdCultivo())
            );
        }
        request.setAttribute("listaCultivos", lista);
        request.setAttribute("contadoresHistorial", contadoresHistorial);
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }

    // ── Helper: escape JSON ───────────────────────────────────────────────────

    // Escapa caracteres especiales para prevenir XSS e inyeccion JSON al serializar manualmente
    private String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")   // backslash primero para no re-escapar los reemplazos siguientes
                .replace("\"", "\\\"")   // comilla doble sin escapar romperia la cadena JSON
                .replace("\n", "\\n")    // saltos de linea invalidan JSON de una sola linea
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ── POST: solo ADMINISTRADOR ──────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesionAdmin(request, response)) return;

        String idStr       = request.getParameter("id");
        String nombre      = request.getParameter("nombreCultivo");
        String tipo        = request.getParameter("tipoCultivo");
        String fSiembraStr = request.getParameter("fechaSiembra");
        // La cosecha es opcional: un cultivo activo no tiene fecha de termino definida
        String fCosechaStr = request.getParameter("fechaCosecha");

        // Nombre, tipo y siembra son los datos minimos para registrar un cultivo valido
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
            // null modela un cultivo sin fecha de cosecha; no es un error de validacion
            fCosecha = estaVacio(fCosechaStr) ? null : Date.valueOf(fCosechaStr);
        } catch (IllegalArgumentException e) {
            reenviarAdminConError("Formato de fecha invalido. Use el selector de fechas.",
                                  request, response);
            return;
        }

        CultivoDao dao = new CultivoDao();
        // La presencia de "id" diferencia entre editar un cultivo existente y crear uno nuevo
        if (!estaVacio(idStr)) {
            dao.actualizarCultivo(Integer.parseInt(idStr), nombre.trim(),
                                  tipo.trim(), fSiembra, fCosecha);
        } else {
            dao.registrarCultivo(new cultivo(nombre.trim(), tipo.trim(), fSiembra, fCosecha));
        }

        // Redirect-After-POST evita que recargar la pagina repita la insercion o actualizacion
        response.sendRedirect("ServletCultivo");
    }

    // ── Helper: reenvio con error ─────────────────────────────────────────────

    // Forward en lugar de redirect para preservar el mensaje de error en el scope de request
    private void reenviarAdminConError(String mensaje,
                                       HttpServletRequest request,
                                       HttpServletResponse response)
            throws ServletException, IOException {
        // El JSP lee este atributo para renderizar el aviso de error al usuario
        request.setAttribute("mensajeError", mensaje);
        // Recarga el listado para que la vista pueda mostrar la tabla junto al mensaje de error
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }
}