package com.paujil.controlador;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// Se eliminó /ServletTrabajo de aquí porque ese servlet maneja
// tanto acciones de admin como de trabajador, y ya tiene sus
// propias guardas de sesión internas por acción.
@WebFilter({"/templates/administrador/*", "/GestionarRoles", "/ServletCultivo"})
public class FiltroAdministrador implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        boolean esAdmin = (session != null
                && "administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario")));

        if (esAdmin) {
            chain.doFilter(request, response);
        } else {
            res.sendRedirect(req.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
        }
    }
}