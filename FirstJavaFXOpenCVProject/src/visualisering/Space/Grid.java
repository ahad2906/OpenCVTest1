package visualisering.Space;

import Jama.Matrix;
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
            GOAL_RIGHT = 160, UNIT_WIDTH = 1675, UNIT_HEIGHT = 1210, OFFSET = .82f;
    private float a, b, c, d, e, f, g, h;
    public final Vector2D CELL_SPACING;
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


        float x1 = topLeft.getX(), y1 = topLeft.getY(),
                x2 = topRight.getX(), y2 = topRight.getY(),
                x3 = bottomRight.getX(), y3 = bottomRight.getY(),
                x4 = bottomLeft.getX(), y4 = bottomLeft.getY(),
                X1 = 0, Y1 = 0, X2 = WIDTH, Y2 = 0,
                X3 = WIDTH, Y3 = HEIGHT, X4 = 0, Y4 = HEIGHT;

        double M_a[][] = { { x1, y1, 1, 0, 0, 0, -x1 * X1, -y1 * X1 },
                { x2, y2, 1, 0, 0, 0, -x2 * X2, -y2 * X2 },
                { x3, y3, 1, 0, 0, 0, -x3 * X3, -y3 * X3 },
                { x4, y4, 1, 0, 0, 0, -x4 * X4, -y4 * X4 },
                { 0, 0, 0, x1, y1, 1, -x1 * Y1, -y1 * Y1 },
                { 0, 0, 0, x2, y2, 1, -x2 * Y2, -y2 * Y2 },
                { 0, 0, 0, x3, y3, 1, -x3 * Y3, -y3 * Y3 },
                { 0, 0, 0, x4, y4, 1, -x4 * Y4, -y4 * Y4 } };

        double M_b[][] = { { X1 }, { X2 }, { X3 }, { X4 }, { Y1 }, { Y2 },
                { Y3 }, { Y4 } };
        Matrix A = new Matrix(M_a);
        Matrix B = new Matrix(M_b);

        Matrix C = A.solve(B);
        a = (float)C.get(0,0);
        b = (float)C.get(1,0);
        c = (float)C.get(2,0);
        d = (float)C.get(3,0);
        e = (float)C.get(4,0);
        f = (float)C.get(5,0);
        g = (float)C.get(6,0);
        h = (float)C.get(7,0);

    }

    /**
     * Used to translate a coordinate from original grid into this grid
     * @param pos original coordinate to be translated
     * @return Vector2D translated coordinate
     */
    public Vector2D translatePos(Vector2D pos){
        float x = pos.getX(), y = pos.getY();
        /*pos = Vector2D.CopyOf(pos)
                .subtract(offset) //Offset the point
                .scale(scale); //Scale to actual grid size
        */
        return new Vector2D(
                (a*x+b*y+c)/(g*x+h*y+1),
                (d*x+e*y+f)/(g*x+h*y+1));
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
        return length/UNIT_SCALE*OFFSET;
    }

    /**
     * Used to translate a real world length into a grid length
     * @param length the lenght in mm to be translated
     * @return float scaled length
     */
    public float translateLengthToScale(float length){
        return length*UNIT_SCALE*(2-OFFSET);
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
