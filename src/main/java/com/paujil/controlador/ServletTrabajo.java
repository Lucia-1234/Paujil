package com.paujil.controlador;


import com.paujil.dao.CultivoDao;
import com.paujil.dao.LoteDao;
import com.paujil.dao.TipoTrabajoDao;
import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.tipoTrabajo;
import com.paujil.modelo.trabajo;
import com.paujil.servicio.AsignacionServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.List;
import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;

/**
 * ServletTrabajo: Clase controladora encargada de orquestar el flujo de las labores agrícolas.
 * Responsabilidad: Interceptar peticiones HTTP, coordinar la lógica con AsignacionServicio y dirigir la vista.
 */
// Javadoc formal que describe el propósito del servlet a nivel de clase.
// Documenta que este servlet actúa como orquestador: recibe, delega y redirige.

@WebServlet("/ServletTrabajo")
// Registra el servlet en el contenedor web bajo la ruta "/ServletTrabajo".
// Cualquier petición GET o POST a esa URL será procesada por esta clase.

public class ServletTrabajo extends HttpServlet {
// Declara la clase pública, extiende HttpServlet para heredar el ciclo de vida HTTP.

    private final AsignacionServicio servicio = new AsignacionServicio();
    // Campo de instancia: se crea UNA SOLA VEZ cuando el servlet es instanciado por el contenedor.
    // 'final' garantiza que la referencia no cambiará durante la vida del servlet.
    // Separar la lógica de negocio aquí (en lugar de en el servlet) sigue el principio de responsabilidad única.

    @Override
    // Indica al compilador que sobreescribe doGet de HttpServlet.
    // Si el nombre fuera incorrecto, el compilador avisaría con un error de compilación.

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // Maneja todas las peticiones GET: consultas y navegación entre vistas (lectura).
    // Declara que puede propagar ServletException e IOException al contenedor.

        String accion = request.getParameter("accion");
        // Lee el parámetro "accion" de la URL (?accion=listar, ?accion=eliminar, etc.).
        // Devuelve null si el parámetro no está presente en la solicitud.

        if (accion == null) accion = "listar";
        // Si no se especificó acción, establece "listar" como comportamiento por defecto.
        // Evita NullPointerException en el switch y define un punto de entrada seguro.

        switch (accion) {
        // Enrutador principal: distribuye la ejecución según la acción solicitada.
        // Más legible que una cadena de if-else para múltiples ramas.

            case "listar":
            // Vista administrativa: muestra el listado completo de trabajos para gestión.

                if (!verificarSesionAdmin(request, response)) return;
                // Guardia de seguridad: si no hay sesión admin válida, la utilidad redirige al login
                // y este return evita continuar ejecutando el código de abajo.

                request.setAttribute("listaAsignaciones", servicio.listarTodas());
                // Consulta todas las asignaciones de trabajo via el servicio y las inyecta en el request.
                // El JSP accederá a este dato con ${listaAsignaciones}.

                request.setAttribute("listaTiposTrabajo", new TipoTrabajoDao().listarTipos());
                // Carga el catálogo de tipos de trabajo disponibles (para filtros o selectores en la vista).

                request.getRequestDispatcher("/templates/administrador/listar_trabajos.jsp").forward(request, response);
                // Forward interno: transfiere el control al JSP con todos los atributos cargados.
                // La URL del navegador NO cambia (diferencia clave con sendRedirect).

                break;
                // Termina el case "listar"; sin break se caería al siguiente case (bug grave en switch).

            case "eliminar":
            // Vista administrativa: elimina una labor específica por su ID.

                if (!verificarSesionAdmin(request, response)) return;
                // Solo administradores pueden eliminar trabajos.

                String idStr = request.getParameter("id");
                // Lee el ID de la asignación a eliminar desde la URL (?accion=eliminar&id=5).

                if (estaVacio(idStr)) {
                    response.sendRedirect("ServletTrabajo?accion=listar&status=error");
                    return;
                }
                // Validación rápida: si no vino ID, redirige con status=error y aborta.
                // El parámetro "status=error" permite al JSP mostrar un mensaje de error al usuario.

                try {
                    boolean eliminado = servicio.eliminarTrabajo(Integer.parseInt(idStr));
                    // Parsea el ID de String a int e invoca la lógica de eliminación en el servicio.
                    // El servicio devuelve true si la eliminación fue exitosa, false si falló.

                    response.sendRedirect("ServletTrabajo?accion=listar&status=" + (eliminado ? "eliminado" : "error"));
                    // PRG (Post-Redirect-Get): redirige con feedback dinámico en la URL.
                    // Operador ternario: si eliminado=true → "eliminado", si false → "error".

                } catch (NumberFormatException e) {
                    response.sendRedirect("ServletTrabajo?accion=listar&status=error");
                    // Si el ID no era numérico (ej: "abc"), captura la excepción y redirige con error.
                }
                break;

            case "prepararCreacion":
            // Vista administrativa: prepara y muestra el formulario de alta de nuevos trabajos.

                if (!verificarSesionAdmin(request, response)) return;
                // Solo admins pueden acceder al formulario de creación.

                request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
                // Carga todos los cultivos para el <select> del formulario (el trabajador trabaja sobre un cultivo).

                request.setAttribute("catalogoLotes", new LoteDao().listarLotes());
                // Carga lotes para poder mostrar el nombre del lote junto a cada cultivo en el selector.

                request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos());
                // Carga solo los usuarios activos: no tiene sentido asignar trabajo a usuarios inactivos.

