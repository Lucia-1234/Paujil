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

// Filtro que intercepta rutas administrativas y gestores de roles.
@WebFilter({"/templates/administrador/*", "/GestionarRoles"}) 
public class FiltroAdministrador implements Filter { // Implementa interfaz Filter.

    @Override // Sobrescribe doFilter.
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        // Conversión a tipos HTTP para acceder a la sesión.
        HttpServletRequest req = (HttpServletRequest) request; 
        HttpServletResponse res = (HttpServletResponse) response; 

        // Obtiene sesión existente (sin crear una nueva).
        HttpSession session = req.getSession(false); 

        // Verifica si la sesión no es nula y el rol corresponde a 'administrador'.
        boolean esAdmin = (session != null && "administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario"))); 

        if (esAdmin) { // Si el acceso es válido.
            chain.doFilter(request, response); // Permite continuar la petición.
        } else { // Si el acceso es denegado.
            // Redirige al login.
            res.sendRedirect(req.getContextPath() + "/templates/login.jsp?error=acceso_denegado"); 
        } // Fin if-else.
    } // Fin doFilter.
} // Fin clase.