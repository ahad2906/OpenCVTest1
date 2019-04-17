package ImageSegmentation;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;

public class ImageSegmentation extends Application {

    /**
     * main class: Genererer og håndtere main window med dets resurser (style, graphics, osv.)
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            // load FMXL resurserne
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ImageSegmentation.fxml"));
            BorderPane root = loader.load();

            // sæt baggrundsfarve
            root.setStyle("-fx-background-color: whitesmoke");
            //lav og design en scene
            Scene scene = new Scene(root, 1000, 800);
            scene.getStylesheets().add(getClass().getResource("wwpd_application.css").toExternalForm());
            // lav en stage med den givne titel og forrige oprettede scene
            primaryStage.setTitle("Image Segmentation");
            primaryStage.setScene(scene);
            primaryStage.show();

            // hent controlleren
            ImageSegController controller = loader.getController();
            controller.init();

            // angiv opførsel når applikation lukkes ned
            primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    controller.setClosed();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }

}
