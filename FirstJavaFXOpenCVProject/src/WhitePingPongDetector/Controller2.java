package WhitePingPongDetector;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import sample.utils.Utils;

import java.awt.image.ImageProducer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller2 {

    @FXML
    private Button button;
    @FXML
    private ImageView originalFrame;
    @FXML
    private ImageView maskImage;
    @FXML
    private ImageView morphImage;
    @FXML
    private Label hsvValues;

    // timer til at hente video stream
    private ScheduledExecutorService timer;
    // openCV objekt som realisere billed opfanging (optagelse)
    private VideoCapture videoCapture = new VideoCapture();
    // flag til at ændre knap funktionalitet
    private boolean cameraActive = false;
    // ID for det kamera der skal bruges
    private static int cameraID = 0;

    /**
     * Aktionen når knappen til at starte kameraet trykkes på GUI
     */
    @FXML
    protected void startCamera() {

        // set a fixed width for all the image to show and preserve image ratio
        this.imageViewProperties(this.originalFrame, 800);
        this.imageViewProperties(this.maskImage, 500);
        this.imageViewProperties(this.morphImage, 500);

        if (!this.cameraActive) {
            this.videoCapture.open(cameraID); // start video optagelse

            if (this.videoCapture.isOpened()) { // er video streamen tilgængelig?
                this.cameraActive = true;

                Runnable frameGrabber = new Runnable() { // fang et frame hvert 33'te ms (30 frame/s)
                    @Override
                    public void run() {
                        Mat frame = grabFrame(); // fang og behandle et enkelt frame
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(originalFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // opdater knap indhold
                this.button.setText("Stop Kamera");
            } else {
                System.err.println("Umuligt at åbne kamera forbindelse...");
            }
        } else {
            // kameraet er ikke aktiv på dette punkt
            this.cameraActive = false;
            // opdatere igen knap indholdet
            this.button.setText("Start Camera");

            // stop timeren
            this.stopAquisition();
        }
    }

    /**
     * fang et frame fra the åbnede video stream (hvis der er nogen)
     */
    private Mat grabFrame() {
        // init alt
        Mat frame = new Mat();

        // tjek om optagelse er åben
        if (this.videoCapture.isOpened()) {
            try {
                // læs det nuværende frame
                this.videoCapture.read(frame);
                // hvis frame ikke er tomt, behandl det
                if (!frame.empty()) {

                    // init
                    Mat grayImage = new Mat();
                    Mat blurredImage = new Mat();
                    Mat hsvImage = new Mat();
                    Mat mask = new Mat();
                    Mat morhpOutput = new Mat();


                    // konverter framet framet til et HSV frame
                    Imgproc.cvtColor(frame, hsvImage, Imgproc.COLOR_BGR2HSV);

                    // slørre HSV framet
                    Imgproc.blur(hsvImage, blurredImage, new Size(7,7));

                    Scalar valuesMin = new Scalar(0,150,108);
                    Scalar valuesMax = new Scalar(180,255,255);
                    Core.inRange(hsvImage, valuesMin, valuesMax, mask);

                    // opdater billedet oppe til højre i UI
                    this.updateImageView(this.maskImage, Utils.mat2Image(mask));


                    // Morphological operators
                    // Dilate elements of size x*x (gør objekt større)
                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4,4));
                    // Erode elements of size x*x (gør objekt mindre)
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(  10,10));

                    // forstørre elementet x gange
                    Imgproc.dilate(mask, morhpOutput, dilateElement);
                    Imgproc.dilate(morhpOutput, morhpOutput, dilateElement);

                    //findAndDrawRectangles(morhpOutput, morhpOutput);


                    Imgproc.Canny(morhpOutput, morhpOutput, 30, 3);

                    // opdater billedet nede til højre i UI
                    this.updateImageView(this.morphImage, Utils.mat2Image(morhpOutput));




                    /*

                    // fjern noget baggrundsstøj ved at slørre framet
                    //Imgproc.blur(frame, blurredImage, new Size(7, 7));
                    //Imgproc.medianBlur(grayImage, blurredImage, 3);

                    // convert the frame to HSV
                    //Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

                    // convert the frame to HLS (HSL)
                    //Imgproc.cvtColor(blurredImage, hslImage, Imgproc.COLOR_BGR2HLS);

                    // threshold HSV image to select color (balls)
                    //Core.inRange(blurredImage, valuesMin, valuesMax, mask);
                    // show the partial output

                    // morphological opreators
                    // Dilate elements of size x*x (gør objekt større)
                    //Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2));
                    // Erode elements of size x*x (gør objekt mindre)
                    //Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(  14,14));

                    //Imgproc.erode(mask, morhpOutput, erodeElement);
                    //Imgproc.erode(morhpOutput, morhpOutput, erodeElement);

                    //Imgproc.dilate(mask, morhpOutput, dilateElement);
                    //Imgproc.dilate(morhpOutput, morhpOutput, dilateElement);
                    //Imgproc.dilate(morhpOutput, morhpOutput, dilateElement);
                    //Imgproc.dilate(morhpOutput, morhpOutput, dilateElement);

                    //Imgproc.medianBlur(morhpOutput, morhpOutput, 3);

                    //MatOfPoint2f approx = new MatOfPoint2f();
                    //Imgproc.approxPolyDP(morhpOutput, approx, Imgproc.arcLength(morhpOutput, true) * 0.02, true);

                    //this.updateImageView(this.morphImage, Utils.mat2Image(mask));
                    */


                }
            } catch (Exception e) {
                System.err.println("Exception under billede udarbejdelse" + e);  // log den fangede error
            }
        }
        return frame;
    }

    private Mat findAndDrawRectangles(Mat sourceFrame, Mat frame) {

        Mat blurredFrame = new Mat();
        Imgproc.medianBlur(sourceFrame, blurredFrame, 3);


        // lav en rektangel med øverst/venstre vertex at (x,y) med (højde, bredde)
        Rect rect = new Rect(10, 20, 40, 60);

        int lineThickness = 10;
        Scalar lineColor = new Scalar(255,0,0);

        // tegn trekanten på billedet med
        Imgproc.rectangle(sourceFrame,rect ,lineColor, lineThickness);

        return blurredFrame;
    }


    /**
     * Set typical {@link ImageView} properties: a fixed width and the
     * information to preserve the original image ration
     *
     * @param image
     *            the {@link ImageView} to use
     * @param dimension
     *            the width of the image to set
     */
    private void imageViewProperties(ImageView image, int dimension) {
        // set a fixed width for the given ImageView
        image.setFitWidth(dimension);
        // preserve the image ratio
        image.setPreserveRatio(true);
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
        if (this.videoCapture.isOpened()) {
            // release the camera
            this.videoCapture.release();
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
