import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carga el archivo FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("marco.fxml"));
        Parent root = loader.load();

        // Configura la escena y el escenario
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Youtube.com");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Inicia la aplicación
    }
}

