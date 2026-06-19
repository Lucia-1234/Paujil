package com.paujil.dao; 

import com.paujil.modelo.tipoTrabajo; 
import java.sql.*; 
import java.util.ArrayList;
import java.util.List; 
import paujil.basedatos.clase_Conexion; 

public class TipoTrabajoDao { // Clase DAO para gestión de tipos de trabajo.

    // Método para listar todos los tipos registrados.
    public List<tipoTrabajo> listarTipos() { // Inicio método listar.
        List<tipoTrabajo> lista = new ArrayList<>(); // Inicializa la lista.
        String sql = "SELECT * FROM tipos_trabajo"; // Query de selección.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta a BD.
             PreparedStatement ps = con.prepareStatement(sql); // Prepara sentencia.
             ResultSet rs = ps.executeQuery()) { // Ejecuta consulta.
            while (rs.next()) { // Itera filas.
                tipoTrabajo tipo = new tipoTrabajo(); // Crea objeto.
                tipo.setIdTipoTrabajo(rs.getInt("id_tipo_trabajo")); // Mapea ID.
                tipo.setNombreTipo(rs.getString("nombre_tipo")); // Mapea nombre.
                lista.add(tipo); // Agrega a lista.
            } // Fin while.
        } catch (SQLException e) { // Manejo de errores.
            e.printStackTrace(); // Imprime traza error.
        } // Fin catch.
        return lista; // Retorna la lista resultante.
    } // Fin método listarTipos.

    // Método para registrar un nuevo tipo de trabajo.
    public boolean registrarTipo(String nombre) { // Inicio método registrar.
        String sql = "INSERT INTO tipos_trabajo (nombre_tipo) VALUES (?)"; // Query inserción.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setString(1, nombre.trim()); // Setea nombre limpio.
            return ps.executeUpdate() > 0; // Ejecuta e indica éxito.
        } catch (SQLException e) { // Manejo errores.
            e.printStackTrace(); // Imprime traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método registrarTipo.

    // Método para eliminar un tipo de trabajo por ID.
    public boolean eliminarTipo(int id) { // Inicio método eliminar.
        String sql = "DELETE FROM tipos_trabajo WHERE id_tipo_trabajo = ?"; // Query borrado.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, id); // Setea ID para borrar.
            return ps.executeUpdate() > 0; // Ejecuta e indica éxito.
        } catch (SQLException e) { // Manejo errores.
            e.printStackTrace(); // Imprime traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método eliminarTipo.

    // Método para editar el nombre de un tipo existente.
    public boolean editarTipo(int id, String nuevoNombre) { // Inicio método editar.
        String sql = "UPDATE tipos_trabajo SET nombre_tipo = ? WHERE id_tipo_trabajo = ?"; // Query update.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setString(1, nuevoNombre.trim()); // Setea nuevo nombre.
            ps.setInt(2, id); // Setea ID para filtrar.
            return ps.executeUpdate() > 0; // Ejecuta e indica éxito.
        } catch (SQLException e) { // Manejo errores.
            e.printStackTrace(); // Imprime traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método editarTipo.

    // Método para contar trabajos asociados a un tipo, útil para validaciones.
    public int contarTrabajosPorTipo(int idTipoTrabajo) { // Inicio método contar.
        String sql = "SELECT COUNT(*) FROM trabajos WHERE id_tipo_trabajo = ?"; // Query conteo.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, idTipoTrabajo); // Setea ID para filtrar.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta.
                if (rs.next()) return rs.getInt(1); // Retorna contador.
            } // Fin try-with-resources rs.
        } catch (SQLException e) { // Manejo errores.
            e.printStackTrace(); // Imprime traza.
        } // Fin catch.
        return 0; // Retorna 0 por defecto.
    } // Fin método contarTrabajosPorTipo.
} // Fin clase TipoTrabajoDao.