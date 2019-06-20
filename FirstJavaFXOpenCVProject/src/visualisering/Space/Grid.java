package visualisering.Space;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.Debug;
import visualisering.View.Colors;
import visualisering.View.IDrawable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a coordinate system grid
 * @author DFallingHammer
 * @version 1.0.5
 */
public class Grid implements IDrawable {
    public final float WIDTH, HEIGHT, UNIT_SCALE;
    public static final float CELLS_HOR = 9f, CELLS_VER = 7f, GOAL_LEFT = 90f,
            GOAL_RIGHT = 160, UNIT_WIDTH = 1675, UNIT_HEIGHT = 1210, OFFSET = 1f;
    private float a, b, c, d, e, f, g, h;
    public final Vector2D CELL_SPACING;
    Vector2D scale, offset;
    Color color;

    public Grid(float width, float height){
        this.WIDTH = width;
        this.HEIGHT = height;
        this.UNIT_SCALE = WIDTH/UNIT_WIDTH;
        CELL_SPACING = new Vector2D(
                WIDTH/CELLS_HOR,
                HEIGHT/CELLS_VER
        );
    }

    /**
     * Sets the scale of the grid compared to original points
     * @param corners corners
     */
    public void setScale(Vector2D[] corners){
        //First find the corners
        int mg = 20;//Margin

        List<Vector2D> c_list = new LinkedList<>(Arrays.asList(corners));

        Vector2D topLeft = c_list.get(0);
        float dist = Float.MAX_VALUE;
        for (Vector2D v : c_list){
            if (v.getSqrMagnitude() < dist){
                dist = v.getSqrMagnitude();
                topLeft = v;
            }
        }

        c_list.remove(topLeft);

        Vector2D bottomRight = c_list.get(0);
        dist = Float.MIN_VALUE;
        for (Vector2D v : c_list){
            if (v.getSqrMagnitude() > dist){
                dist = v.getSqrMagnitude();
                bottomRight = v;
            }
        }

        c_list.remove(bottomRight);

        Vector2D topRight = c_list.get(0);
        Vector2D bottomLeft = c_list.get(1);
        if (topRight.getX() < bottomLeft.getX()){
            Vector2D v = topRight;
            topRight = bottomLeft;
            bottomLeft = v;
        }

        c_list.clear();

        if (Debug.DEBUG)
            System.out.println("TL: "+topLeft+" TR: "+topRight+" BL: "+bottomLeft+" BR: "+bottomRight +
                    "Width: "+WIDTH+" Height: "+HEIGHT);


        float w, h;
        w = (topRight.getX() + bottomRight.getX() - (topLeft.getX() + bottomLeft.getX()))/2f;
        h = (bottomRight.getY() + bottomLeft.getY() - (topLeft.getY() + topRight.getY()))/2f;

        scale = new Vector2D(WIDTH/w, HEIGHT/h);
        offset = new Vector2D(
                (topLeft.getX() + bottomLeft.getX())/2f,
                (topRight.getY() + topLeft.getY())/2f
        );

    }

    /**
     * Used to translate a coordinate from original grid into this grid
     * @param pos original coordinate to be translated
     * @return Vector2D translated coordinate
     */
    public Vector2D translatePos(Vector2D pos){
        pos = Vector2D.CopyOf(pos)
                .subtract(offset) //Offset the point
                .scale(scale); //Scale to actual grid size
        return pos;
    }

    public Vector2D[] translatePositions(Vector2D[] positions){
        Vector2D[] newPos = new Vector2D[positions.length];
        for (int i = 0; i < newPos.length; i++){
            newPos[i] = translatePos(positions[i]);
        }
        return newPos;
    }

    /**
     * Used to translate a length from the grid into real world millimeters
     * @param length the lenght to be translated
     * @return float length in millimeters
     */
    public float translateLengthToMilimeters(float length){
        return length/UNIT_SCALE;
    }

    /**
     * Used to translate a real world length into a grid length
     * @param length the lenght in mm to be translated
     * @return float scaled length
     */
    public float translateLengthToScale(float length){
        return length*UNIT_SCALE;
    }

    @Override
    public void draw(GraphicsContext context) {
        //Draw grid
        context.setStroke(color);
        context.setLineWidth(2);
        //Draws the vertical grid-lines
        for (int i = 1; i < CELLS_HOR; i++){
            context.strokeLine(i*CELL_SPACING.getX(), 0, i*CELL_SPACING.getX(), HEIGHT);
        }

        //Draws the horizontal grid-lines
        for (int i = 1; i < CELLS_VER; i++){
            context.strokeLine(0, i*CELL_SPACING.getY(), WIDTH, i*CELL_SPACING.getY());
        }

        //Borders
        context.setStroke(Colors.OBSTACLE);
        context.strokeLine(0,0,WIDTH,0); //Top
        context.strokeLine(0,HEIGHT, WIDTH, HEIGHT); //Bottom
        context.strokeLine(0,0,0, HEIGHT); //Left
        context.strokeLine(WIDTH,0,WIDTH, HEIGHT); //Right
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color col) {
        this.color = col;
    }

    public Vector2D getCenterPos(){
        return new Vector2D(WIDTH/2,HEIGHT/2);
    }

    public Vector2D getLeftCenterPos() {
        return new Vector2D(0, HEIGHT/2);
    }

    public Vector2D getRightCenterPos() {
        return new Vector2D(WIDTH, HEIGHT/2);
    }
}
