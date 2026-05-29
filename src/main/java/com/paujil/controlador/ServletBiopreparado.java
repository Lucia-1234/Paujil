package com.paujil.controlador;

import com.paujil.dao.BiopreparadoDao;
import com.paujil.modelo.biopreparado;
import com.paujil.modelo.biopreparado.ingredienteBio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/ServletBiopreparado")
public class ServletBiopreparado extends HttpServlet {

    // ── Guarda de sesión (solo administrador) ─────────────────────────────────
    private boolean sesionAdminValida(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null
                || session.getAttribute("idUsuario") == null
                || !"administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario"))) {
            res.sendRedirect(req.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
            return false;
        }
        return true;
    }

    // ── GET: listar o eliminar ────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (!sesionAdminValida(req, res)) return;

        String accion = req.getParameter("accion");
        BiopreparadoDao dao = new BiopreparadoDao();

        if ("eliminar".equals(accion)) {
            String idStr = req.getParameter("id");
            if (idStr != null && !idStr.isBlank()) {
                try {
                    dao.eliminarBiopreparado(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            res.sendRedirect("ServletBiopreparado");
            return;
        }

        // Vista principal: listar
        List<biopreparado> lista = dao.listarBiopreparados();
        req.setAttribute("listaBiopreparados", lista);
        req.getRequestDispatcher("/templates/administrador/biopreparados.jsp")
           .forward(req, res);
    }

    // ── POST: registrar o actualizar ──────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (!sesionAdminValida(req, res)) return;

        req.setCharacterEncoding("UTF-8");

        String idStr        = req.getParameter("id");
        String nombre       = req.getParameter("nombreBio");
        String descripcion  = req.getParameter("descripcionBio");
        String precioStr    = req.getParameter("precioBio");
        String fCreacionStr = req.getParameter("fechaCreacion");
        String fVencStr     = req.getParameter("fechaVencimiento");
        String preparacion  = req.getParameter("preparacionBio");

        // Validación básica de campos obligatorios
        if (estaVacio(nombre) || estaVacio(fCreacionStr) || estaVacio(fVencStr)) {
            redirigirConError(req, res, "Nombre y fechas son obligatorios.");
            return;
        }

        Date fCreacion, fVencimiento;
        double precio = 0;
        try {
            fCreacion    = Date.valueOf(fCreacionStr);
            fVencimiento = Date.valueOf(fVencStr);
            if (!estaVacio(precioStr)) precio = Double.parseDouble(precioStr);
        } catch (IllegalArgumentException e) {
            redirigirConError(req, res, "Formato de fecha o precio inválido.");
            return;
        }

        if (fVencimiento.before(fCreacion)) {
            redirigirConError(req, res, "La fecha de vencimiento debe ser posterior a la de creación.");
            return;
        }

        // Construir biopreparado
        biopreparado b = new biopreparado(nombre.trim(),
                descripcion != null ? descripcion.trim() : "",
                precio, fCreacion, fVencimiento,
                preparacion != null ? preparacion.trim() : "");

        // Construir lista de ingredientes desde parámetros múltiples
        List<ingredienteBio> ingredientes = parsearIngredientes(req);

        BiopreparadoDao dao = new BiopreparadoDao();
        boolean ok;
        if (idStr != null && !idStr.isBlank()) {
            ok = dao.actualizarBiopreparado(Integer.parseInt(idStr), b, ingredientes);
        } else {
            ok = dao.registrarBiopreparado(b, ingredientes);
        }

        res.sendRedirect("ServletBiopreparado?status=" + (ok ? "success" : "error"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Lee los arrays de parámetros nombresIng[], cantidadesIng[], unidadesIng[]
     * enviados por el formulario y los convierte en ingredienteBio.
     */
    private List<ingredienteBio> parsearIngredientes(HttpServletRequest req) {
        List<ingredienteBio> lista = new ArrayList<>();
        String[] nombres    = req.getParameterValues("nombresIng[]");
        String[] cantidades = req.getParameterValues("cantidadesIng[]");
        String[] unidades   = req.getParameterValues("unidadesIng[]");

        if (nombres == null) return lista;

        for (int i = 0; i < nombres.length; i++) {
            String nom = nombres[i];
            if (nom == null || nom.isBlank()) continue;
            double cant = 0;
            try {
                if (cantidades != null && i < cantidades.length)
                    cant = Double.parseDouble(cantidades[i]);
            } catch (NumberFormatException ignored) {}
            String uni = (unidades != null && i < unidades.length) ? unidades[i] : "";
            lista.add(new ingredienteBio(nom.trim(), cant, uni != null ? uni.trim() : ""));
        }
        return lista;
    }

    private boolean estaVacio(String s) {
        return s == null || s.isBlank();
    }

    private void redirigirConError(HttpServletRequest req, HttpServletResponse res,
                                   String msg) throws IOException {
        res.sendRedirect("ServletBiopreparado?status=error&msg=" +
                java.net.URLEncoder.encode(msg, "UTF-8"));
    }
}