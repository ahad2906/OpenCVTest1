package WhitePingPongDetector;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
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
    private ImageView cannyImage;
    @FXML
    private Slider hueStart;
    @FXML
    private Slider hueStop;
    @FXML
    private Slider saturationStart;
    @FXML
    private Slider saturationStop;
    @FXML
    private Slider valueStart;
    @FXML
    private Slider valueStop;
    // FXML label to show the current values set with the sliders
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

    // property for object binding
    private ObjectProperty<String> hsvValuesProp;

    /**
     * Aktionen når knappen til at starte kameraet trykkes på GUI
     */
    @FXML
    protected void startCamera() {

        // bind a text property with the string containing the current range of
        // HSV values for object detection
        hsvValuesProp = new SimpleObjectProperty<>();
        this.hsvValues.textProperty().bind(hsvValuesProp);

        // set a fixed width for all the image to show and preserve image ratio
        this.imageViewProperties(this.originalFrame, 800);
        Tooltip.install(originalFrame, new Tooltip("Original frame"));
        this.imageViewProperties(this.maskImage, 300);
        Tooltip.install(maskImage, new Tooltip("Mask frame"));
        this.imageViewProperties(this.morphImage, 300);
        Tooltip.install(morphImage, new Tooltip("Morph frame"));
        this.imageViewProperties(this.cannyImage, 300);
        Tooltip.install(cannyImage, new Tooltip("Canny frame"));


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

                    Mat hsvImage = new Mat();
                    // konverter framet til et HSV frame
                    Imgproc.cvtColor(frame, hsvImage, Imgproc.COLOR_BGR2HSV);

                    Mat blurredImage = new Mat();
                    // slørre framet
                    Imgproc.blur(hsvImage, blurredImage, new Size(7,7));

                    Mat mask = new Mat();
                    // minimum og maximum værdier for RBG værdier
                    Scalar valuesMin = new Scalar(0,150,108);
                    Scalar valuesMax = new Scalar(180,255,255);

                    // get thresholding values from the UI
                    // remember: H ranges 0-180, S and V range 0-255
                    Scalar minValues = new Scalar(this.hueStart.getValue(), this.saturationStart.getValue(),
                            this.valueStart.getValue());
                    Scalar maxValues = new Scalar(this.hueStop.getValue(), this.saturationStop.getValue(),
                            this.valueStop.getValue());

                    // show the current selected HSV range
                    String valuesToPrint = "Hue range: " + minValues.val[0] + "-" + maxValues.val[0]
                            + "\tSaturation range: " + minValues.val[1] + "-" + maxValues.val[1] + "\tValue range: "
                            + minValues.val[2] + "-" + maxValues.val[2];
                    ImageSegmentation.utils.Utils.onFXThread(this.hsvValuesProp, valuesToPrint);

                    // udvælger elementer fra udvalgte RBG-range og konverterer til hvid farve
                    Core.inRange(hsvImage, minValues, maxValues, mask);

                    // opdater billedet oppe til højre i UI
                    this.updateImageView(this.maskImage, Utils.mat2Image(mask));


                    Mat morhpOutput = new Mat();
                    // Morphological operators
                    // Dilate elements of size x*x (gør objekt større)
                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4,4));
                    // Erode elements of size x*x (gør objekt mindre)
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(  10,10));

                    // forstørre elementet x gange
                    Imgproc.dilate(mask, morhpOutput, dilateElement);
                    Imgproc.dilate(morhpOutput, morhpOutput, dilateElement);

                    Mat cannyOutput = new Mat();
                    // tegner streger/kanter af elementer i framet
                    Imgproc.Canny(morhpOutput, cannyOutput, 30, 3);

                    List<MatOfPoint> contours = new ArrayList<>();
                    Mat hierarchy = new Mat();
                    Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

                    //Mat drawing = Mat.zeros(cannyOutput.size(), CvType.CV_8UC3);

                    for (int i=0; i< contours.size(); i++) {
                        Scalar color = new Scalar(0, 255, 0);
                        MatOfPoint temp_contour = contours.get(i);
                        MatOfPoint2f new_mat = new MatOfPoint2f( temp_contour.toArray() );
                        int contourSize = (int)temp_contour.total();
                        // tegner contours (stregerne i cannyOutput)
                        MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                        Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize*0.05, true);
                        MatOfPoint points = new MatOfPoint( approxCurve_temp.toArray() );

                        String shape;

                        if (approxCurve_temp.toArray().length == 12) {
                            shape = "plus";
                            System.out.println(shape);
                            Imgproc.drawContours(frame, contours, -1, new Scalar(255,0,0), 2);

                        }

                        Rect rect = Imgproc.boundingRect(points);
                        // Imgproc.drawContours(destinationFrame, sourceFrameWithContours)
                        // Imgproc.drawContours(frame, contours, i, color, 5, 8, hierarchy, 0, new Point());
                        // tegn firkant, hvis brdde og højde krav er opfyldt
                        if(Math.abs(rect.width) > 200 && Math.abs(rect.height) > 200) {
                            // tegner firkant med (x,y)-koordinater
                            Imgproc.rectangle(frame, new Point(rect.x+20, rect.y+20), new Point(rect.x + rect.width-20, rect.y + rect.height-20), new Scalar(170, 0, 150, 0), 15);
                            // gem koordinaterne
                            String koord = rect.x+20 + "," + (rect.y+20);
                            String koord1 = rect.x + rect.width-20 + "," + (rect.y + rect.height-20);
                            String koord2 = rect.x+20 + "," + (rect.y + rect.height-20);
                            String koord3 = rect.x + rect.width-20 + "," + (rect.y);
                            // print koordinaterne ud på billdet
                            Imgproc.putText(frame, koord, new Point(rect.x, rect.y), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                            Imgproc.putText(frame, koord1, new Point(rect.x+rect.width, rect.y+rect.height), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                            Imgproc.putText(frame, koord2, new Point(rect.x, rect.y+rect.height), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                            Imgproc.putText(frame, koord3, new Point(rect.x+rect.width, rect.y), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                        }

                    }
                    // opdater billedet nede til højre i UI
                    this.updateImageView(this.morphImage, Utils.mat2Image(morhpOutput));
                    // opdater billedet nede til venstre i UI
                    this.updateImageView(this.cannyImage, Utils.mat2Image(cannyOutput));
                }
            } catch (Exception e) {
                // log den fangede error
                System.err.println("Exception under billede udarbejdelse" + e);
            }
        }
        return frame;
    }

    private Mat grabFrame1() {
        // init alt
        Mat frame = new Mat();

        // tjek om optagelse er åben
        if (this.videoCapture.isOpened()) {
            try {
                // læs det nuværende frame
                this.videoCapture.read(frame);
                // hvis frame ikke er tomt, behandl det
                if (!frame.empty()) {
                    int hey;
                    Mat grayImage = new Mat();
                    Mat detectedEdges = new Mat();
                    // konverter framet framet til et HSV frame
                    Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.adaptiveThreshold(grayImage, grayImage, 125, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 12);
                    // reduce noise with a 3x3 kernel
                    Imgproc.medianBlur(grayImage, detectedEdges, 3);
                    //Imgproc.GaussianBlur(grayImage, detectedEdges, new Size(3,3), 2, 2);
                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS,
                            new  Size((2*2)+1, (2*2)+1));
                    // Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
                    // canny detector, with ratio of lower:upper threshold of 3:1
                    int threshold =150;
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);
                    // using Canny's output as a mask, display the result
                    Mat circles = new Mat();
                    Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);
                    Imgcodecs.imwrite("C:\\Users\\gunnh\\OneDrive\\Desktop\\TestBilleder\\testCanny4.png", detectedEdges);
                    Imgproc.HoughCircles(detectedEdges, circles, Imgproc.CV_HOUGH_GRADIENT,
                            1, 10, 19, 18, 5, 10);
                    for(int i = 0; i < circles.cols(); i++) {
                        double[] c = circles.get(0, i);
                        System.out.println(i + ": " + Math.round(c[0]) + ", " + Math.round(c[1]));
                        Point center = new Point(Math.round(c[0]), Math.round(c[1]));
                        Imgproc.circle(frame, center, 1, new Scalar(0,100,100), 3, 8, 0);
                        int radius = (int) Math.round(c[2]);

                        Imgproc.circle(frame, center, radius, new Scalar(225, 0, 225), 3, 8 ,0);
                        String koord = Math.round(c[0]) + ": " + Math.round(c[1]);
                        Imgproc.putText(frame, koord, center, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                    }
                    System.out.println(circles.cols());
                    this.updateImageView(this.morphImage, Utils.mat2Image(detectedEdges));


                }
            } catch (Exception e) {
                System.err.println("Exception under billede udarbejdelse" + e);  // log den fangede error
            }
        }
        return frame;
    }

    private Mat findAndDrawBalls(Mat inputFrame, Mat outputFrame) {
        //Imgproc.cvtColor(maskedImage, maskedImage, Imgproc.COLOR_BGR2GRAY);
        //Imgproc.medianBlur(maskedImage, maskedImage, 5);
        Mat circles = new Mat();

        Imgproc.HoughCircles(inputFrame, circles, Imgproc.HOUGH_GRADIENT, 1.0
                , (double)inputFrame.rows()/2
                ,100, 10, 2, 40);

        for (int i=0; i<circles.cols(); i++) {
            double[] c = circles.get(0,i);
            Point center = new Point(Math.round(c[0]), Math.round(c[1]));
            // circle center
            Imgproc.circle(outputFrame, center, 1, new Scalar(0,255,0), 3, 8, 0 );
            // circle outline
            int radius = (int) Math.round(c[2]);
            Imgproc.circle(outputFrame, center, radius, new Scalar(0,0,255), 2, 5, 0);
        }
        return outputFrame;
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
