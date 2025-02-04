import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args); // Llama al método launch() de Application
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Carga el archivo FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("marco.fxml"));
        Parent root = loader.load();

        // Configura la escena y el escenario
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Youtube.com");

        // Mostrar la ventana
        primaryStage.show();
    }
}