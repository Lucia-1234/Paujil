DROP DATABASE IF EXISTS finca_pajuil;
CREATE DATABASE finca_pajuil;
USE finca_pajuil;

-- 1. Tabla Rol
CREATE TABLE roles (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre_rol VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Tabla de usuarios
CREATE TABLE usuarios (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(120) NOT NULL,
    fecha_nacimiento DATE, -- Más profesional que guardar la edad estática
    direccion_usuario VARCHAR(125),
    contrasena_usuario VARCHAR(255) NOT NULL,
    estado_usuario VARCHAR(20) DEFAULT 'Activo'
);

-- 3. Tabla usuario rol (Muchos a Muchos)
CREATE TABLE usuario_rol (
    id_usuario_rol INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_rol INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE,
    FOREIGN KEY (id_rol) REFERENCES roles(id_rol) ON DELETE CASCADE
);

-- 4. Tabla de correos 
CREATE TABLE correos (
    id_correo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    direccion_correo VARCHAR(120) NOT NULL UNIQUE,
    tipo_correo ENUM('personal', 'laboral', 'alternativo'),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

-- 5. Tabla de Telefonos 
CREATE TABLE telefonos (
    id_telefono INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    numero_telefono CHAR(10),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

-- 6. Tabla de trabajos 
CREATE TABLE trabajos (
    id_trabajo INT AUTO_INCREMENT PRIMARY KEY,
    descripcion_trabajo TEXT NOT NULL,
    fecha_asignacion DATE NOT NULL,
    fecha_finalizacion DATE,
    observaciones_trabajo TEXT,
    estado_trabajo ENUM('Pendiente', 'En curso', 'Finalizado') DEFAULT 'Pendiente'
);

-- 7. Tabla Asignar trabajos (Asociamos al usuario directamente)
CREATE TABLE asignar_trabajos (
    id_asignar INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_trabajo INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_trabajo) REFERENCES trabajos(id_trabajo) ON DELETE CASCADE
);

-- 8. Tabla de Cultivos
CREATE TABLE cultivos (
    id_cultivo INT AUTO_INCREMENT PRIMARY KEY,
    nombre_cultivo VARCHAR(120) NOT NULL,
    tipo_cultivo VARCHAR(100),
    fecha_siembra DATE NOT NULL,
    fecha_cosecha DATE,
    area DECIMAL(10,2)
);

-- 9. Tabla de lote
CREATE TABLE lotes (
    id_lote INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    coordenadas TEXT, -- Formato GeoJSON o lat/long
    area DECIMAL(10,2)
);

-- 10. Tabla de Cultivo lote
CREATE TABLE cultivo_lote (
    id_cultivo_lote INT AUTO_INCREMENT PRIMARY KEY,
    id_cultivo INT NOT NULL,
    id_lote INT NOT NULL,
    FOREIGN KEY (id_cultivo) REFERENCES cultivos(id_cultivo),
    FOREIGN KEY (id_lote) REFERENCES lotes(id_lote)
);

-- 11. Tabla intermedia Cultivo-Trabajador (Corregido: apunta a usuarios)
CREATE TABLE cultivo_trabajador (
    id_cultivo_trabajador INT AUTO_INCREMENT PRIMARY KEY,
    id_cultivo INT NOT NULL,
    id_usuario INT NOT NULL,
    FOREIGN KEY (id_cultivo) REFERENCES cultivos(id_cultivo),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
);

-- 12. Tabla trabajos realizados (Corregido: apunta a usuarios)
CREATE TABLE trabajos_realizados (
    id_trabajo_realizado INT AUTO_INCREMENT PRIMARY KEY,
    id_cultivo INT NOT NULL,
    descripcion_trabajo TEXT,
    id_usuario INT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_finalizo DATE NOT NULL,
    observaciones TEXT,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_cultivo) REFERENCES cultivos(id_cultivo)
);
   
-- 13. Tabla de Visitantes
CREATE TABLE visitantes (
    id_visitante INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

-- 14. Tabla de Insumos (Se removieron las columnas cíclicas incorrectas)
CREATE TABLE insumos (
    id_insumo INT AUTO_INCREMENT PRIMARY KEY,
    nombre_insumo VARCHAR(120) NOT NULL,
    tipo_insumo VARCHAR(100),
    cantidad DECIMAL(10,2) NOT NULL,
    unidad VARCHAR(50),
    fecha_compra DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL
);

-- 15. Tabla Cultivos insumo
CREATE TABLE cultivo_insumo (
    id_cultivo_insumo INT AUTO_INCREMENT PRIMARY KEY,
    id_cultivo INT NOT NULL,
    id_insumo INT NOT NULL,
    FOREIGN KEY (id_cultivo) REFERENCES cultivos(id_cultivo),
    FOREIGN KEY (id_insumo) REFERENCES insumos(id_insumo)
);

-- 16. Tabla de recetas biopreparados
CREATE TABLE biopreparados (
    id_biopreparado INT AUTO_INCREMENT PRIMARY KEY,
    nombre_biopreparado VARCHAR(200) NOT NULL,
    funcionalidad_biopreparado TEXT,
    precio_biopreparado DECIMAL(10,2),
    fecha_creacion DATE NOT NULL,
    fecha_vencimiento DATE NOT NULL
);

-- 17. Tabla relaciones insumos biopreparados
CREATE TABLE insumos_biopreparados (
    id_insumo_biopreparado INT AUTO_INCREMENT PRIMARY KEY,
    id_insumo INT NOT NULL,
    id_biopreparado INT NOT NULL,
    FOREIGN KEY (id_insumo) REFERENCES insumos(id_insumo),
    FOREIGN KEY (id_biopreparado) REFERENCES biopreparados(id_biopreparado)
);

-- 18. Tabla ingredientes biopreparados 
CREATE TABLE ingredientes_biopreparados (
    id_ingrediente INT AUTO_INCREMENT PRIMARY KEY,
    nombre_ingrediente VARCHAR(120) NOT NULL,
    cantidad_ingredientes DECIMAL(10,2),
    unidad_ingredientes VARCHAR(50),
    id_biopreparado INT NOT NULL,
    FOREIGN KEY (id_biopreparado) REFERENCES biopreparados(id_biopreparado) ON DELETE CASCADE
);

-- 19. Tabla evento 
CREATE TABLE eventos (
    id_evento INT AUTO_INCREMENT PRIMARY KEY,
    tipo_evento ENUM('Trabajo', 'Visita') NOT NULL,
    descripcion_evento TEXT NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado_evento ENUM('Pendiente', 'En curso', 'Finalizado') DEFAULT 'Pendiente'
);

-- 20. Tabla evento visitas 
CREATE TABLE evento_visitas (
    id_evento_visitas INT AUTO_INCREMENT PRIMARY KEY,
    id_visitante INT NOT NULL,
    id_evento INT NOT NULL,
    FOREIGN KEY (id_visitante) REFERENCES visitantes(id_visitante),
    FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE
);

-- 21. Tabla evento trabajador (Corregido: apunta a usuarios)
CREATE TABLE evento_trabajador (
    id_evento_trabajador INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_evento INT NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario),
    FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE
);

-- 22. Tabla color 
CREATE TABLE colores (
    id_color INT AUTO_INCREMENT PRIMARY KEY,
    color_asignado ENUM('Rojo', 'Verde', 'Azul', 'Amarillo') NOT NULL
);

-- 23. Tabla de Colores evento
CREATE TABLE colores_evento (
    id_color_evento INT AUTO_INCREMENT PRIMARY KEY,
    id_color INT NOT NULL,
    id_evento INT NOT NULL,
    FOREIGN KEY (id_color) REFERENCES colores(id_color),
    FOREIGN KEY (id_evento) REFERENCES eventos(id_evento) ON DELETE CASCADE
);

-- 24. Tabla notificaciones 
CREATE TABLE notificaciones (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    tipo VARCHAR(50), -- trabajo, insumo, biopreparado
    mensaje TEXT NOT NULL,
    fecha DATETIME NOT NULL,
    estado VARCHAR(20) DEFAULT 'No leída',
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario) ON DELETE CASCADE
);

-- 25. Tabla Reseñas
CREATE TABLE resenas (
    id_resena INT AUTO_INCREMENT PRIMARY KEY,
    id_visitante INT NOT NULL,
    calificacion INT CHECK (calificacion BETWEEN 1 AND 5),
    comentario TEXT,
    fecha DATETIME NOT NULL,
    FOREIGN KEY (id_visitante) REFERENCES visitantes(id_visitante) ON DELETE CASCADE
);

-- 26. Tabla ruta 
CREATE TABLE rutas (
    id_ruta INT AUTO_INCREMENT PRIMARY KEY,
    origen VARCHAR(100),
    destino VARCHAR(100),
    distancia DECIMAL(10,2), -- km
    tiempo_estimado INT, -- minutos
    id_lote INT,
    FOREIGN KEY (id_lote) REFERENCES lotes(id_lote) ON DELETE SET NULL
);
