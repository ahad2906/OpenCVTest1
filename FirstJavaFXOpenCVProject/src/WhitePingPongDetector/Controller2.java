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
import visualisering.VisuController;

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
    private Button button3;
    @FXML
    private ImageView originalFrame;
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

    //Her er målene til de diverse objekter til systemet/banen/boldene/robotten

    //double camHeight = 171.9; // SKAL måles hver gang der testes med nyt opstilling
    double camHeight = 167.8; // SKAL måles hver gang der testes med nyt opstilling

    double objectHeightBolde = 3.9;//3.7-4 cm

    double objectHeightKors = 3.3;
    
    double objectHeightBaneHjørne = 7.1;

    //double objectHeightRobotGreen = 29.6; // skal helst måles hver gang der testes med nyt opstilling
    double objectHeightRobotGreen = 30.4; // skal helst måles hver gang der testes med nyt opstilling

    //double objectHeightRobotBlue = 30.7; // skal helst måles hver gang der testes med nyt opstilling
    double objectHeightRobotBlue = 30.2; // skal helst måles hver gang der testes med nyt opstilling


    private Point[] field = new Point[4];

    private Point[] cross = new Point[12];

    private ArrayList<Point> balls;

    private ArrayList<Point> robotPoints;

    public ArrayList<Point> getRobot(){
        return robotPoints;
    }

    public ArrayList<Point> getBalls(){
        return balls;
    }

    public Point[] getField() {
        return field;
    }

    public void setField(Point[] field) {
        this.field = field;
    }

    public Point[] getCross() {
        return cross;
    }

    public void setCross(Point[] cross) {
        this.cross = cross;
    }

    private VisuController visuController;

    Point centerpoint;

    public void addVisuController(VisuController visuController) {
        this.visuController = visuController;
    }

    public void update() {
        Mat frame = grabFrame();
        Image imageToShow = Utils.mat2Image(frame);
        updateImageView(originalFrame, imageToShow);
    }

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
        this.imageViewProperties(this.maskImage, 300);
        Tooltip.install(maskImage, new Tooltip("Mask frame (banen)"));

        // Frame til morfologisk transformering af maskImage (til banen)
        this.imageViewProperties(this.morphImage, 300);
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

                // Fang et frame hvert x'te ms (x frame/s)
                // Fang og behandl et enkelt frame
                Runnable frameGrabber = this::update;

                this.timer = Executors.newSingleThreadScheduledExecutor();
                // Her sættes framerate (Runnable, initialDelay, framerate, tidsenhed )
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 1, TimeUnit.MILLISECONDS);

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

    @FXML
    protected void startTracking(){
        visuController.start();
    }

    @FXML
    protected void startRobot(){
        String start = "Start Robot", stop = "Stop Robot";
        if (button3.getText().equals(start)){
            visuController.startRobot();
            button3.setText(stop);
        }
        else {
            visuController.stopRobot();
            button3.setText(start);
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
                    //Find punkter til bolde/bander/Robot/Kors
                    balls = grabFrameCirkel();
                    robotPoints = grabFrameRobotCirkel();

                    // openCV objekt, brug til HSV konvertiering
                    Mat hsvImage = new Mat();
                    hsvConverter(frame, hsvImage);

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

                    // Tilknyt HSV værdier
                    String valuesToPrint = "Hue range2: " + minValues.val[0] + "-" + maxValues.val[0]
                            + "\tSaturation range2: " + minValues.val[1] + "-" + maxValues.val[1]
                            + "\tValue range2: " + minValues.val[2] + "-" + maxValues.val[2];
                    WhitePingPongDetector.Utils.Utils.onFXThread(this.hsvValuesProp2, valuesToPrint);

                    // Udvælger elementer fra udvalgte RBG/HSV-range og konverterer til hvid farve i nye frame
                    Mat mask = new Mat();
                    Core.inRange(hsvImage, minValues, maxValues, mask);

                    // Opdater billedet oppe til højre i UI
                    this.updateImageView(this.maskImage, Utils.mat2Image(hueStart));


                    // Morphological operators
                    // Dilate = elementer af størrelse (x*x)pixel (gør objekt større)
                    // Erode  = elementer af størrelse (x*x)pixel (gør objekt mindre)
                    Mat morhpOutput = new Mat();
                    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4));
                    Mat dilateElement2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4));
                    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(4, 4));

                    // Forstørre elementet x gange
                    Imgproc.dilate(hueStart, morhpOutput, dilateElement); // 1. gang
                    Imgproc.erode(morhpOutput, morhpOutput, erodeElement); // 2. gang
                    Imgproc.dilate(morhpOutput, morhpOutput, dilateElement); // 2. gang
                    Imgproc.dilate(morhpOutput, morhpOutput, dilateElement2); // 2. gang
                    Imgproc.erode(morhpOutput, morhpOutput, erodeElement); // 2. gang



                    List<MatOfPoint> contours = new ArrayList<>();
                    Mat hierarchy = new Mat();
                    Imgproc.findContours(morhpOutput, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);


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


                        if (approxCurve_temp.toArray().length == 12) {
                            Point[] aa = approxCurve_temp.toArray();
                            Point[] projectedPointsCross = new Point[12];
                            centerpoint = new Point(frame.width()/2, frame.height()/2);

                            for (int k = 0; k<aa.length; k++){
                                Point p = aa[k];
                                projectedPointsCross[k] = projectPoint(camHeight,objectHeightKors,centerpoint,p);
                            }
                            cross = projectedPointsCross;
                            int count = 1;
                            for(Point a : aa){
                                if(((a.x>xstart.getValue() && a.x<xstop.getValue())&&(a.y>ystart.getValue() && a.y<ystop.getValue()))) {

                                    String countString = count++ + "";
                                    Imgproc.putText(frame, countString, a, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                                    Imgproc.circle(frame, a, 1, new Scalar(0,100,100),3, 8, 0);

                                }
                            }

                        }
                        if (approxCurve_temp.toArray().length == 4) {
                            Rect rect = Imgproc.boundingRect(points);
                            // Tegn firkant, hvis bredde og højde krav er opfyldt
                            if (Math.abs(rect.width) > 400 && Math.abs(rect.height) > 200) {
                                Point[] as = approxCurve_temp.toArray();

                                Point[] projectedPointsField = new Point[4];
                                centerpoint = new Point(frame.width()/2, frame.height()/2);

                                for (int k = 0; k<as.length; k++){
                                    Point p = as[k];
                                    projectedPointsField[k] = projectPoint(camHeight,objectHeightBaneHjørne,centerpoint,p);
                                }

                                field = projectedPointsField; //Giver field fieldet sine koordinater
                                for(Point aaa : as) {
                                    String countString = aaa.toString();
                                    Imgproc.putText(frame, countString, aaa, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 255, 0), 2);
                                    Imgproc.circle(frame, aaa, 5, new Scalar(0, 225, 100), 3, 8, 0);
                                }
                            }
                        }

                    }
                    Imgproc.drawContours(frame, contours, -1, new Scalar(255, 0, 0), 2, Imgproc.CHAIN_APPROX_SIMPLE);

                    //Tregn bolde
                    for (Point p : balls) {
                        int radius = 10;
                        Imgproc.circle(frame, p, radius, new Scalar(225, 0, 225), 3, 8 ,0);
                        String koord = p.toString();
                        //Imgproc.putText(frame,koord, p, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                        Imgproc.circle(frame, p, 1, new Scalar(0,100,100), 3, 8, 0);
                    }
                    //tegn robotpunter
                    Point p  = robotPoints.get(0);
                    int radius = 20;
                    Imgproc.circle(frame, p, radius, new Scalar(0, 255, 255), 3, 8 ,0);
                    String koord = p.toString();
                    //Imgproc.putText(frame,koord, p, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                    Imgproc.circle(frame, p, 1, new Scalar(0,100,100), 3, 8, 0);
                    Point p1  = robotPoints.get(1);
                    int radius1 = 20;
                    Imgproc.circle(frame, p1, radius1, new Scalar(0, 0, 225), 3, 8 ,0);
                    String koord1 = p1.toString();
                    //Imgproc.putText(frame,koord1, p1, Imgproc.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 0), 2);
                    Imgproc.circle(frame, p1, 1, new Scalar(0,100,100), 3, 8, 0);



                    // opdater billedet midt til højre i UI
                    this.updateImageView(this.morphImage, Utils.mat2Image(morhpOutput));
                }
            } catch (Exception e) {
                // log den fangede error
                System.err.println("Exception under billede udarbejdelse" + e);
            }
        }
        return frame;
    }

    /**
     *
     * @param camHeight = kameraets højde i cm fra jorden
     * @param objectHeight = objektets højde i cm fra jorden
     * @param centerPoint = centrum af kameraet  ((frame.width/2), (frame.heigth/2))
     * @param projectPoint = punktet til objektet på framet, som skal projekteres
     * @return
     */
    private Point projectPoint(double camHeight, double objectHeight, Point centerPoint, Point projectPoint) {

        //camHeight og objectHeight er angivet i pxel, så de konverteres
        camHeight *= 2.8; // cm til pixel - udregnet ved evt.: beregne pixel afstand på bande længde
        objectHeight *= 2.8;

        double grundlinje = Math.sqrt(Math.pow(centerPoint.x-projectPoint.x, 2)+Math.pow(centerPoint.y-projectPoint.y, 2));
        double vinkelProjectPoint = Math.toDegrees(Math.asin(camHeight/(Math.sqrt(Math.pow(camHeight,2)+Math.pow(grundlinje, 2)))));

        double robotTopVinkel = 90-vinkelProjectPoint;
        double projectLength = (objectHeight*Math.sin(Math.toRadians(robotTopVinkel)))/Math.sin(Math.toRadians(vinkelProjectPoint));
        double grundLinje2 = grundlinje-projectLength;
        double strengthFactor = grundLinje2/grundlinje;
        double xChange = centerPoint.x-projectPoint.x;
        double yChange = centerPoint.y-projectPoint.y;
        Point newPoint = new Point(centerPoint.x-xChange*strengthFactor, centerPoint.y-yChange*strengthFactor);

        return newPoint;
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

                    // reduce noise with a 3x3 kernel
                    Mat detectedEdges = new Mat();
                    Imgproc.medianBlur(grayImage, detectedEdges, 3);

                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new  Size(1, 1));

                    // canny detector, with ratio of lower:upper threshold of 3:1
                    int threshold =150;

                    // forstør/mindsk elementer
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);

                    Imgproc.GaussianBlur(grayImage, detectedEdges, new Size(3,3), 2, 2);


                    // using Canny's output as a mask, display the result
                    Mat circles = new Mat();
                    Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);

                    Imgproc.HoughCircles(detectedEdges, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 19, 18, 0, 10);
                    ArrayList<Point> returnValue = new ArrayList<>();

                    centerpoint = new Point(frame.width()/2, frame.height()/2);

                    for(int i = 0; i < circles.cols(); i++) {
                        double[] c = circles.get(0, i);
                        Point center = new Point(Math.round(c[0]), Math.round(c[1]));

                        Point temp = projectPoint(camHeight,objectHeightBolde, centerpoint, center);
                        returnValue.add(temp);
                    }

                    return returnValue;
                }
            } catch (Exception e) {
                // Log den fangede error
                System.err.println("Exception under billede udarbejdelse" + e);
            }
        }
        return null;
    }


    public ArrayList<Point> grabFrameRobotCirkel() {

        ArrayList<Point> points = new ArrayList<>();

        Point blue = grabFrameRobotPoint(new Scalar(100,150,0), new Scalar(140,255,255));
        Point blueProjected = projectPoint(camHeight,objectHeightRobotBlue,centerpoint, blue);
        Point green = grabFrameRobotPoint(new Scalar(40, 40, 0 ), new Scalar(75, 255, 255));
        Point greenProjected = projectPoint(camHeight,objectHeightRobotGreen,centerpoint, green);

        points.add(blueProjected);
        points.add(greenProjected);


        return points;

    }


    public Point grabFrameRobotPoint(Scalar scalarStart, Scalar scalarStop) {
        // init alt
        Mat frame = new Mat();

        // tjek om optagelse er åben
        if (this.videoCapture.isOpened()) {
            try {
                // læs det nuværende frame
                this.videoCapture.read(frame);
                // hvis frame ikke er tomt, behandl det
                if (!frame.empty()) {
                    // reduce noise with a 3x3 kernel
                    Mat hsv = new Mat();
                    hsvConverter(frame, hsv);
                    Mat grayImage = new Mat();
                    Core.inRange(hsv, scalarStart, scalarStop, grayImage);
                    Mat detectedEdges = new Mat();

                    Imgproc.adaptiveThreshold(grayImage, grayImage, 125, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 11, 12);

                    Imgproc.medianBlur(grayImage, detectedEdges, 3);

                    Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_CROSS, new  Size(5, 5));

                    int threshold =150;

                    // forstør/mindsk elementer
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);
                    Imgproc.erode(detectedEdges, detectedEdges, kernel);
                    Imgproc.dilate(detectedEdges, detectedEdges, kernel);

                    Imgproc.GaussianBlur(grayImage, detectedEdges, new Size(3,3), 2, 2);



                    // using Canny's output as a mask, display the result
                    Mat circles = new Mat();
                    Imgproc.Canny(detectedEdges, detectedEdges, threshold, threshold * 3);

                    Imgproc.HoughCircles(detectedEdges, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 10, 19, 18, 15, 20);
                    Point returnValue;
                    double[] c = circles.get(0, 0);
                    Point center = new Point(Math.round(c[0]), Math.round(c[1]));
                    returnValue = center;

                    return returnValue;
                }
            } catch (Exception e) {
                // Log den fangede error
                System.err.println("Exception under billede udarbejdelse" + e);
            }
        }
        return null;
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
