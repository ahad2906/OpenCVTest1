package sample;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import sample.utils.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Controller {

    @FXML
    private Button button;
    @FXML
    private ImageView currentFrame;

    private ScheduledExecutorService timer; // timer til at hente video stream
    private VideoCapture capture = new VideoCapture(); // openCV objekt som realisere billed opfanging (optagelse)
    private boolean cameraActive = false; // flag til at ændre knap funktionalitet
    private static int cameraID = 0; // ID for det kamera der skal bruges


    /**
     * Aktionen når knappen til at starte kameraet trykkes på GUI
     *
     * @param event
     */
    @FXML
    protected void startCamera(ActionEvent event) {
        if (!this.cameraActive) {
            this.capture.open(cameraID); // start video optagelse

            if (this.capture.isOpened()) { // er video streamen tilgængelig?
                this.cameraActive = true;

                Runnable frameGrabber = new Runnable() { // fang et frame hvert 33'te ms (30 frame/s)
                    @Override
                    public void run() {
                        Mat frame = grabFrame(); // fang og behandle et enkelt frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(currentFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                this.button.setText("Stop Kamera"); // opdater knap indhold
            } else {
                System.err.println("Umuligt at åbne kamera forbindelse...");
            }
        } else {
            this.cameraActive = false; // kameraet er ikke aktiv på dette punkt
            this.button.setText("Start Camera"); // opdatere igen knap indholdet

            this.stopAquisition(); // stop timeren
        }
    }


    /**
     * fang et frame fra the åbnede video stream (hvis der er nogen)
     */
    private Mat grabFrame() {
        Mat frame = new Mat(); // init alt

        if (this.capture.isOpened()) { // tjek om optagelse er åben
            try {
                this.capture.read(frame); // læs det nuværende frame
                if (!frame.empty()) { // hvis frame ikke er tomt, behandl det
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                }
            } catch (Exception e) {
                System.err.println("Exception under billede udarbejdelse" + e);  // log den fangede error
            }
        }
        return frame;
    }

    private void stopAquisition() {
        if (this.timer!=null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown(); // stop timeren
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println("Exception ved stopning af fram opfanigningen, forsøger at frigive kamera nu...: " + e);
            }
        }
    }

    /**
     * opdatere ImageView i JavaFX main thread
     * @param view
     * @param image
     */
    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * Når applikationen lukkes ned, stoppes opfangelse fra kamera
     */
    protected void setClosed() {
        this.stopAquisition();
    }
}
