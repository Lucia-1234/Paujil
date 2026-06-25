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
// Importación estática de utilidades compartidas entre servlets:
// estaVacio(), verificarSesionAdmin(), verificarSesionUsuario(), etc.
// El '*' importa todos los métodos estáticos de esa clase utilitaria.

@WebServlet("/ServletCultivo")
// Mapea este servlet a la URL "/ServletCultivo".
// Cualquier petición HTTP a esa ruta será manejada por esta clase.

public class ServletCultivo extends HttpServlet {
// Declara la clase pública y la extiende de HttpServlet.
// Al extender HttpServlet, hereda el ciclo de vida del servlet y los métodos HTTP.

    private static final int MAX_NOMBRE = 80;
    // Constante que define el límite máximo de caracteres para el nombre del cultivo.
    // 'static final' significa que es compartida por todas las instancias y no cambia en tiempo de ejecución.
    // Previene ataques de desbordamiento de buffer o strings inusualmente largos.

    private static final int MAX_TIPO = 60;
    // Constante que define el límite máximo de caracteres para el tipo de cultivo.
    // Misma razón de seguridad que MAX_NOMBRE.

    @Override
    // Indica al compilador que este método sobreescribe uno de la clase padre (HttpServlet).
    // Si el nombre estuviera mal escrito, el compilador avisaría.

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // Método que maneja todas las peticiones HTTP GET a "/ServletCultivo".
    // GET se usa para leer/mostrar información (no para modificar datos).
    // Declara que puede lanzar ServletException o IOException.

        String accion = request.getParameter("accion");
        // Lee el parámetro "accion" de la URL (ej: /ServletCultivo?accion=eliminar).
        // Si no existe, devuelve null. Determina qué operación ejecutar.

        CultivoDao dao = new CultivoDao();
        // Crea una instancia del DAO de cultivos.
        // Este objeto encapsula todas las operaciones SQL relacionadas con cultivos.

        if ("verTrabajador".equals(accion)) {
        // Compara usando .equals() en el literal (no en la variable) para evitar NullPointerException
        // si 'accion' fuera null. Esta rama atiende a usuarios con rol trabajador.

            if (!verificarSesionUsuario(request, response)) return;
            // Llama a la utilidad que comprueba si hay una sesión válida de usuario (no admin).
            // Si no hay sesión activa, la utilidad redirige al login y este método retorna inmediatamente.

            request.setAttribute("listaCultivos", dao.listarCultivos());
            // Consulta todos los cultivos de la BD y los guarda en el request como atributo.
            // El JSP accederá a este dato con ${listaCultivos}.

            request.setAttribute("catalogoLotes", new LoteDao().listarLotes());
            // Instancia el DAO de lotes, lista todos los lotes y los guarda en el request.
            // Necesario para que la vista pueda mostrar el nombre del lote asociado a cada cultivo.

            request.getRequestDispatcher("/templates/trabajador/cultivos_trabajador.jsp").forward(request, response);
            // Redirige internamente (sin cambiar la URL del navegador) al JSP de trabajadores.
            // El forward transfiere el control al JSP junto con los atributos cargados arriba.

            return;
            // Termina la ejecución del doGet para esta rama; evita caer en el código de abajo.
        }

