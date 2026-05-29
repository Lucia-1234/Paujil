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

// Protegemos tanto la carpeta física de JSP como los Servlets de acciones de administrador
@WebFilter({"/templates/administrador/*", "/GestionarRoles", "/ServletCultivo", "/ServletTrabajo"})
public class FiltroAdministrador implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false); // No creamos sesión nueva, solo revisamos la actual

        // 1. Verificamos si existe sesión y si el rol es 'administrador'
        boolean esAdmin = (session != null && "administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario")));

        // 2. Si es administrador, permitimos continuar
        if (esAdmin) {
            chain.doFilter(request, response);
        } else {
            // 3. Si no, lo mandamos al login
            // Es buena práctica añadir un parámetro de error para que el usuario sepa por qué fue redirigido
            res.sendRedirect(req.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
        }
    }
}