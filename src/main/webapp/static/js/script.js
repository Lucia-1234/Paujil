document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('formRegistro');
    const feedback = document.getElementById('mensaje-feedback');

    if (form) {
        form.addEventListener('submit', function(event) {
            // Limpiamos mensajes previos
            feedback.style.display = 'none';
            feedback.className = 'feedback-message';
            
            const pass1 = document.getElementById('txtContrasena').value;
            const pass2 = document.getElementById('txtConfirmarContrasena').value;
            const telefono = document.getElementById('txtTelefono').value;

            // 1. Validación de contraseñas
            if (pass1 !== pass2) {
                event.preventDefault();
                mostrarError("Las contraseñas no coinciden.");
                return;
            }

            // 2. Validación de teléfono (solo 10 dígitos)
            if (!/^\d{10}$/.test(telefono)) {
                event.preventDefault();
                mostrarError("El teléfono debe tener exactamente 10 dígitos.");
                return;
            }
        });
    }

    /**
     * Función auxiliar para mostrar mensajes de error
     */
    function mostrarError(mensaje) {
        feedback.textContent = mensaje;
        feedback.style.display = 'block';
        feedback.classList.add('error-box');
        // Scroll suave al inicio para que el usuario vea el error
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
});


function gestionarUsuario(idUsuario, accion) {
    // Usamos fetch para llamar a tu Servlet de gestión (ejemplo: ServletGestionarUsuario)
    fetch(`${window.location.origin}/TuProyecto/ServletGestionarUsuario?id=${idUsuario}&accion=${accion}`)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Si la operación fue exitosa, removemos el elemento del DOM con una animación
                const userElement = document.getElementById(`user-${idUsuario}`);
                userElement.style.transition = "opacity 0.5s";
                userElement.style.opacity = "0";
                setTimeout(() => userElement.remove(), 500);
            } else {
                alert("Error al procesar la solicitud.");
            }
        })
        .catch(error => console.error('Error:', error));
}


/**
 * Gestiona la aprobación o denegación de usuarios de forma asíncrona.
 * @param {number} idUsuario - El ID del usuario a gestionar.
 * @param {string} accion - 'aceptar' o 'denegar'.
 */
function gestionarUsuario(idUsuario, accion) {
    // 1. Definimos la URL (usando el contexto base si es necesario)
    const url = "GestionarRoles"; 

    // 2. Preparamos los datos
    const formData = new URLSearchParams();
    formData.append('id_usuario', idUsuario);
    formData.append('accion', accion);

    // 3. Enviamos la petición asíncrona
    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        },
        body: formData.toString()
    })
    .then(response => {
        if (response.ok) {
            // Éxito: Buscamos el elemento en el DOM y lo eliminamos con estilo
            const item = document.getElementById(`user-${idUsuario}`);
            if (item) {
                item.style.transition = "opacity 0.5s ease, transform 0.5s ease";
                item.style.opacity = "0";
                item.style.transform = "translateX(20px)";
                
                // Esperamos a que termine la animación para removerlo del DOM
                setTimeout(() => {
                    item.remove();
                    
                    // Opcional: Si la lista queda vacía, avisar al usuario
                    const lista = document.querySelector('.request-list');
                    if (lista && lista.children.length === 0) {
                        lista.innerHTML = '<li style="color:white; padding: 20px; text-align: center;">No hay usuarios pendientes.</li>';
                    }
                }, 500);
            }
        } else {
            console.error("Error en la respuesta del servidor");
            alert("Hubo un problema al procesar la acción. Inténtalo de nuevo.");
        }
    })
    .catch(error => {
        console.error('Error de red:', error);
        alert("No se pudo conectar con el servidor.");
    });
}
