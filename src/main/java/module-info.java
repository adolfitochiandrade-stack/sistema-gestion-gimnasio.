module com.example.gymcentral {
    // 1. Módulos obligatorios para la interfaz gráfica de JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // 2. Módulo esencial para la conexión a la Base de Datos (XAMPP MySQL)
    // ¡Esto quitará los errores rojos de tu clase Conexion_DB!
    requires java.sql;

    // 3. Permisos para que JavaFX pueda iniciar la aplicación desde tu paquete App
    exports App;
    opens App to javafx.graphics, javafx.fxml;

    // 4. Permisos para que JavaFX asocie tus archivos FXML con tus controladores
    exports Controlador;
    opens Controlador to javafx.fxml;

    // 5. Permisos indispensables para que los componentes como TableView
    // puedan leer automáticamente las propiedades de tus clases de entidad (User, Cliente, etc.)
    exports Modelo;
    opens Modelo to javafx.base;

    //Apache POI
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    //OpenPDF
    requires com.github.librepdf.openpdf;

}