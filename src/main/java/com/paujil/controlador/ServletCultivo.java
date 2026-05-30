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

// Importaciones estaticas para reducir acoplamiento y centralizar utilidades transversales
import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;

@WebServlet("/ServletCultivo")
public class ServletCultivo extends HttpServlet {

    //  GET 
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Discriminador de rutas: determina que operacion y que vista corresponden a esta solicitud
        String accion = request.getParameter("accion");
        CultivoDao dao = new CultivoDao();

        //  Vista del TRABAJADOR: solo lectura 
        if ("verTrabajador".equals(accion)) {
            // Acceso limitado a sesion de usuario comun, el trabajador no puede modificar cultivos
            if (!verificarSesionUsuario(request, response)) return;
            request.setAttribute("listaCultivos", dao.listarCultivos());
            request.getRequestDispatcher("/templates/trabajador/cultivos_trabajador.jsp")
                   .forward(request, response);
            return;
        }

        //   ADMINISTRADOR 
        // Cualquier accion no identificada como trabajador requiere privilegios de admin
        if (!verificarSesionAdmin(request, response)) return;

        if ("eliminar".equals(accion)) {
            String idStr = request.getParameter("id");
            if (!estaVacio(idStr)) {
                try {
                    dao.eliminarCultivo(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {
                    // ID no numerico se descarta, la redireccion siguiente refresca el listado sin eliminar
                }
            }
            // Redirect-after-action evita que recargar la pagina repita la eliminacion
            response.sendRedirect("ServletCultivo");
            return;
        }

        // Caso por defecto para admin: muestra el listado completo con opciones de gestion
        request.setAttribute("listaCultivos", dao.listarCultivos());
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }

    //  POST: solo ADMINISTRADOR 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesionAdmin(request, response)) return;

        String idStr       = request.getParameter("id");
        String nombre      = request.getParameter("nombreCultivo");
        String tipo        = request.getParameter("tipoCultivo");
        String fSiembraStr = request.getParameter("fechaSiembra");
        String fCosechaStr = request.getParameter("fechaCosecha");

        // La cosecha es opcional (cultivo puede estar en curso), pero siembra y tipo son datos mínimos del negocio
        if (estaVacio(nombre) || estaVacio(tipo) || estaVacio(fSiembraStr)) {
            reenviarAdminConError("Nombre, tipo y fecha de siembra son obligatorios.",
                                  request, response);
            return;
        }

        Date fSiembra;
        Date fCosecha;
        try {
            // Date.valueOf requiere formato "yyyy-MM-dd"; el mensaje de error orienta al usuario a usar el selector
            fSiembra = Date.valueOf(fSiembraStr);
            // Fecha de cosecha nula modela un cultivo activo sin fecha de termino definida
            fCosecha = estaVacio(fCosechaStr) ? null : Date.valueOf(fCosechaStr);
        } catch (IllegalArgumentException e) {
            reenviarAdminConError("Formato de fecha inválido. Use el selector de fechas.",
                                  request, response);
            return;
        }

        CultivoDao dao = new CultivoDao();
        // La presencia de "id" diferencia entre actualizar un cultivo existente y registrar uno nuevo
        if (!estaVacio(idStr)) {
            dao.actualizarCultivo(Integer.parseInt(idStr), nombre.trim(),
                                  tipo.trim(), fSiembra, fCosecha);
        } else {
            dao.registrarCultivo(new cultivo(nombre.trim(), tipo.trim(), fSiembra, fCosecha));
        }

        // Redirige sin parametros de estado porque los errores de BD no se manejan aqui explicitamente
        response.sendRedirect("ServletCultivo");
    }

    //  Helper 

    // Usa forward en lugar de redirect para preservar el mensaje de error en el scope de request,
    // que no sobrevive una redireccion HTTP
    private void reenviarAdminConError(String mensaje,
                                       HttpServletRequest request,
                                       HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        // Recarga el listado para que la vista pueda renderizar la tabla junto al mensaje de error
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }
}