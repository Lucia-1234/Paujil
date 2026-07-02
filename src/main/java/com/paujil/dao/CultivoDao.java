package com.paujil.dao; 

import com.paujil.modelo.cultivo;            
import java.sql.*;                              
import java.util.ArrayList;                    
import java.util.List;                          
import paujil.basedatos.clase_Conexion;         

public class CultivoDao { // Declaración de la clase pública CultivoDao

    // Método para listar todos los registros incluyendo el id_lote
    public List<cultivo> listarCultivos() {// Retorna la lista completa de cultivos
        List<cultivo> lista = new ArrayList<>();// Lista donde se acumularán los objetos cultivo leídos
        // CAMBIO: Se añadió id_lote a la consulta
        String sql = "SELECT id_cultivo, nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha, id_lote FROM cultivos"; // Trae todas las columnas relevantes, incluida la FK id_lote

        try (Connection con = clase_Conexion.MetodoConectar(); // Abre conexión (se cierra sola al salir del try)
             PreparedStatement ps = con.prepareStatement(sql); // Prepara la consulta (sin parámetros, trae todo)
             ResultSet rs = ps.executeQuery()) {   // Ejecuta la consulta y obtiene el cursor de resultados

            while (rs.next()) {  // Recorre cada fila del resultado
                cultivo c = new cultivo(); // Crea un nuevo objeto cultivo vacío
                c.setIdCultivo(rs.getInt("id_cultivo")); // Asigna el id leído de la fila actual
                c.setNombreCultivo(rs.getString("nombre_cultivo"));  // Asigna el nombre del cultivo
                c.setTipoCultivo(rs.getString("tipo_cultivo"));  // Asigna el tipo de cultivo
                c.setFechaSiembra(rs.getDate("fecha_siembra")); // Asigna la fecha de siembra (java.sql.Date)
                c.setFechaCosecha(rs.getDate("fecha_cosecha"));  // Asigna la fecha de cosecha (puede ser null si aún no se cosechó)
                c.setIdLote(rs.getInt("id_lote")); // CAMBIO: Seteamos el id_lote  // Asigna el id del lote al que pertenece el cultivo
                lista.add(c);  // Agrega el objeto ya completo a la lista de resultados
            }
        } catch (SQLException e) { // Captura cualquier error de conexión o consulta
            System.err.println("Error al listar cultivos: " + e.getMessage()); // Log corto del error
            e.printStackTrace();  // Traza completa para depuración
        }
        return lista; // Devuelve la lista (vacía si hubo error)
    }

    // Método para insertar incluyendo el id_lote
    // Cambia esto en registrarCultivo y actualizarCultivo
        public int registrarCultivo(cultivo c) throws SQLException { // <--- AÑADE throws SQLException
            // A diferencia de otros métodos, este NO atrapa la excepción internamente:
            // la declara con "throws SQLException" para que quien lo llame (ej. un Servlet) decida cómo manejarla.
            String sql = "INSERT INTO cultivos (nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha, id_lote) VALUES (?, ?, ?, ?, ?)";
            // Sentencia INSERT con 5 parámetros, uno por cada columna a insertar (excepto el id, que es autogenerado)

            // Eliminamos el try-catch interno aquí. 
            // La conexión se cierra sola gracias al try-with-resources.
            try (Connection con = clase_Conexion.MetodoConectar(); // Abre conexión
                 PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                 // El flag RETURN_GENERATED_KEYS le indica al driver que debe devolver
                 // el id autogenerado (autoincrement) que la BD asigne a la nueva fila

                ps.setString(1, c.getNombreCultivo()); // Parámetro 1: nombre del cultivo
                ps.setString(2, c.getTipoCultivo()); // Parámetro 2: tipo del cultivo
                ps.setDate(3, c.getFechaSiembra()); // Parámetro 3: fecha de siembra
                if (c.getFechaCosecha() != null) ps.setDate(4, c.getFechaCosecha()); // Parámetro 4: si hay fecha de cosecha, se asigna
                else ps.setNull(4, Types.DATE); // Si no hay fecha de cosecha, se envía NULL explícito (no se puede omitir un "?")
                ps.setInt(5, c.getIdLote()); // Parámetro 5: id del lote al que pertenece el cultivo

                ps.executeUpdate();  // Ejecuta el INSERT (no se guarda el resultado porque no interesa el número de filas aquí)

                try (ResultSet rs = ps.getGeneratedKeys()) { // Obtiene el ResultSet con el/los id(s) generado(s) por la BD
                    return rs.next() ? rs.getInt(1) : -1;  // Si hay una clave generada, la retorna; si no, retorna -1 como señal de fallo
                }
            }
            // Si ocurre un error, la excepción sube automáticamente al Servlet
            // (no hay catch: SQLException se propaga hacia quien invocó este método)
        }

