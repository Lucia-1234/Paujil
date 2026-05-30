package com.paujil.utils;

import com.paujil.modelo.Rol;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

public final class ServletUtils {

    private ServletUtils() {}

    public static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }

    public static String obtenerParam(HttpServletRequest req, String nombre) {
        String v = req.getParameter(nombre);
        return (v != null && !v.isBlank()) ? v.trim() : null;
    }

    public static Optional<Integer> obtenerParamEntero(HttpServletRequest req, String nombre) {
        try {
            return Optional.of(Integer.parseInt(req.getParameter(nombre)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Optional<java.sql.Date> parsearFecha(String fechaStr) {
        try {
            return Optional.of(java.sql.Date.valueOf(fechaStr));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static boolean verificarSesionAdmin(HttpServletRequest req,
                                               HttpServletResponse res) throws IOException {
        return verificarSesion(req, res, Rol.ADMINISTRADOR);
    }

    public static boolean verificarSesionUsuario(HttpServletRequest req,
                                                 HttpServletResponse res) throws IOException {
        return verificarSesion(req, res, null);
    }

    public static boolean verificarSesion(HttpServletRequest req,
                                          HttpServletResponse res,
                                          Rol rolRequerido) throws IOException {
        HttpSession session = req.getSession(false);
        boolean valida = session != null && session.getAttribute("idUsuario") != null;

        if (valida && rolRequerido != null) {
            String rolSesion = (String) session.getAttribute("rolUsuario");
            valida = rolRequerido.getNombre().equalsIgnoreCase(rolSesion);
        }

        if (!valida) {
            res.sendRedirect(req.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
        }
        return valida;
    }

    public static void reenviarConError(String mensaje, String vista,
                                        HttpServletRequest req, HttpServletResponse res,
                                        String... camposAPreservar)
            throws ServletException, IOException {
        req.setAttribute("mensajeError", mensaje);
        for (String campo : camposAPreservar) {
            req.setAttribute(campo, req.getParameter(campo));
        }
        req.getRequestDispatcher(vista).forward(req, res);
    }
}