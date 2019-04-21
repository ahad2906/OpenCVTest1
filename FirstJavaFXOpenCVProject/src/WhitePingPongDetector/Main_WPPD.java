package WhitePingPongDetector;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;


public class Main_WPPD extends Application {

    @Override
    public void start (Stage primaryStage) throws Exception {
        try {
            // load FXML resurser
            FXMLLoader loader = new FXMLLoader(getClass().getResource("WPPD.fxml"));
            // gemmer root elementet så controllerne kan bruge det
            BorderPane rootElement = (BorderPane) loader.load();
            // lav og design en scene
            Scene scene = new Scene(rootElement, 1700, 1000);
            scene.getStylesheets().add(getClass().getResource("wwpd_application.css").toExternalForm());
            // lav stage med den givne titel og forrige oprettede scene
            primaryStage.setTitle("Camera Feed Window");
            primaryStage.setScene(scene);
            primaryStage.show();

            // sæt den rigtige opførsel når applikationen lukkes ned
            Controller2 controller = loader.getController();
            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    controller.setClosed();
                }
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }

}
