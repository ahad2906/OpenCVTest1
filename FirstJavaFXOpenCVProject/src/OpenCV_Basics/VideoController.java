package OpenCV_Basics;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import sample.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class VideoController {

    @FXML
    private Button button;
    @FXML
    private CheckBox grayscale;
    @FXML
    private CheckBox logoCheckBox;
    @FXML
    private ImageView histogram;
    @FXML
    private ImageView currentFrame;

    private ScheduledExecutorService timer;
    private VideoCapture capture;
    private boolean cameraActive;
    private Mat logo;

    public void initialize() {
        this.capture = new VideoCapture();
        this.cameraActive = false;
    }

    @FXML
    protected void startCamera() {
        this.currentFrame.setFitWidth(600); // sæt en fast bredde for farmeet
        this.currentFrame.setPreserveRatio(true); // bevar billede ratio

        if (!this.cameraActive) {
            this.capture.open(0); // start video optagelse

            if (this.capture.isOpened()){ // er video stream tilgængelig?
                this.cameraActive = true;

                Runnable frameGrabber = new Runnable() { // fang et frame hvert 33'te ms (30 frames/s)
                    @Override
                    public void run() {
                        Mat frame = grabFrame(); // fang og behandl et enkelt frame
                        Image imageToShow = Utils.mat2Image(frame); // konverter og vis framet
                        updateImageView(currentFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                this.button.setText("Stop Camera"); // opdater knap tekst
            } else {
                System.err.println("Umuligt at åbne kamera forbindelse...");
            }
        } else {
            this.cameraActive = false; // kameraet er ved dette punkt ikke aktiv
            this.button.setText("Start Camera"); // opdater igen knap tekst

            this.stopAcquisition();
        }
    }

    @FXML
    protected void loadLogo() {
        if (logoCheckBox.isSelected()) {
            this.logo = Imgcodecs.imread("Resources/random_logo.png");
        }
    }

    /**
     * fang et frame fra det åbne video strem (hvis der er nogen)
     * @return
     */
    private Mat grabFrame() {
        Mat frame = new Mat(); // læs nuværende frame

        if (this.capture.isOpened()) { // hvis framet ikke er tomt, behandl det
            try {
                this.capture.read(frame);

                if (logoCheckBox.isSelected() && this.logo != null) { // tilføj logo
                    Rect roi = new Rect(frame.cols() - logo.cols(), frame.rows() - logo.rows(), logo.cols(), logo.rows());
                    Mat imageROI = frame.submat(roi);
                    Core.addWeighted(imageROI, 1.0, logo, 0.8, 0.0, imageROI);

                    //tilføj logo: metode 2
                    // logo.copyTo(imageROI, logo);
                }

                // hvis grayscale er valgt i checkbox, konverter framet
                // (frame + logo)
                if (grayscale.isSelected()) {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                }

                this.showHistogram(frame, grayscale.isSelected()); // vis histogrammet

            } catch (Exception e) {
                System.err.println("Exception under frame udarbejdelse: " + e);
            }
        }
        return frame;
    }

    private void showHistogram(Mat frame, boolean gray) {
        // split frames i adskillige images
        List<Mat> images = new ArrayList<Mat>();
        Core.split(frame, images);

        MatOfInt histSize = new MatOfInt(256); // sæt nummeret af bind ved 256
        MatOfInt channels = new MatOfInt(0); // sæt kun 1 kanal
        MatOfFloat histRange = new MatOfFloat(0, 256); // sæt range

        // compute histogrammerne for the B, G and R komponenter
        Mat hist_b = new Mat();
        Mat hist_g = new Mat();
        Mat hist_r = new Mat();

        // B komponent eller grå image
        Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist_b, histSize, histRange, false);

        // G og R komponent (hvis image ikke er i gray scale)
        if (!gray) {
            Imgproc.calcHist(images.subList(1, 2), channels, new Mat(), hist_g, histSize, histRange, false);
            Imgproc.calcHist(images.subList(2, 3), channels, new Mat(), hist_r, histSize, histRange, false);
        }

        // tegn histogrammerne
        int hist_w = 150; // bredde af hist
        int hist_h = 150; // højde af hist
        int bin_w = (int) Math.round(hist_w / histSize.get(0, 0)[0]);

        Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar(0, 0, 0));
        //normaliser resultatet til [0, histImage.rows()]
        Core.normalize(hist_b, hist_b, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());

        // normaliser for G og R komponenter
        if (!gray) {
            Core.normalize(hist_g, hist_g, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
            Core.normalize(hist_r, hist_r, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
        }

        // tegn histogram(merne)
        for (int i = 1; i < histSize.get(0,0)[0]; i++) {
            // B komponent eller gray image
            Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_b.get(i-1,0)[0])),
            new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i,0)[0])), new Scalar(255,0,0),2,8,0);

            if (!gray) {
                Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_g.get(i-1,0)[0])),
                        new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i,0)[0])), new Scalar(255,0,0),2,8,0);
                Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_r.get(i-1,0)[0])),
                        new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i,0)[0])), new Scalar(255,0,0),2,8,0);
            }
        }

        // visualiser histogram
        Image histImg = Utils.mat2Image(histImage);
        updateImageView(histogram, histImg);
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();;
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println("Exceptin ved stop af fram optagelse, forsøger at frigive kameraet nu... " + e);
            }
        }

        if (this.capture.isOpened()) {
            this.capture.release(); // frigiv kameraet
        }
    }


    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    protected void setClosed() {
        this.stopAcquisition();
    }

}
