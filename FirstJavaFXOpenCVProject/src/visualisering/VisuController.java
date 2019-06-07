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

import java.util.*;

public class VisuController {
    private final int UPDATETIME = 1000;
    Kort map;
    Path path;
    private long lastTime;
    private Controller2 otherController;
    private boolean started = false;

    public void createMap(Kort map){
        this.map = map;
    }

    public void start(){
        if (!started){
            createObjects();
            started = true;
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

    private void createObjects(){
        Grid grid = map.getGrid();

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
    }

    private void updateBalls(Vector2D[] vA, Grid grid){
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
        map.setBalls(new HashSet<>(Arrays.asList(balls)));
    }

    private void updateRobot(Vector2D[] vA, Grid grid){
        Robot robot = map.getRobot();
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
                Vector2D.Middle(bagpunkter[0], bagpunkter[1]),
                forPunkt
        };

        if (robot == null){
            robot = new Robot();
            //Finder robotens størrelse
            float width = Vector2D.Distance(vA[0], vA[1]);
            robot.setWidth(width);
            robot.setHeight(width);
            //Farven
            robot.setColor(Colors.ROBOT);
        }

        //Finder midten af roboten
        Vector2D pos = Vector2D.Middle(vA[0], vA[1]);
        robot.setPos(pos);
        //Finder robotens vinkel
        float angle = Vector2D.Angle(vA[0], vA[1]);
        robot.setRotation(angle);

        map.setRobot(robot);
    }

    public void  updateGrid(Point[] points){
        Grid grid = map.getGrid();
        if (grid == null){
            grid = new Grid(map.getWIDTH(), map.getHEIGHT());
            grid.setColor(Colors.GRID);
        }

        Vector2D[] vA = pointToVector(points);

        grid.setScale(vA[0], vA[1], vA[2], vA[3]);
        map.setGrid(grid);
    }

    public void updatePositions(List<Point> ballPoints, List<Point> robotPoints){
        Grid grid = map.getGrid();
        List<Vector2D> ballPos = new ArrayList<>();
        List<Vector2D> robotPos = new ArrayList<>();

        //Update balls
        for (Point p : ballPoints){
            ballPos.add(new Vector2D((float)p.x, (float)p.y));
        }
        updateBalls(ballPos.toArray(new Vector2D[0]), grid);

        //Update robot
        for (Point p : robotPoints){
            robotPos.add(new Vector2D((float)p.x, (float)p.y));
        }
        updateRobot(robotPos.toArray(new Vector2D[0]), grid);
    }

    public void updateCross(Point[] points){
        Kryds cross = map.getCross();

        Vector2D[] vA = pointToVector(points);

        if (cross == null){
            cross = new Kryds();
            cross.setColor(Colors.OBSTACLE);
        }

        cross.setCorners(vA);
    }

    public void updateMap(){
        map.update();
    }

    private Vector2D[] pointToVector(Point[] points){
        Vector2D[] vA = new Vector2D[points.length];
        for (int i = 0; i < vA.length; i++){
            vA[i] = new Vector2D((float)points[i].x, (float)points[i].y);
        }
        return vA;
    }
}
