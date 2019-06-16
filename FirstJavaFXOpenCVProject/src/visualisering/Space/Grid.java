package visualisering.Space;

import Jama.Matrix;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.opencv.core.Mat;
import visualisering.Debug;
import visualisering.Objects.Bold;
import visualisering.View.Colors;
import visualisering.View.IDrawable;

/**
 * Represents a coordinate system grid
 * @author DFallingHammer
 * @version 1.0.5
 */
public class Grid implements IDrawable {
    public final float WIDTH, HEIGHT;
    public final float CELLS_HOR = 9f, CELLS_VER = 7f, GOAL_LEFT = 90f,
            GOAL_RIGHT = 160, UNIT_WIDTH = 1670, UNIT_HEIGHT = 1220;
    private float a, b, c, d, e, f, g, h;
    public final Vector2D CELL_SPACING;
    private final Vector2D UNIT_SCALE;
    Color color;

    public Grid(float width, float height){
        this.WIDTH = width;
        this.HEIGHT = height;
        this.UNIT_SCALE = new Vector2D(
                WIDTH/UNIT_WIDTH,
                HEIGHT/UNIT_HEIGHT
        );
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

        Vector2D topLeft = corners[0];
        for (int i = 1; i < corners.length; i++){
            if (corners[i].getY() < topLeft.getY()+mg && corners[i].getX() < topLeft.getX()+mg){
                topLeft = corners[i];
            }
        }

        Vector2D topRight = corners[0];
        for (int i = 1; i < corners.length; i++){
            if (corners[i].getY() < topRight.getY()+mg && corners[i].getX() > topRight.getX()-mg){
                topRight = corners[i];
            }
        }

        Vector2D bottomLeft = corners[0];
        for (int i = 1; i < corners.length; i++){
            if (corners[i].getY() > bottomLeft.getY()-mg && corners[i].getX() < bottomLeft.getX()+mg){
                bottomLeft = corners[i];
            }
        }

        Vector2D bottomRight = corners[0];
        for (int i = 1; i < corners.length; i++){
            if (corners[i].getY() > bottomRight.getY()-mg && corners[i].getX() > bottomRight.getX()-mg){
                bottomRight = corners[i];
            }
        }

        if (Debug.DEBUG)
            System.out.println("TL: "+topLeft+" TR: "+topRight+" BL: "+bottomLeft+" BR: "+bottomRight);

        float x1 = topLeft.getX(), y1 = topLeft.getY(),
                x2 = topRight.getX(), y2 = topRight.getY(),
                x3 = bottomRight.getX(), y3 = bottomRight.getY(),
                x4 = bottomLeft.getX(), y4 = bottomLeft.getY(),
                X1 = 0, Y1 = 0, X2 = WIDTH, Y2 = 0,
                X3 = WIDTH, Y3 = HEIGHT, X4 = 0, Y4 = HEIGHT;

        Matrix A = new Matrix(new double[][] {
                {x1, y1, 1, 0, 0, 0, -x1*X1, -y1*X1},
                {x2, y2, 1, 0, 0, 0, -x2*X2, -y2*X2},
                {x3, y3, 1, 0, 0, 0, -x3*X3, -y3*X3},
                {x4, x4, 1, 0, 0, 0, -x4*X4, -y4*X4},
                {0, 0, 0, x1, y1, 1, -x1*Y1, -y1*Y1},
                {0, 0, 0, x2, y2, 1, -x2*Y2, -y2*Y2},
                {0, 0, 0, x3, y3, 1, -x3*Y3, -y3*Y3},
                {0, 0, 0, x4, y4, 1, -x4*Y4, -y4*Y4}});

        Matrix B = new Matrix(new double[][]{
                {X1}, {X2}, {X3}, {X4},
                {Y1}, {Y2}, {Y3}, {Y4}});

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
        return length/((UNIT_SCALE.getX()+UNIT_SCALE.getY())/2f);
    }

    /**
     * Used to translate a real world length into a grid length
     * @param length the lenght in mm to be translated
     * @return float scaled length
     */
    public float translateLengthToScale(float length){
        return length*((UNIT_SCALE.getX()+UNIT_SCALE.getY())/2f);
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
