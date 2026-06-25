package com.paujil.controlador;

import com.paujil.dao.AsignacionDao;
import com.paujil.dao.CultivoDao;
import com.paujil.dao.LoteDao;
import com.paujil.modelo.cultivo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import java.io.IOException;
import java.sql.Date;
import static com.paujil.utils.ServletUtils.*;

@WebServlet("/ServletCultivo")
// Define la clase como un componente web ejecutable (Servlet)
public class ServletCultivo extends HttpServlet { 

    // Constante de seguridad: impide el desbordamiento limitando la longitud
    private static final int MAX_NOMBRE = 80; 
    // Constante de seguridad: cota superior para el tipo de cultivo
    private static final int MAX_TIPO   = 60; 

    @Override // Sobrescribe el comportamiento predeterminado
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { // Maneja peticiones GET (visualización)

        String accion = request.getParameter("accion"); // Captura intención del usuario
        CultivoDao dao = new CultivoDao(); // Instancia el puente a la BD

        if ("verTrabajador".equals(accion)) { // Vista para trabajadores
            if (!verificarSesionUsuario(request, response)) return; // Guardia de autenticación
            request.setAttribute("listaCultivos", dao.listarCultivos()); // Carga cultivos.
            request.setAttribute("catalogoLotes", new LoteDao().listarLotes()); // Carga lotes para mostrar el nombre en la vista.
            request.getRequestDispatcher("/templates/trabajador/cultivos_trabajador.jsp").forward(request, response);
            return;
        }

        if ("eliminar".equals(accion)) { // Lógica para remover un cultivo
            String idStr = request.getParameter("id");
            if (!estaVacio(idStr)) {
                try {
                    int id = Integer.parseInt(idStr);

                    // Bloquea si el cultivo tiene asignaciones activas.
                    if (new AsignacionDao().tieneAsignacionesActivasPorCultivo(id)) {
                        response.sendRedirect(request.getContextPath() +
                            "/ServletCultivo?status=error&motivo=asignaciones_activas");
                        return;
                    }

                    dao.eliminarCultivo(id); // Solo llega aquí si no tiene asignaciones activas.
                } catch (NumberFormatException ignored) {}
            }
            response.sendRedirect(request.getContextPath() + "/ServletCultivo?status=eliminado");
            return;
        }

        // Carga el catálogo necesario para la interfaz de administración
        request.setAttribute("listaCultivos", dao.listarCultivos());
        request.setAttribute("catalogoLotes", new LoteDao().listarLotes());
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp").forward(request, response);
    }

    @Override // Sobrescribe método POST para operaciones de escritura
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        if (!verificarSesionAdmin(request, response)) return; // Filtro de permisos

        String idStr = request.getParameter("id"); // ID para actualización
        String nombre = sanitizar(request.getParameter("nombreCultivo")); // Limpieza de datos
        String tipo = sanitizar(request.getParameter("tipoCultivo")); 
        String fSiembraStr = sanitizar(request.getParameter("fechaSiembra"));
        String fCosechaStr = sanitizar(request.getParameter("fechaCosecha"));
        String idLoteStr = request.getParameter("idLote"); // ID del lote seleccionado

        // Validación defensiva: verifica obligatoriedad y límites de caracteres
        if (estaVacio(nombre) || estaVacio(fSiembraStr) || estaVacio(idLoteStr) || 
            excedeLongitud(nombre, MAX_NOMBRE) || excedeLongitud(tipo, MAX_TIPO)) {
            reenviarAdminConError("Datos inválidos o incompletos.", request, response);
            return;
        }

        // Validación: el nombre del cultivo no puede estar compuesto únicamente por dígitos.
        if (nombre.matches("\\d+")) { // Verifica si el nombre es solo números.
            reenviarAdminConError("El nombre del cultivo no puede contener solo números. Incluya al menos una letra.", request, response); // Rechaza.
            return; // Aborta.
        }

        // Validación: el tipo del cultivo, si se ingresa, no puede estar compuesto únicamente por dígitos.
        if (tipo != null && !tipo.isEmpty() && tipo.matches("\\d+")) { // Solo valida si tipo fue ingresado.
            reenviarAdminConError("El tipo de cultivo no puede contener solo números. Incluya al menos una letra.", request, response); // Rechaza.
            return; // Aborta.
        }

        try { 
            // 1. Parseo y validación de fechas
            Date fSiembra = Date.valueOf(fSiembraStr);
            Date fCosecha = estaVacio(fCosechaStr) ? null : Date.valueOf(fCosechaStr);
            int idLote = Integer.parseInt(idLoteStr);

            if (fSiembra.toLocalDate().isAfter(java.time.LocalDate.now())) { // La siembra no puede ser posterior a hoy.
                reenviarAdminConError("La fecha de siembra no puede ser posterior a hoy.", request, response);
                return;
            }

            if (fCosecha != null && fCosecha.toLocalDate().isBefore(fSiembra.toLocalDate())) {
                reenviarAdminConError("La cosecha no puede ser anterior a la siembra.", request, response);
                return;
            }

            // 2. Ejecución de lógica en BD
            CultivoDao dao = new CultivoDao();
            if (!estaVacio(idStr)) { 
                dao.actualizarCultivo(Integer.parseInt(idStr), nombre, tipo, fSiembra, fCosecha, idLote);
            } else { 
                dao.registrarCultivo(new cultivo(nombre, tipo, fSiembra, fCosecha, idLote));
            }
            
            // 3. PRG: Redirección post-éxito
            response.sendRedirect("ServletCultivo"); 
            return; // Importante el return para salir del método tras el redirect

        } catch (Exception e) {
            e.printStackTrace();

            String mensajePersonalizado = "Ocurrió un error inesperado.";

            // Detectamos si es un error de clave duplicada (muy común en SQL)
            if (e.getMessage().contains("Duplicate entry")) {
                mensajePersonalizado = "El nombre del cultivo ya existe en este lote. Por favor, elige otro nombre.";
            } else if (e.getMessage().contains("foreign key")) {
                mensajePersonalizado = "No se puede realizar la operación porque depende de otros registros (lotes) que no existen.";
            }

            request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
            request.setAttribute("catalogoLotes", new LoteDao().listarLotes());
            request.setAttribute("mensajeError", mensajePersonalizado); // Aquí enviamos el mensaje amigable

            request.getRequestDispatcher("/templates/administrador/cultivos.jsp").forward(request, response);
        }
    }

    private void reenviarAdminConError(String mensaje, HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
    
        // Recargamos la información necesaria para que la página no se vea vacía
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        request.setAttribute("catalogoLotes", new LoteDao().listarLotes());

        // Enviamos el mensaje de error
        request.setAttribute("mensajeError", mensaje);

        // Forward mantiene el request, por lo tanto el mensaje llega al JSP
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp").forward(request, response);
    }
    
    private String sanitizar(String valor) { 
        return (valor != null) ? valor.trim() : null; 
    }

    private boolean excedeLongitud(String valor, int max) { 
        return valor != null && valor.length() > max; 
    }
}