                request.setAttribute("listaTiposTrabajo", new TipoTrabajoDao().listarTipos());
                // Carga el catálogo de tipos de trabajo para el selector del formulario.

                request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp").forward(request, response);
                // Muestra el formulario con todos los catálogos pre-cargados.
                break;

            case "misTrabajos":
            // Vista de trabajador: muestra TODAS sus tareas asignadas (sin filtro de estado).

                if (!verificarSesionUsuario(request, response)) return;
                // Valida sesión de usuario (no necesariamente admin).

                int idUsuario = (int) request.getSession(false).getAttribute("idUsuario");
                // Obtiene el ID del usuario autenticado desde la sesión activa.
                // getSession(false) → no crea sesión nueva si no existe (retorna null si no hay sesión).
                // Cast a (int) porque getAttribute devuelve Object.

                request.setAttribute("listaMisTrabajos", servicio.listarPorUsuario(idUsuario, null));
                // null como segundo parámetro indica "sin filtro de estado": devuelve todas las tareas del usuario.

                request.getRequestDispatcher("/templates/trabajador/trabajos_asignados.jsp").forward(request, response);
                // Muestra la vista de todos los trabajos del trabajador autenticado.
                break;

            case "pendientes":
            // Vista de trabajador: muestra solo las tareas con estado "Pendiente" (no iniciadas aún).

                if (!verificarSesionUsuario(request, response)) return;

                int idUserPend = (int) request.getSession(false).getAttribute("idUsuario");
                // Recupera el ID del usuario de la sesión para filtrar solo SUS tareas.

                request.setAttribute("listaPendientes", servicio.listarPorUsuario(idUserPend, "Pendiente"));
                // Filtra las tareas del usuario con estado exactamente igual a "Pendiente".

                request.getRequestDispatcher("/templates/trabajador/trabajos_pendientes.jsp").forward(request, response);
                break;

            case "finalizados":
            // Vista de trabajador: muestra solo las tareas con estado "Finalizado" (completadas y aprobadas).

                if (!verificarSesionUsuario(request, response)) return;

                int idUserFin = (int) request.getSession(false).getAttribute("idUsuario");
                // Recupera ID de usuario de la sesión para el filtro.

