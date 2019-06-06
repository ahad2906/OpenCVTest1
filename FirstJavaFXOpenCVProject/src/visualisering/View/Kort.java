package visualisering.View;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import visualisering.Debug;
import visualisering.Objects.Bold;
import visualisering.Objects.Forhindring;
import visualisering.Objects.Mål;
import visualisering.Objects.Robot;
import visualisering.Space.Grid;
import visualisering.Space.Node;
import visualisering.Space.Vector2D;

import java.util.HashSet;
import java.util.Set;

public class Kort {
    private Grid grid;
    private Node[][] nodes;
    private Robot robot;
    private Set<Bold> balls;
    private Set<Mål> goals;
    private Set<Forhindring> obstacles;
    private Set<IDrawable> debug;
    private Canvas canvas;
    private boolean scaled = false;
    private final float WIDTH, HEIGHT;

    public Kort(Canvas canvas){
        this.canvas = canvas;
        this.WIDTH = (float)canvas.getWidth();
        this.HEIGHT = (float)canvas.getHeight();

    }

    public void setScale(Vector2D[] corners){
        //TODO: set scale from here
        //TODO: remember to update the scale of every object
    }

    public Grid getGrid() {
        return grid;
    }

    public Node[][] getNodes() {
        return nodes;
    }

    public Robot getRobot() {
        return robot;
    }

    public Set<Bold> getBalls() {
        return balls;
    }

    public Set<Mål> getGoals() {
        return goals;
    }

    public Set<Forhindring> getObstacles() {
        return obstacles;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public boolean isScaled() {
        return scaled;
    }

    public float getWIDTH() {
        return WIDTH;
    }

    public float getHEIGHT() {
        return HEIGHT;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public void setNodes(Node[][] nodes) {
        this.nodes = nodes;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    public void setBalls(Set<Bold> balls) {
        this.balls = balls;
    }

    public void setGoals(Set<Mål> goals) {
        this.goals = goals;
    }

    public void setObstacles(Set<Forhindring> obstacles) {
        this.obstacles = obstacles;
    }

    public void addDebugObject(IDrawable obj){
        if (debug == null)
            debug = new HashSet<>();
        debug.add(obj);
    }

    public void update(){
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.clearRect(0,0,WIDTH,HEIGHT); //Clear canvas

        grid.draw(context);
        robot.draw(context);
        for(Bold ball:balls)
            ball.draw(context);

        for(Forhindring obstacle:obstacles)
            obstacle.draw(context);

        for(Mål goal:goals)
            goal.draw(context);

        if(Debug.DEBUG)
            for(IDrawable obj:debug)
                obj.draw(context);
    }
}
