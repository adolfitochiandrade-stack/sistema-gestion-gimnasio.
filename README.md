# Sistema de Gestión para Gimnasio 🏋️‍♂️

Aplicación de escritorio desarrollada en JavaFX para la administración integral de clientes, pagos de membresías, control de inventario y generación de reportes.

## 🛠️ Tecnologías Utilizadas

* **Lenguaje:** Java 17+
* **Interfaz Gráfica:** JavaFX (FXML + CSS)
* **Gestor de Dependencias:** Maven
* **Base de Datos:** MySQL / MariaDB
* **Exportación de Documentos:** iText PDF / Apache POI (Excel)

## 🚀 Funcionalidades Principales

* **Gestión de Clientes:** Alta, modificación y seguimiento del estado de membresías.
* **Control de Pagos:** Registro de cuotas mensuales e historial financiero.
* **Inventario de Productos:** Administración y control de existencias de suplementos y accesorios.
* **Reportes:** Generación de resúmenes de ventas e ingresos en formatos PDF y Excel.

## 📋 Requisitos e Instalación

1. **Base de Datos:**
   * Importa el archivo `gymfinal.sql` adjunto en tu gestor de base de datos (MySQL/MariaDB via XAMPP).
   * Asegúrate de configurar la conexión en `src/main/java/Modelo/Conexion_DB.java`.

2. **Ejecución del Proyecto:**
   * Clona el repositorio: `git clone <URL_DEL_REPOSITORIO>`
   * Ejecuta el proyecto mediante Maven: `./mvnw clean javafx:run`
