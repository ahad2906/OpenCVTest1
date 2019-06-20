package visualisering;

import WhitePingPongDetector.Controller2;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import org.opencv.core.Point;
import visualisering.Objects.Bold;
import visualisering.Objects.Kryds;
import visualisering.Objects.Mål;
import visualisering.Objects.Robot;
import visualisering.Space.Grid;
import visualisering.Space.Path;
import visualisering.Space.Vector2D;
import visualisering.View.Colors;
import visualisering.View.Kort;
import visualisering.View.Text;
import websocket.RobotController;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class VisuController {
    private final int UPDATETIME = 50;
    private int nbOfBalls = 1;
    Kort map;
    Path path;
    private long lastTime;
    private Controller2 otherController;
    private RobotController robotController;
    private boolean started = false, wasAtGoal;
    private AnimationTimer animationTimer;
    private Text timerTxt;
    private long time, prevTime;
    private boolean doCount;
    private Thread timer;

    private int tries;

    public VisuController(Controller2 other) {
        otherController = other;
    }

    public Canvas createView(float width) {
        this.map = new Kort(width);
        return map.getCanvas();
    }

    public void start() {
        if (started) return;
        started = true;

        //Grid
        Grid grid = new Grid(map.getWIDTH(), map.getHEIGHT());
        Point[] points = otherController.getField();
        Vector2D[] vA = pointToVector(points);

        grid.setScale(vA);
        grid.setColor(Colors.GRID);
        map.setGrid(grid);

        //Laver animationTimer text
        timerTxt = new Text();
        timerTxt.setColor(Color.WHITE);
        map.addDebugObject(timerTxt);

        //RobotController
        /*robotController = new RobotController(grid);
        robotController.start();*/

        //Skab objekterne
        createObjects(grid);
        //createPath();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //Runs every UPDATETIME
                long cur = System.currentTimeMillis();
                if (cur - lastTime > UPDATETIME) {
                    try {
                        //Updating positions
                        updatePositions();

                        //PathFinding
                        if (robotController != null && !robotController.isTargeting()) {

                            createPath();
                            if (path.size() > 1) {
                                robotController.target(path);
                            }
                        }

                        //Draw map
                        map.update();

                        lastTime = System.currentTimeMillis();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        animationTimer.start();
    }

    private void updatePositions() {
        Grid grid = map.getGrid();

        //Fetch points
        List<Vector2D> robotPos = new ArrayList<>();
        List<Point> robotPoints = otherController.getRobot();
        List<Vector2D> ballPos = new ArrayList<>();
        List<Point> ballPoints = otherController.getBalls();

        //Update balls
        if (map.getRobot() != null && ballPoints != null && ballPoints.size() >= nbOfBalls) {
            for (Point p : ballPoints) {
                //Oversætter og skalerer punktet til en Vector2D
                Vector2D v = grid.translatePos(new Vector2D((float) p.x, (float) p.y));
                //Hvis bolden er indenfor banen tilføjes denne til listen
                if (v.getY() < grid.HEIGHT && v.getY() > 0 && v.getX() < grid.WIDTH && v.getX() > 0
                        && grid.translateLengthToMilimeters(Vector2D.Distance(v, map.getRobot().getPos())) > 120) {
                    ballPos.add(v);
                }
            }
            map.setBalls(createBalls(ballPos.toArray(new Vector2D[0]), grid));
        }

        //Update robot
        if (robotPoints != null && robotPoints.size() == 2 && !robotPoints.contains(null)) {
            for (Point p : robotPoints) {
                robotPos.add(new Vector2D((float) p.x, (float) p.y));
            }
            map.setRobot(updateRobot(robotPos.toArray(new Vector2D[0]), grid));
        }

        //Update cross
        List<Point> cPoints = Arrays.asList(otherController.getCross());
        if (!cPoints.contains(null) && cPoints.size() == 12) {
            map.setCross(updateCross(pointToVector(cPoints.toArray(new Point[0])), grid));
        }
    }

    private void createPath() {
        if (path != null) map.removeDebugObject(path);

        Bold ball = null;
        Bold[] balls = map.getBalls().toArray(new Bold[0]);

        if (balls.length <= 0) {
            tries++;
            if (tries > 20) {
                if (wasAtGoal) {
                    robotController.close();
                    doCount = false;
                } else {
                    path.setTarget(map.getLeftgoal());
                }
            }
            return;
        }
        tries = 0;

        //Finder den korteste sti
        Path[] paths = new Path[balls.length];
        float min = Float.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < balls.length; i++) {
            paths[i] = new Path(map);
            paths[i].setTarget(balls[i]);
            float dist = paths[i].getLength();
            if (dist < min) {
                min = dist;
                index = i;
            }
        }

        //Sætter den korteste sti til path field'et
        path = paths[index];
        path.setColor(Colors.PATH);
        map.addDebugObject(path);

        System.out.println("Path lenght in mm is: " + map.getGrid().translateLengthToMilimeters(path.getLength()));
    }

    private void createObjects(Grid grid) {
        /*//Nodes
        Node[][] nodes = new Node[(int)grid.CELLS_HOR][(int)grid.CELLS_VER];
        for (int i = 0; i < nodes.length; i++){
            for (int j = 0; j < nodes[i].length; j++){
                float x, y;
                x = grid.CELL_SPACING.getX()*i + grid.CELL_SPACING.getX()/2;
                y = grid.CELL_SPACING.getY()*j + grid.CELL_SPACING.getY()/2;
                nodes[i][j] = new Node(x, y);
                if (Debug.DEBUG){
                    nodes[i][j].setColor(Colors.NODE);
                    map.addDebugObject(nodes[i][j]);
                }
            }
        }
        map.setNodes(nodes);*/

        //Goals
        Mål goal = new Mål();
        goal.setPos(grid.getLeftCenterPos());
        goal.setWidth(5);
        goal.setHeight(grid.translateLengthToScale(grid.GOAL_LEFT));
        goal.setColor(Colors.GOAL);
        map.setLeftgoal(goal);

        goal = new Mål();
        goal.setPos(grid.getRightCenterPos());
        goal.setWidth(5);
        goal.setHeight(grid.translateLengthToScale(grid.GOAL_RIGHT));
        goal.setColor(Colors.GOAL);
        map.setRightgoal(goal);
    }

    private Set<Bold> createBalls(Vector2D[] vA, Grid grid) {
        Bold[] balls = new Bold[vA.length];
        for (int i = 0; i < vA.length; i++) {
            balls[i] = new Bold();
            float diameter = grid.translateLengthToScale(40);
            balls[i].setWidth(diameter);
            balls[i].setHeight(diameter);
            balls[i].setColor(Colors.BALL);
            balls[i].setPos(vA[i]);
        }
        return new HashSet<>(Arrays.asList(balls));
    }

    private Robot updateRobot(Vector2D[] vA, Grid grid) {
        //Oversætter positionerne
        vA = grid.translatePositions(vA);

        Robot robot = map.getRobot();

        //Hvis der ikkke findes nogen instans af robotten, så lav en
        if (robot == null) {
            robot = new Robot();
            //Farven
            robot.setColor(Colors.ROBOT);

            //Finder robotens størrelse
            float size = Vector2D.Distance(vA[0], vA[1]);
            robot.setWidth(size);
            robot.setHeight(size);
        }

        robot.setFrontAndBack(vA);

        return robot;
    }

    private Kryds updateCross(Vector2D[] vA, Grid grid) {
        vA = grid.translatePositions(vA);

        Kryds cross = map.getCross();

        if (cross == null) {
            cross = new Kryds();
            cross.setColor(Colors.OBSTACLE);
        }

        for (int j = 0; j < 2; j++) {
            if (!(Vector2D.Distance(vA[0], vA[1]) > Vector2D.Distance(vA[1], vA[4]))) {
                Vector2D[] newVa = Arrays.copyOf(vA, vA.length);
                newVa[0] = vA[11];
                for (int i = 1; i < vA.length; i++) {
                    newVa[i] = vA[i - 1];
                }
                vA = newVa;
            }
        }

        //Finder de to horizontale punkter
        Vector2D[] hor = {
                Vector2D.Middle(vA[2], vA[3]),
                Vector2D.Middle(vA[8], vA[9])
        };

        //Finder de to vertikale punkter
        Vector2D[] ver = {
                Vector2D.Middle(vA[5], vA[6]),
                Vector2D.Middle(vA[0], vA[11])
        };

        //Finder midten
        Vector2D position = Vector2D.Middle(hor[0], hor[1]);
        //Finder vinklen
        float rotation = Vector2D.Angle(hor[0], hor[1]);
        //Finder højde og bredde
        float width = Vector2D.Distance(hor[0], hor[1]);
        float height = Vector2D.Distance(ver[0], ver[1]);

        //Tjekker om krydset er rigtigt
        int margin = 5;
        //Hvis krydset er forvringet eller krydsets størrelse er for stort er det ikke et kryds
        if (((width >= height + margin || width <= height - margin) &&
                ((width + height) / 2 > grid.translateLengthToScale(250) ||
                        (width + height) / 2 < grid.translateLengthToScale(180))) ||
                //Hvis krydset er for tæt på robotten (under 150mm)
                Vector2D.Distance(position, map.getRobot().getPos()) < grid.translateLengthToScale(150))
            return cross;

        /*if (cross.getPos() != null &&
                //Hvis krydset har flyttet sig mere end 150mm
                Vector2D.Distance(cross.getPos(), position) > grid.translateLengthToScale(150)){
            return cross;
        }*/

        //Sætter hjørnerne
        cross.setCorners(vA);

        //Sætter parameterne
        cross.setWidth(width);
        cross.setHeight(height);
        cross.setPos(position);
        cross.setRotation(rotation);

        //cross.setPoints(vA, 0, grid.translateLengthToScale(250));

        return cross;
    }

    private Vector2D[] pointToVector(Point[] points) {
        Vector2D[] vA = new Vector2D[points.length];
        for (int i = 0; i < vA.length; i++) {
            vA[i] = new Vector2D((float) points[i].x, (float) points[i].y);
        }
        return vA;
    }

    public void stopRobot() {
        robotController.close();
        robotController = null;
        doCount = false;
    }

    public void startRobot() {
        robotController = new RobotController(map.getGrid());
        robotController.start();
        robotController.setRobot(map.getRobot());
        doCount = true;

        timer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (doCount) {
                    //Set timer text
                    long cur = System.currentTimeMillis();
                    if (prevTime != 0) {
                        time += cur - prevTime;

                        long millis, seconds, minutes;
                        minutes = TimeUnit.MILLISECONDS.toMinutes(time);
                        seconds = TimeUnit.MILLISECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(minutes);
                        millis = time - TimeUnit.SECONDS.toMillis(seconds) - TimeUnit.MINUTES.toMillis(minutes);
                        timerTxt.setText(minutes+"m "+seconds+"s "+millis);
                    }
                    prevTime = cur;

                }
            }
        });

        timer.start();
    }

    public void close() {
        animationTimer.stop();
        started = false;
        stopRobot();
    }
}
