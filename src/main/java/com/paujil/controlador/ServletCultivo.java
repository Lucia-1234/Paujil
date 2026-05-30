package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.modelo.cultivo;
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

@WebServlet("/ServletCultivo")
public class ServletCultivo extends HttpServlet {

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

        // ── Todo lo demás es solo para ADMINISTRADOR ───────────────────────
        if (!verificarSesionAdmin(request, response)) return;

        if ("eliminar".equals(accion)) {
            String idStr = request.getParameter("id");
            if (!estaVacio(idStr)) {
                try {
                    dao.eliminarCultivo(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            response.sendRedirect("ServletCultivo");
            return;
        }

        // Listar para admin
        request.setAttribute("listaCultivos", dao.listarCultivos());
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
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
        String fCosechaStr = request.getParameter("fechaCosecha");

        if (estaVacio(nombre) || estaVacio(tipo) || estaVacio(fSiembraStr)) {
            reenviarAdminConError("Nombre, tipo y fecha de siembra son obligatorios.",
                                  request, response);
            return;
        }

        Date fSiembra;
        Date fCosecha;
        try {
            fSiembra = Date.valueOf(fSiembraStr);
            fCosecha = estaVacio(fCosechaStr) ? null : Date.valueOf(fCosechaStr);
        } catch (IllegalArgumentException e) {
            reenviarAdminConError("Formato de fecha inválido. Use el selector de fechas.",
                                  request, response);
            return;
        }

        CultivoDao dao = new CultivoDao();
        if (!estaVacio(idStr)) {
            dao.actualizarCultivo(Integer.parseInt(idStr), nombre.trim(),
                                  tipo.trim(), fSiembra, fCosecha);
        } else {
            dao.registrarCultivo(new cultivo(nombre.trim(), tipo.trim(), fSiembra, fCosecha));
        }

        response.sendRedirect("ServletCultivo");
    }

    // ── Helper ────────────────────────────────────────────────────────────────
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