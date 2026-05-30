package com.paujil.controlador;

import com.paujil.dao.BiopreparadoDao;
import com.paujil.modelo.biopreparado;
import com.paujil.modelo.biopreparado.ingredienteBio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;

@WebServlet("/ServletBiopreparado")
public class ServletBiopreparado extends HttpServlet {

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        String accion = req.getParameter("accion");
        BiopreparadoDao dao = new BiopreparadoDao();

        // ── Vista TRABAJADOR: solo lectura ─────────────────────────────────
        if ("verTrabajador".equals(accion)) {
            if (!verificarSesionUsuario(req, res)) return;
            req.setAttribute("listaBiopreparados", dao.listarBiopreparados());
            req.getRequestDispatcher("/templates/trabajador/biopreparados_trabajador.jsp")
               .forward(req, res);
            return;
        }

        // ── Todo lo demás: solo ADMINISTRADOR ─────────────────────────────
        if (!verificarSesionAdmin(req, res)) return;

        if ("eliminar".equals(accion)) {
            String idStr = req.getParameter("id");
            if (!estaVacio(idStr)) {
                try {
                    dao.eliminarBiopreparado(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {}
            }
            res.sendRedirect("ServletBiopreparado");
            return;
        }

        // Listar para admin
        req.setAttribute("listaBiopreparados", dao.listarBiopreparados());
        req.getRequestDispatcher("/templates/administrador/biopreparados.jsp")
           .forward(req, res);
    }

    // ── POST: solo ADMINISTRADOR ──────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!verificarSesionAdmin(req, res)) return;

        req.setCharacterEncoding("UTF-8");

        String idStr        = req.getParameter("id");
        String nombre       = req.getParameter("nombreBio");
        String descripcion  = req.getParameter("descripcionBio");
        String precioStr    = req.getParameter("precioBio");
        String fCreacionStr = req.getParameter("fechaCreacion");
        String fVencStr     = req.getParameter("fechaVencimiento");
        String preparacion  = req.getParameter("preparacionBio");

        if (estaVacio(nombre) || estaVacio(fCreacionStr) || estaVacio(fVencStr)) {
            redirigirConError(res, "Nombre y fechas son obligatorios.");
            return;
        }

        Date fCreacion, fVencimiento;
        double precio = 0;
        try {
            fCreacion    = Date.valueOf(fCreacionStr);
            fVencimiento = Date.valueOf(fVencStr);
            if (!estaVacio(precioStr)) precio = Double.parseDouble(precioStr);
        } catch (IllegalArgumentException e) {
            redirigirConError(res, "Formato de fecha o precio inválido.");
            return;
        }

        if (fVencimiento.before(fCreacion)) {
            redirigirConError(res, "La fecha de vencimiento debe ser posterior a la de creación.");
            return;
        }

        biopreparado b = new biopreparado(
                nombre.trim(),
                descripcion  != null ? descripcion.trim()  : "",
                precio,
                fCreacion,
                fVencimiento,
                preparacion  != null ? preparacion.trim()  : "");

        List<ingredienteBio> ingredientes = parsearIngredientes(req);
        BiopreparadoDao dao = new BiopreparadoDao();
        boolean ok;

        if (!estaVacio(idStr)) {
            ok = dao.actualizarBiopreparado(Integer.parseInt(idStr), b, ingredientes);
        } else {
            ok = dao.registrarBiopreparado(b, ingredientes);
        }

        res.sendRedirect("ServletBiopreparado?status=" + (ok ? "success" : "error"));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
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

    private void redirigirConError(HttpServletResponse res, String msg) throws IOException {
        res.sendRedirect("ServletBiopreparado?status=error&msg=" +
                java.net.URLEncoder.encode(msg, "UTF-8"));
    }
}