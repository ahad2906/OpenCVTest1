package visualisering;

import WhitePingPongDetector.Controller2;
import javafx.animation.AnimationTimer;
import org.opencv.core.Point;
import visualisering.Objects.*;
import visualisering.Space.Grid;
import visualisering.Space.Node;
import visualisering.Space.Path;
import visualisering.Space.Vector2D;
import visualisering.View.Colors;
import visualisering.View.Kort;
import websocket.RobotController;
import websocket.RobotSocket;

import java.io.IOException;
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

    public VisuController(Controller2 other){
        otherController = other;
    }

    public void createMap(Kort map){
        this.map = map;
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
        //robotController.start();

        //Skab objekterne
        createObjects(grid);
        //createPath();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                //Runs every UPDATETIME
                long cur = System.currentTimeMillis();
                if (cur - lastTime > UPDATETIME) {
                    lastTime = cur;

                    otherController.update();
                    //TODO: fetch data;
                    updatePositions();

                    //Draw map
                    map.update();
                }
            }
        }.start();
    }

    private void updatePositions(){
        Grid grid = map.getGrid();

        //Fetch points
        List<Vector2D>  robotPos = new ArrayList<>();
        List<Point> robotPoints = otherController.getRobot();
        List<Vector2D> ballPos = new ArrayList<>();
        List<Point> ballPoints = otherController.getBalls();

        //Update balls
        if (ballPoints.size() >= nbOfBalls){
            for (Point p : ballPoints){
                ballPos.add(new Vector2D((float)p.x, (float)p.y));
            }
            map.setBalls(createBalls(ballPos.toArray(new Vector2D[0]), grid));
        }

        //Update robot
        if (robotPoints.size() == 2 && !robotPoints.contains(null)) {
            for (Point p : robotPoints) {
                robotPos.add(new Vector2D((float) p.x, (float) p.y));
            }
            map.setRobot(updateRobot(robotPos.toArray(new Vector2D[0]), grid));
        }

        //Update cross
        Point[] cPoints = otherController.getCross();
        if (cPoints != null && cPoints.length >=12){
            map.setCross(updateCross(pointToVector(cPoints), grid));
        }
    }

    private void createPath() {
        if (path != null) map.removeDebugObject(path);

        path = new Path(map.getRobot().getPos());
        path.setColor(Colors.PATH);

        Set<Bold> balls = new HashSet<>(map.getBalls());
        Bold ball = null;
        float min = Float.MAX_VALUE;
        int size = balls.size();
        for (int i = 0; i < size; i++){
            for (Bold b : balls){
                float dist = Vector2D.Distance(b.getPos(), path.getLast());
                if (dist < min){
                    min = dist;
                    ball = b;
                }
            }
            if (ball != null) {
                min = Float.MAX_VALUE;
                balls.remove(ball);
                path.addPoint(ball.getPos());
                ball = null;
            }
        }
        map.addDebugObject(path);

        System.out.println("Path lenght is: "+path.getLenght());
        System.out.println("Path lenght in mm is: "+map.getGrid().translateLengthToMilimeters(path.getLenght()));
    }

    private void createObjects(Grid grid){
        //Nodes
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
        map.setNodes(nodes);

        //The Robot:
        //map.setRobot(updateRobot(TestData.robotPos, grid));

        //Obstacles
        /*Kryds cross = new Kryds();
        cross.setColor(Colors.OBSTACLE);
        cross.setCorners(grid.translatePositions(TestData.cross));
        map.setCross(cross);*/

        //Goals
        Set<Mål> goals = new HashSet<>();
        Mål goal = new Mål();
        goal.setPos(grid.getLeftCenterPos());
        goal.setWidth(5);
        goal.setHeight(grid.GOAL_LEFT*grid.CELL_SPACING.getY());
        goal.setColor(Colors.GOAL);
        goals.add(goal);

        goal = new Mål();
        goal.setPos(grid.getRightCenterPos());
        goal.setWidth(5);
        goal.setHeight(grid.GOAL_RIGHT*grid.CELL_SPACING.getY());
        goal.setColor(Colors.GOAL);
        goals.add(goal);
        map.setGoals(goals);

        //Balls:
        Vector2D[] vA = TestData.getBalls();
        map.setBalls(createBalls(vA, grid));
    }

    private Set<Bold> createBalls(Vector2D[] vA, Grid grid){
        Bold[] balls = new Bold[vA.length];
        for (int i = 0; i < vA.length; i++){
            balls[i] = new Bold();
            balls[i].setWidth(grid.CELL_SPACING.getX());
            balls[i].setHeight(grid.CELL_SPACING.getY());
            balls[i].setColor(Colors.BALL);
            balls[i].setPos(
                    grid.translatePos(vA[i])
            );
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
            robot.setWidth(size/*1.5f*/);
            robot.setHeight(size/*1.5f*/);

            robotController.setRobot(robot);

            return robot;
        }

        robot.setFrontAndBack(vA);
        /*//Beregner ændringen siden sidste check
        float pos_change = Vector2D.Distance(robot.getPos(), pos);
        //Hvis den er for stor eller for lille ændres den ikke
        if (pos_change > 2 && pos_change < 20){
            robot.setPos(pos);

            //Beregner ændringen i vinkeln siden sidst
            float rot_change = robot.getRotation()-angle;
            //Hvis den møder kriterierne ændres dennne
            if (rot_change < 30 && rot_change > -30){
                robot.setRotation(angle);
            }
        }*/

        return robot;
    }

    private Kryds updateCross(Vector2D[] vA, Grid grid){
        vA = grid.translatePositions(vA);

        Vector2D[] hor = {
                Vector2D.Middle(vA[2],vA[3]),
                Vector2D.Middle(vA[8],vA[9])
        };

        Vector2D[] ver = {
                Vector2D.Middle(vA[5],vA[6]),
                Vector2D.Middle(vA[0],vA[11])
        };

        Kryds cross = map.getCross();

        Vector2D position = Vector2D.Middle(hor[0], hor[1]);

        float rotation = Vector2D.Angle(hor[0], hor[1]);

        if (cross == null){
            cross = new Kryds();
            cross.setColor(Colors.OBSTACLE);

            /*float width = Vector2D.Distance(hor[0], hor[1]);
            float height = Vector2D.Distance(ver[0], ver[1]);
            cross.setWidth(width);
            cross.setHeight(height);
            cross.setPos(position);
            cross.setRotation(rotation);

            return cross;*/
        }

        float width = Vector2D.Distance(hor[0], hor[1]);
        float height = Vector2D.Distance(ver[0], ver[1]);
        cross.setWidth(width);
        cross.setHeight(height);
        cross.setPos(position);
        cross.setRotation(rotation);
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

    public void startRobot() {
        if (map.getRobot() != null) {
            createPath();
            robotController.setRobotTarget(path.getNext());
        }

    }
}
