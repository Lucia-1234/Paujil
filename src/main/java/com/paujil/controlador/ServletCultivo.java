package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.dao.AsignacionDao;
import com.paujil.modelo.cultivo;
import com.paujil.modelo.asignacion;
import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;

import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Define la clase como un componente web ejecutable (Servlet)
public class ServletCultivo extends HttpServlet { 

    // Constante de seguridad: impide el desbordamiento de búfer limitando la longitud de los datos de entrada
    private static final int MAX_NOMBRE = 80; 
    // Constante de seguridad: establece una cota superior para el tipo de cultivo
    private static final int MAX_TIPO   = 60; 

    @Override // Indica que sobreescribimos el comportamiento predeterminado del contenedor web
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { // Maneja peticiones HTTP GET (visualización)

        String accion = request.getParameter("accion"); // Captura la intención del usuario (ej: "eliminar", "historial")
        CultivoDao dao = new CultivoDao(); // Instancia el puente hacia la base de datos de cultivos

        if ("verTrabajador".equals(accion)) { // Evalúa si el usuario es un trabajador solicitando su vista
            if (!verificarSesionUsuario(request, response)) return; // Guardia de autenticación; si falla, detiene el flujo
            request.setAttribute("listaCultivos", dao.listarCultivos()); // Carga los cultivos autorizados en el request
            request.getRequestDispatcher("/templates/trabajador/cultivos_trabajador.jsp") // Define el destino de la vista
                   .forward(request, response); // Renderiza la página para el usuario
            return; // Termina la ejecución para evitar procesamientos posteriores
        }

        if (!verificarSesionAdmin(request, response)) return; // Filtro de seguridad crítico; bloquea accesos no administrativos

        if ("historial".equals(accion)) { // Lógica para el historial de labores
            String idStr = request.getParameter("id"); // Obtiene el identificador único del cultivo
            if (estaVacio(idStr)) { // Validación de integridad: redirige si el ID es nulo
                response.sendRedirect(request.getContextPath() + "/ServletCultivo"); // Redirección hacia el listado principal
                return; // Finaliza
            }
            try { // Bloque de control para asegurar que el ID sea numérico
                int idCultivo = Integer.parseInt(idStr); // Conversión segura del parámetro string a entero
                AsignacionDao asignacionDao = new AsignacionDao(); // Instancia el DAO para obtener labores
                request.setAttribute("historialCultivo", asignacionDao.listarPorCultivo(idCultivo)); // Consulta y vincula el historial
                request.setAttribute("idCultivoActivo",  idCultivo); // Marca el cultivo para el front-end
            } catch (NumberFormatException e) { // Manejo silencioso: si el ID no es válido, no cargamos historial
            } 
            request.setAttribute("listaCultivos", dao.listarCultivos()); // Refresca el listado general
            request.getRequestDispatcher("/templates/administrador/cultivos.jsp") // Envía a la vista del administrador
                   .forward(request, response); // Renderiza la página
            return; // Termina ejecución
        }

        if ("historialJson".equals(accion)) { // Endpoint técnico para comunicación asíncrona (AJAX)
            response.setContentType("application/json; charset=UTF-8"); // Define el tipo de contenido como JSON
            String idStr = request.getParameter("id"); // Extrae el ID del cultivo
            if (estaVacio(idStr)) { // Si falta el ID, retorna respuesta vacía para prevenir errores
                response.getWriter().write("[]"); // Respuesta JSON vacía
                return; // Termina
            }
            try { // Inicia lógica de serialización de datos
                int idCultivo = Integer.parseInt(idStr); // Conversión a entero
                List<asignacion> lista = new AsignacionDao().listarPorCultivo(idCultivo); // Obtiene lista de labores
                StringBuilder json = new StringBuilder("["); // Construye manualmente el array JSON para mayor control
                for (int i = 0; i < lista.size(); i++) { // Itera sobre los resultados
                    asignacion a = lista.get(i); // Accede al objeto de asignación actual
                    if (i > 0) json.append(","); // Inserta separador de elementos
                    json.append("{") // Abre objeto JSON
                        .append("\"idTrabajoRealizado\":").append(a.getId()).append(",") // Mapeo de campos
                        .append("\"descripcionTrabajo\":\"").append(escaparJson(a.getDescripcionTrabajo())).append("\",") // Sanitiza texto
                        .append("\"nombreUsuario\":\"").append(escaparJson(a.getNombreUsuario())).append("\",") // Sanitiza nombre
                        .append("\"fechaInicio\":\"").append(a.getFechaInicio()).append("\",") // Asigna fecha inicio
                        .append("\"fechaFinalizo\":\"").append(a.getFechaFinalizacion()).append("\",") // Asigna fecha fin
                        .append("\"observaciones\":").append(a.getObservaciones() != null // Verifica integridad de observaciones
                            ? "\"" + escaparJson(a.getObservaciones()) + "\"" : "null") // Aplica sanitización o retorna null
                        .append("}"); // Cierra el objeto
                }
                json.append("]"); // Cierra el array JSON
                response.getWriter().write(json.toString()); // Envía la respuesta al cliente
            } catch (NumberFormatException e) { // Manejo de errores de formato
                response.getWriter().write("[]"); // Devuelve estructura vacía si falla la conversión
            }
            return; // Termina
        }

        if ("eliminar".equals(accion)) { // Lógica para remover un cultivo
            String idStr = request.getParameter("id"); // Captura el ID a eliminar
            if (!estaVacio(idStr)) { // Valida si el ID no es nulo
                try { // Intenta borrar
                    dao.eliminarCultivo(Integer.parseInt(idStr)); // Ejecuta operación en el DAO
                } catch (NumberFormatException ignored) {} // Ignora error si el ID está corrupto
            }
            response.sendRedirect(request.getContextPath() + "/ServletCultivo?status=eliminado"); // Redirección para limpiar caché POST
            return; // Termina
        }

        if ("eliminarRegistro".equals(accion)) { // Lógica para eliminar labores específicas
            String idStr = request.getParameter("id"); // ID de la labor
            String idCultivoStr = request.getParameter("idCultivo"); // ID del cultivo para el redireccionamiento
            if (!estaVacio(idStr)) { // Valida presencia de datos
                try { // Intenta la eliminación
                    new AsignacionDao().eliminarAsignacion(Integer.parseInt(idStr)); // Procesa en BD
                } catch (NumberFormatException ignored) {} // Silencia error
            }
            String redirect = request.getContextPath() + "/ServletCultivo?status=registroEliminado"; // Prepara URL destino
            if (!estaVacio(idCultivoStr)) redirect += "&idCultivoActivo=" + idCultivoStr; // Mantiene el contexto de cultivo
            response.sendRedirect(redirect); // Ejecuta la redirección
            return; // Termina
        }

        List<cultivo> lista = dao.listarCultivos(); // Carga la lista inicial para el panel de administración
        AsignacionDao asignacionDao = new AsignacionDao(); // Instancia DAO de asignaciones
        Map<Integer, Integer> contadoresHistorial = new HashMap<>(); // Mapa para contar registros de labores por cultivo
        for (cultivo c : lista) { // Itera sobre cada cultivo encontrado
            contadoresHistorial.put(c.getIdCultivo(), asignacionDao.contarPorCultivo(c.getIdCultivo())); // Almacena el conteo
        }
        request.setAttribute("listaCultivos", lista); // Pasa la lista de cultivos al JSP
        request.setAttribute("contadoresHistorial", contadoresHistorial); // Pasa los conteos de labores al JSP
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp").forward(request, response); // Renderiza interfaz administrativa
    }

