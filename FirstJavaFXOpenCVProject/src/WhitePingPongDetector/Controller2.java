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
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.videoio.VideoCapture;
import sample.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Controller2 {

    // Variabler til elementer i FXML Scene/vindue
    @FXML
    private Button button;
    @FXML
    private ImageView originalFrame;
    @FXML
    private ImageView originalFrame2;
    @FXML
    private ImageView maskImage;
    @FXML
    private ImageView maskImage2;
    @FXML
    private ImageView morphImage;
    @FXML
    private ImageView cannyImage;
    @FXML
    private ImageView cannyImage2;
    @FXML
    private Slider hueStart1;
    @FXML
    private Slider hueStop1;
    @FXML
    private Slider saturationStart;
    @FXML
    private Slider saturationStop;
    @FXML
    private Slider valueStart;
    @FXML
    private Slider valueStop;
    @FXML
    private Slider hueStart2;
    @FXML
    private Slider hueStop2;
    @FXML
    private Slider xstart;
    @FXML
    private Slider xstop;
    @FXML
    private Slider ystart;
    @FXML
    private Slider ystop;
    // FXML label to show the current values set with the sliders
    @FXML
    private Label hsvValues;
    @FXML
    private Label hsvValues2;

    // Timer til at hente video stream
    private ScheduledExecutorService timer;
    // openCV objekt som realisere billedopfangning (optagelse)
    private VideoCapture videoCapture = new VideoCapture();
    // Flag til at ændre knap funktionalitet
    private boolean cameraActive = false;
    // ID for det kamera der skal bruges, typisk 0
    // Deaktivér standard kamera i 'enhedshåndtering', hvis ekstern kamera skal bruges
    private static int cameraID = 0;

    // Variabel for at binde trackbars for HSV/RGB-værdier i FXML Scene
    private ObjectProperty<String> hsvValuesProp;
    private ObjectProperty<String> hsvValuesProp2;

    /**
     * Aktionen når knappen til at starte kameraet trykkes på GUI
     */
    @FXML
    protected void startCamera() {

        // Bind a text property with the string containing the current range of
        // HSV values for object detection
        hsvValuesProp = new SimpleObjectProperty<>();
        this.hsvValues.textProperty().bind(hsvValuesProp);

        hsvValuesProp2 = new SimpleObjectProperty<>();
        this.hsvValues2.textProperty().bind(hsvValuesProp2);

        // Set a fixed width for all the image to show and preserve image ratio
        // Frame til boldene
        this.imageViewProperties(this.originalFrame2, 600);
        Tooltip.install(originalFrame2, new Tooltip("Original frame2 til boldene"));

        // Frame til mask output af originalFrame2 (til boldene)
        this.imageViewProperties(this.maskImage2, 200);
        Tooltip.install(maskImage2, new Tooltip("Mask frame (boldene)"));

        // Frame til canny output af originalFrame2 (til boldene)
        this.imageViewProperties(this.cannyImage2, 200);
        Tooltip.install(cannyImage2, new Tooltip("Canny frame (boldene)"));

        // Frame til banen
        this.imageViewProperties(this.originalFrame, 600);
        Tooltip.install(originalFrame, new Tooltip("Original frame til banen"));

        // Frame til den røde farve i originalFrame (til banen)
        this.imageViewProperties(this.maskImage, 200);
        Tooltip.install(maskImage, new Tooltip("Mask frame (banen)"));

        // Frame til morfologisk transformering af maskImage (til banen)
        this.imageViewProperties(this.morphImage, 200);
        Tooltip.install(morphImage, new Tooltip("Morph frame (banen)"));

        // Frame til canny output af morphImage (til banen)
        this.imageViewProperties(this.cannyImage, 200);
        Tooltip.install(cannyImage, new Tooltip("Canny frame (banen)"));


        if (!this.cameraActive) {
            // Start videooptagelse
            this.videoCapture.open(cameraID);

            // Er video streamen tilgængelig?
            if (this.videoCapture.isOpened()) {
                this.cameraActive = true;

                // Fang et frame hvert 33'te ms (30 frame/s)
                Runnable frameGrabber = () -> {
                    // Fang og behandl et enkelt frame
                    Mat frame = grabFrame();
                    Image imageToShow = Utils.mat2Image(frame);
                    updateImageView(originalFrame, imageToShow);
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                // Her sættes framerate (Runnable, initialDelay, framerate, tidsenhed )
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 20, TimeUnit.MILLISECONDS);

                // Opdater knap indhold
                this.button.setText("Stop Kamera");
            } else {
                System.err.println("Umuligt at åbne kamera forbindelse...");
            }
        } else {
            // Kameraet er ikke aktiv på dette punkt
            this.cameraActive = false;
            // Opdatere igen knap indholdet
            this.button.setText("Start Camera");
            // Stop timeren
            this.stopAquisition();
        }
    }

    /**
     * Fang et frame fra the åbnede video stream (hvis der er nogen)
     * Det er her framet skal bearbejdes
     */
    private Mat grabFrame() {
        // Init alt
        Mat frame = new Mat();

        // Tjek om videooptagelse er åben
        if (this.videoCapture.isOpened()) {
            try {
                // Læs det nuværende frame
                this.videoCapture.read(frame);
                // Hvis frame ikke er tomt, behandl det
                if (!frame.empty()) {

                    //TODO koordinater til bolde
                    ArrayList<Point> balls = grabFrameCirkel();
                    for (Point p : balls) {
                        //System.out.println(p.toString());
                    }
                    ArrayList<Point> robotPoints = grabFrameRobotCirkel();
                    for (Point p : robotPoints) {
                        //System.out.println(p.toString());
                    }

                    //grabFrameCirkel()
                    // openCV objekt, brug til HSV konvertiering
                    Mat hsvImage = new Mat();
                    //hsvConverter(frame, hsvImage);
                    hsvConverter(frame, hsvImage);

                    // Slørre billedet
                    Mat blurredImage = new Mat();
                    blurFrame(hsvImage, blurredImage);

                    // Minimum og maximum for RBG værdier
                    //Scalar valuesMin = new Scalar(0,150,108);
                    //Scalar valuesMax = new Scalar(180,255,255);

                    // Henter threshold værdier fra UI
                    // Bemærk: H [0-180], S og V [0-255]
                    Mat hueStart = new Mat();
                    Mat hueLower = new Mat();
                    Mat hueUpper = new Mat();
                    Core.inRange(hsvImage,new Scalar(this.hueStart1.getValue(),this.saturationStart.getValue(), this.valueStart.getValue()),
                            new Scalar(this.hueStop1.getValue(), this.saturationStop.getValue(), this.valueStop.getValue()), hueLower);
                    Core.inRange(hsvImage,new Scalar(this.hueStart2.getValue(),this.saturationStart.getValue(), this.valueStart.getValue()),
                            new Scalar(this.hueStop2.getValue(), this.saturationStop.getValue(), this.valueStop.getValue()), hueUpper);


                    Core.addWeighted(hueLower,1.0, hueUpper, 1.0,0.0, hueStart);

                    Scalar minValues = new Scalar(this.hueStart1.getValue(), this.saturationStart.getValue(), this.valueStart.getValue());
                    Scalar maxValues = new Scalar(this.hueStop1.getValue(), this.saturationStop.getValue(), this.valueStop.getValue());

  //                  Scalar minValues2 = new Scalar(this.hueStart2.getValue(), this.saturationStart2.getValue(), this.valueStart2.getValue());
//                    Scalar maxValues2 = new Scalar(this.hueStop2.getValue(), this.saturationStop2.getValue(), this.valueStop2.getValue());

                    // Tilknyt HSV værdier
                    String valuesToPrint = "Hue range2: " + minValues.val[0] + "-" + maxValues.val[0]
                            + "\tSaturation range2: " + minValues.val[1] + "-" + maxValues.val[1]
                            + "\tValue range2: " + minValues.val[2] + "-" + maxValues.val[2];
                    WhitePingPongDetector.Utils.Utils.onFXThread(this.hsvValuesProp2, valuesToPrint);

                    // Udvælger elementer fra udvalgte RBG/HSV-range og konverterer til hvid farve i nye frame
                    Mat mask = new Mat();
                    Core.inRange(hsvImage, minValues, maxValues, mask);
                    //Core.inRange(hsvImage, minValues2, maxValues2, mask);

                    // Opdater billedet oppe til højre i UI
                    this.updateImageView(this.maskImage, Utils.mat2Image(hueStart));


                    // Morphological operators
                    // Dilate = elementer af størrelse (x*x)pixel (gør objekt større)
                    // Erode  = elementer af størrelse (x*x)pixel (gør objekt mindre)
                    Mat morhpOutput = new Mat();
                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4));

                    // Forstørre elementet x gange
                    Imgproc.dilate(hueStart, morhpOutput, dilateElement); // 1. gang
                    Imgproc.dilate(morhpOutput, morhpOutput, dilateElement); // 2. gang
                    Imgproc.dilate(morhpOutput, morhpOutput, dilateElement); // 2. gang

                    // Tegner streger/kanter af elementer i framet
                    Mat cannyOutput = new Mat();
                    Imgproc.Canny(morhpOutput, cannyOutput, 30, 3);

                    List<MatOfPoint> contours = new ArrayList<>();
                    Mat hierarchy = new Mat();
                    //Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
                    Imgproc.findContours(morhpOutput, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

                    //Mat drawing = Mat.zeros(cannyOutput.size(), CvType.CV_8UC3);

                    List<Moments> mu = new ArrayList<Moments>(contours.size());
                    Imgproc.line(frame, new Point(xstart.getValue(), ystart.getValue()), new Point(xstop.getValue(), ystart.getValue()), new Scalar(0, 100, 100), 4);
                    Imgproc.line(frame, new Point(xstart.getValue(), ystop.getValue()), new Point(xstop.getValue(), ystop.getValue()), new Scalar(0, 100, 100), 4);
                    Imgproc.line(frame, new Point(xstart.getValue(), ystop.getValue()), new Point(xstart.getValue(), ystart.getValue()), new Scalar(0, 100, 100), 4);
                    Imgproc.line(frame, new Point(xstop.getValue(), ystop.getValue()), new Point(xstop.getValue(), ystart.getValue()), new Scalar(0, 100, 100), 4);
                    for (int i = 0; i < contours.size(); i++) {
                        MatOfPoint temp_contour = contours.get(i);
                        MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
                        int contourSize = (int) temp_contour.total();
                        // tegner contours (stregerne i cannyOutput)
                        MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
                        Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize * 0.05, true);
                        MatOfPoint points = new MatOfPoint(approxCurve_temp.toArray());

                        String shape;

                        if (approxCurve_temp.toArray().length == 12) {
                            Point[] aa = approxCurve_temp.toArray(); //TODO her er Points til plus
                            int count = 1;
                            boolean cross = true;
                            for(Point a : aa){
                                if(!((a.x>xstart.getValue() && a.x<xstop.getValue())&&(a.y>ystart.getValue() && a.y<ystop.getValue()))) {
                                    cross = false;
                                }
                                else{
                                    String countString = count++ + "";
                                    System.out.println(a.x + ", " + a.y + " Dette er point!");
                                    Imgproc.putText(frame, countString, a, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                                    Imgproc.circle(frame, a, 1, new Scalar(0,100,100),3, 8, 0);

                                }
                            }
                            if(cross) {
                                mu.add(i, Imgproc.moments(contours.get(i), false));
                                Moments p = mu.get(i);
                                int x = (int) (p.get_m10() / p.get_m00());
                                int y = (int) (p.get_m01() / p.get_m00());
                                String koordCentrum = x - 20 + "," + (y - 20);
                                Imgproc.putText(frame, koordCentrum, new Point(x - 20, y - 20), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                                shape = "plus";
                                System.out.println(shape);
                                Imgproc.drawContours(frame, contours, -1, new Scalar(255, 0, 0), 2);
                            }
                        }

                        Rect rect = Imgproc.boundingRect(points);
                        // Imgproc.drawContours(destinationFrame, sourceFrameWithContours)
                        // Imgproc.drawContours(frame, contours, i, color, 5, 8, hierarchy, 0, new Point());
                        // Tegn firkant, hvis bredde og højde krav er opfyldt
                        if (Math.abs(rect.width) > 400 && Math.abs(rect.height) > 200) {
                            // tegner firkant med (x,y)-koordinater
                            Imgproc.rectangle(frame, new Point(rect.x + 20, rect.y + 20), new Point(rect.x + rect.width - 20, rect.y + rect.height - 20), new Scalar(170, 0, 150, 0), 2);
                            // gem koordinaterne
                            //TODO Koordinater til banen
                            String koord = rect.x + 20 + "," + (rect.y + 20);
                            String koord1 = rect.x + rect.width - 20 + "," + (rect.y + rect.height - 20);
                            String koord2 = rect.x + 20 + "," + (rect.y + rect.height - 20);
                            String koord3 = rect.x + rect.width - 20 + "," + (rect.y);
                            // print koordinaterne ud på billdet
                            /*Imgproc.putText(frame, koord, new Point(rect.x, rect.y), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                            Imgproc.putText(frame, koord1, new Point(rect.x+rect.width, rect.y+rect.height), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                            Imgproc.putText(frame, koord2, new Point(rect.x, rect.y+rect.height), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                            Imgproc.putText(frame, koord3, new Point(rect.x+rect.width, rect.y), Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                            */
                        }

                    }
                    // opdater billedet midt til højre i UI
                    this.updateImageView(this.morphImage, Utils.mat2Image(morhpOutput));
                    // opdater billedet nede til højre i UI
                    this.updateImageView(this.cannyImage, Utils.mat2Image(cannyOutput));
                }
            } catch (Exception e) {
                // log den fangede error
                System.err.println("Exception under billede udarbejdelse" + e);
            }
        }
        return frame;
    }

    /**
     * Konvertere inputFrame til grå farve i outputFrame
     * @param inputFrame
     * @param outputFrame
     * @return outputFrame
     */
    Mat grayConverter(Mat inputFrame, Mat outputFrame) {
        Imgproc.cvtColor(inputFrame, outputFrame, Imgproc.COLOR_BGR2GRAY);
        return outputFrame;
    }

    /**
     * Konverter inputFrame til HSV i outputFrame
     * @param inputFrame
     * @param outputFrame
     * @return outputFrame
     */
    Mat hsvConverter(Mat inputFrame, Mat outputFrame) {
        Imgproc.cvtColor(inputFrame, outputFrame, Imgproc.COLOR_BGR2HSV);
        return outputFrame;
    }

    /**
     * Konverter inputFrame til HSV i outputFrame
     * @param inputFrame
     * @param outputFrame
     * @return outputFrame
     */
    Mat hslConverter(Mat inputFrame, Mat outputFrame) {
        Imgproc.cvtColor(inputFrame, outputFrame, Imgproc.COLOR_BGR2HLS);
        return outputFrame;
    }

    /**
     * Slørre inputFrame med (x*x)pixel og gemmer i outputFrame
     * @param inputFrame
     * @param outputFrame
     * @return outputFrame
     */
    Mat blurFrame(Mat inputFrame, Mat outputFrame) {
        Imgproc.blur(inputFrame, outputFrame, new Size(7,7));
        return outputFrame;
    }

    public ArrayList<Point> grabFrameCirkel() {
        // init alt
        Mat frame = new Mat();

        // tjek om optagelse er åben
        if (this.videoCapture.isOpened()) {
            try {
                // læs det nuværende frame
                this.videoCapture.read(frame);
                // hvis frame ikke er tomt, behandl det
                if (!frame.empty()) {

                    Mat grayImage = new Mat();
                    grayConverter(frame, grayImage);
                    Imgproc.adaptiveThreshold(grayImage, grayImage, 125, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 12);


                    // konverter framet framet til et HSV frame
                    //*Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

                    // reduce noise with a 3x3 kernel
                    Mat detectedEdges = new Mat();
                    Imgproc.medianBlur(grayImage, detectedEdges, 3);

                    //Imgproc.GaussianBlur(grayImage, detectedEdges, new Size(3,3), 2, 2);
                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new  Size(5, 5));

                    // Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
                    // canny detector, with ratio of lower:upper threshold of 3:1
                    int threshold =150;

                    // forstør/mindsk elementer
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);



                    // using Canny's output as a mask, display the result
                    Mat circles = new Mat();
                    Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);
                    //Imgcodecs.imwrite("C:\\Users\\gunnh\\OneDrive\\Desktop\\TestBilleder\\testCanny4.png", detectedEdges);

                    Imgproc.HoughCircles(detectedEdges, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 19, 18, 0, 10);
                    ArrayList<Point> returnValue = new ArrayList<>();

                    for(int i = 0; i < circles.cols(); i++) {
                        double[] c = circles.get(0, i);
                        System.out.println(i + ": " + Math.round(c[0]) + ", " + Math.round(c[1]));
                        Point center = new Point(Math.round(c[0]), Math.round(c[1]));
                        Imgproc.circle(frame, center, 1, new Scalar(0,100,100), 3, 8, 0);
                        int radius = (int) Math.round(c[2]);

                        Imgproc.circle(frame, center, radius, new Scalar(225, 0, 225), 3, 8 ,0);
                        String koord = Math.round(c[0]) + ": " + Math.round(c[1]);
                        Imgproc.putText(frame, koord, center, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                        returnValue.add(center);

                    }

                    System.out.println(circles.cols());


                    this.updateImageView(this.cannyImage2, Utils.mat2Image(detectedEdges));
                    this.updateImageView(this.originalFrame2, Utils.mat2Image(frame));


                return returnValue;
                }
            } catch (Exception e) {
                // Log den fangede error
                System.err.println("Exception under billede udarbejdelse" + e);
            }
        }
        //return frame;
        return null;
    }
    public ArrayList<Point> grabFrameRobotCirkel() {
        // init alt
        Mat frame = new Mat();

        // tjek om optagelse er åben
        if (this.videoCapture.isOpened()) {
            try {
                // læs det nuværende frame
                this.videoCapture.read(frame);
                // hvis frame ikke er tomt, behandl det
                if (!frame.empty()) {

                    Mat grayImage = new Mat();
                    grayConverter(frame, grayImage);
                    Imgproc.adaptiveThreshold(grayImage, grayImage, 125, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 12);


                    // konverter framet framet til et HSV frame
                    //*Imgproc.cvtColor(frame, grayImage, Imgproc.COLOR_BGR2GRAY);

                    // reduce noise with a 3x3 kernel
                    Mat detectedEdges = new Mat();
                    Imgproc.medianBlur(grayImage, detectedEdges, 3);

                    //Imgproc.GaussianBlur(grayImage, detectedEdges, new Size(3,3), 2, 2);
                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new  Size(5, 5));

                    // Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
                    // canny detector, with ratio of lower:upper threshold of 3:1
                    int threshold =150;

                    // forstør/mindsk elementer
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);



                    // using Canny's output as a mask, display the result
                    Mat circles = new Mat();
                    Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);
                    //Imgcodecs.imwrite("C:\\Users\\gunnh\\OneDrive\\Desktop\\TestBilleder\\testCanny4.png", detectedEdges);

                    //Imgproc.HoughCircles(detectedEdges, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 19, 18, 0, 10);


                    this.updateImageView(this.cannyImage2, Utils.mat2Image(detectedEdges));
                    this.updateImageView(this.originalFrame2, Utils.mat2Image(frame));

                    Imgproc.HoughCircles(detectedEdges, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 19, 18, 15, 20);
                    ArrayList<Point> returnValue = new ArrayList<>();
                    for(int i = 0; i < circles.cols(); i++) {
                        double[] c = circles.get(0, i);
                        System.out.println(i + ": " + Math.round(c[0]) + ", " + Math.round(c[1]));
                        Point center = new Point(Math.round(c[0]), Math.round(c[1]));
                        Imgproc.circle(frame, center, 1, new Scalar(0,100,100), 3, 8, 0);
                        int radius = (int) Math.round(c[2]);

                        Imgproc.circle(frame, center, radius, new Scalar(0, 0, 225), 3, 8 ,0);
                        String koord = Math.round(c[0]) + ": " + Math.round(c[1]);
                        Imgproc.putText(frame, koord, center, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                        returnValue.add(center);
                    }

                    System.out.println(circles.cols());

                    this.updateImageView(this.cannyImage2, Utils.mat2Image(detectedEdges));
                    this.updateImageView(this.originalFrame2, Utils.mat2Image(frame));


                    return returnValue;
                }
            } catch (Exception e) {
                // Log den fangede error
                System.err.println("Exception under billede udarbejdelse" + e);
            }
        }
        //return frame;
        return null;
    }

    private ArrayList<Point> grabFrameCirkel2() {
        // init alt
        Mat frame = new Mat();

        // tjek om optagelse er åben
        if (this.videoCapture.isOpened()) {
            try {
                // læs det nuværende frame
                this.videoCapture.read(frame);
                // hvis frame ikke er tomt, behandl det
                if (!frame.empty()) {

                    Mat hsvImage = new Mat();
                    hsvConverter(frame, hsvImage);

                    // Minimum og maximum for RBG værdier
                    Scalar valuesMin = new Scalar(0,0,210);
                    Scalar valuesMax = new Scalar(180,78,255);

                    // Udvælger elementer fra udvalgte RBG/HSV-range og konverterer til hvid farve i nye frame
                    Mat mask = new Mat();
                    Core.inRange(hsvImage, valuesMin, valuesMax, mask);

                    this.updateImageView(this.maskImage2, Utils.mat2Image(mask));

                    Imgproc.adaptiveThreshold(mask, mask, 125, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 12);

                    // reduce noise with a 3x3 kernel
                    Mat detectedEdges = new Mat();
                    //Imgproc.medianBlur(hsvImage, detectedEdges, 3);


                    //Imgproc.GaussianBlur(grayImage, detectedEdges, new Size(3,3), 2, 2);
                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new  Size((2*2)+1, (2*2)+1));

                    // Imgproc.blur(grayImage, detectedEdges, new Size(3, 3));
                    // canny detector, with ratio of lower:upper threshold of 3:1

                    // forstør/mindsk elementer
                    Imgproc.erode(mask, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);


                    // using Canny's output as a mask, display the result
                    int threshold =150;
                    Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);

                    Mat circles = new Mat();
                    Imgproc.HoughCircles(detectedEdges, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 19, 18, 0, 10);

                    ArrayList<Point> returnValue = new ArrayList<>();
                    for(int i = 0; i < circles.cols(); i++) {
                        double[] c = circles.get(0, i);
                        //System.out.println(i + ": " + Math.round(c[0]) + ", " + Math.round(c[1]));
                        Point center = new Point(Math.round(c[0]), Math.round(c[1]));
                        Imgproc.circle(frame, center, 1, new Scalar(0,100,100), 3, 8, 0);
                        int radius = (int) Math.round(c[2]);
                        returnValue.add(center);
                        Imgproc.circle(frame, center, radius, new Scalar(225, 0, 225), 3, 8 ,0);
                        String koord = Math.round(c[0]) + ": " + Math.round(c[1]);
                        Imgproc.putText(frame, koord, center, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                    }

                    System.out.println(circles.cols());

                    this.updateImageView(this.cannyImage2, Utils.mat2Image(detectedEdges));
                    this.updateImageView(this.originalFrame2, Utils.mat2Image(frame));
                    return returnValue;

                }
            } catch (Exception e) {
                // Log den fangede error
                System.err.println("Exception under billede udarbejdelse" + e);
            }
        }
        return null;
        //return frame;
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

    /**
     * En timer til at stoppe videooptagelse
     */
    private void stopAquisition() {
        if (this.timer!=null && !this.timer.isShutdown()) {
            try {
                // stop timeren
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                System.err.println("Exception ved stopning af fram opfanigningen, forsøger at frigive kamera nu...: " + e);
            }
        }
        if (this.videoCapture.isOpened()) {
            // frigiv kameraet
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
