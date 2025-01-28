import java.io.File;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class marcoController {

    @FXML
    private Button btnPlay;

    @FXML
    private Label lblDuration;

    @FXML
    private MediaView mediaView;

    private MediaPlayer mediaPlayer;

    @FXML
    void btnPlay(MouseEvent event) {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();

            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause(); // Pausa la reproducción
                btnPlay.setText("Play"); // Cambia el texto del botón a "Play"
            } else {
                mediaPlayer.play(); // Continúa la reproducción
                btnPlay.setText("Pause"); // Cambia el texto del botón a "Pause"
            }
        }
    }

    @FXML
    void btnStop(MouseEvent event) {

    }

    @FXML
    void selectMedia(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de media");

        // Establecer el directorio inicial a la carpeta "media" dentro del proyecto
        File mediaFolder = new File("media");
        if (mediaFolder.exists()) {
            fileChooser.setInitialDirectory(mediaFolder);
        }

        // Filtrar los archivos para mostrar solo los de tipo media (por ejemplo, .mp4,
        // .mp3)
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de media (*.mp4, *.mp3)",
                "*.mp4", "*.mp3");
        fileChooser.getExtensionFilters().add(extFilter);

        // Mostrar el diálogo de selección de archivos
        Stage stage = (Stage) mediaView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            // Cargar el archivo seleccionado en el MediaPlayer
            Media media = new Media(selectedFile.toURI().toString());
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            Scene scene = mediaView.getScene();
            mediaView.fitWidthProperty().bind(scene.widthProperty());
            mediaView.fitHeightProperty().bind(scene.heightProperty());

            // Opcional: Reproducir automáticamente el archivo seleccionado
            mediaPlayer.setAutoPlay(true);
        }
    }
}
