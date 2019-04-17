package ImageSegmentation;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import sample.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ImageSegController {

    // FXML knapper
    @FXML
    private Button cameraButton;
    // FXML område der viser det nuværende frame
    @FXML
    private ImageView originalFrame;
    // checkbox for Canny
    @FXML
    private CheckBox canny;
    // canny threshold værdi
    @FXML
    private Slider threshold;
    // checkbox for baggrunds adskillelse
    @FXML
    private CheckBox dilateErode;
    // Inverse af treshold værdi for baggrundsadskillelse
    @FXML
    private CheckBox inverse;

    // en timer for at hente video stream
    private ScheduledExecutorService timer;
    // openCV objekt udfører billede optagelse
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive;

    Point clickedPoint = new Point(0, 0);
    Mat oldFrame;

    protected void init() {
        this.threshold.setShowTickLabels(true);
    }

    /**
     * Når knappen trykkes på GUI
     */
    @FXML
    protected void startCamera() {
        originalFrame.setFitWidth(500);
        originalFrame.setPreserveRatio(true);

        originalFrame.setOnMouseClicked(event -> {
            System.out.println("[" + event.getX() + ", " + event.getY() + "]");
            clickedPoint.x = event.getX();
            clickedPoint.y = event.getY();
        });

        if (!this.cameraActive) {
            this.canny.setDisable(true);
            this.dilateErode.setDisable(true);

            //start billede optagelse
            this.capture.open(0);

            // er video stream tilgængelig?
            if (this.capture.isOpened()) {
                this.cameraActive = true;

                // fang et frame hvert 33'te ms (30 frame/s)
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        // fang og behandl et enkelt frame
                        Mat frame = grabFrame();
                        // konverter og vis framet
                        Image imageToShow = Utils.mat2Image(frame);
                        updateImageView(originalFrame, imageToShow);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // opdater knap tekst
                this.cameraButton.setText("Stop Kamera");
            } else{
                System.err.println("Kan ikke pbne kamera forbindelses...");
            }
        } else {
            // kameraet er ved dette punkt ikke aktivt
            this.cameraActive = false;
            // opdater igen knap tekst
            this.cameraButton.setText("Start Kamera");
            // aktiver schecboks
            this.canny.setDisable(false);
            this.dilateErode.setDisable(false);

            // stop timer
            this.stopAcquisition();
        }
    }

    /**
     * Hent et frame fra et åbent video stream (hver der er nogen)
     */
    private Mat grabFrame() {
        Mat frame = new Mat();

        // tjek om capture er åben
        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);

                // hvis framet ikke er tomt, behandl det
                if (!frame.empty()) {
                    // håndter edge detection
                    if (this.canny.isSelected()) {
                        frame = this.doCanny(frame);
                        //frame = this.doSobel(frame);
                    }
                    // forgrunds detektion
                    else if (this.dilateErode.isSelected()) {
                        // Es. 2.1
                        //frame = this.doBackgroundRemovalFloodFill(frame);
                        // Es. 2.2
                        frame = this.doBackgroundRemovalAbsDiff(frame);
                        //Es. 2.3
                        //frame = this.doBackgroundRemoval(frame);
                    }
                }



            } catch (Exception e) {
                System.err.println("Exception under billede udarbejdelse...");
                e.printStackTrace();
            }
        }
        return frame;
    }

    private Mat doBackgroundRemovalAbsDiff(Mat currFrame) {
        Mat greyImage = new Mat();
        Mat foregroundIamge = new Mat();

        if (oldFrame == null) { oldFrame = currFrame; }

        Core.absdiff(currFrame, oldFrame, foregroundIamge);
        Imgproc.cvtColor(foregroundIamge, greyImage, Imgproc.COLOR_BGR2GRAY);

        int thresh_type = Imgproc.THRESH_BINARY_INV;
        if (this.inverse.isSelected()) { thresh_type = Imgproc.THRESH_BINARY; }

        Imgproc.threshold(greyImage, greyImage, 10, 255, thresh_type);
        currFrame.copyTo(foregroundIamge, greyImage);

        oldFrame = currFrame;
        return foregroundIamge;
    }

    private Mat doBackgroundRemovalFloodFill(Mat frame) {

        Scalar newVal = new Scalar(255, 255, 255);
        Scalar loDiff = new Scalar(50, 50, 50);
        Scalar upDiff = new Scalar(50, 50, 50);
        Point seedPoint = clickedPoint;
        Mat mask = new Mat();
        Rect rect = new Rect();

        //Imgproc.floodFill(frame, mask, seedPoint, newVal);
        Imgproc.floodFill(frame, mask, seedPoint, newVal, rect, loDiff, upDiff, Imgproc.FLOODFILL_FIXED_RANGE);

        return frame;
    }

    /**
     * udfør operationer nødvændig for fjernelse af uniform baggrund
     * @param frame
     * @return
     */
    private Mat doBackgroundRemoval(Mat frame) {
        // init
        Mat hsvImg = new Mat();
        List<Mat> hsvPlanes = new ArrayList<>();
        Mat thresholdImg = new Mat();

        int thresh_type = Imgproc.THRESH_BINARY_INV;
        if (this.inverse.isSelected()) {
            thresh_type = Imgproc.THRESH_BINARY;
        }

        // threshold billedet med dets gennemsnitlige hue værdi
        hsvImg.create(frame.size(), CvType.CV_8U);
        Imgproc.cvtColor(frame, hsvImg, Imgproc.COLOR_BGR2HSV);
        Core.split(hsvImg, hsvPlanes);

        // hent den gennemsnitlige hut værdi af billedet
        double threshValue = this.getHistAverage(hsvImg, hsvPlanes.get(0));

        Imgproc.threshold(hsvPlanes.get(0), thresholdImg, threshValue, 179.0, thresh_type);

        // dilate to fill gaps, erode to smooth edges
        Imgproc.dilate(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 1);
        Imgproc.erode(thresholdImg, thresholdImg, new Mat(), new Point(-1, -1), 3);

        Imgproc.threshold(thresholdImg, thresholdImg, threshValue, 179.0, Imgproc.THRESH_BINARY);

        // create the new image
        Mat foreground = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(255, 255, 255));
        frame.copyTo(foreground, thresholdImg);

        return foreground;
    }

    private double getHistAverage(Mat hsvImg, Mat hueValues) {
        // init
        double average = 0.0;
        Mat hist_hue = new Mat();
        // 0-180: range af Hue værdier
        MatOfInt histSize = new MatOfInt(180);
        List<Mat> hue = new ArrayList<>();
        hue.add(hueValues);

        // compute histogrammet
        Imgproc.calcHist(hue, new MatOfInt(0), new Mat(), hist_hue, histSize, new MatOfFloat(0, 179));

        // hent den gennemsnitlige Hue værdi af billedet
        // (sum(bin(h)*h))/(image-height*image-width)
        // -----------------
        // equivalent to get the hue of each pixel in the image, add them, and
        // divide for the image size (height and width)
        for (int h = 0; h < 180; h++)
        {
            // for each bin, get its value and multiply it for the corresponding
            // hue
            average += (hist_hue.get(h, 0)[0] * h);
        }

        // return the average hue of the image
        return average = average / hsvImg.size().height / hsvImg.size().width;
    }

    private Mat doCanny(Mat frame) {
        // init
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // reduce noise with a 3x3 kernel
        Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));

        // canny detector, with ratio of lower:upper threshold of 3:1
        Imgproc.Canny(detectedEdges, detectedEdges, this.threshold.getValue(), this.threshold.getValue() * 3);

        // using Canny's output as a mask, display the result
        Mat dest = new Mat();
        frame.copyTo(dest, detectedEdges);

        return dest;
    }

    private Mat doSobel(Mat frame) {
        // init
        Mat grayImage = new Mat();
        Mat detectedEdges = new Mat();
        int scale = 1;
        int delta = 0;
        int ddepth = CvType.CV_16S;
        Mat grad_x = new Mat();
        Mat grad_y = new Mat();
        Mat abs_grad_x = new Mat();
        Mat abs_grad_y = new Mat();

        // reduce noise with a 3x3 kernel
        Imgproc.GaussianBlur(frame, frame, new Size(3, 3), 0, 0, Core.BORDER_DEFAULT);

        // convert to grayscale
        Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

        // Gradient X
        // Imgproc.Sobel(grayImage, grad_x, ddepth, 1, 0, 3, scale,
        // this.threshold.getValue(), Core.BORDER_DEFAULT );
        Imgproc.Sobel(grayImage, grad_x, ddepth, 1, 0);
        Core.convertScaleAbs(grad_x, abs_grad_x);

        // Gradient Y
        // Imgproc.Sobel(grayImage, grad_y, ddepth, 0, 1, 3, scale,
        // this.threshold.getValue(), Core.BORDER_DEFAULT );
        Imgproc.Sobel(grayImage, grad_y, ddepth, 0, 1);
        Core.convertScaleAbs(grad_y, abs_grad_y);

        // Total Gradient (approximate)
        Core.addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, detectedEdges);
        // Core.addWeighted(grad_x, 0.5, grad_y, 0.5, 0, detectedEdges);

        return detectedEdges;
    }

    @FXML
    protected void cannySelected() {
        // check whether the other checkbox is selected and deselect it
        if (this.dilateErode.isSelected()) {
            this.dilateErode.setSelected(false);
            this.inverse.setDisable(true);
        }

        // enable the threshold slider
        if (this.canny.isSelected())
            this.threshold.setDisable(false);
        else
            this.threshold.setDisable(true);

        // now the capture can start
        this.cameraButton.setDisable(false);
    }

    @FXML
    protected void dilateErodeSelected()
    {
        // check whether the canny checkbox is selected, deselect it and disable
        // its slider
        if (this.canny.isSelected()) {
            this.canny.setSelected(false);
            this.threshold.setDisable(true);
        }

        if (this.dilateErode.isSelected())
            this.inverse.setDisable(false);
        else
            this.inverse.setDisable(true);

        // now the capture can start
        this.cameraButton.setDisable(false);
    }

    private void stopAcquisition() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            // release the camera
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }

    protected void setClosed() {
        this.stopAcquisition();
    }

}
