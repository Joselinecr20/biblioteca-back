-- ============================================
-- GESTOR DE BIBLIOTECA VIRTUAL
-- SCRIPT SQL - VERSIÓN FINAL DEFINITIVA v3.0
-- PROYECTO UNIVERSITARIO - PROGRAMACION II
-- ============================================
-- CAMBIOS RESPECTO A VERSIÓN ANTERIOR:
-- 1. AGREGADO: max_prestamos en tabla rol
--    Para definir límite de préstamos por rol
-- 2. AGREGADO: tabla libro_imagen
--    Para manejar múltiples imágenes por libro
-- ============================================

DROP DATABASE IF EXISTS biblioteca;
CREATE DATABASE biblioteca;
USE biblioteca;

-- ============================================
-- TABLA: rol
-- 3 roles: admin, bibliotecario, estudiante
-- max_prestamos: límite de libros simultáneos
--   estudiante   = 3
--   bibliotecario = 5
--   admin        = 99 (ilimitado)
-- ============================================
CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(200),
    max_prestamos INT DEFAULT 3
);

-- ============================================
-- TABLA: cuenta
-- Credenciales de acceso al sistema
-- El rol determina qué puede hacer cada usuario
-- ============================================
CREATE TABLE cuenta (
    id_cuenta INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    id_rol INT NOT NULL,
    estado ENUM('activo', 'inactivo') DEFAULT 'activo',
    fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso DATETIME,
    FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

-- ============================================
-- TABLA: usuario
-- Datos personales de cada persona del sistema
-- Vinculado 1 a 1 con cuenta (id_cuenta UNIQUE)
-- ============================================
CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(100) UNIQUE,
    telefono VARCHAR(20),
    id_cuenta INT NOT NULL UNIQUE,
    foto_url TEXT, 
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_cuenta) REFERENCES cuenta(id_cuenta)
);

-- ============================================
-- TABLA: biblioteca
-- Sedes de la biblioteca
-- Permite manejar múltiples ubicaciones
-- ============================================
CREATE TABLE biblioteca (
    id_biblioteca INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    ubicacion VARCHAR(255)
);

-- ============================================
-- TABLA: categoria
-- Clasificación de los libros
-- ============================================
CREATE TABLE categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================
-- TABLA: autor
-- Datos del autor del libro
-- Separado para poder buscar libros por autor
-- ============================================
CREATE TABLE autor (
    id_autor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100),
    nacionalidad VARCHAR(100)
);

-- ============================================
-- TABLA: libro
-- Catálogo de libros del sistema
-- portada_url: imagen principal para las cards
--   (referencia rápida sin consultar libro_imagen)
-- El estado NO está aquí, está en libro_biblioteca
-- ============================================
CREATE TABLE libro (
    id_libro INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    editorial VARCHAR(255),
    anio_publicacion YEAR,
    descripcion TEXT,
    portada_url VARCHAR(255),
    isbn VARCHAR(50) UNIQUE,
    id_categoria INT,
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
);

-- ============================================
-- TABLA: libro_imagen
-- Múltiples imágenes por libro
-- es_portada: TRUE solo para la imagen principal
-- orden: para mostrarlas en secuencia
-- Así se pueden agregar fotos del interior,
-- contraportada, etc.
-- ============================================
CREATE TABLE libro_imagen (
    id_imagen INT AUTO_INCREMENT PRIMARY KEY,
    id_libro INT NOT NULL,
    url VARCHAR(255) NOT NULL,
    es_portada BOOLEAN DEFAULT FALSE,
    orden INT DEFAULT 1,
    descripcion VARCHAR(100),
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro),
    INDEX idx_imagen_libro (id_libro)
);

-- ============================================
-- TABLA: libro_autor
-- Un libro puede tener varios autores
-- Un autor puede tener varios libros
-- ============================================
CREATE TABLE libro_autor (
    id_libro INT,
    id_autor INT,
    PRIMARY KEY (id_libro, id_autor),
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro),
    FOREIGN KEY (id_autor) REFERENCES autor(id_autor)
);

-- ============================================
-- TABLA: libro_biblioteca
-- Inventario de libros por sede
-- cantidad_total: copias totales en esa sede
-- cantidad_disponible: copias disponibles ahora
-- El backend descuenta/suma según préstamos
-- ============================================
CREATE TABLE libro_biblioteca (
    id_libro_biblioteca INT AUTO_INCREMENT PRIMARY KEY,
    id_libro INT NOT NULL,
    id_biblioteca INT NOT NULL,
    cantidad_total INT DEFAULT 0,
    cantidad_disponible INT DEFAULT 0,
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro),
    FOREIGN KEY (id_biblioteca) REFERENCES biblioteca(id_biblioteca),
    UNIQUE KEY uq_libro_biblioteca (id_libro, id_biblioteca)
);

