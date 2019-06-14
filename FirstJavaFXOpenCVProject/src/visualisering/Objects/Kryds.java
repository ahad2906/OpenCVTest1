package visualisering.Objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.Space.Vector2D;
import visualisering.View.IDrawable;

import java.util.Arrays;

public class Kryds extends SpaceObject implements IDrawable {
    private Color color;
    private Vector2D points;

    public void setPoints(Vector2D[] vA){
        if (Vector2D.Distance(vA[0], vA[11]) > Vector2D.Distance(vA[11], vA[10])){
            Vector2D[] newVa = Arrays.copyOf(vA, vA.length);
            newVa[0] = vA[11];
            for (int i = 1; i < vA.length; i++){
                newVa[i] = vA[i-1];
            }
            vA = newVa;
        }

        //Finder de to horizontale punkter
        Vector2D[] hor = {
                Vector2D.Middle(vA[2],vA[3]),
                Vector2D.Middle(vA[8],vA[9])
        };

        //Finder de to vertikale punkter
        Vector2D[] ver = {
                Vector2D.Middle(vA[5],vA[6]),
                Vector2D.Middle(vA[0],vA[11])
        };

        //Finder midten
        Vector2D position = Vector2D.Middle(hor[0], hor[1]);
        //Finder vinklen
        float rotation = Vector2D.Angle(hor[0], hor[1]);
        //Finder højde og bredde
        float width = Vector2D.Distance(hor[0], hor[1]);
        float height = Vector2D.Distance(ver[0], ver[1]);

        //Sætter parameterne
        setWidth(width);
        setHeight(height);
        setPos(position);
        setRotation(rotation);
    }

    @Override
    public void draw(GraphicsContext context) {
        context.save();
        context.translate(position.getX(), position.getY());
        context.rotate(rotation);
        context.translate(-position.getX(), -position.getY());

        context.setFill(color);
        context.fillRect(
                position.getX()-width/2,
                position.getY()-width/10,
                width, width/5
        );
        context.fillRect(
                position.getX()-height/10,
                position.getY()-height/2,
                height/5, height
        );

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
}
