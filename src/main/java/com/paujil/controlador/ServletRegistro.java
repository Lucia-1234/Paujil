package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.validador; // <-- IMPORTANTE: Importamos nuestra clase de utilidades
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import paujil.basedatos.clase_Conexion;

@WebServlet("/ServletRegistro")
public class ServletRegistro extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Recolección de datos
        String nombre = request.getParameter("txtNombre");
        String correo = request.getParameter("txtEmail").toLowerCase().trim();
        String telefono = request.getParameter("txtTelefono");
        String fechaNacimientoStr = request.getParameter("txtFechaNacimiento");
        java.sql.Date fechaNacimiento = java.sql.Date.valueOf(fechaNacimientoStr);
        String direccion = request.getParameter("txtDireccion");
        String rolSeleccionado = request.getParameter("txtRol");
        String contrasena = request.getParameter("txtContrasena");
        String confirmarContrasena = request.getParameter("txtConfirmarContrasena");

        // 2. Filtro de Validaciones (Ahora usando la clase Validador)
        if (!validador.esContrasenaSegura(contrasena)) {
            request.setAttribute("campoError", "txtContrasena"); // Marcamos el campo visualmente
            enviarError("La contraseña debe tener al menos una mayúscula, un número y un símbolo.", request, response);
            return;
        }
        if (!validador.esContrasenaSegura(contrasena)) {
            enviarError("¡Casi listo! Tu contraseña necesita ser más robusta (mayúsculas, números y símbolos).", request, response);
            return;
        }
        if (!validador.esMayorDeEdad(fechaNacimientoStr)) {
            enviarError("Para registrarte, necesitamos que seas mayor de 18 años.", request, response);
            return;
        }
        if (!validador.esTelefonoValido(telefono)) {
            enviarError("El teléfono debe tener 10 dígitos.", request, response);
        return;
        }

        // 3. Verificación de duplicidad
        UsuarioDao dao = new UsuarioDao();
        if (dao.correoExiste(correo) || dao.telefonoExiste(telefono)) {
            enviarError("Parece que ese correo o teléfono ya están registrados.", request, response);
            return;
        }

        // 4. Procesamiento de la transacción
        String estadoInicial = "Pendiente";
        try (Connection con = clase_Conexion.MetodoConectar()) {
            if (con == null) throw new Exception("Error de conexión");
            con.setAutoCommit(false); // Iniciamos transacción

            // A. PRIMERO: Inserción Usuario
            String sqlUser = "INSERT INTO usuarios (nombre_usuario, fecha_nacimiento, direccion_usuario, contrasena_usuario, estado_usuario) VALUES (?, ?, ?, ?, ?)";
            int idGenerado = -1;

            try (PreparedStatement ps = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setDate(2, fechaNacimiento);
                ps.setString(3, direccion);
                ps.setString(4, contrasena);
                ps.setString(5, estadoInicial);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idGenerado = rs.getInt(1);
                }
            }

            // B. AHORA QUE YA TENEMOS ID, insertamos el resto:

            // 1. Insertar Correo
            String sqlCorreo = "INSERT INTO correos (id_correo, id_usuario, direccion_correo) VALUES (?, ?, ?)";
            try (PreparedStatement psC = con.prepareStatement(sqlCorreo)) {
                psC.setNull(1, java.sql.Types.INTEGER);
                psC.setInt(2, idGenerado);
                psC.setString(3, correo);
                psC.executeUpdate();
            }

            // 2. Insertar Teléfono
            String sqlTelefono = "INSERT INTO telefonos (id_telefono, id_usuario, numero_telefono) VALUES (?, ?, ?)";
            try (PreparedStatement psT = con.prepareStatement(sqlTelefono)) {
                psT.setNull(1, java.sql.Types.INTEGER);
                psT.setInt(2, idGenerado);
                psT.setString(3, telefono);
                psT.executeUpdate();
            }

            // 3. Insertar Rol
            String sqlRol = "INSERT INTO usuario_rol (id_usuario_rol, id_usuario, id_rol) VALUES (?, ?, ?)";
            try (PreparedStatement psR = con.prepareStatement(sqlRol)) {
                psR.setNull(1, java.sql.Types.INTEGER);
                psR.setInt(2, idGenerado);
                psR.setInt(3, Integer.parseInt(rolSeleccionado));
                psR.executeUpdate();
            }

            con.commit(); // Se guarda todo si nada falló
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?registro=success");

        } catch (Exception e) {
            e.printStackTrace(); // Esto te dirá en consola qué falló
            enviarError("Error interno en el sistema al guardar el registro.", request, response);
        }
    }
    
        private void enviarError(String mensaje, HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setAttribute("mensaje", mensaje);

        request.getRequestDispatcher("/templates/registro_usuario.jsp").forward(request, response);
    }
    
    
}