-- ============================================
-- TABLA: reserva
-- FLUJO: Estudiante solicita → Bibliotecario decide
-- pendiente  → recién creada por el estudiante
-- aprobada   → bibliotecario la aprobó
-- rechazada  → bibliotecario la rechazó
-- cancelada  → estudiante la canceló
-- ============================================
CREATE TABLE reserva (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_libro INT NOT NULL,
    id_biblioteca INT NOT NULL,
    fecha_reserva DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion DATE NOT NULL,
    estado ENUM('pendiente', 'aprobada', 'rechazada', 'cancelada') DEFAULT 'pendiente',
    atendida_por INT,
    observaciones VARCHAR(255),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro),
    FOREIGN KEY (id_biblioteca) REFERENCES biblioteca(id_biblioteca),
    FOREIGN KEY (atendida_por) REFERENCES cuenta(id_cuenta),
    INDEX idx_reserva_usuario (id_usuario),
    INDEX idx_reserva_estado (estado)
);

-- ============================================
-- TABLA: prestamo
-- Se crea cuando una reserva es aprobada
-- aprobado_por: bibliotecario que autorizó
-- fecha_devolucion: fecha límite para devolver
-- fecha_devolucion_real: cuándo devolvió de verdad
-- activo    → libro en manos del estudiante
-- devuelto  → regresado a tiempo
-- retrasado → pasó la fecha sin devolver
-- ============================================
CREATE TABLE prestamo (
    id_prestamo INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_libro INT NOT NULL,
    id_biblioteca INT NOT NULL,
    id_reserva INT,
    aprobado_por INT NOT NULL,
    fecha_prestamo DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_devolucion DATE NOT NULL,
    fecha_devolucion_real DATE,
    estado ENUM('activo', 'devuelto', 'retrasado') DEFAULT 'activo',
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_libro) REFERENCES libro(id_libro),
    FOREIGN KEY (id_biblioteca) REFERENCES biblioteca(id_biblioteca),
    FOREIGN KEY (id_reserva) REFERENCES reserva(id_reserva),
    FOREIGN KEY (aprobado_por) REFERENCES cuenta(id_cuenta),
    INDEX idx_prestamo_usuario (id_usuario),
    INDEX idx_prestamo_estado (estado)
);

-- ============================================
-- TABLA: multa
-- Se genera automáticamente cuando:
-- fecha_devolucion_real > fecha_devolucion
-- monto = dias_retraso * 0.25
-- pendiente → estudiante aún no ha pagado
-- pagada    → bibliotecario registró el pago
-- ============================================
CREATE TABLE multa (
    id_multa INT AUTO_INCREMENT PRIMARY KEY,
    id_prestamo INT NOT NULL,
    monto DECIMAL(10, 2) NOT NULL,
    dias_retraso INT NOT NULL,
    estado ENUM('pendiente', 'pagada') DEFAULT 'pendiente',
    fecha_generacion DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_pago DATETIME,
    FOREIGN KEY (id_prestamo) REFERENCES prestamo(id_prestamo)
);

-- ============================================
-- DATOS INICIALES: roles con límites
-- ============================================
INSERT INTO rol (nombre, descripcion, max_prestamos) VALUES
('admin',         'acceso total al sistema',                99),
('bibliotecario', 'gestiona libros, reservas y prestamos',  5),
('estudiante',    'puede buscar libros y hacer reservas',   3);

-- ============================================
-- DATOS INICIALES: cuentas de prueba
-- IMPORTANTE: en produccion usar BCrypt
-- ============================================
INSERT INTO cuenta (usuario, password, id_rol) VALUES
('admin',          'admin123', 1),
('bibliotecario1', 'bib123',   2),
('estudiante1',    'est123',   3);

-- ============================================
-- DATOS INICIALES: biblioteca
-- ============================================
INSERT INTO biblioteca (nombre, ubicacion) VALUES
('biblioteca central', 'san salvador, el salvador');

-- ============================================
-- DATOS INICIALES: categorias
-- ============================================
INSERT INTO categoria (nombre) VALUES
('ficcion'),
('clasico'),
('ciencia'),
('historia'),
('infantil'),
('tecnologia');

-- ============================================
-- DATOS INICIALES: autores
-- ============================================
INSERT INTO autor (nombre, apellido, nacionalidad) VALUES
('gabriel', 'garcia marquez', 'colombiana'),
('miguel',  'de cervantes',   'espanola'),
('isabel',  'allende',        'chilena'),
('julio',   'cortazar',       'argentina'),
('juan',    'rulfo',          'mexicana');