        if ("eliminar".equals(accion)) {
        // Segunda rama: maneja la eliminación de un cultivo cuando accion=eliminar.

            String idStr = request.getParameter("id");
            // Lee el parámetro "id" de la URL (ej: /ServletCultivo?accion=eliminar&id=5).
            // Llega como String; se debe convertir a entero más adelante.

            if (!estaVacio(idStr)) {
            // Solo continúa si el id no es null ni una cadena vacía.
            // Evita procesar IDs inválidos que podrían causar errores en la BD.

                try {
                // Bloque de manejo de excepciones para capturar errores de parseo o de BD.

                    int id = Integer.parseInt(idStr);
                    // Convierte el String "id" a entero.
                    // Si el valor no es numérico (ej: "abc"), lanzará NumberFormatException.

                    if (new AsignacionDao().tieneAsignacionesActivasPorCultivo(id)) {
                    // Consulta la BD: ¿tiene este cultivo asignaciones activas de trabajadores?
                    // Si las tiene, no se puede eliminar para mantener integridad referencial.

                        response.sendRedirect(request.getContextPath() +
                            "/ServletCultivo?status=error&motivo=asignaciones_activas");
                        // Redirige al cliente con un parámetro de error en la URL.
                        // El JSP puede leer "status=error" para mostrar un mensaje al usuario.

                        return;
                        // Termina la ejecución; no continúa con la eliminación.
                    }

                    dao.eliminarCultivo(id);
                    // Si no tiene asignaciones activas, procede a eliminar el cultivo de la BD.
                    // Este método ejecuta un DELETE en la tabla cultivos.

                } catch (NumberFormatException ignored) {}
                // Si el ID no era un número válido, simplemente lo ignora.
                // No se lanza error al usuario; se redirige de todas formas abajo.
            }

            response.sendRedirect(request.getContextPath() + "/ServletCultivo?status=eliminado");
            // Patrón PRG (Post-Redirect-Get): redirige tras la acción para evitar reenvíos del formulario.
            // El parámetro "status=eliminado" permite al JSP mostrar un mensaje de éxito.

            return;
            // Termina el doGet para esta rama.
        }

        // --- Flujo por defecto: vista de administrador ---

        request.setAttribute("listaCultivos", dao.listarCultivos());
        // Carga todos los cultivos desde la BD y los pone disponibles para el JSP de admin.

        request.setAttribute("catalogoLotes", new LoteDao().listarLotes());
        // Carga el catálogo de lotes y lo pone disponible para el JSP de admin.
        // Se usa para el <select> de lotes en el formulario de registro/edición.

