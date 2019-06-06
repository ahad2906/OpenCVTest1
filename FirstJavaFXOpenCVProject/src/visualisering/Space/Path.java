package visualisering.Space;

import com.sun.istack.internal.NotNull;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.View.IDrawable;

import java.util.ArrayList;
import java.util.List;

public class Path implements IDrawable {
    private List<Vector2D> path;
    private final int WIDTH = 2;
    private Color color;

    public Path (@NotNull Vector2D startPoint){
        path = new ArrayList<>();
        path.add(startPoint);
    }

    public float getLenght(){
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

    public void smoothPath(){
        //TODO: algorithm to smooth out the path, make it more direct from the robot to the goal
    }

    public Vector2D getLast(){
        return path.get(path.size()-1);
    }

    public void addPoint(@NotNull Vector2D point){
        path.add(point);
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
