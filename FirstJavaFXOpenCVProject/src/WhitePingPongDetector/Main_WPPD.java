package WhitePingPongDetector;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;
import visualisering.VisuController;
import visualisering.View.Colors;
import visualisering.View.Kort;


public class Main_WPPD extends Application {

    @Override
    public void start (Stage primaryStage) throws Exception {
        try {
            // load FXML resurser
            FXMLLoader loader = new FXMLLoader(getClass().getResource("WPPD.fxml"));
            // gemmer root elementet så controllerne kan bruge det
            BorderPane rootElement = (BorderPane) loader.load();
            // lav og design en scene
            Scene scene = new Scene(rootElement, 2000, 900);
            scene.getStylesheets().add(getClass().getResource("wwpd_application.css").toExternalForm());
            // lav stage med den givne titel og forrige oprettede scene
            primaryStage.setTitle("Camera Feed Window");
            primaryStage.setScene(scene);
            primaryStage.show();


            // sæt den rigtige opførsel når applikationen lukkes ned
            Controller2 controller = loader.getController();
//            controller.startCamera();
            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    controller.setClosed();
                }
            }));

            //Starter visualiseringen
            int width = 600, height = 400;
            Stage stage = new Stage();
            Group root = new Group();
            Canvas canvas = new Canvas(width, height);

            VisuController visuController = new VisuController(controller);
            visuController.createMap(new Kort(canvas));


            root.getChildren().add(canvas);
            stage.setTitle("JavaFX Scene Graph Demo");
            stage.setScene(new Scene(root, Colors.BACKGROUND));
            stage.show();


            controller.addVisuController(visuController);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static void main(String[] args) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        launch(args);
    }

}
