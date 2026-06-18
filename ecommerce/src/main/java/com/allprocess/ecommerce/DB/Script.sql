-- ==========================================
-- SISTEMA E-COMMERCE
-- ==========================================
CREATE DATABASE Ecommerceallprocess;
use Ecommerceallprocess;
-- ==========================================
-- 1. ROLES Y USUARIOS
-- ==========================================
CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(150)
);

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,

    -- Credenciales (Identificador principal)
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,

    -- Datos Personales
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    dni VARCHAR(20) UNIQUE,
    telefono VARCHAR(20),

    -- Control y Seguridad
    id_rol INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

-- Libreta de direcciones (1 a Muchos)
CREATE TABLE direccion_usuario (
    id_direccion INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    alias VARCHAR(50) NOT NULL, -- Ej: 'Casa', 'Oficina'
    direccion TEXT NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    referencia TEXT,
    CONSTRAINT fk_direccion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE
);

-- ==========================================
-- 2. CATEGORÍAS Y PROVEEDORES (Catálogos)
-- ==========================================
CREATE TABLE categoria_producto (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE proveedor (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL
);

-- ==========================================
-- 3. CATÁLOGO CENTRAL Y STOCK
-- ==========================================
CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    descripcion TEXT,
    imagen_url VARCHAR(255), -- Parche visual para el front
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    id_categoria INT NOT NULL,
    id_proveedor INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria_producto(id_categoria),
    CONSTRAINT fk_producto_proveedor FOREIGN KEY (id_proveedor) REFERENCES proveedor(id_proveedor)
);

-- Índice para optimizar drásticamente la barra de búsqueda
CREATE INDEX idx_producto_nombre ON producto(nombre);

-- ==========================================
-- 4. TRANSACCIONES (VENTAS)
-- ==========================================
CREATE TABLE venta (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,

    -- Fotografía inmutable de la dirección al momento de la compra
    direccion_envio TEXT NOT NULL,
    ciudad_envio VARCHAR(100) NOT NULL,
    referencia_envio TEXT,

    costo_envio DECIMAL(10,2) NOT NULL DEFAULT 0.00, -- Control financiero
    total DECIMAL(10,2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente) REFERENCES usuario(id_usuario)
);

CREATE TABLE venta_detalle (
    id_detalle_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL, -- Fotografía inmutable del precio
    subtotal DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
);

-- ==========================================
-- 5. PAGOS (Control Financiero Estricto)
-- ==========================================
CREATE TABLE pago (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    -- Restricción fuerte por ENUM
    estado ENUM('PENDIENTE', 'APROBADO', 'RECHAZADO', 'REEMBOLSADO') NOT NULL DEFAULT 'PENDIENTE',
    transaccion_id VARCHAR(255) UNIQUE,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- SIN CASCADE: Protege el historial contable de borrados accidentales
    CONSTRAINT fk_pago_venta FOREIGN KEY (id_venta) REFERENCES venta(id_venta)
);

-- ==========================================
-- RELLENADO DE TABLAS:
-- ==========================================

-- ==========================================
-- 1. ROLES Y USUARIOS
-- ==========================================

-- Insertar Roles (Ahora son 3)
INSERT INTO rol (id_rol, nombre, descripcion) VALUES
(1, 'ADMINISTRADOR', 'Acceso total al sistema, configuración y finanzas'),
(2, 'VENDEDOR', 'Gestión de catálogo, atención de pedidos y stock'),
(3, 'CLIENTE', 'Usuario comprador de la tienda B2B/B2C');

-- Insertar Usuarios (Se asignan los nuevos roles)
INSERT INTO usuario (id_usuario, email, password_hash, nombres, apellidos, dni, telefono, id_rol, activo) VALUES
-- Administradores (Rol 1)
(1, 'admin@allprocess.com', 'admin12345', 'Carlos', 'Mendoza', '12345678', '987654321', 1, TRUE),
-- Vendedores (Rol 2)
(2, 'ventas1@allprocess.com', 'venta12345', 'Sofía', 'Ramírez', '87654321', '911222333', 2, TRUE),
(3, 'ventas2@allprocess.com', 'venta12345', 'Miguel', 'Torres', '44556677', '988777666', 2, TRUE),
-- Clientes (Rol 3)
(4, 'cliente1@gmail.com', 'cliente12345', 'Laura', 'Gómez', '11223344', '912345678', 3, TRUE),
(5, 'cliente2@gmail.pe', 'cliente12345', 'Roberto', 'Sánchez', '22334455', '998877665', 3, TRUE),
(6, 'mantenimiento@textilinka.com', '12345', 'Juan', 'Pérez', '33445566', '955444333', 3, TRUE),
(7, 'logistica@agroexport.pe', '12345', 'María', 'Fernández', '99887766', '966555444', 3, TRUE);

-- Insertar Direcciones de Clientes
INSERT INTO direccion_usuario (id_usuario, alias, direccion, ciudad, referencia) VALUES
(4, 'Planta Principal', 'Av. Los Faisanes 123, Zona Industrial', 'Lima', 'Frente a la fábrica de plásticos'),
(4, 'Oficina Administrativa', 'Calle Las Begonias 456, Piso 4', 'Lima', 'Edificio Empresarial'),
(5, 'Taller', 'Av. Argentina 890', 'Callao', 'Portón azul metalizado'),
(6, 'Sede Huaral', 'Carretera al Norte Km 80', 'Huaral', 'Al lado del grifo'),
(7, 'Almacén 3', 'Av. Evitamiento 1500', 'Lima', 'Parque logístico');

