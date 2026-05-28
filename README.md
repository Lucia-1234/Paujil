# Paujil

Requisitos Funcionales
RF01 — Gestión de usuarios
•	El sistema debe permitir el registro de nuevos usuarios con nombre, correo, teléfono, fecha de nacimiento, dirección, contraseña y rol solicitado.
•	El sistema debe validar que el correo y teléfono no estén duplicados antes de registrar.
•	El sistema debe permitir asociar múltiples correos a un usuario con tipo (personal, laboral, alternativo).
•	El sistema debe permitir asociar múltiples teléfonos a un usuario.
•	El sistema debe guardar las contraseñas cifradas con BCrypt, nunca en texto plano.
•	Todo usuario recién registrado queda en estado Pendiente hasta que el administrador lo apruebe.
•	El administrador puede aceptar (cambiar a Activo) o denegar (eliminar) usuarios pendientes.
•	El sistema debe permitir asignar múltiples roles a un mismo usuario.
RF02 — Autenticación y sesión
•	El sistema debe permitir iniciar sesión con correo, contraseña y rol.
•	El sistema debe verificar la contraseña contra el hash BCrypt almacenado.
•	El sistema debe redirigir al panel correspondiente según el rol: administrador o trabajador.
•	El sistema debe invalidar la sesión anterior al iniciar una nueva.
•	El sistema debe cerrar sesión y redirigir al login cuando la sesión expire.
•	El sistema debe bloquear el acceso a rutas protegidas si no hay sesión activa.
RF03 — Gestión de cultivos
•	El sistema debe permitir registrar cultivos con nombre, tipo, fecha de siembra y fecha de cosecha.
•	El sistema debe listar todos los cultivos registrados.
•	El sistema debe permitir editar los datos de un cultivo existente.
•	El sistema debe permitir eliminar un cultivo.
•	El sistema debe permitir asociar un cultivo a uno o varios lotes de la finca.
•	El sistema debe permitir asociar trabajadores a cultivos específicos.
•	El sistema debe permitir asociar insumos a cultivos.
•	El sistema debe permitir asociar biopreparados a cultivos.
RF04 — Registros de trabajo por cultivo
•	El sistema debe permitir registrar trabajos realizados en un cultivo con descripción, fecha de inicio, fecha de finalización y observaciones.
•	El responsable del registro debe tomarse automáticamente de la sesión activa.
•	El sistema debe mostrar el historial de trabajos realizados por cultivo ordenado del más reciente al más antiguo.
•	El sistema debe validar que la fecha de inicio no sea posterior a la fecha de finalización.
RF05 — Gestión de trabajos asignados
•	El administrador debe poder asignar trabajos a usuarios activos asociándolos a un cultivo.
•	El sistema debe registrar el trabajo de forma atómica en trabajos, trabajos_cultivo y asignar_trabajos.
•	El sistema debe listar todos los trabajos con el nombre del cultivo y el trabajador asignado.
•	El administrador debe poder eliminar un trabajo asignado.
•	El trabajador debe poder ver únicamente los trabajos que le han sido asignados.
RF06 — Gestión de biopreparados
•	El sistema debe permitir registrar biopreparados con nombre, funcionalidad, precio, fecha de creación y fecha de vencimiento.
•	El sistema debe permitir registrar los ingredientes de cada biopreparado con nombre, cantidad y unidad.
•	El sistema debe listar todos los biopreparados disponibles.
•	El sistema debe permitir editar y eliminar biopreparados.
•	El sistema debe alertar cuando un biopreparado esté próximo a vencer.
RF07 — Control de acceso por roles
•	El sistema debe restringir el acceso a vistas y servlets del administrador a usuarios con rol administrador.
•	El sistema debe restringir el acceso al panel del trabajador a usuarios con rol trabajador.
•	El sistema debe redirigir al login con mensaje de error si un usuario no autorizado intenta acceder a rutas protegidas.
•	El sistema debe impedir que el navegador almacene en caché páginas protegidas.
________________________________________
Requisitos No Funcionales
RNF01 — Seguridad
•	Las contraseñas deben almacenarse con hash BCrypt con factor de coste 12.
•	Ningún dato sensible debe viajar en parámetros GET ni en campos de formulario manipulables.
•	El rol del usuario debe determinarse en el servidor desde la sesión, nunca desde el cliente.
•	Todas las queries deben usar PreparedStatement para prevenir inyección SQL.
•	Las páginas protegidas deben incluir cabeceras Cache-Control: no-cache, no-store para evitar acceso por botón atrás tras cerrar sesión.
•	El sistema debe validar todos los campos en el servidor independientemente de la validación del cliente.
RNF02 — Integridad de datos
•	Las operaciones que involucren múltiples tablas deben ejecutarse dentro de una transacción con setAutoCommit(false), commit() y rollback().
•	Las claves foráneas deben estar definidas con ON DELETE CASCADE donde corresponda.
•	Los campos opcionales deben manejarse con setNull() en los DAOs.
•	No deben existir correos ni teléfonos duplicados en el sistema.
RNF03 — Arquitectura
•	El proyecto debe seguir el patrón MVC: modelos en com.paujil.modelo, DAOs en com.paujil.dao, servlets en com.paujil.controlador, utilidades en com.paujil.utils.
•	Los JSPs no deben contener lógica de acceso a base de datos — los datos deben venir de los servlets como atributos de request.
•	La conexión a BD debe cerrarse siempre en el bloque finally o mediante try-with-resources.
•	Las credenciales de conexión a BD no deben estar hardcodeadas en el código fuente.
RNF04 — Usabilidad
•	El sistema debe mostrar mensajes claros ante errores de validación, duplicados o fallos de BD.
•	Los formularios deben conservar el contexto al reenviar con error.
•	Las fechas deben ingresarse mediante <input type="date"> para garantizar el formato yyyy-MM-dd.
•	El panel del administrador y el del trabajador deben tener interfaces diferenciadas y adaptadas a sus funciones.
•	El sistema debe ser responsive para uso desde dispositivos móviles en campo.
RNF05 — Rendimiento
•	Las consultas que listen registros deben usar columnas explícitas en el SELECT, evitando SELECT *.
•	Las consultas frecuentes como el login y el listado de cultivos deben ejecutarse en menos de 2 segundos bajo condiciones normales.
•	La conexión a BD debe gestionarse mediante un pool de conexiones para evitar cuellos de botella.
RNF06 — Mantenibilidad
•	Los nombres de columnas en los DAOs deben coincidir exactamente con el esquema SQL en snake_case.
•	Los campos usados solo para vistas deben estar claramente separados en los modelos y no persistirse en BD.
•	El código debe estar comentado en los puntos de lógica compleja como transacciones y filtros de seguridad.
•	Las convenciones de nombres deben ser consistentes en todos los paquetes del proyecto.
RNF07 — Disponibilidad y compatibilidad
•	El sistema debe ejecutarse sobre Jakarta EE compatible con Tomcat 10+.
•	El driver JDBC debe ser com.mysql.cj.jdbc.Driver (MySQL Connector/J 8+).
•	La aplicación debe funcionar correctamente en Chrome, Firefox y Edge en sus versiones actuales.
•	El sistema debe soportar al menos 10 usuarios concurrentes sin degradación notable del rendimiento.


