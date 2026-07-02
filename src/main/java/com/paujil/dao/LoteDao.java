package com.paujil.dao; 

import com.paujil.modelo.lote;                
import java.sql.*;                              
import java.util.ArrayList;                     
import java.util.List;                          
import paujil.basedatos.clase_Conexion;         

/**
 * DAO (Data Access Object) para operaciones CRUD sobre la tabla lotes.
 * Un DAO encapsula toda la lógica de acceso a datos, separándola de la lógica de negocio.
 */
public class LoteDao { // Declaración de la clase pública LoteDao

    // ─────────────────────────────────────────────────────────────
    // CRUD BÁSICO
    // ─────────────────────────────────────────────────────────────

    /**
     * Retorna todos los lotes ordenados alfabéticamente.
     */
    public List<lote> listarLotes() { // Método público que devuelve una lista de objetos "lote"
        List<lote> lista = new ArrayList<>(); // Crea la lista vacía donde se irán agregando los lotes leídos
        String sql = "SELECT id_lote, nombre_lote FROM lotes ORDER BY nombre_lote"; // Consulta SQL: trae id y nombre, ordenados por nombre

        try (Connection con = clase_Conexion.MetodoConectar();// Abre la conexión a la BD (se cerrará automáticamente al salir del try)
             PreparedStatement ps = con.prepareStatement(sql);  // Prepara la sentencia SQL (protege contra SQL injection, aunque aquí no hay parámetros)
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta y obtiene el cursor de resultados

            while (rs.next()) {  // Recorre cada fila del resultado, una por una
                lista.add(new lote(rs.getInt("id_lote"), rs.getString("nombre_lote"))); // Crea un objeto lote con los datos de la fila y lo agrega a la lista
            }
        } catch (SQLException e) { // Captura cualquier error ocurrido durante la conexión/consulta
            System.err.println("Error al listar lotes: " + e.getMessage()); // Imprime un mensaje corto de error en consola (salida de error estándar)
            e.printStackTrace(); // Imprime la traza completa del error, útil para depuración
        }
        return lista;  // Devuelve la lista (vacía si hubo error, o con datos si todo salió bien)
    }