-- ==========================================
-- 2. CATEGORÍAS Y PROVEEDORES
-- ==========================================

-- Insertar Categorías Industriales (Ampliadas)
INSERT INTO categoria_producto (id_categoria, nombre) VALUES
(1, 'Motores Eléctricos'),
(2, 'Neumática e Hidráulica'),
(3, 'Sensores y Automatización'),
(4, 'Transmisión de Potencia'),
(5, 'Válvulas Industriales'),
(6, 'Equipos de Medición'),
(7, 'Herramientas de Corte');

-- Insertar Proveedores (Ampliados)
INSERT INTO proveedor (id_proveedor, nombre) VALUES
(1, 'Siemens'),
(2, 'Festo'),
(3, 'Schneider Electric'),
(4, 'SKF'),
(5, 'SMC Corporation'),
(6, 'Fluke'),
(7, 'Sandvik Coromant');

-- ==========================================
-- 3. CATÁLOGO CENTRAL Y STOCK
-- ==========================================

-- Insertar Productos (Se agregan más productos cruzando con las nuevas categorías)
INSERT INTO producto (id_producto, nombre, descripcion, imagen_url, precio, stock, id_categoria, id_proveedor, activo) VALUES
(1, 'Motor Trifásico 5HP', 'Motor asíncrono de inducción 380V, 4 polos.', '/img/p1.jpg', 550.00, 15, 1, 1, TRUE),
(2, 'Cilindro Neumático ISO 15552', 'Cilindro de doble efecto, diámetro 63mm.', '/img/p2.jpg', 85.50, 40, 2, 2, TRUE),
(3, 'Sensor Inductivo M12', 'Sensor de proximidad inductivo PNP NO.', '/img/p3.jpg', 45.00, 120, 3, 3, TRUE),
(4, 'Rodamiento de Bolas 6204', 'Rodamiento rígido de bolas, sellado ZZ.', '/img/p4.jpg', 12.50, 500, 4, 4, TRUE),
(5, 'Electroválvula 5/2 vías', 'Válvula direccional, conexión G1/4, 24V DC.', '/img/p5.jpg', 65.00, 80, 5, 5, TRUE),
(6, 'Multímetro Industrial True-RMS', 'Multímetro digital de alta precisión para campo.', '/img/p6.jpg', 320.00, 25, 6, 6, TRUE),
(7, 'Fresa de Carburo Sólido 10mm', 'Fresa de 4 filos para mecanizado de acero.', '/img/p7.jpg', 55.00, 150, 7, 7, TRUE),
(8, 'Variador de Frecuencia 10HP', 'Inversor de voltaje para control de motores 380V.', '/img/p8.jpg', 850.00, 8, 1, 1, TRUE);

-- ==========================================
-- 4. TRANSACCIONES (VENTAS)
-- ==========================================

-- Insertar Ventas (El id_cliente ahora va del 4 al 7)
INSERT INTO venta (id_venta, id_cliente, direccion_envio, ciudad_envio, referencia_envio, costo_envio, total, estado) VALUES
(1, 4, 'Av. Los Faisanes 123, Zona Industrial', 'Lima', 'Frente a la fábrica', 50.00, 690.00, 'EN_PROCESO'),
(2, 5, 'Av. Argentina 890', 'Callao', 'Portón azul', 15.00, 140.00, 'PENDIENTE'),
(3, 6, 'Carretera al Norte Km 80', 'Huaral', 'Al lado del grifo', 35.00, 675.00, 'ENTREGADA'),
(4, 7, 'Av. Evitamiento 1500', 'Lima', 'Parque logístico', 0.00, 850.00, 'ENVIADA');

-- Insertar Detalles de Venta (Asegurando consistencia matemática)
-- Venta 1 (Cliente 4): 1 Motor ($550) + 2 Sensores ($90) + Envío ($50) = $690
INSERT INTO venta_detalle (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 1, 550.00, 550.00),
(1, 3, 2, 45.00, 90.00);

-- Venta 2 (Cliente 5): 10 Rodamientos ($125) + Envío ($15) = $140
INSERT INTO venta_detalle (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES
(2, 4, 10, 12.50, 125.00);

-- Venta 3 (Cliente 6): 2 Multímetros ($640) + Envío ($35) = $675
INSERT INTO venta_detalle (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES
(3, 6, 2, 320.00, 640.00);

-- Venta 4 (Cliente 7): 1 Variador ($850) + Envío ($0) = $850
INSERT INTO venta_detalle (id_venta, id_producto, cantidad, precio_unitario, subtotal) VALUES
(4, 8, 1, 850.00, 850.00);

-- ==========================================
-- 5. PAGOS
-- ==========================================

-- Insertar Pagos (valores alineados con MetodoPagoEnum: TARJETA, YAPE, TRANSFERENCIA, MERCADO_PAGO)
INSERT INTO pago (id_venta, metodo_pago, monto, estado, transaccion_id) VALUES
(1, 'TARJETA',       690.00, 'APROBADO',  'TRX-9876543210-VISA'),
(2, 'TRANSFERENCIA', 140.00, 'PENDIENTE', NULL),
(3, 'TRANSFERENCIA', 675.00, 'APROBADO',  'PAY-ABC123XYZ'),
(4, 'TRANSFERENCIA', 850.00, 'APROBADO',  'OP-BANCARIA-009988');