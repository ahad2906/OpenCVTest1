package visualisering;

import WhitePingPongDetector.Controller2;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
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
import websocket.RobotController;

import java.util.*;

public class VisuController {
    private final int UPDATETIME = 50;
    private int nbOfBalls = 1;
    Kort map;
    Path path;
    private long lastTime;
    private Controller2 otherController;
    private RobotController robotController;
    private boolean started = false;
    private AnimationTimer timer;

    private int tries;

    public VisuController(Controller2 other){
        otherController = other;
    }

    public Canvas createView(float width){
        this.map = new Kort(width);
        return map.getCanvas();
    }

    public void start(){
        if (started) return;
        started = true;

        //Grid
        Grid grid = new Grid(map.getWIDTH(), map.getHEIGHT());
        Point[] points = otherController.getField();
        Vector2D[] vA = pointToVector(points);

        grid.setScale(vA);
        grid.setColor(Colors.GRID);
        map.setGrid(grid);

        //RobotController
        robotController = new RobotController(grid);
        robotController.start();

        //Skab objekterne
        createObjects(grid);
        //createPath();

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                //Runs every UPDATETIME
                long cur = System.currentTimeMillis();
                if (cur - lastTime > UPDATETIME) {
                    try {
                        lastTime = cur;

                        //Updating positions
                        updatePositions();

                        //PathFinding
                        if (!robotController.isTargeting()) {
                            createPath();
                            if (path.size() > 1) {
                                robotController.target(path);
                            }
                        }

                        //Draw map
                        map.update();
                    }
                    catch (NullPointerException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        timer.start();
    }

    private void updatePositions(){
        Grid grid = map.getGrid();

        //Fetch points
        List<Vector2D>  robotPos = new ArrayList<>();
        List<Point> robotPoints = otherController.getRobot();
        List<Vector2D> ballPos = new ArrayList<>();
        List<Point> ballPoints = otherController.getBalls();

        //Update balls
        if (map.getRobot() != null && ballPoints != null && ballPoints.size() >= nbOfBalls){
            for (Point p : ballPoints){
                //Oversætter og skalerer punktet til en Vector2D
                Vector2D v = grid.translatePos(new Vector2D((float)p.x, (float)p.y));
                //Hvis bolden er indenfor banen tilføjes denne til listen
                if (v.getY() < grid.HEIGHT && v.getY() > 0 && v.getX() < grid.WIDTH && v.getX() > 0
                        && grid.translateLengthToMilimeters(Vector2D.Distance(v, map.getRobot().getPos())) > 60){
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
        if (!cPoints.contains(null) && cPoints.size() ==12){
            map.setCross(updateCross(pointToVector(cPoints.toArray(new Point[0])), grid));
        }
    }

    private void createPath() {
        if (path != null) map.removeDebugObject(path);

        path = new Path(map.getRobot().getPos(), map);
        path.setColor(Colors.PATH);

        Bold ball = null;
        Set<Bold> balls = map.getBalls();

        if (balls == null || balls.size() <= 0){
            tries++;
            if (tries > 9){
                path.setTarget(map.getLeftgoal());
            }
            return;
        }

        tries = 0;
        float min = Float.MAX_VALUE;
        for (Bold b : balls){
            float dist = Vector2D.Distance(b.getPos(), path.getLast());
            if (dist < min){
                min = dist;
                ball = b;
            }
        }
        if (ball != null) {
            path.setTarget(ball);
        }
        map.addDebugObject(path);

        System.out.println("Path lenght in mm is: "+map.getGrid().translateLengthToMilimeters(path.getLength()));
    }

    private void createObjects(Grid grid){
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

    private Set<Bold> createBalls(Vector2D[] vA, Grid grid){
        Bold[] balls = new Bold[vA.length];
        for (int i = 0; i < vA.length; i++){
            balls[i] = new Bold();
            float diameter = grid.translateLengthToScale(40);
            balls[i].setWidth(diameter);
            balls[i].setHeight(diameter);
            balls[i].setColor(Colors.BALL);
            balls[i].setPos(vA[i]);
        }
        return new HashSet<>(Arrays.asList(balls));
    }

    private Robot updateRobot(Vector2D[] vA, Grid grid){
        //Oversætter positionerne
        vA = grid.translatePositions(vA);

        Robot robot = map.getRobot();

        //Hvis der ikkke findes nogen instans af robotten, så lav en
        if(robot == null){
            robot = new Robot();
            //Farven
            robot.setColor(Colors.ROBOT);

            //Finder robotens størrelse
            float size = Vector2D.Distance(vA[0], vA[1]);
            robot.setWidth(size);
            robot.setHeight(size);

            robotController.setRobot(robot);
        }

        robot.setFrontAndBack(vA);

        return robot;
    }

    private Kryds updateCross(Vector2D[] vA, Grid grid){
        vA = grid.translatePositions(vA);

        Kryds cross = map.getCross();

        if (cross == null){
            cross = new Kryds();
            cross.setColor(Colors.OBSTACLE);
        }

        cross.setPoints(vA, 0, grid.translateLengthToScale(250));

        /*
        //Beregner ændringen siden sidste check
        float pos_change = Vector2D.Distance(cross.getPos(), position);
        //Hvis den er for stor eller for lille ændres den ikke
        if (pos_change > 2 && pos_change < 20){
            cross.setPos(position);

            //Beregner ændringen i vinkeln siden sidst
            float rot_change = cross.getRotation()-rotation;
            //Hvis den møder kriterierne ændres dennne
            if (rot_change < 30 && rot_change > -30){
                cross.setRotation(rotation);
            }
        }*/

        return cross;
    }

    private Vector2D[] pointToVector(Point[] points){
        Vector2D[] vA = new Vector2D[points.length];
        for (int i = 0; i < vA.length; i++){
            vA[i] = new Vector2D((float)points[i].x, (float)points[i].y);
        }
        return vA;
    }

    public void stopRobot() {
        robotController.close();

    }

    //TODO: næste gang, tænd sluk visu samt robot
    public void startRobot(){
        robotController.start();
    }

    public void close(){
        timer.stop();
        started = false;
        stopRobot();
    }
}
