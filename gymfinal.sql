-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Tiempo de generación: 26-08-2026 a las 18:17:35
-- Versión del servidor: 10.4.32-MariaDB
-- Versión de PHP: 8.0.30

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `gymfinal`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cliente`
--

CREATE TABLE `cliente` (
  `idCliente` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `telefono` varchar(15) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `idUsuario` int(11) NOT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `cliente`
--

INSERT INTO `cliente` (`idCliente`, `nombre`, `telefono`, `correo`, `idUsuario`, `fecha_registro`) VALUES
(1, 'Sara Maria Juárez Cruz', '2491225443', 'Sariku@gmail.com', 4, '2026-07-14 04:06:00'),
(2, 'Jeshua Garcia Fragoso', '2491229288', 'jeusugf8@gmail.com', 4, '2026-07-14 04:06:00'),
(8, 'Javier Notario Davila', '2491223454', 'notariodav43@gmail.com', 4, '2026-07-14 20:26:10'),
(10, 'Manuel Flores Pérez', '2492346554', 'manuelonzy@gmail.com', 4, '2026-07-22 01:14:35'),
(13, 'Andrea Lezama Orea', '2492345678', 'Andylezama@gmail.com', 4, '2026-08-04 00:08:04'),
(14, 'Esmeralda Rodriguez Villafan', '2495676554', 'esme@gmail.com', 4, '2026-08-05 02:53:11'),
(16, 'Jaime Romero Torres', '2491225443', 'romerojaime34@gmail.com', 4, '2026-08-05 19:01:27'),
(17, 'Beatriz Pérez Cruz', '2493452343', 'Betycruz@gmail.com', 4, '2026-08-06 03:42:45'),
(18, 'Alejandro González Flores', '2496545543', 'Aleflores2007@gmail.com', 4, '2026-08-06 16:01:18'),
(19, 'Diego Aquino Juárez', '2491180403', 'diegopi@gmail.com', 4, '2026-08-09 00:51:32'),
(20, 'Gael Rosas Flores', '2491680932', 'gaelzito@gmail.com', 4, '2026-08-12 02:28:53'),
(21, 'Ingrid Sanchez Rodrigez', '2213432343', 'sanchezingrid24@gmail.com', 4, '2026-08-12 02:30:50'),
(22, 'Amairani Lira Cruz', '2491340549', 'amy10@gmail.com', 4, '2026-08-12 02:32:28'),
(23, 'Marivel Andrade Martinez', '2215434333', 'Andramari@gmail.com', 4, '2026-08-12 03:21:26');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `pago`
--

CREATE TABLE `pago` (
  `idPago` int(11) NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `fechaPago` date NOT NULL,
  `fechaVencimiento` date NOT NULL,
  `idCliente` int(11) NOT NULL,
  `idUsuario` int(11) NOT NULL,
  `fechaRegistro` datetime DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `pago`
--

INSERT INTO `pago` (`idPago`, `monto`, `fechaPago`, `fechaVencimiento`, `idCliente`, `idUsuario`, `fechaRegistro`) VALUES
(10, 300.00, '2026-04-01', '2026-05-01', 10, 4, '2026-08-03 12:15:15'),
(11, 300.00, '2026-06-09', '2026-07-09', 1, 4, '2026-08-03 19:44:09'),
(12, 300.00, '2026-08-05', '2026-09-02', 14, 4, '2026-08-05 12:27:33'),
(13, 1200.00, '2026-08-06', '2026-12-06', 18, 4, '2026-08-06 10:03:06'),
(14, 300.00, '2026-08-05', '2026-09-05', 13, 4, '2026-08-08 18:48:55'),
(15, 300.00, '2026-08-08', '2026-09-08', 19, 4, '2026-08-08 18:53:16'),
(16, 300.00, '2026-08-11', '2026-09-11', 8, 4, '2026-08-11 21:22:22'),
(17, 700.00, '2026-08-05', '2026-10-05', 2, 4, '2026-08-11 21:23:26'),
(18, 600.00, '2026-06-03', '2026-08-03', 16, 4, '2026-08-11 21:27:11'),
(19, 300.00, '2026-06-09', '2026-07-09', 17, 4, '2026-08-11 21:27:51'),
(20, 900.00, '2026-05-20', '2026-08-20', 20, 4, '2026-08-11 21:29:08'),
(21, 300.00, '2026-08-11', '2026-09-11', 21, 4, '2026-08-11 21:29:39'),
(22, 600.00, '2026-08-02', '2026-10-02', 22, 4, '2026-08-11 21:30:33');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `producto`
--

CREATE TABLE `producto` (
  `idProducto` int(11) NOT NULL,
  `nombreProducto` varchar(100) NOT NULL,
  `descripcion` varchar(255) DEFAULT NULL,
  `stock` int(11) NOT NULL,
  `idUsuario` int(11) NOT NULL,
  `precio_compra` decimal(10,2) NOT NULL,
  `precio_venta` decimal(10,2) NOT NULL,
  `fecha_registro` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `producto`
--

INSERT INTO `producto` (`idProducto`, `nombreProducto`, `descripcion`, `stock`, `idUsuario`, `precio_compra`, `precio_venta`, `fecha_registro`) VALUES
(3, 'Creatina Monohidratada', 'Creatina pura sin sabor, 300g', 13, 4, 350.00, 550.00, '2026-08-09 01:10:14'),
(5, 'Gold Standard Whey', 'Proteína en polvo sabor chocolate, 900g', 10, 4, 650.00, 950.00, '2026-08-09 01:10:14'),
(6, 'Gold Standard Whey', 'Proteína en polvo sabor vainilla, 900g', 10, 4, 650.00, 950.00, '2026-08-09 01:10:14'),
(7, 'Agua Ciel', 'Agua purificada, 600ml', 50, 4, 8.00, 15.00, '2026-08-09 01:10:14'),
(8, 'Agua Bonafont', 'Agua natural embotellada, 1L', 29, 4, 10.00, 18.00, '2026-08-09 01:10:14'),
(9, 'Electrolit Fresa Kiwi', 'Suero rehidratante sabor fresa kiwi, 625ml', 29, 4, 18.00, 30.00, '2026-08-09 01:10:14'),
(10, 'Electrolit Coco', 'Suero rehidratante sabor coco, 625ml', 34, 4, 18.00, 30.00, '2026-08-09 01:10:14'),
(11, 'Gatorade Mora Azul', 'Bebida deportiva sabor mora azul, 600ml', 20, 4, 16.00, 28.00, '2026-08-09 01:10:14'),
(12, 'Powerade Mora', 'Bebida rehidratante sabor mora, 600ml', 20, 4, 15.00, 26.00, '2026-08-09 01:10:14'),
(13, 'Monster Energy', 'Bebida energética, 473ml', 21, 4, 22.00, 38.00, '2026-08-09 01:15:44');

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `usuario`
--

CREATE TABLE `usuario` (
  `idUsuario` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `correo` varchar(100) NOT NULL,
  `contrasenia` varchar(255) NOT NULL,
  `rol` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `usuario`
--

INSERT INTO `usuario` (`idUsuario`, `username`, `correo`, `contrasenia`, `rol`) VALUES
(4, 'Magnum', 'magnum@gmail.com', '1221', 'Administrador');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `cliente`
--
ALTER TABLE `cliente`
  ADD PRIMARY KEY (`idCliente`),
  ADD KEY `FK_cliente_usuario` (`idUsuario`);

--
-- Indices de la tabla `pago`
--
ALTER TABLE `pago`
  ADD PRIMARY KEY (`idPago`),
  ADD KEY `FK_pago_cliente` (`idCliente`),
  ADD KEY `FK_pago_usuario` (`idUsuario`);

--
-- Indices de la tabla `producto`
--
ALTER TABLE `producto`
  ADD PRIMARY KEY (`idProducto`),
  ADD KEY `FK_producto_usuario` (`idUsuario`);

--
-- Indices de la tabla `usuario`
--
ALTER TABLE `usuario`
  ADD PRIMARY KEY (`idUsuario`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `cliente`
--
ALTER TABLE `cliente`
  MODIFY `idCliente` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT de la tabla `pago`
--
ALTER TABLE `pago`
  MODIFY `idPago` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=23;

--
-- AUTO_INCREMENT de la tabla `producto`
--
ALTER TABLE `producto`
  MODIFY `idProducto` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT de la tabla `usuario`
--
ALTER TABLE `usuario`
  MODIFY `idUsuario` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `cliente`
--
ALTER TABLE `cliente`
  ADD CONSTRAINT `FK_cliente_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuario` (`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `pago`
--
ALTER TABLE `pago`
  ADD CONSTRAINT `FK_pago_cliente` FOREIGN KEY (`idCliente`) REFERENCES `cliente` (`idCliente`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `FK_pago_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuario` (`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Filtros para la tabla `producto`
--
ALTER TABLE `producto`
  ADD CONSTRAINT `FK_producto_usuario` FOREIGN KEY (`idUsuario`) REFERENCES `usuario` (`idUsuario`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