-- ============================================
-- DATOS INICIALES: libros
-- ============================================
INSERT INTO libro (titulo, editorial, anio_publicacion, descripcion, isbn, id_categoria) VALUES
('cien anos de soledad',    'editorial sudamericana',    1967, 'obra maestra del realismo magico latinoamericano', '978-0-06-088328-7', 1),
('don quijote de la mancha','francisco de robles',       1605, 'primera novela moderna de la literatura universal', '978-84-376-0494-7', 2),
('la casa de los espiritus','plaza y janes',             1982, 'saga familiar con elementos de realismo magico',   '978-0-15-115801-9', 1),
('rayuela',                 'editorial sudamericana',    1963, 'novela experimental de la literatura latinoamericana','978-84-376-0708-5', 1),
('pedro paramo',            'fondo de cultura economica',1955, 'obra fundamental de la literatura mexicana',       '978-968-16-0271-3', 2);

-- ============================================
-- DATOS INICIALES: imagenes de libros
-- ============================================
INSERT INTO libro_imagen (id_libro, url, es_portada, orden, descripcion) VALUES
(1, 'https://covers.openlibrary.org/b/isbn/9780060883287-L.jpg',  TRUE,  1, 'portada principal'),
(2, 'https://covers.openlibrary.org/b/isbn/9788437604947-L.jpg',  TRUE,  1, 'portada principal'),
(3, 'https://covers.openlibrary.org/b/isbn/9780151158019-L.jpg',  TRUE,  1, 'portada principal'),
(4, 'https://covers.openlibrary.org/b/isbn/9788437607085-L.jpg',  TRUE,  1, 'portada principal'),
(5, 'https://covers.openlibrary.org/b/isbn/9789681602710-L.jpg',  TRUE,  1, 'portada principal');

-- ============================================
-- DATOS INICIALES: relacion libro_autor
-- ============================================
INSERT INTO libro_autor (id_libro, id_autor) VALUES
(1, 1), (2, 2), (3, 3), (4, 4), (5, 5);

-- ============================================
-- DATOS INICIALES: inventario por biblioteca
-- ============================================
INSERT INTO libro_biblioteca (id_libro, id_biblioteca, cantidad_total, cantidad_disponible) VALUES
(1, 1, 3, 3),
(2, 1, 2, 2),
(3, 1, 4, 4),
(4, 1, 2, 2),
(5, 1, 3, 3);

-- ============================================
-- DATOS INICIALES: usuarios de prueba
-- ============================================
INSERT INTO usuario (nombre, apellido, correo, telefono, id_cuenta) VALUES
('admin',   'sistema',   'admin@biblioteca.edu.sv',  '0000-0000', 1),
('carlos',  'martinez',  'carlos@biblioteca.edu.sv', '7000-0001', 2),
('maria',   'gonzalez',  'maria@uees.edu.sv',        '7000-0002', 3);

-- ============================================
-- VERIFICACION: conteo de registros
-- ============================================
SELECT 'rol'              AS tabla, COUNT(*) AS registros FROM rol
UNION ALL SELECT 'cuenta',           COUNT(*) FROM cuenta
UNION ALL SELECT 'usuario',          COUNT(*) FROM usuario
UNION ALL SELECT 'biblioteca',       COUNT(*) FROM biblioteca
UNION ALL SELECT 'categoria',        COUNT(*) FROM categoria
UNION ALL SELECT 'autor',            COUNT(*) FROM autor
UNION ALL SELECT 'libro',            COUNT(*) FROM libro
UNION ALL SELECT 'libro_imagen',     COUNT(*) FROM libro_imagen
UNION ALL SELECT 'libro_autor',      COUNT(*) FROM libro_autor
UNION ALL SELECT 'libro_biblioteca', COUNT(*) FROM libro_biblioteca
UNION ALL SELECT 'reserva',          COUNT(*) FROM reserva
UNION ALL SELECT 'prestamo',         COUNT(*) FROM prestamo
UNION ALL SELECT 'multa',            COUNT(*) FROM multa;

-- ============================================
-- FLUJO PRINCIPAL DEL SISTEMA
-- ============================================
-- PASO 1: Estudiante busca libro disponible
--         cantidad_disponible > 0 en libro_biblioteca
--         Las cards muestran portada_url del libro
--
-- PASO 2: Estudiante crea reserva
--         estado = 'pendiente'
--         Valida que no tenga multas pendientes
--         Valida que no supere max_prestamos del rol
--
-- PASO 3: Bibliotecario revisa reservas pendientes
--         Decide aprobar o rechazar
--
-- PASO 4: Si aprueba:
--         - Se crea préstamo (estado = 'activo')
--         - Se descuenta cantidad_disponible
--         - Reserva pasa a 'aprobada'
--
-- PASO 5: Estudiante devuelve el libro
--         - Se registra fecha_devolucion_real
--         - Se suma cantidad_disponible
--         - Si hay retraso → se genera multa automática
--           monto = dias_retraso * 0.25
--
-- PASO 6: Estudiante paga la multa
--         - Bibliotecario registra el pago
--         - multa.estado pasa a 'pagada'
-- ============================================