    /**
     * Busca un lote por su ID. Retorna null si no existe.
     */
    public lote buscarPorId(int id) { // Método que busca un único lote según su id
        String sql = "SELECT id_lote, nombre_lote FROM lotes WHERE id_lote = ?"; // Consulta con un parámetro (?) para el id
        try (Connection con = clase_Conexion.MetodoConectar(); // Abre conexión (auto-cierre garantizado)
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la sentencia con el placeholder "?"

            ps.setInt(1, id);// Asigna el valor de "id" al primer "?" de la consulta
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta; ResultSet también se cierra automáticamente
                if (rs.next()) {// Si existe al menos una fila (debería ser máximo una, por ser PK)
                    return new lote(rs.getInt("id_lote"), rs.getString("nombre_lote")); // Construye y retorna el objeto lote encontrado
                }
            }
        } catch (SQLException e) {// Captura errores de conexión o de la consulta
            System.err.println("Error al buscar lote id=" + id + ": " + e.getMessage()); // Log del error incluyendo el id buscado
            e.printStackTrace(); // Traza completa para depuración
        }
        return null; // Si no se encontró nada (o hubo error), retorna null
    }

    /**
     * Inserta un nuevo lote. Retorna true si se creó correctamente.
     */
    public boolean registrarLote(lote l) { // Recibe un objeto lote (con el nombre ya cargado) y lo inserta
        String sql = "INSERT INTO lotes (nombre_lote) VALUES (?)";// Sentencia INSERT con un solo parámetro: nombre_lote
        try (Connection con = clase_Conexion.MetodoConectar();// Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) {// Prepara el INSERT

            ps.setString(1, l.getNombreLote()); // Asigna el nombre del lote al parámetro "?"
            return ps.executeUpdate() > 0; // Ejecuta el INSERT; executeUpdate() retorna cuántas filas se afectaron.
            // Si es mayor a 0, la inserción fue exitosa → true
        } catch (SQLException e) { // Captura errores (ej: violación de UNIQUE, conexión caída, etc.)
            System.err.println("Error al registrar lote: " + e.getMessage()); // Mensaje de error en consola
            e.printStackTrace(); // Traza completa
            return false;  // Ante cualquier error, se considera que la operación falló
        }
    }

    /**
     * Actualiza el nombre de un lote existente.
     */
    public boolean actualizarLote(int id, String nombre) {// Recibe el id del lote a modificar y su nuevo nombre
        String sql = "UPDATE lotes SET nombre_lote = ? WHERE id_lote = ?"; // UPDATE con dos parámetros: nuevo nombre y el id a filtrar
        try (Connection con = clase_Conexion.MetodoConectar(); // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el UPDATE

            ps.setString(1, nombre); // Primer "?" → nuevo nombre
            ps.setInt(2, id); // Segundo "?" → id del lote a actualizar
            return ps.executeUpdate() > 0;  // Si se actualizó al menos una fila, retorna true
        } catch (SQLException e) { // Captura errores SQL
            System.err.println("Error al actualizar lote id=" + id + ": " + e.getMessage()); // Log con el id afectado
            e.printStackTrace();  // Traza completa
            return false;  // Error → operación fallida
        }
    }

    /**
     * Elimina un lote solo si no tiene cultivos asignados.
     * Retorna false si el lote tiene cultivos o si ocurrió un error.
     */
    public boolean eliminarLote(int id) { // Elimina un lote de forma segura (con validación previa)
        if (tieneCultivosAsignados(id)) { // Antes de borrar, verifica si el lote tiene cultivos relacionados (FK)
            return false;// Si tiene cultivos, se bloquea el borrado (evita romper integridad referencial)
        }
        String sql = "DELETE FROM lotes WHERE id_lote = ?";  // Sentencia DELETE filtrando por id
        try (Connection con = clase_Conexion.MetodoConectar();// Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara el DELETE

            ps.setInt(1, id); // Asigna el id al parámetro "?"
            return ps.executeUpdate() > 0; // Si se borró al menos una fila, retorna true
        } catch (SQLException e) {  // Captura errores SQL
            System.err.println("Error al eliminar lote id=" + id + ": " + e.getMessage()); // Log con el id
            e.printStackTrace(); // Traza completa
            return false;  // Error → no se pudo eliminar
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDACIONES
    // ─────────────────────────────────────────────────────────────

    // Verifica si ya existe un lote con el mismo nombre en la base de datos.
    // Si se pasa idExcluir > 0, excluye ese registro (sirve para edición: un lote no se bloquea a sí mismo).
    // Si idExcluir = 0, busca en todos los registros (sirve para creación).
    public boolean existeNombreLote(String nombre, int idExcluir) { // Método de validación de nombre único

        // Construye la consulta SQL según si se excluye un id o no.
        // LOWER() en ambos lados hace la comparación insensible a mayúsculas ("Lote A" == "lote a").
        String sql = idExcluir > 0 // Operador ternario: elige una de dos consultas según el caso
            ? "SELECT COUNT(*) FROM lotes WHERE LOWER(nombre_lote) = LOWER(?) AND id_lote <> ?" // Caso edición: excluye el propio id
            : "SELECT COUNT(*) FROM lotes WHERE LOWER(nombre_lote) = LOWER(?)"; // Caso creación: revisa todos los registros

        // try-with-resources: Connection y PreparedStatement se cierran automáticamente
        // al salir del bloque, sin importar si hay excepción o no.
        try (Connection con = clase_Conexion.MetodoConectar(); // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) {  // Prepara la consulta (con 1 o 2 parámetros, según el caso)

            // Asigna el nombre al primer parámetro (?) de la consulta.
            ps.setString(1, nombre); // Siempre se asigna el nombre como primer parámetro

            // Solo asigna el segundo parámetro si realmente hay un id a excluir.
            // Si la consulta no tiene segundo ?, llamar setInt(2,...) lanzaría una excepción.
            if (idExcluir > 0) ps.setInt(2, idExcluir);  // Solo si aplica, asigna el id a excluir como segundo parámetro

            // Ejecuta la consulta y abre el ResultSet también con try-with-resources.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta el COUNT(*) y obtiene el resultado

                // COUNT(*) siempre retorna exactamente una fila, así que rs.next() casi siempre es true.
                // rs.getInt(1) lee el valor de la primera (y única) columna de esa fila.
                // Si el conteo es > 0, el nombre ya existe → retorna true.
                return rs.next() && rs.getInt(1) > 0;  // true si existe al menos un registro con ese nombre

            }

        } catch (SQLException e) { // Captura errores de conexión/consulta

            // Registra el error en consola para que aparezca en los logs del servidor.
            System.err.println("Error al verificar nombre de lote: " + e.getMessage()); // Log del error

            // Imprime el stack trace completo para facilitar el diagnóstico en desarrollo.
            e.printStackTrace(); // Traza completa

            // Ante una falla de BD, retorna false (no bloquea la operación).
            // El constraint UNIQUE definido en la tabla lotes actúa como red de seguridad final:
            // si duplicado llega a la BD de todas formas, MySQL lanza su propio error.
            return false;  // Ante error, no bloquea (deja que el UNIQUE de la BD actúe como respaldo)
        }
    }

    /**
     * Verifica si un lote tiene cultivos asociados mediante la FK id_lote en cultivos.
     * Se usa para proteger el borrado de lotes con cultivos activos.
     * Ante un error de BD retorna true para bloquear el borrado por precaución.
     */
    public boolean tieneCultivosAsignados(int idLote) {  // Verifica existencia de cultivos relacionados a un lote
        String sql = "SELECT COUNT(*) FROM cultivos WHERE id_lote = ?";  // Cuenta cuántos cultivos referencian este id_lote
        try (Connection con = clase_Conexion.MetodoConectar(); // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la consulta

            ps.setInt(1, idLote); // Asigna el id del lote a consultar
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta el COUNT(*)
                return rs.next() && rs.getInt(1) > 0;  // true si hay al menos un cultivo asociado
            }
        } catch (SQLException e) {  // Captura errores
            System.err.println("Error al verificar cultivos del lote id=" + idLote + ": " + e.getMessage()); // Log con el id
            e.printStackTrace(); // Traza completa
            return true; // Bloquea ante la duda para proteger integridad. // Ante error, asume que SÍ tiene cultivos (comportamiento seguro/conservador)
        }
    }

    /**
     * Cuenta cuántos cultivos tiene asignados un lote.
     * Útil para mostrar información en la UI antes de intentar borrar.
     */
    public int contarCultivosPorLote(int idLote) {// Devuelve el número exacto de cultivos de un lote
        String sql = "SELECT COUNT(*) AS total FROM cultivos WHERE id_lote = ?"; // Consulta con alias "total" para la columna del conteo
        try (Connection con = clase_Conexion.MetodoConectar();// Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara la consulta

            ps.setInt(1, idLote);  // Asigna el id del lote
            try (ResultSet rs = ps.executeQuery()) {  // Ejecuta la consulta
                if (rs.next()) return rs.getInt("total");  // Si hay resultado, retorna el valor de la columna "total"
            }
        } catch (SQLException e) {  // Captura errores
            System.err.println("Error al contar cultivos del lote id=" + idLote + ": " + e.getMessage()); // Log con el id
            e.printStackTrace();  // Traza completa
        }
        return 0;  // Si hubo error o no hay resultado, retorna 0 por defecto
    }

    /**
     * Lista los cultivos asociados a un lote (relación inversa).
     * Útil para mostrar en la UI qué cultivos usa un lote antes de editarlo o eliminarlo.
     *
     * @return Lista de nombres de cultivo que pertenecen al lote.
     */
    public List<String> listarNombresCultivosPorLote(int idLote) { // Devuelve solo los nombres de los cultivos de un lote
        List<String> nombres = new ArrayList<>();  // Lista donde se acumularán los nombres
        String sql = "SELECT nombre_cultivo FROM cultivos WHERE id_lote = ? ORDER BY nombre_cultivo"; // Consulta ordenada alfabéticamente
        try (Connection con = clase_Conexion.MetodoConectar(); // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) {// Prepara la consulta

            ps.setInt(1, idLote);  // Asigna el id del lote a filtrar
            try (ResultSet rs = ps.executeQuery()) {  // Ejecuta la consulta
                while (rs.next()) {  // Recorre cada fila del resultado
                    nombres.add(rs.getString("nombre_cultivo")); // Agrega el nombre del cultivo a la lista
                }
            }
        } catch (SQLException e) {   // Captura errores
            System.err.println("Error al listar cultivos del lote id=" + idLote + ": " + e.getMessage()); // Log con el id
            e.printStackTrace();  // Traza completa
        }
        return nombres;   // Devuelve la lista (vacía si no hay cultivos o hubo error)
    }

} // Fin clase LoteDao.