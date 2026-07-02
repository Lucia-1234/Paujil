package com.paujil.dao; // Declara el paquete al que pertenece esta clase

import com.paujil.modelo.usuario;                // Importa la clase modelo "usuario" (representa un registro de la tabla usuarios)
import java.sql.*;                                // Importa todas las clases de JDBC: Connection, PreparedStatement, ResultSet, Date, etc.
import java.util.ArrayList;                       // Importa ArrayList, implementación concreta de List
import java.util.List;                            // Importa la interfaz List, usada como tipo de retorno
import paujil.basedatos.clase_Conexion;           // Importa la clase encargada de abrir la conexión a la base de datos

public class UsuarioDao { // Clase DAO para gestión de usuarios.  // Declaración de la clase pública UsuarioDao

    // Método para verificar si un correo ya existe en BD.
    public boolean correoExiste(String correo) { // Inicio método.                       // Recibe un correo y verifica si ya está registrado
        String sql = "SELECT id_correo FROM correos WHERE direccion_correo = ?"; // SQL búsqueda.  // Busca en la tabla correos una fila con esa dirección exacta
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.                // Abre conexión (se cierra sola al salir del try)
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.              // Prepara la sentencia con el parámetro "?"
            ps.setString(1, correo); // Setea correo.                                     // Asigna el correo recibido al parámetro
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta consulta.                 // Ejecuta la consulta y obtiene el resultado
                return rs.next(); // Retorna true si encuentra coincidencia.              // Si hay al menos una fila, el correo ya existe → true
            } // Cierra RS.                                                               // El ResultSet se cierra automáticamente aquí
        } catch (SQLException e) { // Manejo errores.                                     // Captura cualquier error de conexión/consulta
            System.err.println("Error al verificar correo: " + e.getMessage()); // Log error. // Imprime mensaje de error (nota: no llama a e.printStackTrace() aquí)
        } // Fin catch.
        return false; // Retorna false por defecto.                                       // Si hubo error, asume que el correo NO existe (no bloquea el registro)
    } // Fin método.

    // Método para verificar si un teléfono ya existe.
    public boolean telefonoExiste(String numero) { // Inicio método.                      // Misma lógica que correoExiste, pero para teléfonos
        String sql = "SELECT id_telefono FROM telefonos WHERE numero_telefono = ?"; // SQL búsqueda. // Busca en la tabla telefonos ese número exacto
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.                // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.              // Prepara la consulta
            ps.setString(1, numero); // Setea número.                                     // Asigna el número recibido al parámetro
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta consulta.                 // Ejecuta la consulta
                return rs.next(); // Retorna existencia.                                  // true si existe al menos una coincidencia
            } // Cierra RS.
        } catch (SQLException e) { // Manejo errores.                                     // Captura errores SQL
            System.err.println("Error al verificar telefono: " + e.getMessage()); // Log error. // Log del error (sin traza completa)
        } // Fin catch.
        return false; // Retorna false por defecto.                                       // Ante error, asume que no existe
    } // Fin método.

    // Lista usuarios en estado 'Pendiente'.
    public List<usuario> listarUsuariosPendientes() { // Inicio método.                   // Devuelve los usuarios cuyo registro aún no fue aprobado
        List<usuario> lista = new ArrayList<>(); // Inicializa lista.                     // Lista donde se acumularán los usuarios encontrados
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.fecha_nacimiento, u.direccion_usuario, " +
                     // Trae los datos básicos del usuario desde la tabla "usuarios" (alias u)
                     "(SELECT c.direccion_correo FROM correos c WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, " +
                     // Subconsulta: obtiene el primer correo asociado a ese usuario (asume 1 correo por usuario aunque la tabla permitiría varios)
                     "(SELECT t.numero_telefono FROM telefonos t WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, " +
                     // Subconsulta: obtiene el primer teléfono asociado a ese usuario
                     "(SELECT r.nombre_rol FROM usuario_rol ur INNER JOIN roles r ON ur.id_rol = r.id_rol WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol " +
                     // Subconsulta: hace JOIN entre usuario_rol y roles para traer el nombre del primer rol asignado
                     "FROM usuarios u WHERE u.estado_usuario = 'Pendiente' ORDER BY u.id_usuario ASC";
                     // Filtra solo usuarios con estado 'Pendiente', ordenados por id ascendente (orden de llegada)
        try (Connection con = clase_Conexion.MetodoConectar();                            // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql);                            // Prepara la consulta (sin parámetros)
             ResultSet rs = ps.executeQuery()) {                                          // Ejecuta la consulta
            while (rs.next()) {                                                           // Recorre cada fila del resultado
                usuario u = new usuario();                                                // Crea un objeto usuario vacío
                u.setIdUsuario(rs.getInt("id_usuario"));                                  // Asigna el id
                u.setNombre(rs.getString("nombre_usuario"));                              // Asigna el nombre
                u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));                     // Asigna la fecha de nacimiento
                u.setDireccion(rs.getString("direccion_usuario"));                        // Asigna la dirección
                u.setCorreo(rs.getString("direccion_correo"));                            // Asigna el correo obtenido de la subconsulta
                u.setTelefono(rs.getString("numero_telefono"));                           // Asigna el teléfono obtenido de la subconsulta
                u.setRol(rs.getString("nombre_rol"));                                     // Asigna el nombre del rol obtenido de la subconsulta
                lista.add(u);                                                             // Agrega el usuario ya completo a la lista
            }
        } catch (SQLException e) { e.printStackTrace(); }                                 // Si hay error, solo imprime la traza (lista queda vacía)
        return lista;                                                                     // Devuelve la lista de usuarios pendientes
    } // Fin método.

    // Lista usuarios activos.
    public List<usuario> listarUsuariosActivos() { // Inicio método.                      // Devuelve los usuarios con estado 'Activo'
        List<usuario> lista = new ArrayList<>();                                          // Lista de resultados
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.estado_usuario, " +
                     // Trae id, nombre y estado del usuario (aquí no se trae fecha_nacimiento ni dirección, a diferencia del método anterior)
                     "(SELECT c.direccion_correo FROM correos c WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, " +
                     // Mismo patrón de subconsulta para el correo
                     "(SELECT t.numero_telefono FROM telefonos t WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, " +
                     // Mismo patrón de subconsulta para el teléfono
                     "(SELECT r.nombre_rol FROM usuario_rol ur INNER JOIN roles r ON ur.id_rol = r.id_rol WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol " +
                     // Mismo patrón de subconsulta para el rol
                     "FROM usuarios u WHERE u.estado_usuario = 'Activo' ORDER BY u.nombre_usuario ASC";
                     // Filtra solo estado 'Activo', ordenado alfabéticamente por nombre (a diferencia de pendientes, que ordena por id)
        try (Connection con = clase_Conexion.MetodoConectar();                            // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql);                            // Prepara consulta
             ResultSet rs = ps.executeQuery()) {                                          // Ejecuta consulta
            while (rs.next()) {                                                           // Recorre cada fila
                usuario u = new usuario();                                                // Crea objeto usuario
                u.setIdUsuario(rs.getInt("id_usuario"));                                  // Asigna id
                u.setNombre(rs.getString("nombre_usuario"));                              // Asigna nombre
                u.setEstado(rs.getString("estado_usuario"));                              // Asigna estado ('Activo')
                u.setCorreo(rs.getString("direccion_correo"));                            // Asigna correo
                u.setTelefono(rs.getString("numero_telefono"));                           // Asigna teléfono
                u.setRol(rs.getString("nombre_rol"));                                     // Asigna rol
                lista.add(u);                                                             // Agrega a la lista
            }
        } catch (SQLException e) { e.printStackTrace(); }                                 // Ante error, solo traza (lista queda vacía o parcial)
        return lista;                                                                     // Devuelve la lista de usuarios activos
    } // Fin método.

    // Lista todos los usuarios (Activos e Inactivos).
    public List<usuario> listarTodosLosUsuarios() { // Inicio método.                     // Devuelve usuarios en estado Activo o Inactivo (excluye Pendientes)
        List<usuario> lista = new ArrayList<>();                                          // Lista de resultados
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.estado_usuario, " +
                     // Misma estructura de columnas que listarUsuariosActivos
                     "(SELECT c.direccion_correo FROM correos c WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, " +
                     "(SELECT t.numero_telefono FROM telefonos t WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, " +
                     "(SELECT r.nombre_rol FROM usuario_rol ur INNER JOIN roles r ON ur.id_rol = r.id_rol WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol " +
                     "FROM usuarios u WHERE u.estado_usuario IN ('Activo', 'Inactivo') ORDER BY u.nombre_usuario ASC";
                     // Diferencia clave: usa IN (...) para traer dos estados a la vez, en lugar de un solo "="
        try (Connection con = clase_Conexion.MetodoConectar();                            // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql);                            // Prepara consulta
             ResultSet rs = ps.executeQuery()) {                                          // Ejecuta consulta
            while (rs.next()) {                                                           // Recorre cada fila
                usuario u = new usuario();                                                // Crea objeto usuario
                u.setIdUsuario(rs.getInt("id_usuario"));                                  // Asigna id
                u.setNombre(rs.getString("nombre_usuario"));                              // Asigna nombre
                u.setEstado(rs.getString("estado_usuario"));                              // Asigna estado (Activo o Inactivo)
                u.setCorreo(rs.getString("direccion_correo"));                            // Asigna correo
                u.setTelefono(rs.getString("numero_telefono"));                           // Asigna teléfono
                u.setRol(rs.getString("nombre_rol"));                                     // Asigna rol
                lista.add(u);                                                             // Agrega a la lista
            }
        } catch (SQLException e) { e.printStackTrace(); }                                 // Ante error, solo traza
        return lista;                                                                     // Devuelve la lista completa
    } // Fin método.

    // Actualiza estado del usuario.
    public boolean actualizarEstado(int idUsuario, String nuevoEstado) { // Inicio método. // Cambia el campo estado_usuario (ej: de 'Pendiente' a 'Activo')
        String sql = "UPDATE usuarios SET estado_usuario = ? WHERE id_usuario = ?";       // UPDATE simple con 2 parámetros
        try (Connection con = clase_Conexion.MetodoConectar();                            // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) {                          // Prepara UPDATE
            ps.setString(1, nuevoEstado);                                                 // Parámetro 1: nuevo valor del estado
            ps.setInt(2, idUsuario);                                                      // Parámetro 2: id del usuario a modificar
            return ps.executeUpdate() > 0;                                                // true si se actualizó al menos una fila
        } catch (SQLException e) { e.printStackTrace(); return false; }                   // Ante error, traza y retorna false
    } // Fin método.
    
    
    // Verifica si el usuario tiene asignaciones en estado activo (no finalizadas).
    // Se usa antes de eliminar para evitar borrar trabajadores con trabajo pendiente.
    public boolean tieneAsignacionesActivas(int idUsuario) {                              // Validación previa a eliminar un usuario
        String sql = "SELECT COUNT(*) FROM asignaciones " +
                     "WHERE id_usuario = ? " +
                     "AND estado_trabajo IN ('Pendiente', 'En proceso', 'En revisión')";
                     // Cuenta asignaciones de trabajo cuyo estado indique que aún no han terminado
        try (Connection con = clase_Conexion.MetodoConectar();                            // Abre conexión
             PreparedStatement ps = con.prepareStatement(sql)) {                          // Prepara consulta
            ps.setInt(1, idUsuario);                                                      // Asigna el id de usuario a verificar
            try (ResultSet rs = ps.executeQuery()) {                                      // Ejecuta el COUNT(*)
                if (rs.next()) return rs.getInt(1) > 0; // True si hay al menos una.       // Si el conteo es mayor a 0, tiene asignaciones activas
            }
        } catch (SQLException e) {                                                        // Captura errores
            System.err.println("Error al verificar asignaciones activas: " + e.getMessage()); // Log del error
        }
        return false; // Por seguridad, si falla la consulta no bloquea.                  // OJO: a diferencia de tieneCultivosAsignados en LoteDao,
                                                                                            // aquí ante un error NO se bloquea (retorna false), es el comportamiento opuesto
    }

    // Elimina usuario y todos sus registros dependientes (correos, teléfonos, rol).
    // Las tablas correos, telefonos y usuario_rol no tienen ON DELETE CASCADE,
    // por lo que se borran manualmente en transacción antes de eliminar el usuario.
    public boolean eliminarUsuario(int idUsuario) { // Inicio método.                     // Elimina un usuario junto con sus datos relacionados, de forma atómica
        Connection con = null; // Conexión nula para manejo manual.                       // Se declara fuera del try porque se necesita en el catch y el finally
        try { // Try inicio.
            con = clase_Conexion.MetodoConectar(); // Conecta.                            // Abre la conexión
            con.setAutoCommit(false); // Inicia transacción manual.                       // Desactiva el autocommit: todos los cambios se agrupan en una sola transacción
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuario_rol WHERE id_usuario = ?")) { // Elimina roles.
                ps.setInt(1, idUsuario); ps.executeUpdate(); // Ejecuta.                  // Borra las filas de usuario_rol asociadas (relación usuario-rol)
            } // Cierra PS.
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM correos WHERE id_usuario = ?")) { // Elimina correos.
                ps.setInt(1, idUsuario); ps.executeUpdate(); // Ejecuta.                  // Borra los correos del usuario
            } // Cierra PS.
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM telefonos WHERE id_usuario = ?")) { // Elimina teléfonos.
                ps.setInt(1, idUsuario); ps.executeUpdate(); // Ejecuta.                  // Borra los teléfonos del usuario
            } // Cierra PS.
            int filas; // Variable para verificar si el usuario existía.                  // Se usará para saber si realmente se borró un usuario
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuarios WHERE id_usuario = ?")) { // Elimina usuario.
                ps.setInt(1, idUsuario); filas = ps.executeUpdate(); // Ejecuta.          // Borra el usuario y guarda cuántas filas se afectaron
            } // Cierra PS.
            if (filas == 0) { con.rollback(); return false; } // Rollback si no existía el usuario. // Si no se borró ningún usuario (no existía), deshace todo lo anterior también
            con.commit(); // Confirma transacción.                                        // Si todo salió bien, confirma los 4 DELETE de forma permanente
            return true; // Éxito.                                                        // Indica que la eliminación fue exitosa
        } catch (SQLException e) { // Manejo error.
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Rollback. // Ante cualquier error, deshace todos los cambios de la transacción
            e.printStackTrace(); return false; // Error.                                  // Traza del error original y retorna false
        } finally { // Cierra.
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); } // Cierra. 
            // Pase lo que pase (éxito o error), restaura el autocommit a true y cierra la conexión manualmente
            // (aquí NO hay try-with-resources porque la conexión se maneja "a mano" para controlar la transacción)
        } // Fin finally.
    } // Fin método.

    // Registra usuario completo mediante transacción.
    public boolean registrarUsuarioCompleto(String nombre, String correo, String telefono, Date fechaNac, String dir, String passHash, int idRol) { // Inicio.
        // Recibe todos los datos necesarios para crear un usuario y sus registros relacionados (correo, teléfono, rol) en una sola transacción
        Connection con = null;  // Conexión manual, igual que en eliminarUsuario
        try {
            con = clase_Conexion.MetodoConectar();// Abre conexión
            con.setAutoCommit(false); // Inicia transacción manual (todo se confirma junto o se deshace junto)
            int idGenerado; // Guardará el id autoincremental del nuevo usuario
            String sqlUser = "INSERT INTO usuarios (nombre_usuario, fecha_nacimiento, direccion_usuario, contrasena_usuario, estado_usuario) VALUES (?, ?, ?, ?, 'Pendiente')";
            // INSERT del usuario base; el estado se fija en 'Pendiente' directamente en el SQL (no es un parámetro)
            try (PreparedStatement ps = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                // Pide que la BD devuelva el id autogenerado del nuevo usuario
                ps.setString(1, nombre); ps.setDate(2, fechaNac); ps.setString(3, dir); ps.setString(4, passHash);
                // Asigna nombre, fecha de nacimiento, dirección y contraseña (ya hasheada) a los 4 parámetros
                ps.executeUpdate(); // Ejecuta el INSERT del usuario
                try (ResultSet rs = ps.getGeneratedKeys()) { // Obtiene el id generado
                    if (rs.next()) idGenerado = rs.getInt(1); else { con.rollback(); return false; }
                    // Si se obtuvo el id, se guarda; si no (caso anómalo), deshace la transacción y retorna false
                }
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO correos (id_usuario, direccion_correo) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado); ps.setString(2, correo); ps.executeUpdate();
                // Inserta el correo asociado al id recién generado
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO telefonos (id_usuario, numero_telefono) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado); ps.setString(2, telefono); ps.executeUpdate();
                // Inserta el teléfono asociado al id recién generado
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado); ps.setInt(2, idRol); ps.executeUpdate();
                // Inserta la relación usuario-rol asociada al id recién generado
            }
            con.commit();// Si los 4 INSERT salieron bien, confirma la transacción completa
            return true;// Registro exitoso
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            // Ante cualquier error en cualquiera de los 4 INSERT, deshace todo (no queda un usuario "a medias")
            e.printStackTrace(); return false; // Traza del error y retorna false
        } finally {
            if (con != null) try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            // Cierra la conexión siempre al final
            // (nota: a diferencia de eliminarUsuario, aquí NO se restaura setAutoCommit(true) antes de cerrar;
            // no es un problema porque la conexión se cierra de todas formas, pero es una pequeña inconsistencia entre ambos métodos)
        }
    } // Fin método.
} // Fin clase UsuarioDao.