                request.setAttribute("listaFinalizados", servicio.listarPorUsuario(idUserFin, "Finalizado"));
                // Filtra las tareas del usuario con estado "Finalizado".

                request.getRequestDispatcher("/templates/trabajador/trabajos_finalizados.jsp").forward(request, response);
                break;

            default:
                response.sendRedirect("ServletTrabajo?accion=listar");
                // Si llega un valor de accion no reconocido (ej: accion=hackintento), redirige al listado.
                // Defensa contra parámetros manipulados o URLs mal formadas.
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // Maneja peticiones POST: operaciones que modifican datos (crear, actualizar, eliminar tipos).

        String accion = request.getParameter("accion");
        // Captura la acción enviada desde el formulario (campo hidden o parámetro POST).

        if (accion == null) accion = "";
        // Si no viene acción, inicializa como String vacío para evitar NullPointerException en el switch.

        switch (accion) {

            case "registrar":
            // Persiste un nuevo trabajo/asignación en la BD.

                if (!verificarSesionAdmin(request, response)) return;
                // Solo admins pueden registrar trabajos.

                registrarTrabajo(request, response);
                // Delega la lógica a un método privado para mantener el doPost limpio y legible.
                break;

            case "actualizarEstado":
            // Permite al trabajador avanzar el estado de su tarea (iniciar → enviar a revisión).

                if (!verificarSesionUsuario(request, response)) return;
                // Cualquier usuario autenticado puede actualizar el estado de SUS tareas.

                actualizarEstado(request, response);
                // Delega al método privado que gestiona la transición de estados.
                break;

            case "revisar":
            // Permite al admin aprobar o devolver una tarea que el trabajador envió a revisión.

                if (!verificarSesionAdmin(request, response)) return;
                // Solo administradores tienen potestad de revisar y aprobar trabajos.

                revisarTrabajo(request, response);
                // Delega al método privado de revisión.
                break;

            case "crearTipo":
            // Crea un nuevo tipo de trabajo vía AJAX (responde con JSON, no redirige).

                if (!verificarSesionAdmin(request, response)) return;
                crearTipo(request, response);
                break;

            case "editarTipo":
            // Modifica el nombre de un tipo de trabajo existente vía AJAX.

                if (!verificarSesionAdmin(request, response)) return;
                editarTipo(request, response);
                break;

            case "eliminarTipo":
            // Elimina un tipo de trabajo vía AJAX, con validación de integridad referencial.

                if (!verificarSesionAdmin(request, response)) return;
                eliminarTipo(request, response);
                break;

            default:
                response.sendRedirect("ServletTrabajo?accion=listar");
                // Cualquier acción POST no reconocida redirige al listado de forma segura.
        }
    }

    // ─────────────────────────── MÉTODOS PRIVADOS AUXILIARES ───────────────────────────

    private void registrarTrabajo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // Método privado que encapsula la lógica de registro de un nuevo trabajo.
    // Al ser privado, solo es accesible dentro de esta clase.

        String desc = request.getParameter("descripcion");
        // Lee la descripción textual de la labor a registrar desde el formulario.

        String fechaStr = request.getParameter("fechaAsignacion");
        // Lee la fecha de asignación como String (ej: "2024-03-15").

        String idCultivoStr = request.getParameter("idCultivo");
        // Lee el ID del cultivo sobre el cual se realizará la labor.

        String idUsuarioStr = request.getParameter("idUsuario");
        // Lee el ID del trabajador al que se le asignará la tarea.

        String idTipoTrabajoStr = request.getParameter("idTipoTrabajo");
        // Lee el ID del tipo de trabajo seleccionado (ej: poda, riego, fumigación).

        if (estaVacio(desc) || estaVacio(fechaStr) || estaVacio(idCultivoStr)
                || estaVacio(idUsuarioStr) || estaVacio(idTipoTrabajoStr)) {
        // Validación defensiva: todos los campos son obligatorios.
        // Si cualquiera está vacío o null, rechaza el formulario completo.

            enviarError("Todos los campos son obligatorios.", request, response);
            return;
            // Muestra error y aborta el método; no continúa con el guardado.
        }

        if (desc.trim().length() < 10 || desc.trim().length() > 500) {
        // Valida rango de longitud de la descripción:
        // - Mínimo 10 caracteres: evita descripciones sin sentido (ej: "ok", "x").
        // - Máximo 500 caracteres: protege el campo de texto en la BD de desbordamientos.
        // trim() elimina espacios en blanco antes de contar, para evitar trampa de espacios.

            enviarError("La descripción debe estar entre 10 y 500 caracteres.", request, response);
            return;
        }

        try {
            int idCultivo = Integer.parseInt(idCultivoStr);
            // Convierte el ID del cultivo de String a int; lanzará NumberFormatException si no es numérico.

            int idUsuario = Integer.parseInt(idUsuarioStr);
            // Convierte el ID del usuario de String a int.

            int idTipoTrabajo = Integer.parseInt(idTipoTrabajoStr);
            // Convierte el ID del tipo de trabajo de String a int.

            Date fechaAsignacion = Date.valueOf(fechaStr);
            // Convierte el String de fecha (formato "yyyy-MM-dd") a java.sql.Date.
            // Lanzará IllegalArgumentException si el formato es incorrecto.

            trabajo t = new trabajo(desc.trim(), idTipoTrabajo);
            // Crea el objeto modelo 'trabajo' con la descripción limpia y el tipo.
            // trim() asegura que no se guarden espacios superfluos en la BD.

            boolean ok = servicio.registrarTrabajoCompleto(t, idCultivo, idUsuario, fechaAsignacion);
            // Llama al servicio para persistir el trabajo y crear la asignación en la BD.
            // El servicio puede aplicar reglas adicionales (ej: verificar que el cultivo esté activo).
            // Devuelve true si todo salió bien, false si algo falló en la BD.

            if (ok) response.sendRedirect("ServletTrabajo?accion=listar&status=success");
            // PRG: si el registro fue exitoso, redirige al listado con feedback positivo.

            else enviarError("Error al guardar en base de datos.", request, response);
            // Si el servicio retornó false, muestra un error al administrador.

        } catch (IllegalArgumentException e) {
        // IllegalArgumentException cubre tanto NumberFormatException (parseo de IDs)
        // como el error de Date.valueOf (formato de fecha inválido).

            enviarError("Datos inválidos.", request, response);
            // Mensaje genérico de error de parseo para el administrador.
        }
    }

    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // Método privado que gestiona el avance del flujo de estados de una tarea.
    // Flujo: Pendiente → En proceso → En revisión → Finalizado (aprobado por admin).

        String idAsignacionStr = request.getParameter("idAsignacion");
        // Lee el ID de la asignación cuyo estado se va a actualizar.

        String observaciones = request.getParameter("observaciones");
        // Notas adicionales que el trabajador puede añadir al avanzar el estado.
        // Son opcionales pero tienen límite de longitud.

        String btnAccion = request.getParameter("btnAccion");
        // Identifica qué botón pulsó el trabajador: "iniciar" o "finalizar".
        // Determina el nuevo estado al que avanzará la tarea.

        if (estaVacio(idAsignacionStr)) {
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error");
            return;
        }
        // Si no vino ID de asignación, es una petición inválida; redirige con error.

        if (observaciones != null && observaciones.trim().length() > 500) {
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error_obs");
            return;
        }
        // Valida que las observaciones no superen 500 caracteres (igual que la descripción).
        // Solo valida si se enviaron observaciones (el campo es opcional).

        try {
            int idAsignacion = Integer.parseInt(idAsignacionStr);
            // Parsea el ID de la asignación a entero.

            String nuevoEstado = "iniciar".equals(btnAccion) ? "En proceso"
                               : "finalizar".equals(btnAccion) ? "En revisión"
                               : null;
            // Lógica de transición de estados mediante operador ternario encadenado:
            // - Si el botón fue "iniciar" → nuevo estado = "En proceso".
            // - Si el botón fue "finalizar" → nuevo estado = "En revisión" (NO finalizado directamente).
            //   El trabajador envía a revisión; el admin es quien aprueba y marca como "Finalizado".
            // - Cualquier otro valor → null (acción desconocida).

            boolean ok = servicio.actualizarEstado(idAsignacion, observaciones, nuevoEstado);
            // Llama al servicio para actualizar el estado en la BD.
            // El servicio puede tener reglas adicionales, por ejemplo: no puede enviar a revisión
            // si la tarea lleva menos de 2 horas en estado "En proceso".

            String status;
            if (!ok && "En revisión".equals(nuevoEstado)) {
            // Caso especial: si falló Y el estado destino era "En revisión",
            // probablemente falló por la regla de las 2 horas mínimas de trabajo.

                status = "tiempo_insuficiente";
                // Status específico para mostrar al trabajador un mensaje claro y diferenciado.

            } else {
                status = ok
                    ? (nuevoEstado != null ? nuevoEstado.toLowerCase().replace(" ", "_") : "guardado")
                    : "error";
                // Si ok=true:
                //   - Si hay nuevo estado, lo convierte a minúsculas y reemplaza espacios por guiones
                //     (ej: "En proceso" → "en_proceso"), para usarlo como parámetro URL seguro.
                //   - Si nuevoEstado es null (acción desconocida), usa "guardado" como fallback.
                // Si ok=false → "error".
            }

            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=" + status);
            // Redirige a la vista de tareas del trabajador con el status correspondiente.

        } catch (NumberFormatException e) {
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error");
            // Si el ID no era numérico, redirige con error genérico.
        }
    }

    private void enviarError(String mensaje, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // Método auxiliar que centraliza la lógica de "mostrar error en el formulario de admin".
    // Evita duplicar las mismas 5 líneas en cada punto de validación fallida.

        request.setAttribute("mensajeError", mensaje);
        // Inyecta el mensaje de error en el request para que el JSP lo renderice.

        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        // Re-carga los cultivos para que el <select> no aparezca vacío tras el error.

        request.setAttribute("catalogoLotes", new LoteDao().listarLotes());
        // Re-carga los lotes para mantener los nombres junto a los cultivos en el selector.

        request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos());
        // Re-carga los trabajadores activos para el selector de asignación.

        request.setAttribute("listaTiposTrabajo", new TipoTrabajoDao().listarTipos());
        // Re-carga los tipos de trabajo para el selector del formulario.

        request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp").forward(request, response);
        // Vuelve al formulario con todos los catálogos y el mensaje de error visibles.
        // forward (no redirect) mantiene el request con los atributos cargados.
    }

    private void crearTipo(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Método auxiliar para crear un tipo de trabajo vía AJAX.
    // Responde con JSON en lugar de redirigir, ya que es una llamada asíncrona.

        response.setContentType("application/json; charset=UTF-8");
        // Establece el tipo de contenido de la respuesta como JSON con codificación UTF-8.
        // El navegador sabrá que debe interpretar la respuesta como JSON.

        String nombre = request.getParameter("nombreTipo");
        // Lee el nombre del nuevo tipo de trabajo enviado por AJAX.

        if (estaVacio(nombre) || nombre.trim().length() < 3 || nombre.trim().length() > 50
                || !nombre.matches("[\\p{L}\\s\\-]+")) {
        // Validación múltiple del nombre:
        // 1. No puede ser vacío/null.
        // 2. Mínimo 3 caracteres.
        // 3. Máximo 50 caracteres.
        // 4. Regex [\\p{L}\\s\\-]+: solo permite letras Unicode (\\p{L}), espacios (\\s) y guiones (\\-).
        //    Rechaza números y caracteres especiales como !@#$ que no tienen sentido en un nombre de tipo.

            response.getWriter().write("{\"ok\":false,\"mensaje\":\"Nombre inválido.\"}");
            // Escribe directamente el JSON de error en el cuerpo de la respuesta HTTP.
            return;
        }

        TipoTrabajoDao dao = new TipoTrabajoDao();
        // Instancia el DAO para ejecutar el INSERT.

        if (!dao.registrarTipo(nombre.trim()))
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"Error al guardar.\"}");
        // Si el guardado falló (ej: nombre duplicado, error de BD), responde con JSON de error.

        else response.getWriter().write(construirJsonTipos(dao.listarTipos()));
        // Si el guardado fue exitoso, devuelve la lista ACTUALIZADA de tipos en JSON.
        // El frontend JavaScript usará esta lista para actualizar la UI sin recargar la página.
    }

    private void editarTipo(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Método auxiliar para editar un tipo de trabajo existente vía AJAX.

        response.setContentType("application/json; charset=UTF-8");
        // Define que la respuesta será JSON.

        String idStr = request.getParameter("id");
        // Lee el ID del tipo a editar.

        String nombre = request.getParameter("nombreTipo");
        // Lee el nuevo nombre para el tipo.

        if (estaVacio(idStr) || estaVacio(nombre)) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"Datos inválidos.\"}");
            return;
        }
        // Validación básica: ambos campos son obligatorios para poder editar.

        try {
            int id = Integer.parseInt(idStr);
            // Parsea el ID a entero; lanzará NumberFormatException si no es numérico.

            TipoTrabajoDao dao = new TipoTrabajoDao();

            if (!dao.editarTipo(id, nombre.trim()))
                response.getWriter().write("{\"ok\":false,\"mensaje\":\"Error al editar.\"}");
            // Si el UPDATE falló, responde con JSON de error.

            else response.getWriter().write(construirJsonTipos(dao.listarTipos()));
            // Si fue exitoso, devuelve la lista actualizada para que el frontend la refresque.

        } catch (NumberFormatException e) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"ID inválido.\"}");
            // Si el ID no era numérico, informa del error específico.
        }
    }

    private void eliminarTipo(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Método auxiliar para eliminar un tipo de trabajo vía AJAX.
    // Incluye verificación de integridad referencial antes de eliminar.

        response.setContentType("application/json; charset=UTF-8");

        String idStr = request.getParameter("id");
        // Lee el ID del tipo a eliminar.

        try {
            int id = Integer.parseInt(idStr);
            // Parsea el ID a entero.

            TipoTrabajoDao dao = new TipoTrabajoDao();

            if (dao.contarTrabajosPorTipo(id) > 0)
                response.getWriter().write("{\"ok\":false,\"mensaje\":\"Tipo en uso.\"}");
            // ANTES de eliminar, verifica si algún trabajo usa este tipo.
            // Si hay trabajos asociados, no se puede eliminar (integridad referencial).
            // Esto evita violar la restricción de clave foránea en la BD y da un mensaje claro al admin.

            else if (!dao.eliminarTipo(id))
                response.getWriter().write("{\"ok\":false,\"mensaje\":\"Error al eliminar.\"}");
            // Si no tiene dependencias pero el DELETE falló, informa del error.

            else response.getWriter().write(construirJsonTipos(dao.listarTipos()));
            // Si la eliminación fue exitosa, devuelve la lista actualizada.

        } catch (Exception e) {
        // Captura genérica: cubre NumberFormatException (ID inválido) y cualquier otro error inesperado.

            response.getWriter().write("{\"ok\":false,\"mensaje\":\"Error técnico.\"}");
            // Mensaje genérico de error técnico para el frontend.
        }
    }

    private void revisarTrabajo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // Método auxiliar para que el admin apruebe o devuelva una tarea en revisión.
    // Solo aplica a tareas con estado "En revisión" enviadas por el trabajador.

        String idAsignacionStr = request.getParameter("idAsignacion");
        // Lee el ID de la asignación a revisar.

        String observacionesAdmin = request.getParameter("observacionesAdmin");
        // Comentarios del administrador (ej: "Falta limpiar el área", "Aprobado, buen trabajo").

        String accionRevision = request.getParameter("accionRevision");
        // Determina qué decide el admin: "aprobar" o "devolver" la tarea al trabajador.

        if (estaVacio(idAsignacionStr) || estaVacio(accionRevision)) {
            response.sendRedirect("ServletTrabajo?accion=listar&status=error");
            return;
        }
        // Ambos parámetros son obligatorios: sin ID no se sabe qué revisar,
        // sin acción no se sabe qué decisión tomar.

        try {
            int idAsignacion = Integer.parseInt(idAsignacionStr);
            // Parsea el ID de la asignación.

            boolean aprobado = "aprobar".equals(accionRevision);
            // Convierte la acción textual a booleano:
            // true → el admin aprueba (la tarea pasará a "Finalizado").
            // false → el admin devuelve (la tarea regresará a "En proceso" u otro estado).

            boolean ok = servicio.revisarAsignacion(idAsignacion, observacionesAdmin, aprobado);
            // Llama al servicio con la decisión del admin.
            // El servicio actualiza el estado en BD y puede guardar las observaciones del admin.

            String status = ok ? (aprobado ? "aprobado" : "devuelto") : "error";
            // Construye el status de feedback:
            // - ok=true + aprobado=true → "aprobado"
            // - ok=true + aprobado=false → "devuelto"
            // - ok=false → "error" (algo falló en la BD o en la lógica del servicio)

            response.sendRedirect("ServletTrabajo?accion=listar&status=" + status);
            // PRG: redirige al listado con el feedback correspondiente.

        } catch (NumberFormatException e) {
            response.sendRedirect("ServletTrabajo?accion=listar&status=error");
            // Si el ID era inválido, redirige con error genérico.
        }
    }

    private String construirJsonTipos(List<tipoTrabajo> tipos) {
    // Construye manualmente una cadena JSON con la lista de tipos de trabajo.
    // Se hace manualmente (sin librería como Gson/Jackson) para mantener la dependencia mínima.

        StringBuilder json = new StringBuilder("{\"ok\":true,\"tipos\":[");
        // Inicia el objeto JSON con la clave "ok":true y abre el array "tipos".

        for (int i = 0; i < tipos.size(); i++) {
        // Itera sobre cada tipo de trabajo en la lista.

            if (i > 0) json.append(",");
            // Agrega coma separadora entre elementos, pero NO antes del primero (i=0).
            // Evita el JSON inválido de: [{"id":1},{"id":2},] (coma final).

            json.append("{\"id\":").append(tipos.get(i).getIdTipoTrabajo())
                .append(",\"nombre\":\"").append(escaparJson(tipos.get(i).getNombreTipo())).append("\"}");
            // Construye el objeto JSON de cada tipo: {"id":1,"nombre":"Poda"}.
            // escaparJson sanitiza el nombre antes de insertarlo en el JSON.
        }

        return json.append("]}").toString();
        // Cierra el array y el objeto raíz, convierte el StringBuilder a String y retorna.
        // Resultado ejemplo: {"ok":true,"tipos":[{"id":1,"nombre":"Poda"},{"id":2,"nombre":"Riego"}]}
    }

    private String escaparJson(String s) {
    // Sanitiza un String para que sea seguro incrustarlo dentro de un valor JSON entre comillas.
    // Sin esto, un nombre como: She said "hello" rompería el JSON generado.

        return (s == null) ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
        // Si s es null, devuelve String vacío (seguro para JSON).
        // replace("\\", "\\\\") → escapa las barras invertidas primero (SIEMPRE primero, orden importa).
        // replace("\"", "\\\"") → escapa las comillas dobles que romperían la estructura JSON.
    }
}