        request.getRequestDispatcher("/templates/administrador/cultivos.jsp").forward(request, response);
        // Delega el renderizado al JSP de administrador con todos los datos cargados.
    }

    @Override
    // Indica que este método sobreescribe el doPost de HttpServlet.

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // Método que maneja peticiones HTTP POST a "/ServletCultivo".
    // POST se usa para operaciones que modifican datos: crear o actualizar cultivos.

        if (!verificarSesionAdmin(request, response)) return;
        // Comprueba que quien envía el formulario es un administrador autenticado.
        // Si no lo es, la utilidad redirige al login y este método retorna inmediatamente.
        // Barrera de seguridad crítica: impide que usuarios no autorizados modifiquen datos.

        String idStr = request.getParameter("id");
        // Lee el parámetro "id" del formulario. Si viene con valor, es una actualización;
        // si viene vacío o null, es un registro nuevo.

        String nombre = sanitizar(request.getParameter("nombreCultivo"));
        // Lee el nombre del cultivo del formulario y lo limpia con sanitizar() (quita espacios en blanco al inicio/fin).

        String tipo = sanitizar(request.getParameter("tipoCultivo"));
        // Lee el tipo de cultivo y lo sanitiza. Es opcional; puede ser null.

        String fSiembraStr = sanitizar(request.getParameter("fechaSiembra"));
        // Lee la fecha de siembra como String (ej: "2024-03-15") y la sanitiza.

        String fCosechaStr = sanitizar(request.getParameter("fechaCosecha"));
        // Lee la fecha de cosecha como String y la sanitiza. Es opcional; puede quedar null.

        String idLoteStr = request.getParameter("idLote");
        // Lee el ID del lote seleccionado en el formulario (vendrá como String numérico).

        if (estaVacio(nombre) || estaVacio(fSiembraStr) || estaVacio(idLoteStr) ||
            excedeLongitud(nombre, MAX_NOMBRE) || excedeLongitud(tipo, MAX_TIPO)) {
        // Validación defensiva de múltiples condiciones en una sola expresión:
        // 1. Nombre vacío o null → inválido.
        // 2. Fecha de siembra vacía → inválido (campo obligatorio).
        // 3. Lote no seleccionado → inválido.
        // 4. Nombre supera los 80 caracteres → inválido.
        // 5. Tipo supera los 60 caracteres → inválido.

            reenviarAdminConError("Datos inválidos o incompletos.", request, response);
            // Si alguna condición falla, muestra un error y vuelve al formulario con los datos cargados.

            return;
            // Termina el doPost; no continúa con la lógica de guardado.
        }

        if (nombre.matches("\\d+")) {
        // Comprueba con expresión regular si el nombre está compuesto únicamente por dígitos.
        // "\\d+" en Java equivale al regex \d+ que significa "uno o más dígitos".
        // Un nombre como "12345" no tiene sentido semántico para un cultivo.

            reenviarAdminConError("El nombre del cultivo no puede contener solo números. Incluya al menos una letra.", request, response);
            // Rechaza la entrada con un mensaje claro y descriptivo.

            return;
            // Aborta el procesamiento.
        }

        if (tipo != null && !tipo.isEmpty() && tipo.matches("\\d+")) {
        // Triple condición de guardia para el campo tipo:
        // 1. tipo != null → el campo fue enviado.
        // 2. !tipo.isEmpty() → no está vacío (el campo tipo es opcional, si está vacío se acepta).
        // 3. tipo.matches("\\d+") → si fue ingresado, no puede ser solo números.

            reenviarAdminConError("El tipo de cultivo no puede contener solo números. Incluya al menos una letra.", request, response);
            // Rechaza con mensaje explicativo.

            return;
            // Aborta el procesamiento.
        }

        try {
        // Bloque try-catch principal que envuelve toda la lógica de parseo y persistencia.
        // Cualquier excepción no controlada aterrizará en el catch de abajo.

            Date fSiembra = Date.valueOf(fSiembraStr);
            // Convierte el String de fecha (ej: "2024-03-15") a java.sql.Date.
            // Lanzará IllegalArgumentException si el formato no es "yyyy-MM-dd".

            Date fCosecha = estaVacio(fCosechaStr) ? null : Date.valueOf(fCosechaStr);
            // Si la fecha de cosecha no fue ingresada, se guarda null.
            // Si fue ingresada, se convierte igual que la fecha de siembra.
            // Operador ternario: condición ? valor_si_true : valor_si_false.

            int idLote = Integer.parseInt(idLoteStr);
            // Convierte el ID del lote de String a entero.
            // Lanzará NumberFormatException si el valor no es numérico.

            if (fSiembra.toLocalDate().isAfter(java.time.LocalDate.now())) {
            // Convierte fSiembra a LocalDate (API moderna de Java) y compara con la fecha actual.
            // Un cultivo no puede haberse sembrado en el futuro; sería dato inconsistente.

                reenviarAdminConError("La fecha de siembra no puede ser posterior a hoy.", request, response);
                return;
                // Rechaza y termina el método.
            }

            if (fCosecha != null && fCosecha.toLocalDate().isBefore(fSiembra.toLocalDate())) {
            // Solo valida si se ingresó fecha de cosecha (no null).
            // Comprueba coherencia temporal: la cosecha debe ser igual o posterior a la siembra.

                reenviarAdminConError("La cosecha no puede ser anterior a la siembra.", request, response);
                return;
                // Rechaza y termina el método.
            }

            CultivoDao dao = new CultivoDao();
            // Instancia el DAO de cultivos para ejecutar la operación en la BD.

            if (!estaVacio(idStr)) {
            // Si viene un ID en el formulario, es una operación de actualización (UPDATE).

                dao.actualizarCultivo(Integer.parseInt(idStr), nombre, tipo, fSiembra, fCosecha, idLote);
                // Ejecuta el UPDATE en la BD con todos los campos modificados.
                // Integer.parseInt(idStr) convierte el ID de String a int aquí, dentro del if.

            } else {
            // Si no viene ID, es una operación de registro (INSERT).

                dao.registrarCultivo(new cultivo(nombre, tipo, fSiembra, fCosecha, idLote));
                // Crea un nuevo objeto modelo 'cultivo' con los datos del formulario
                // y lo persiste en la BD mediante el DAO.
            }

            response.sendRedirect("ServletCultivo");
            // Patrón PRG: redirige al GET del mismo servlet para evitar doble envío si el usuario recarga.
            // La lista de cultivos se mostrará actualizada.

            return;
            // Termina el método tras el redirect. Aunque sendRedirect no detiene el código,
            // el return explícito evita continuar ejecutando líneas innecesariamente.

        } catch (Exception e) {
        // Captura cualquier excepción no manejada de forma específica arriba:
        // errores de BD, violaciones de restricciones SQL, etc.

            e.printStackTrace();
            // Imprime el stack trace completo en los logs del servidor para diagnóstico.
            // No se muestra al usuario (es información interna de depuración).

            String mensajePersonalizado = "Ocurrió un error inesperado.";
            // Mensaje genérico por defecto que se mostrará al usuario si no se identifica el error.

            if (e.getMessage().contains("Duplicate entry")) {
            // Inspecciona el mensaje de la excepción buscando el texto "Duplicate entry".
            // Este texto lo genera MySQL cuando se viola una restricción UNIQUE en la BD.

                mensajePersonalizado = "El nombre del cultivo ya existe en este lote. Por favor, elige otro nombre.";
                // Sobrescribe el mensaje genérico con uno específico y comprensible para el usuario.

            } else if (e.getMessage().contains("foreign key")) {
            // Detecta violación de clave foránea: ocurre si el lote referenciado no existe en la BD.

                mensajePersonalizado = "No se puede realizar la operación porque depende de otros registros (lotes) que no existen.";
                // Mensaje específico para el error de integridad referencial.
            }

            request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
            // Recarga la lista de cultivos para que el JSP no se muestre vacío al mostrar el error.

            request.setAttribute("catalogoLotes", new LoteDao().listarLotes());
            // Recarga el catálogo de lotes por la misma razón.

            request.setAttribute("mensajeError", mensajePersonalizado);
            // Pone el mensaje de error como atributo del request para que el JSP lo muestre al usuario.

            request.getRequestDispatcher("/templates/administrador/cultivos.jsp").forward(request, response);
            // Reenvía al JSP de administrador con los datos recargados y el mensaje de error visible.
        }
    }

    private void reenviarAdminConError(String mensaje, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    // Método privado auxiliar que centraliza la lógica de "mostrar error al administrador".
    // Evita repetir las mismas 4 líneas en cada punto de validación fallida.
    // 'private' → solo accesible dentro de esta clase.

        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        // Recarga los cultivos para que la página de administración no aparezca vacía.

        request.setAttribute("catalogoLotes", new LoteDao().listarLotes());
        // Recarga los lotes para que el <select> del formulario tenga opciones disponibles.

        request.setAttribute("mensajeError", mensaje);
        // Inyecta el mensaje de error en el request para que el JSP lo renderice.

        request.getRequestDispatcher("/templates/administrador/cultivos.jsp").forward(request, response);
        // Hace forward al JSP de administrador: muestra el formulario de nuevo con el error visible.
        // forward mantiene la misma URL en el navegador (a diferencia de sendRedirect).
    }

    private String sanitizar(String valor) {
    // Método privado auxiliar que limpia los Strings recibidos del formulario.

        return (valor != null) ? valor.trim() : null;
        // Si el valor no es null, elimina espacios al inicio y al final (trim()).
        // Si es null, devuelve null para que los controles de estaVacio() lo detecten.
        // Previene que "  tomate  " se guarde distinto a "tomate".
    }

    private boolean excedeLongitud(String valor, int max) {
    // Método privado auxiliar que valida que un String no supere una longitud máxima.

        return valor != null && valor.length() > max;
        // Devuelve true (excede) solo si el valor no es null Y su longitud supera el máximo.
        // Si es null, devuelve false (no excede; la validación de vacío se encarga de null).
    }
}