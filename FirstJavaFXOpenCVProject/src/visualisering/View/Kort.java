package visualisering.View;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import visualisering.Debug;
import visualisering.Objects.*;
import visualisering.Space.Grid;
import visualisering.Space.Node;
import visualisering.Space.Vector2D;

import java.util.HashSet;
import java.util.Set;

public class Kort {
    private Grid grid;
    private Node[][] nodes;
    private Robot robot;
    private Kryds cross;
    private Set<Bold> balls;
    private Set<Mål> goals;
    private Mål leftgoal, rightgoal;
    private Set<Forhindring> obstacles;
    private Set<IDrawable> debug;
    private Canvas canvas;
    private final float RATIO = Grid.UNIT_HEIGHT/Grid.UNIT_WIDTH;
    private final float WIDTH, HEIGHT;

    public Kort(float width){
        this.WIDTH = width;
        this.HEIGHT = WIDTH*RATIO;
        this.canvas = new Canvas(WIDTH, HEIGHT);
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

    public Kryds getCross(){
        return cross;
    }

    public Set<Bold> getBalls() {
        return balls;
    }

    public Mål getLeftgoal(){
        return leftgoal;
    }

    public Mål getRightgoal(){
        return rightgoal;
    }

    public Set<Forhindring> getObstacles() {
        return obstacles;
    }

    public Canvas getCanvas() {
        return canvas;
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

    public void setCross(Kryds cross) {
        this.cross = cross;
    }

    public void setBalls(Set<Bold> balls) {
        this.balls = balls;
    }

    public void setLeftgoal(Mål leftgoal){
        this.leftgoal = leftgoal;
    }

    public void setRightgoal(Mål rightgoal){
        this.rightgoal = rightgoal;
    }

    public void setObstacles(Set<Forhindring> obstacles) {
        this.obstacles = obstacles;
    }

    public void addDebugObject(IDrawable obj){
        if (debug == null)
            debug = new HashSet<>();
        debug.add(obj);
    }

    public void removeDebugObject(IDrawable obj){
        try {
            debug.remove(obj);
        }
        catch (NullPointerException e){

        }
    }

    public void update(){
        GraphicsContext context = canvas.getGraphicsContext2D();
        context.clearRect(0,0,WIDTH,HEIGHT); //Clear canvas

        grid.draw(context);
        if (robot != null) robot.draw(context);
        if (balls != null)
            for(Bold ball:balls)
                ball.draw(context);

        if (cross != null) cross.draw(context);

        leftgoal.draw(context);
        rightgoal.draw(context);

        if(Debug.DEBUG)
            for(IDrawable obj:debug)
                obj.draw(context);
    }
}
