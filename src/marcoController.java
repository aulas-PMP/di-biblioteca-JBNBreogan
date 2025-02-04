import java.io.File;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class marcoController {

    @FXML
    private Button btnPlay;

    @FXML
    private Button btnVelocidad;

    @FXML
    private Button collapseEditor;

    @FXML
    private Label lblDuration;

    @FXML
    private MediaView mediaView;

    @FXML
    private Label mediaTtleLabel;

    @FXML
    private ProgressBar pgBar;

    @FXML
    private VBox editorBox;

    @FXML
    private VBox libraryBox;

    @FXML
    private ListView<String> recentMedia;

    private MediaPlayer mediaPlayer;

    private DoubleProperty progress = new SimpleDoubleProperty(0.0);

    private boolean isEditorVisible = true;

    @FXML
    private HBox mediaBox;

    @FXML
    private Button btnTamanho;

    private final int[][] resoluciones = {
            { 256, 144 }, // 144p
            { 640, 360 }, // 360p
            { 1280, 720 }, // 720p
    };
    private int resolucionActual = 0;

    @FXML
    private void initialize() {
        mediaTtleLabel.setText("");
        lblDuration.setText("00:00 / 00:00");

        recentMedia.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedItem = recentMedia.getSelectionModel().getSelectedItem();
                if (selectedItem != null) {
                    // Extraemos el nombre del archivo (sin la duración entre paréntesis)
                    String fileName = selectedItem.split(" \\(")[0];
                    playRecentMedia(fileName);
                }
            }
        });

        pgBar.setOnMouseClicked(event -> {
            if (mediaPlayer != null) {
                double clickPosition = event.getX();
                double progressPercentage = clickPosition / pgBar.getWidth();
                double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
                double newTime = progressPercentage * totalDuration;
                mediaPlayer.seek(javafx.util.Duration.seconds(newTime));
            }
        });
    }

    @FXML
    void btnPlay(MouseEvent event) {
        if (mediaPlayer != null) {
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                btnPlay.setText("Play");
            } else {
                mediaPlayer.play();
                btnPlay.setText("Pause");
            }
        }
    }

    @FXML
    private void collapseEdit(ActionEvent event) {
        if (isEditorVisible) {
            btnTamanho.setVisible(false);
            btnVelocidad.setVisible(false);
            editorBox.setMinWidth(30);
            editorBox.setMaxWidth(0);
            collapseEditor.setText(">");
        } else {
            // Expande el VBox (vuelve a su tamaño original)
            btnTamanho.setVisible(true);
            btnVelocidad.setVisible(true);
            editorBox.setMinWidth(editorBox.USE_COMPUTED_SIZE);
            editorBox.setMaxWidth(editorBox.USE_COMPUTED_SIZE);
            collapseEditor.setText("<");
        }

        // Alterna el estado
        isEditorVisible = !isEditorVisible;
    }

    @FXML
    private void collapseLib(ActionEvent event) {
        libraryBox.setVisible(false);
    }

    @FXML
    void btnStop(MouseEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            lblDuration.setText("00:00 / 00:00");
            btnPlay.setText("Play");
        }
    }

    @FXML
    void cambiarVelocidad(ActionEvent event) {
        if (mediaPlayer != null) {
            if (mediaPlayer.getRate() == 1.0) {
                mediaPlayer.setRate(2.0);
                btnVelocidad.setText("Velocidad x1");
            } else {
                mediaPlayer.setRate(1.0);
                btnVelocidad.setText("Velocidad x2");
            }
        }
    }

    @FXML
    void cambiarTamanho(ActionEvent event) {
        if (mediaPlayer != null && mediaView.getMediaPlayer() != null) {
            Media media = mediaPlayer.getMedia();

            // Verifica si el archivo es un video (contiene video tracks)
            boolean esVideo = media.getSource().endsWith(".mp4"); // Simple, pero funcional
            if (!esVideo) {
                System.out.println("No se puede cambiar la resolución porque es un audio.");
                return;
            }

            // Obtiene la nueva resolución en el ciclo 144p -> 360p -> 720p -> 1080p -> 144p
            int[] nuevaResolucion = resoluciones[resolucionActual];
            mediaView.setFitWidth(nuevaResolucion[0]);
            mediaView.setFitHeight(nuevaResolucion[1]);

            // Actualiza el texto del botón
            btnTamanho.setText("Resolución " + nuevaResolucion[1] + "p");

            // Pasa a la siguiente resolución en el ciclo
            resolucionActual = (resolucionActual + 1) % resoluciones.length;
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Archivos de media (*.mp4, *.mp3)", "*.mp4", "*.mp3"));

        Stage stage = (Stage) mediaView.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            Media media = new Media(selectedFile.toURI().toString());
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }

            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            configureMediaPlayer();

            String fileName = selectedFile.getName();
            String title = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".")) : fileName;
            mediaTtleLabel.setText(title);

            mediaPlayer.setOnReady(() -> {
                String duration = formatTime(mediaPlayer.getTotalDuration().toSeconds());
                String itemText = fileName + " (" + duration + ")";
                if (!recentMedia.getItems().contains(itemText)) {
                    recentMedia.getItems().add(itemText);
                }
            });

            mediaPlayer.setOnPlaying(() -> btnPlay.setText("Pause"));
            mediaPlayer.setAutoPlay(true);
        }
    }

    private void configureMediaPlayer() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            updateDurationLabel();
            double currentTime = newValue.toSeconds();
            double totalDuration = mediaPlayer.getTotalDuration().toSeconds();
            if (totalDuration > 0) {
                progress.set(currentTime / totalDuration);
            }
        });
        pgBar.progressProperty().bind(progress);
        mediaPlayer.setOnReady(this::updateDurationLabel);
        mediaPlayer.setOnEndOfMedia(() -> {
            progress.set(0);
            lblDuration.setText("00:00 / 00:00");
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
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, secs)
                : String.format("%02d:%02d", minutes, secs);
    }

    private void playRecentMedia(String fileName) {
        File mediaFolder = new File("media");
        if (!mediaFolder.exists() || !mediaFolder.isDirectory()) {
            System.out.println("La carpeta de medios no existe.");
            return;
        }

        File selectedFile = new File(mediaFolder, fileName);
        if (!selectedFile.exists()) {
            System.out.println("Archivo no encontrado: " + selectedFile.getAbsolutePath());
            return;
        }

        Media media = new Media(selectedFile.toURI().toString());
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }

        mediaPlayer = new MediaPlayer(media);
        mediaView.setMediaPlayer(mediaPlayer);
        configureMediaPlayer();
        mediaTtleLabel.setText(fileName); // Muestra el nombre del archivo en el label
        mediaPlayer.setOnPlaying(() -> btnPlay.setText("Pause"));
        mediaPlayer.setAutoPlay(true);
    }
}
