package com.paujil.servicio;

import com.paujil.dao.AsignacionDao;
import com.paujil.modelo.asignacion;
import com.paujil.modelo.trabajo;
import java.sql.Date;
import java.util.List;

public class AsignacionServicio {

    private final AsignacionDao dao = new AsignacionDao();

    public boolean registrarTrabajoCompleto(trabajo t, int idCultivo, int idUsuario, Date fechaAsignacion) {
        return dao.registrarTrabajoCompleto(t, idCultivo, idUsuario, fechaAsignacion);
    }

    public List<asignacion> listarTodas() {
        return dao.listarTodas();
    }

    public List<asignacion> listarPorUsuario(int idUsuario, String estado) {
        return dao.listarPorUsuario(idUsuario, estado);
    }

    public boolean actualizarEstado(int idAsignacion, String observaciones, String nuevoEstado) {
        return dao.actualizarEstadoAsignacion(idAsignacion, observaciones, nuevoEstado);
    }

    public boolean eliminarTrabajo(int idTrabajo) {
        return dao.eliminarTrabajo(idTrabajo);
    }
}