    // Método para actualizar incluyendo el id_lote
        // Quitamos el try-catch interno y añadimos 'throws SQLException'
        public boolean actualizarCultivo(int id, String nombre, String tipo, Date siembra, Date cosecha, int idLote) throws SQLException {
            // Igual que registrarCultivo: no atrapa el error, lo declara y lo deja subir
            String sql = "UPDATE cultivos SET nombre_cultivo = ?, tipo_cultivo = ?, fecha_siembra = ?, fecha_cosecha = ?, id_lote = ? WHERE id_cultivo = ?";
            // UPDATE con 6 parámetros: 5 columnas a modificar + el id en el WHERE

            // El try-with-resources se encarga de cerrar la conexión incluso si hay error
            try (Connection con = clase_Conexion.MetodoConectar(); // Abre conexión
                 PreparedStatement ps = con.prepareStatement(sql)) {  // Prepara el UPDATE

                ps.setString(1, nombre);  // Parámetro 1: nuevo nombre
                ps.setString(2, tipo);  // Parámetro 2: nuevo tipo
                ps.setDate(3, siembra);  // Parámetro 3: nueva fecha de siembra

                if (cosecha != null) ps.setDate(4, cosecha);  // Parámetro 4: si hay fecha de cosecha, se asigna
                else ps.setNull(4, Types.DATE);  // Si no, se envía NULL explícito

                ps.setInt(5, idLote);  // Parámetro 5: nuevo id de lote
                ps.setInt(6, id); // Parámetro 6: id del cultivo a actualizar (condición WHERE)

                return ps.executeUpdate() > 0;  // true si al menos una fila fue modificada
            } 
            // NO hay catch aquí, la SQLException sube al Servlet automáticamente
        }

    // Método para eliminar simplificado
    public boolean eliminarCultivo(int id) { // Elimina un cultivo por su id
        // CAMBIO: Ya no necesitamos limpiar tablas intermedias (cultivo_lote)
        String sql = "DELETE FROM cultivos WHERE id_cultivo = ?";  // Sentencia DELETE simple, ya no hay tabla intermedia que limpiar antes
        try (Connection con = clase_Conexion.MetodoConectar();// Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) {// Prepara el DELETE
            
            ps.setInt(1, id);  // Asigna el id del cultivo a eliminar
            return ps.executeUpdate() > 0;  // true si se eliminó al menos una fila
        } catch (SQLException e) {// Captura errores SQL
            e.printStackTrace();  // Traza completa (aquí no se imprime mensaje corto adicional, solo la traza)
            return false;  // Error → no se pudo eliminar
        }
    }

    // Método buscarPorId ajustado
    public cultivo buscarPorId(int id) {  // Busca un único cultivo por su id
        String sql = "SELECT * FROM cultivos WHERE id_cultivo = ?";   // Trae todas las columnas de la fila con ese id
        try (Connection con = clase_Conexion.MetodoConectar();  // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) {  // Prepara la consulta
            ps.setInt(1, id);  // Asigna el id buscado al parámetro
            try (ResultSet rs = ps.executeQuery()) {  // Ejecuta la consulta
                if (rs.next()) {  // Si existe una fila con ese id
                    cultivo c = new cultivo(); // Crea el objeto cultivo a llenar
                    c.setIdCultivo(rs.getInt("id_cultivo"));// Asigna el id
                    c.setNombreCultivo(rs.getString("nombre_cultivo"));// Asigna el nombre
                    c.setTipoCultivo(rs.getString("tipo_cultivo")); // Asigna el tipo
                    c.setFechaSiembra(rs.getDate("fecha_siembra")); // Asigna la fecha de siembra
                    c.setFechaCosecha(rs.getDate("fecha_cosecha")); // Asigna la fecha de cosecha (puede ser null)
                    c.setIdLote(rs.getInt("id_lote")); // CAMBIO: Mapeamos id_lote  // Asigna el id del lote asociado
                    return c;  // Retorna el objeto cultivo ya completo
                }
            }
        } catch (SQLException e) {  // Captura errores SQL
            e.printStackTrace();  // Traza completa (sin mensaje corto adicional)
        }
        return null;  // Si no se encontró nada (o hubo error), retorna null
    }
}