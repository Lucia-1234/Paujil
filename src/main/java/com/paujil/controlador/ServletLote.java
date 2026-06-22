package com.paujil.controlador;

import com.paujil.dao.LoteDao;
import com.paujil.modelo.lote;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;

@WebServlet("/ServletLote")
// Define la clase como un componente web ejecutable (Servlet)
public class ServletLote extends HttpServlet {

    // Constante de seguridad: impide el desbordamiento de búfer limitando la longitud del nombre
    private static final int MAX_NOMBRE = 80;

    @Override // Maneja peticiones HTTP GET (visualización)
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesionAdmin(request, response)) return; // Filtro de seguridad: solo administradores

        String accion = request.getParameter("accion"); // Captura la intención del usuario
        LoteDao dao = new LoteDao(); // Instancia el puente hacia la base de datos de lotes

        if ("eliminar".equals(accion)) { // Lógica para remover un lote
            String idStr = request.getParameter("id"); // Captura el ID a eliminar
            if (!estaVacio(idStr)) { // Valida si el ID no es nulo
                try { // Intenta borrar
                    dao.eliminarLote(Integer.parseInt(idStr)); // Ejecuta operación en el DAO (limpia relaciones primero)
                } catch (NumberFormatException ignored) {} // Ignora error si el ID está corrupto
            }
            response.sendRedirect(request.getContextPath() + "/ServletLote?status=eliminado"); // Redirección PRG
            return; // Termina
        }

        List<lote> lista = dao.listarLotes(); // Carga la lista para el panel de administración
        request.setAttribute("listaLotes", lista); // Pasa la lista de lotes al JSP
        request.getRequestDispatcher("/templates/administrador/lotes.jsp").forward(request, response); // Renderiza vista
    }

    @Override // Sobrescribe método POST para operaciones de escritura/modificación
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!verificarSesionAdmin(request, response)) return; // Verifica permisos de administrador

        String idStr = request.getParameter("id"); // Obtiene el ID si es una actualización
        String nombre = sanitizar(request.getParameter("nombreLote")); // Limpia el nombre del lote

        // Validación defensiva (Guardia de Seguridad): rechaza peticiones maliciosas o incompletas
        if (estaVacio(nombre) || excedeLongitud(nombre, MAX_NOMBRE)) {
            reenviarAdminConError("Solicitud inválida.", request, response); // Rechaza con mensaje genérico
            return; // Aborta
        }

        LoteDao dao = new LoteDao(); // Prepara el acceso a datos
        if (!estaVacio(idStr)) { // Si recibimos ID, operamos como actualización (UPDATE)
            try { // Intenta la actualización
                int id = Integer.parseInt(idStr); // Convierte el ID recibido.
                if (dao.existeNombreLote(nombre, id)) { // Valida que no exista otro lote con el mismo nombre.
                    reenviarAdminConError("Ya existe un lote con ese nombre. Por favor use un nombre diferente.", request, response); // Rechaza duplicado.
                    return; // Aborta.
                }
                dao.actualizarLote(id, nombre); // Persiste cambios
            } catch (NumberFormatException e) { // Maneja error si el ID está malformado
                reenviarAdminConError("Solicitud inválida.", request, response); // Rechaza
                return; // Aborta
            }
        } else { // Si no hay ID, operamos como registro nuevo (CREATE)
            if (dao.existeNombreLote(nombre, 0)) { // Valida que no exista ya un lote con ese nombre.
                reenviarAdminConError("Ya existe un lote con ese nombre. Por favor use un nombre diferente.", request, response); // Rechaza duplicado.
                return; // Aborta.
            }
            dao.registrarLote(new lote(nombre)); // Inserta en base de datos
        }
        response.sendRedirect("ServletLote"); // Redirección final para limpiar el formulario en el navegador
    }

    // --- MÉTODOS AUXILIARES ---

    private String sanitizar(String valor) { // Elimina espacios basura al principio y final
        return (valor != null) ? valor.trim() : null; // Retorna null si el valor original lo era
    }

    private boolean excedeLongitud(String valor, int max) { // Valida longitud para prevenir desbordamientos
        return valor != null && valor.length() > max; // Devuelve true si la cadena es demasiado larga
    }

    private void reenviarAdminConError(String mensaje, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException { // Centraliza la lógica de error y recarga de vista
        request.setAttribute("mensajeError", mensaje); // Establece el mensaje visible para el admin
        request.setAttribute("listaLotes", new LoteDao().listarLotes()); // Refresca listado para no perder estado
        request.getRequestDispatcher("/templates/administrador/lotes.jsp").forward(request, response); // Muestra error
    }
}