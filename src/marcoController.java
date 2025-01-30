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

    @FXML
    private Label mediaTtleLabel;

    private MediaPlayer mediaPlayer;

    @FXML
    private void initialize() {
        // Asegúrate de que el MediaPlayer actualice la etiqueta de duración
        if (mediaPlayer != null) {
            configureMediaPlayer();
        }
        mediaTtleLabel.setText("");
        lblDuration.setText("00:00 / 00:00");
    }

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
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // Detiene la reproducción y resetea el estado del MediaPlayer
            lblDuration.setText("00:00 / 00:00"); // Reinicia el texto de la etiqueta
            btnPlay.setText("Play"); // Asegúrate de que el botón vuelva a mostrar "Play"
        }
    }

    @FXML
    void selectMedia(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo de media");

        File mediaFolder = new File("media");
        if (mediaFolder.exists()) {
            fileChooser.setInitialDirectory(mediaFolder);
        }

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Archivos de media (*.mp4, *.mp3)",
                "*.mp4", "*.mp3");
        fileChooser.getExtensionFilters().add(extFilter);

        Stage stage = (Stage) mediaView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            Media media = new Media(selectedFile.toURI().toString());
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }

            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            // Configurar MediaPlayer
            configureMediaPlayer();

            // Ajustar el tamaño del MediaView según el BorderPane
            mediaView.fitWidthProperty().bind(mediaView.getScene().widthProperty());
            mediaView.fitHeightProperty().bind(mediaView.getScene().heightProperty());

            mediaPlayer.setOnPlaying(() -> btnPlay.setText("Pause"));

            mediaPlayer.setAutoPlay(true);

            String fileName = selectedFile.getName();
            String title = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
            mediaTtleLabel.setText(title);
        }
    }

    private void configureMediaPlayer() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            // Actualiza la etiqueta lblDuration con el tiempo actual y la duración total
            updateDurationLabel();
        });

        mediaPlayer.setOnReady(() -> {
            // Inicializa la etiqueta cuando el archivo esté listo
            updateDurationLabel();
        });
    }

    private void updateDurationLabel() {
        if (mediaPlayer != null) {
            String currentTime = formatTime(mediaPlayer.getCurrentTime().toSeconds());
            String totalTime = formatTime(mediaPlayer.getTotalDuration().toSeconds());
            lblDuration.setText(currentTime + " / " + totalTime);
        }
    }

    private String formatTime(double seconds) {
        int hours = (int) seconds / 3600;
        int minutes = (int) (seconds % 3600) / 60;
        int secs = (int) seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        } else {
            return String.format("%02d:%02d", minutes, secs);
        }
    }
}
