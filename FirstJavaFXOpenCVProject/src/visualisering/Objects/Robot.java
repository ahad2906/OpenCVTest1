package visualisering.Objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.Listener.UpdateListener;
import visualisering.Space.IMovableObject;
import visualisering.Space.Vector2D;
import visualisering.View.IDrawable;

/**
 * Represents the robot
 * @author DFallingHammer
 * @version 1.0.1
 */
public class Robot extends SpaceObject implements IMovableObject, IDrawable, UpdateListener {
    private Color color;
    private Vector2D dest;
    private float speed;
    private Vector2D target;

    @Override
    public void moveTo(Vector2D dest) {
        this.dest = dest;
    }

    @Override
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public void draw(GraphicsContext context) {
        context.save();
        context.translate(position.getX(), position.getY());
        context.rotate(rotation);
        context.translate(-position.getX(), -position.getY());

        context.setFill(color);
        context.fillRect(position.getX()-width/2,position.getY()-height/2, width, height);
        context.setStroke(color.invert());
        context.setLineWidth(height/4);
        context.strokeLine(position.getX(),position.getY(),position.getX()+width/2,position.getY());
        /*context.fillPolygon(
                new double[]{
                        position.getX()-width/2,
                        position.getX()-width/2,
                        position.getX()+width/2},
                new double[]{
                        position.getY()-height/2,
                        position.getY()+height/2,
                        position.getY()},
                3);*/

        context.restore();
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color col) {
        this.color = col;
    }

    public void setTarget(Vector2D target){
        this.target = target;
    }

    public Vector2D getTarget(){
        return target;
    }

    public float getAngleToTarget(){
        float r = Vector2D.Angle(position, target);
        return ((rotation > r)? rotation-r : r-rotation)-180;
    }

    public float getDIstToTarget(){
        return Vector2D.Distance(position, target);
    }

    public void turnToward(Vector2D point){
        float old_rot = getRotation();
        setRotation(Vector2D.Angle(position, point));

        System.out.println("Robot rotated "+(getRotation()-old_rot)+" degrees.");
    }

    @Override
    public void OnUpdate(GraphicsContext context) {
        float m = Vector2D.Distance(position, dest);
        Vector2D newPos = new Vector2D(position.getX()/m, position.getY()/m);
        newPos.scale(speed);

        this.position = newPos;

        draw(context);
    }
}
