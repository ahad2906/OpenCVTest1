package ObjectDetection;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Core;

public class Main_ObjectDetection extends Application
{
    /**
     * The main class for a JavaFX application. It creates and handles the main
     * window with its resources (style, graphics, etc.).
     *
     * This application looks for any tennis ball in the camera video stream and
     * try to select them according to their HSV values. Found tennis balls are
     * framed with a blue line.
     *
     * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
     * @version 2.0 (2017-03-10)
     * @since 1.0 (2015-01-13)
     *
     */
    @Override
    public void start(Stage primaryStage)
    {
        try
        {
            // load the FXML resource
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ObjRecognition.fxml"));
            // store the root element so that the controllers can use it
            BorderPane root = (BorderPane) loader.load();
            // set a whitesmoke background
            root.setStyle("-fx-background-color: whitesmoke;");
            // create and style a scene
            Scene scene = new Scene(root, 1500, 900);
            //scene.getStylesheets().add(getClass().getResource("wwpd_application.css").toExternalForm());
            // create the stage with the given title and the previously created
            // scene
            primaryStage.setTitle("Object Recognition");
            primaryStage.setScene(scene);
            // show the GUI
            primaryStage.show();

            // set the proper behavior on closing the application
            ObjRecognitionController controller = loader.getController();
            primaryStage.setOnCloseRequest((new EventHandler<WindowEvent>() {
                public void handle(WindowEvent we)
                {
                    controller.setClosed();
                }
            }));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        // load the native OpenCV library
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        launch(args);
    }
}