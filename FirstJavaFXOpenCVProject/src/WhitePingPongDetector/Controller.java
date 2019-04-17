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

public class Controller {

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
                    Mat hslImage = new Mat();
                    Mat mask = new Mat();
                    Mat morhpOutput = new Mat();

                    Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

                    // fjern noget baggrundsstøj ved at slørre framet
                    //Imgproc.blur(frame, blurredImage, new Size(7, 7));
                    Imgproc.medianBlur(grayImage, blurredImage, 3);

                    // convert the frame to HSV
                    //Imgproc.cvtColor(blurredImage, hsvImage, Imgproc.COLOR_BGR2HSV);

                    // convert the frame to HLS (HSL)
                    //Imgproc.cvtColor(blurredImage, hslImage, Imgproc.COLOR_BGR2HLS);


                    Scalar valuesMin = new Scalar(70,160,70);
                    Scalar valuesMax = new Scalar(255,255,255);


                    // threshold HSV image to select color (balls)
                    //Core.inRange(blurredImage, valuesMin, valuesMax, mask);
                    // show the partial output

                    Imgproc.threshold(blurredImage, mask, 180, 200, Imgproc.THRESH_BINARY);

                    this.updateImageView(this.maskImage, Utils.mat2Image(mask));

                    // morpholoical operators
                    // Dilate elements of size x*x (gør objekt større)
                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2));
                    // Erode elements of size x*x (gør objekt mindre)
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(  14,14));

                    //Imgproc.erode(mask, morhpOutput, erodeElement);
                    //Imgproc.erode(morhpOutput, morhpOutput, erodeElement);

                    Imgproc.dilate(mask, morhpOutput, dilateElement);
                    Imgproc.dilate(morhpOutput, morhpOutput, dilateElement);
                    Imgproc.dilate(morhpOutput, morhpOutput, dilateElement);
                    Imgproc.dilate(morhpOutput, morhpOutput, dilateElement);

                    Imgproc.medianBlur(morhpOutput, morhpOutput, 3);

                    //MatOfPoint2f approx = new MatOfPoint2f();
                    //Imgproc.approxPolyDP(morhpOutput, approx, Imgproc.arcLength(morhpOutput, true) * 0.02, true);

                    this.updateImageView(this.morphImage, Utils.mat2Image(morhpOutput));

                    frame = this.findAndDrawBalls(morhpOutput, frame);

                    //Imgproc.erode(frame, frame, Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5)));
                    //Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                }
            } catch (Exception e) {
                System.err.println("Exception under billede udarbejdelse" + e);  // log den fangede error
            }
        }
        return frame;
    }


    /**
     * Finder cirkler i et billede
     * @param maskedImage
     * @param frame
     * @return
     */
    private Mat findAndDrawBalls(Mat maskedImage, Mat frame) {
        //Imgproc.cvtColor(maskedImage, maskedImage, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.medianBlur(maskedImage, maskedImage, 5);
        Mat circles = new Mat();

        Imgproc.HoughCircles(maskedImage, circles, Imgproc.HOUGH_GRADIENT, 1.0
                , (double)maskedImage.rows()/2
                ,200, 10, 2, 40);

        for (int i=0; i<circles.cols(); i++) {
            double[] c = circles.get(0,i);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(frame, center, 1, new Scalar(0,255,0), 3, 8, 0 );
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(frame, center, radius, new Scalar(0,0,255), 2, 5, 0);
        }
        return frame;
    }


    private Mat findAndDrawBalls2(Mat maskedImage, Mat frame) {
        // init
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        // find contours
        Imgproc.findContours(maskedImage, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

        // if any contour exist...
        if (hierarchy.size().height > 0 && hierarchy.size().width > 0) {
            // for each contour, display it in blue
            for (int idx = 0; idx >= 0; idx = (int) hierarchy.get(0, idx)[0]) {
                Imgproc.drawContours(frame, contours, idx, new Scalar(250, 0, 0));
            }
        }
        return frame;
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
