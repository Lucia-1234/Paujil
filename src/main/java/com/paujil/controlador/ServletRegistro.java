package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.Rol;
import com.paujil.modelo.validador;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ServletRegistro")
public class ServletRegistro extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lee todos los campos del formulario antes de iniciar cualquier validacion
        String nombre    = request.getParameter("txtNombre");
        String correo    = request.getParameter("txtEmail");
        String telefono  = request.getParameter("txtTelefono");
        String fechaNac  = request.getParameter("txtFechaNacimiento");
        String direccion = request.getParameter("txtDireccion");
        // Valor esperado: "trabajador" o "administrador"; se normaliza antes de resolver el enum
        String rolStr    = request.getParameter("txtRol");
        String pass      = request.getParameter("txtContrasena");
        // Campo de confirmacion; solo se usa para validar coincidencia, no se persiste
        String passConf  = request.getParameter("txtConfirmarContrasena");

        // Normaliza correo y rol a minusculas para comparaciones seguras sin importar el caso del input
        if (correo   != null) correo   = correo.trim().toLowerCase();
        // trim() en telefono evita que espacios accidentales rompan la validacion del patron de digitos
        if (telefono != null) telefono = telefono.trim();
        if (rolStr   != null) rolStr   = rolStr.trim().toLowerCase();

        // Validacion temprana: todos los campos son obligatorios para construir un usuario valido
        if (estaVacio(nombre) || estaVacio(correo) || estaVacio(telefono)
                || estaVacio(fechaNac) || estaVacio(direccion)
                || estaVacio(rolStr)   || estaVacio(pass)) {
            enviarError("Todos los campos son obligatorios.", request, response);
            return;
        }

        // Verifica coincidencia antes de aplicar cualquier politica de seguridad sobre la contrasena
        if (!pass.equals(passConf)) {
            enviarError("Las contrasenas no coinciden.", request, response);
            return;
        }

        // Delega la politica de complejidad al validador centralizado para mantener una sola fuente de verdad
        if (!validador.esContrasenaSegura(pass)) {
            enviarError("La contrasena no es segura (minimo 8 caracteres, "
                    + "mayusculas, minusculas, numero y simbolo).", request, response);
            return;
        }

        // Patron exacto de 10 digitos; rechaza guiones, espacios o codigos de pais
        if (!telefono.matches("\\d{10}")) {
            enviarError("El telefono debe tener exactamente 10 digitos.", request, response);
            return;
        }

        // Rol.desde() resuelve el string al enum correspondiente y lanza excepcion si el valor no existe
        // El id numerico del enum debe coincidir con los registros reales de la tabla 'roles' en BD
        Rol rol;
        try {
            rol = Rol.desde(rolStr);
        } catch (IllegalArgumentException e) {
            // Valor de rol no reconocido; puede indicar manipulacion del formulario
            enviarError("Rol no valido.", request, response);
            return;
        }

        // Date.valueOf espera formato estricto "yyyy-MM-dd"; cualquier otra forma lanza excepcion
        java.sql.Date fechaSql;
        try {
            fechaSql = java.sql.Date.valueOf(fechaNac);
        } catch (IllegalArgumentException e) {
            enviarError("Formato de fecha invalido.", request, response);
            return;
        }

        UsuarioDao dao = new UsuarioDao();

        // Consulta de duplicado antes de insertar para evitar violar restricciones UNIQUE en BD
        if (dao.correoExiste(correo)) {
            enviarError("El correo electronico ya esta registrado.", request, response);
            return;
        }
        // Verificacion separada del telefono para dar un mensaje de error especifico al usuario
        if (dao.telefonoExiste(telefono)) {
            enviarError("El numero de telefono ya esta registrado.", request, response);
            return;
        }

        // BCrypt hashea la contrasena en texto plano; nunca se almacena el valor original
        String passHash = com.paujil.utils.Seguridad.encriptar(pass);

        // Delega la insercion atomica (usuario + correo + telefono + rol) a una transaccion en el DAO
        boolean exito = dao.registrarUsuarioCompleto(
                nombre,
                correo,
                telefono,
                fechaSql,
                direccion,
                // Se envia el hash, nunca la contrasena en claro
                passHash,
                // getIdBd() traduce el enum al id numerico que espera la clave foranea en BD
                rol.getIdBd()
        );

        if (exito) {
            // El usuario se crea en estado 'Pendiente'; requiere aprobacion del administrador para activarse
            response.sendRedirect(request.getContextPath()
                    + "/templates/login.jsp?registro=success");
        } else {
            enviarError("Error al guardar en base de datos. Intente nuevamente.", request, response);
        }
    }

    // Centraliza la verificacion de vacios para no repetir la condicion null + trim en cada campo
    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    // Usa forward para preservar el mensaje y los valores del formulario en el scope de request;
    // un redirect los perderia y el usuario tendria que reescribir todos los campos
    private void enviarError(String mensaje, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // El JSP lee este atributo para mostrar el aviso de error al usuario
        request.setAttribute("mensaje", mensaje);
        // Repobla el formulario con los valores previos para no obligar al usuario a reingresar todo
        request.setAttribute("nombre",    request.getParameter("txtNombre"));
        request.setAttribute("email",     request.getParameter("txtEmail"));
        request.setAttribute("telefono",  request.getParameter("txtTelefono"));
        request.setAttribute("direccion", request.getParameter("txtDireccion"));
        // La fecha se preserva porque suele ser el campo mas tedioso de reingresar
        request.setAttribute("fecha",     request.getParameter("txtFechaNacimiento"));
        request.getRequestDispatcher("/templates/registro_usuario.jsp")
               .forward(request, response);
    }
}