import java.io.File;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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

    @FXML
    private ProgressBar pgBar;

    @FXML
    private ListView<String> recentMedia;

    private MediaPlayer mediaPlayer;

    private DoubleProperty progress = new SimpleDoubleProperty(0.0);

    @FXML
    private HBox mediaBox;

    @FXML
    private void initialize() {
        mediaTtleLabel.setText("");
        lblDuration.setText("00:00 / 00:00");

        // Manejar clic en la lista de recientes
        recentMedia.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Doble clic para reproducir
                String selectedFileName = recentMedia.getSelectionModel().getSelectedItem();
                if (selectedFileName != null) {
                    playRecentMedia(selectedFileName);
                }
            }
        });

        pgBar.setOnMouseClicked(event -> {
            if (mediaPlayer != null) {
                // Obtener el clic horizontal en la ProgressBar
                double clickPosition = event.getX();
                // Calcular el porcentaje del progreso que se hace clic
                double progressPercentage = clickPosition / pgBar.getWidth();
                // Obtener la duración total del video
                double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
                // Calcular la nueva posición de tiempo en el video
                double newTime = progressPercentage * totalDuration;
                // Establecer el tiempo en el MediaPlayer
                mediaPlayer.seek(javafx.util.Duration.seconds(newTime));
            }
        });
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

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Archivos de media (*.mp4, *.mp3)", "*.mp4", "*.mp3");
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

            // Obtener el nombre del archivo sin extensión
            String fileName = selectedFile.getName();
            String title = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
            mediaTtleLabel.setText(title);

            // Agregar el archivo a la lista de recientes (si no está repetido)
            if (!recentMedia.getItems().contains(fileName)) {
                recentMedia.getItems().add(fileName);
            }

            // Reproducir automáticamente
            mediaPlayer.setOnPlaying(() -> btnPlay.setText("Pause"));
            mediaPlayer.setAutoPlay(true);
        }
    }

    private void configureMediaPlayer() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            // Actualiza la etiqueta de duración
            updateDurationLabel();

            // Sincronizar ProgressBar con el tiempo actual del MediaPlayer
            double currentTime = newValue.toSeconds();
            double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
            if (totalDuration > 0) {
                progress.set(currentTime / totalDuration);
            }
        });

        // Enlazar el ProgressBar con la propiedad de progreso
        pgBar.progressProperty().bind(progress);

        mediaPlayer.setOnReady(this::updateDurationLabel);

        mediaPlayer.setOnEndOfMedia(() -> {
            progress.set(0); // Reiniciar progreso al finalizar
            lblDuration.setText("00:00 / 00:00"); // Reiniciar etiqueta de tiempo
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

    private void playRecentMedia(String fileName) {
        File mediaFolder = new File("media");
        if (!mediaFolder.exists() || !mediaFolder.isDirectory()) {
            System.out.println("La carpeta de medios no existe.");
            return;
        }

        // Buscar el archivo en la carpeta "media"
        File selectedFile = new File(mediaFolder, fileName);
        if (!selectedFile.exists()) {
            System.out.println("Archivo no encontrado: " + selectedFile.getAbsolutePath());
            return;
        }

        // Crear y configurar el MediaPlayer
        Media media = new Media(selectedFile.toURI().toString());
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        configureMediaPlayer();

        // Actualizar la UI
        mediaTtleLabel.setText(fileName);
        mediaPlayer.setOnPlaying(() -> btnPlay.setText("Pause"));
        mediaPlayer.setAutoPlay(true);
    }
}
