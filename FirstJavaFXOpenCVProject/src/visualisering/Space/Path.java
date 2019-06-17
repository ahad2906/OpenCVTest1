package visualisering.Space;

import com.sun.istack.internal.NotNull;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.Objects.Kryds;
import visualisering.Objects.Mål;
import visualisering.Objects.SpaceObject;
import visualisering.View.IDrawable;
import visualisering.View.Kort;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Path implements IDrawable {
    private final Kort map;
    private List<Vector2D> path;
    private SpaceObject target;
    private boolean isCloseEdge;
    private final int WIDTH = 2;
    private Color color;

    public Path (@NotNull Vector2D startPoint, Kort map){
        this.map = map;
        path = new ArrayList<>();
        path.add(startPoint);
    }

    public boolean isCloseEdge(){
        return isCloseEdge;
    }

    public void setTarget(SpaceObject obj){
        this.target = obj;

        /*if (target instanceof Mål){
            //TODO
        }*/

        Grid grid = map.getGrid();
        Kryds cross = map.getCross();

        Vector2D target = this.target.getPos(),
                pos = path.get(0), attackPoint = null;
        path.add(target);

        float d = grid.translateLengthToScale(60);

        /*if (cross.isInside(target)){
            isCloseEdge = true;
            //TODO: vælg tilhørende angrebspunkt
            attackPoint = target;//TODO
        }
        else*/ if (target.getX() < grid.CELL_SPACING.getX()){
            isCloseEdge = true;
            if (target.getY() < grid.CELL_SPACING.getY()){
                attackPoint = Vector2D.RIGHT.scale(5).add(Vector2D.DOWN);
            }
            else if (target.getY() > grid.CELL_SPACING.getY()*(Grid.CELLS_VER -1)){
                attackPoint = Vector2D.RIGHT.scale(5).add(Vector2D.UP);
            }
            else {
                attackPoint = Vector2D.RIGHT;
            }

            attackPoint.scale(d).add(target);
        } else if (target.getX() > grid.CELL_SPACING.getX()*(Grid.CELLS_HOR -1)){
            isCloseEdge = true;
            if (target.getY() < grid.CELL_SPACING.getY()) {
                attackPoint = Vector2D.LEFT.scale(5).add(Vector2D.DOWN);
            }
            else if (target.getY() > grid.CELL_SPACING.getY()*(Grid.CELLS_VER -1)){
                attackPoint = Vector2D.LEFT.scale(5).add(Vector2D.UP);
            }
            else {
                attackPoint = Vector2D.LEFT;
            }

            attackPoint.scale(d).add(target);
        }
        else {
            float D = Vector2D.Distance(pos, target);
            float x1 = pos.getX(), x2 = target.getX(), y1 = pos.getY(), y2 = target.getY();
            attackPoint = new Vector2D(
                    x2-(d/D)*(x2-x1),
                    y2-(d/D)*(y2-y1)
            );
        }

        System.out.println("Path, target is at: "+target+" and attackpoint at: "+attackPoint);
        System.out.println("Path, isClosesEdge = "+isCloseEdge);

        path.add(1, attackPoint);

        if (cross.intersects(pos, attackPoint)){
            System.out.println("Path goes through cross");
            //TODO: tilføj ekstra punkter så man kører udenom
        }

    }

    public float getLength(){
        if (path.size() < 2) //Return if path size is lower than 2.
            return 0;

        Vector2D start, end;
        start = path.get(0);
        float lenght = 0;
        for (int i = 1; i < path.size(); i++){
            end = path.get(i);
            lenght += Vector2D.Distance(start, end);
            start = end;
        }

        return lenght;
    }

    public Vector2D getLast(){
        return path.get(path.size()-1);
    }

    public Vector2D getNext(){
        if (path.size() >= 2)
            return path.remove(1);
        else return null;
    }

    public int size(){
        return path.size();
    }

    @Override
    public void draw(GraphicsContext context) {
        if (path.size() < 2) //Return if path size is lower than 2.
            return;

        context.setStroke(color);
        context.setLineWidth(WIDTH);
        Vector2D start, end;
        start = path.get(0);
        for (int i = 1; i < path.size(); i++){
            end = path.get(i);
            context.strokeLine(start.getX(), start.getY(),
                    end.getX(), end.getY());
            start = end;
        }
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public void setColor(Color col) {
        this.color = col;
    }
}
