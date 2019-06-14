package visualisering.Objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.Listener.UpdateListener;
import visualisering.Space.IMovableObject;
import visualisering.Space.Vector2D;
import visualisering.View.IDrawable;

import javax.swing.text.Style;

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
    private Vector2D front, back;

    public void setFrontAndBack(Vector2D[] vA){
        front = vA[0];
        back = vA[1];

        //Finder midten af roboten
        Vector2D pos = new Vector2D((vA[0].getX()+vA[1].getX())/2, (vA[0].getY()+vA[1].getY())/2);
        //Finder robotens vinkel
        float angle = Vector2D.Angle(vA[0], vA[1]);

        setRotation(angle);
        setPos(pos);
    }

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
        final Vector2D front = Vector2D.CopyOf(this.front),
                back = Vector2D.CopyOf(this.back),
                position = Vector2D.CopyOf(this.position),
                target = Vector2D.CopyOf(this.target);

        Vector2D t_Dir = target.subtract(position);
        Vector2D r_Dir = front.subtract(back);

        float cos0 = Vector2D.DotProduct(r_Dir, t_Dir) /
                (r_Dir.getMagnitude() * t_Dir.getMagnitude());

        float degrees = (float)Math.toDegrees(Math.acos(cos0));

        return (Vector2D.CrossProduct(r_Dir, t_Dir) < 0)? degrees : -degrees ;
    }

    public float getDistToTarget(){
        final Vector2D position = Vector2D.CopyOf(this.position);
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