    @Override // Sobrescribe método POST para operaciones de escritura/modificación
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { // Maneja envíos de formularios

        if (!verificarSesionAdmin(request, response)) return; // Verifica permisos de administrador

        String idStr = request.getParameter("id"); // Obtiene el ID si es una actualización
        String nombre = sanitizar(request.getParameter("nombreCultivo")); // Limpia el nombre de cultivo de espacios en blanco
        String tipo = sanitizar(request.getParameter("tipoCultivo")); // Limpia el tipo de cultivo
        String fSiembraStr = sanitizar(request.getParameter("fechaSiembra")); // Captura la fecha de siembra
        String fCosechaStr = sanitizar(request.getParameter("fechaCosecha")); // Captura la fecha de cosecha

        // Validación defensiva (Guardia de Seguridad): rechaza peticiones maliciosas o incompletas
        if (estaVacio(nombre) || excedeLongitud(nombre, MAX_NOMBRE) 
          || excedeLongitud(tipo, MAX_TIPO) || estaVacio(fSiembraStr)) { // Verifica obligatoriedad y límites
            reenviarAdminConError("Solicitud inválida.", request, response); // Rechaza con mensaje genérico
            return; // Aborta
        }

        Date fSiembra; // Declara variable para fecha de siembra SQL
        Date fCosecha = null; // Inicializa cosecha como nulo
        try { // Bloque de parseo y validación de fechas
            fSiembra = Date.valueOf(fSiembraStr); // Convierte String a formato Date de SQL
            if (!estaVacio(fCosechaStr)) { // Si se proveyó una fecha de cosecha
                fCosecha = Date.valueOf(fCosechaStr); // Convierte String a formato Date
                if (fCosecha.toLocalDate().isBefore(fSiembra.toLocalDate())) { // Valida lógica: siembra antes que cosecha
                    reenviarAdminConError("Solicitud inválida.", request, response); // Rechaza si es inconsistente
                    return; // Aborta
                }
            }
        } catch (IllegalArgumentException e) { // Captura error de formato de fecha
            reenviarAdminConError("Solicitud inválida.", request, response); // Rechaza
            return; // Aborta
        }

        CultivoDao dao = new CultivoDao(); // Prepara el acceso a datos
        if (!estaVacio(idStr)) { // Si recibimos ID, operamos como actualización (UPDATE)
            try { // Intenta la actualización
                dao.actualizarCultivo(Integer.parseInt(idStr), nombre, tipo != null ? tipo : "", fSiembra, fCosecha); // Persiste cambios
            } catch (NumberFormatException e) { // Maneja error si el ID está malformado
                reenviarAdminConError("Solicitud inválida.", request, response); // Rechaza
                return; // Aborta
            }
        } else { // Si no hay ID, operamos como registro nuevo (CREATE)
            dao.registrarCultivo(new cultivo(nombre, tipo != null ? tipo : "", fSiembra, fCosecha)); // Inserta en base de datos
        }
        response.sendRedirect("ServletCultivo"); // Redirección final para limpiar el formulario en el navegador
    }

    // --- MÉTODOS AUXILIARES ---

    private String sanitizar(String valor) { // Elimina espacios basura al principio y final
        return (valor != null) ? valor.trim() : null; // Retorna null si el valor original lo era
    }

    private boolean excedeLongitud(String valor, int max) { // Valida longitud para prevenir desbordamientos
        return valor != null && valor.length() > max; // Devuelve true si la cadena es demasiado larga
    }

    private String escaparJson(String s) { // Previene que caracteres especiales rompan el JSON
        if (s == null) return ""; // Retorna vacío si es null
        return s.replace("\\", "\\\\") // Escapa barra invertida
                .replace("\"", "\\\"") // Escapa comillas dobles para que no cierren el string prematuramente
                .replace("\n", "\\n") // Escapa saltos de línea
                .replace("\r", "\\r") // Escapa retorno de carro
                .replace("\t", "\\t"); // Escapa tabulaciones
    }

    private void reenviarAdminConError(String mensaje, HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException { // Centraliza la lógica de error y recarga de vista
        request.setAttribute("mensajeError", mensaje); // Establece el mensaje visible para el admin
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos()); // Refresca listado para no perder estado
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp").forward(request, response); // Muestra error al usuario
    }
}