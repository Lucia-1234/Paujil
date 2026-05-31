package com.paujil.utils;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

// Intercepta todas las peticiones de la aplicacion gracias al patron "/*"; garantiza que ningun recurso quede cacheado en el cliente
@WebFilter("/*")
public class FiltroNoCache implements Filter {

    // Punto de intercepcion de cada peticion HTTP; se ejecuta antes de que la solicitud llegue al Servlet destino
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Convierte la respuesta generica a su tipo HTTP para acceder a la API de cabeceras; el cast es seguro porque el filtro solo se activa en contextos HTTP
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Instruye a navegadores modernos (HTTP 1.1) a no almacenar la respuesta en ningun cache, ni siquiera temporal; must-revalidate fuerza revalidacion con el servidor antes de usar cualquier copia local
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        // Cabecera de compatibilidad con clientes HTTP 1.0 que no interpretan Cache-Control; asegura que proxies y navegadores antiguos tampoco cacheen la respuesta
        httpResponse.setHeader("Pragma", "no-cache"); // HTTP 1.0
        // Establece la fecha de expiracion en el epoch (1 enero 1970) para indicar a proxies intermedios que la respuesta ya expiro; valor 0 garantiza que ningun proxy la sirva desde cache
        httpResponse.setDateHeader("Expires", 0); // Proxies

        // Transfiere el control al siguiente filtro en la cadena o al Servlet destino si no hay mas filtros; sin esta llamada la peticion quedaria bloqueada en este filtro
        chain.doFilter(request, response);
    }
}