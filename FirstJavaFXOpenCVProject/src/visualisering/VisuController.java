package visualisering;

import WhitePingPongDetector.Controller2;
import javafx.animation.AnimationTimer;
import org.opencv.core.Point;
import visualisering.Objects.Bold;
import visualisering.Objects.Forhindring;
import visualisering.Objects.Mål;
import visualisering.Objects.Robot;
import visualisering.Space.Grid;
import visualisering.Space.Node;
import visualisering.Space.Path;
import visualisering.Space.Vector2D;
import visualisering.View.Colors;
import visualisering.View.Kort;

import java.util.*;

public class VisuController {
    private final int UPDATETIME = 1000;
    Kort map;
    Path path;
    private long lastTime;
    private Controller2 otherController;
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
        Point[] points = otherController.getHjørner();
        Vector2D[] vA = new Vector2D[points.length];

        for (int i = 0; i < vA.length; i++){
            vA[i] = new Vector2D((float)points[i].x, (float)points[i].y);
        }

        grid.setScale(vA[0], vA[1], vA[2], vA[3]);
        grid.setColor(Colors.GRID);
        map.setGrid(grid);

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
        List<Point> robotPoints = otherController.grabFrameRobotCirkel();
        List<Vector2D> ballPos = new ArrayList<>();
        List<Point> ballPoints = otherController.grabFrameCirkel();
        int i = 0;
        boolean robotOk = false, ballOk = false;
        while (true){
            if (ballPoints == null || ballPoints.size() < 10){
                ballPoints = otherController.grabFrameCirkel();
            }
            else ballOk = true;

            if (robotPoints == null || robotPoints.size() < 3){
                ballPoints = otherController.grabFrameCirkel();
            }
            else robotOk = true;

            if (robotOk && ballOk) break;

            if (++i > 20){
                return;
            }
        }

        //Update balls
        for (Point p : ballPoints){
            ballPos.add(new Vector2D((float)p.x, (float)p.y));
        }
        map.setBalls(createBalls(ballPos.toArray(new Vector2D[0]), grid));

        //Update robot
        for (Point p : robotPoints){
            robotPos.add(new Vector2D((float)p.x, (float)p.y));
        }
        map.setRobot(createRobot(robotPos.toArray(new Vector2D[0]), grid));
    }

    private void createPath() {
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
        map.setRobot(createRobot(TestData.robotPos, grid));

        //Obstacles
        Set<Forhindring> obstacles = new HashSet<>();
        Forhindring obstacle = new Forhindring();
        obstacle.setPos(grid.getCenterPos());
        obstacle.setWidth(grid.CELL_SPACING.getX());
        obstacle.setHeight(grid.CELL_SPACING.getY()*5);
        obstacle.setColor(Colors.OBSTACLE);
        obstacles.add(obstacle);

        obstacle = new Forhindring();
        obstacle.setPos(grid.getCenterPos());
        obstacle.setWidth(grid.CELL_SPACING.getX()*5);
        obstacle.setHeight(grid.CELL_SPACING.getY());
        obstacle.setColor(Colors.OBSTACLE);
        obstacles.add(obstacle);
        map.setObstacles(obstacles);

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

    private Robot createRobot(Vector2D[] vA, Grid grid){
        Robot robot = new Robot();
        //Oversætter positionerne
        vA = grid.translatePositions(vA);

        //Finder bag og for ende
        float minDist = Float.MAX_VALUE;
        Vector2D[] bagpunkter = new Vector2D[2];
        for (int i = 0; i < vA.length - 1; i++){
            float dist = Vector2D.Distance(vA[i],vA[i+1]);
            if (minDist > dist){
                minDist = dist;
                bagpunkter[0] = vA[i];
                bagpunkter[1] = vA[i+1];
            }
        }
        Vector2D forPunkt = Vector2D.ZERO;
        for (Vector2D v : vA){
            if (v != bagpunkter[0] && v != bagpunkter[1]){
                forPunkt = v;
            }
        }
        vA = new Vector2D[]{
                new Vector2D((bagpunkter[0].getX()+bagpunkter[1].getX())/2, (bagpunkter[0].getY()+bagpunkter[1].getY())/2),
                forPunkt
        };

        //Finder midten af roboten
        Vector2D pos = new Vector2D((vA[0].getX()+vA[1].getX())/2, (vA[0].getY()+vA[1].getY())/2);
        System.out.println(pos.getX()+", "+pos.getY());
        robot.setPos(pos);
        //Finder robotens størrelse
        float dist = Vector2D.Distance(vA[0], vA[1]);
        System.out.println(dist);
        robot.setWidth(grid.CELL_SPACING.getX()*1.5f);
        robot.setHeight(grid.CELL_SPACING.getY()*1.5f);
        //Finder robotens vinkel
        float angle = Vector2D.Angle(vA[0], vA[1]);
        robot.setRotation(angle);
        //Farven
        robot.setColor(Colors.ROBOT);

        return robot;
    }
}
