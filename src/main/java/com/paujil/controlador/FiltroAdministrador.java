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

// Intercepta todas las rutas de administrador antes de que el servlet las procese
@WebFilter({"/templates/administrador/*", "/GestionarRoles"})
public class FiltroAdministrador implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Downcast necesario para acceder a metodos HTTP especificos como getSession() y getContextPath()
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // false evita crear una sesion nueva si no existe, una sesion ausente implica usuario no autenticado
        HttpSession session = req.getSession(false);

        // Valida simultaneamente que haya sesion activa y que el rol almacenado sea "administrador"
        // equalsIgnoreCase protege contra variaciones de capitalizacion en el valor del atributo
        boolean esAdmin = (session != null
                && "administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario")));

        if (esAdmin) {
            // El usuario tiene privilegios suficientes, pasa el control al siguiente filtro o servlet destino
            chain.doFilter(request, response);
        } else {
            // Redirige al login con un parametro de error que la vista puede usar para mostrar un mensaje contextual
            // getContextPath() garantiza que la ruta sea correcta independientemente del contexto de despliegue
            res.sendRedirect(req.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
        }
    }
}