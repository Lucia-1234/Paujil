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

// Importaciones estáticas para reducir acoplamiento y centralizar utilidades transversales
import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;

@WebServlet("/ServletBiopreparado")
public class ServletBiopreparado extends HttpServlet {

    // GET 
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        // El parametro "accion" actua como discriminador de rutas dentro del mismo endpoint GET
        String accion = req.getParameter("accion");
        BiopreparadoDao dao = new BiopreparadoDao();

        //  Vista TRABAJADOR: solo lectura 
        if ("verTrabajador".equals(accion)) {
            // Verifica sesion de usuario comun, redirige al login si no la hay
            if (!verificarSesionUsuario(req, res)) return;
            req.setAttribute("listaBiopreparados", dao.listarBiopreparados());
            req.getRequestDispatcher("/templates/trabajador/biopreparados_trabajador.jsp")
               .forward(req, res);
            return;
        }

        //  ADMINISTRADOR 
        // Barrera de seguridad: cualquier accion no identificada requiere rol admin
        if (!verificarSesionAdmin(req, res)) return;

        if ("eliminar".equals(accion)) {
            String idStr = req.getParameter("id");
            if (!estaVacio(idStr)) {
                try {
                    dao.eliminarBiopreparado(Integer.parseInt(idStr));
                } catch (NumberFormatException ignored) {
                    // ID malformado, se descarta silenciosamente y se redirige sin eliminar
                }
            }
            // Redirect-after-action previene reenvio del formulario al recargar la pagina
            res.sendRedirect("ServletBiopreparado");
            return;
        }

        // Caso por defecto del admin: mostrar listado completo en la vista de gestion
        req.setAttribute("listaBiopreparados", dao.listarBiopreparados());
        req.getRequestDispatcher("/templates/administrador/biopreparados.jsp")
           .forward(req, res);
    }

    // POST: ADMINISTRADOR 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (!verificarSesionAdmin(req, res)) return;

        // Fuerza UTF-8 antes de leer cualquier parámetro para evitar corrupción de caracteres especiales
        req.setCharacterEncoding("UTF-8");

        String idStr        = req.getParameter("id");
        String nombre       = req.getParameter("nombreBio");
        String descripcion  = req.getParameter("descripcionBio");
        String precioStr    = req.getParameter("precioBio");
        String fCreacionStr = req.getParameter("fechaCreacion");
        String fVencStr     = req.getParameter("fechaVencimiento");
        String preparacion  = req.getParameter("preparacionBio");

        // Validacion minima: nombre y fechas son los campos que definen la identidad del biopreparado
        if (estaVacio(nombre) || estaVacio(fCreacionStr) || estaVacio(fVencStr)) {
            redirigirConError(res, "Nombre y fechas son obligatorios.");
            return;
        }

        Date fCreacion, fVencimiento;
        double precio = 0;
        try {
            // Date.valueOf espera formato ISO "yyyy-MM-dd", cualquier otra forma lanza IllegalArgumentException
            fCreacion    = Date.valueOf(fCreacionStr);
            fVencimiento = Date.valueOf(fVencStr);
            // Precio es opcional, solo se parsea si fue enviado
            if (!estaVacio(precioStr)) precio = Double.parseDouble(precioStr);
        } catch (IllegalArgumentException e) {
            redirigirConError(res, "Formato de fecha o precio inválido.");
            return;
        }

        // Regla de negocio: un biopreparado no puede vencer antes de ser creado
        if (fVencimiento.before(fCreacion)) {
            redirigirConError(res, "La fecha de vencimiento debe ser posterior a la de creación.");
            return;
        }

        // Campos opcionales se normalizan a cadena vacia para evitar null en la capa de persistencia
        biopreparado b = new biopreparado(
                nombre.trim(),
                descripcion  != null ? descripcion.trim()  : "",
                precio,
                fCreacion,
                fVencimiento,
                preparacion  != null ? preparacion.trim()  : "");

        // Los ingredientes se extraen por separado porque son una coleccion de entidades anidadas
        List<ingredienteBio> ingredientes = parsearIngredientes(req);
        BiopreparadoDao dao = new BiopreparadoDao();
        boolean ok;

        // La presencia de "id" distingue entre edicion de registro existente y creación de uno nuevo
        if (!estaVacio(idStr)) {
            ok = dao.actualizarBiopreparado(Integer.parseInt(idStr), b, ingredientes);
        } else {
            ok = dao.registrarBiopreparado(b, ingredientes);
        }

        // El parametro "status" permite que la vista destino muestre retroalimentacion al usuario
        res.sendRedirect("ServletBiopreparado?status=" + (ok ? "success" : "error"));
    }

    //  Helpers 

    // Construye la lista de ingredientes correlacionando tres arrays paralelos enviados por el formulario
    private List<ingredienteBio> parsearIngredientes(HttpServletRequest req) {
        List<ingredienteBio> lista = new ArrayList<>();
        String[] nombres    = req.getParameterValues("nombresIng[]");
        String[] cantidades = req.getParameterValues("cantidadesIng[]");
        String[] unidades   = req.getParameterValues("unidadesIng[]");

        // Si no se enviaron nombres, no hay ingredientes que procesar
        if (nombres == null) return lista;

        for (int i = 0; i < nombres.length; i++) {
            String nom = nombres[i];
            // Ingredientes con nombre vacío se omiten para evitar registros huerfanos en BD
            if (nom == null || nom.isBlank()) continue;
            double cant = 0;
            try {
                // Guarda de indice: los arrays pueden tener longitudes distintas si el formulario es dinamico
                if (cantidades != null && i < cantidades.length)
                    cant = Double.parseDouble(cantidades[i]);
            } catch (NumberFormatException ignored) {
                // Cantidad no numerica se trata como cero, el ingrediente se registra igualmente
            }
            // Unidad tambien es opcional, se normaliza a vacio si esta ausente
            String uni = (unidades != null && i < unidades.length) ? unidades[i] : "";
            lista.add(new ingredienteBio(nom.trim(), cant, uni != null ? uni.trim() : ""));
        }
        return lista;
    }

    // Encapsula la redireccion de error para no repetir la logica de URLEncoder en cada punto de fallo
    private void redirigirConError(HttpServletResponse res, String msg) throws IOException {
        // URLEncoder garantiza que el mensaje llegue integro aunque contenga caracteres especiales o espacios
        res.sendRedirect("ServletBiopreparado?status=error&msg=" +
                java.net.URLEncoder.encode(msg, "UTF-8"));
    }
}