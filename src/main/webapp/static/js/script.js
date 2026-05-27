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


document.addEventListener('DOMContentLoaded', () => {
    
    // 1. Manejo de Confirmación para eliminar cultivos
    const container = document.querySelector('.crop-list');
    
    if (container) {
        container.addEventListener('click', (e) => {
            // Verificar si el clic fue en un botón de eliminar
            const btnDelete = e.target.closest('.btn--delete');
            
            if (btnDelete) {
                if (!confirm('¿Está seguro de que desea eliminar este cultivo? Esta acción no se puede deshacer.')) {
                    e.preventDefault(); // Detiene la navegación del enlace <a>
                }
            }
        });
    }

    // 2. Manejo de "Agregar Registro" (Botón .btn--add)
    // Usamos delegación para capturar el ID del cultivo asignado en data-id
    container.addEventListener('click', (e) => {
        const btnAdd = e.target.closest('.btn--add');
        
        if (btnAdd) {
            const idCultivo = btnAdd.getAttribute('data-id');
            // Aquí rediriges a tu módulo de registro de labores
            window.location.href = `agregar_labor.jsp?idCultivo=${idCultivo}`;
        }
    });

    // 3. Efecto visual suave (Opcional: puedes añadir más lógica aquí)
    console.log("Sistema de gestión de cultivos cargado correctamente.");
});

// Abrir modal para NUEVO registro
function abrirModalNuevo() {
    document.getElementById('editId').value = ""; // ID vacío identifica que es nuevo
    document.getElementById('editNombre').value = "";
    document.getElementById('editTipo').value = "";
    document.getElementById('editSiembra').value = "";
    document.getElementById('editCosecha').value = "";
    document.getElementById('modalEditar').classList.add('is-active');
}

// Abrir modal para EDITAR
function abrirModalEditar(id, nombre, tipo, siembra, cosecha) {
    document.getElementById('editId').value = id;
    document.getElementById('editNombre').value = nombre;
    document.getElementById('editTipo').value = tipo;
    document.getElementById('editSiembra').value = siembra;
    document.getElementById('editCosecha').value = cosecha;
    document.getElementById('modalEditar').classList.add('is-active');
}


const modalConfirm = document.getElementById('modalConfirmacion');

function abrirModalEliminar(url) {
    document.getElementById('btnConfirmarEliminar').href = url;
    modalConfirm.style.display = 'flex';
}

function cerrarModal() {
    modalConfirm.style.display = 'none';
}

// Modifica el evento del botón eliminar en tu js actual:
document.querySelectorAll('.btn--delete').forEach(btn => {
    btn.addEventListener('click', (e) => {
        e.preventDefault(); // Evita que borre directo
        abrirModalEliminar(btn.getAttribute('href'));
    });
});

function abrirModalEditar(id, nombre, tipo, siembra, cosecha) {
    document.getElementById('editId').value = id;
    document.getElementById('editNombre').value = nombre;
    document.getElementById('editTipo').value = tipo;
    document.getElementById('editSiembra').value = siembra;
    document.getElementById('editCosecha').value = cosecha;
    document.getElementById('modalEditar').classList.add('is-active');
}

document.getElementById('btnNuevo').addEventListener('click', () => {
    document.getElementById('modal').classList.remove('hidden');
});

function editarTrabajo(id) {
    document.getElementById('modal').classList.remove('hidden');
    document.getElementById('inputId').value = id;
    // Aquí puedes añadir lógica para cargar los datos en los inputs mediante AJAX si lo deseas
}