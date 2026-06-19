package com.paujil.servicio; 

import com.paujil.dao.AsignacionDao; 
import com.paujil.modelo.asignacion; 
import com.paujil.modelo.trabajo; 
import java.sql.Date;
import java.util.List; 

public class AsignacionServicio { // Clase de servicio para gestionar asignaciones.

    // Instancia privada del DAO; encapsula el acceso a datos.
    private final AsignacionDao dao = new AsignacionDao(); 

    // Registra un trabajo completo vinculándolo a cultivo y usuario.
    public boolean registrarTrabajoCompleto(trabajo t, int idCultivo, int idUsuario, Date fechaAsignacion) {
        return dao.registrarTrabajoCompleto(t, idCultivo, idUsuario, fechaAsignacion);
    }

    // Retorna la lista consolidada de todas las asignaciones.
    public List<asignacion> listarTodas() {
        return dao.listarTodas();
    }

    // Filtra asignaciones por usuario y estado específico.
    public List<asignacion> listarPorUsuario(int idUsuario, String estado) {
        return dao.listarPorUsuario(idUsuario, estado);
    }

    // Actualiza el estado y observaciones de una asignación.
    public boolean actualizarEstado(int idAsignacion, String observaciones, String nuevoEstado) {
        return dao.actualizarEstadoAsignacion(idAsignacion, observaciones, nuevoEstado);
    }

    // Elimina un trabajo mediante su ID.
    public boolean eliminarTrabajo(int idTrabajo) {
        return dao.eliminarTrabajo(idTrabajo);
    }
} // Fin clase.