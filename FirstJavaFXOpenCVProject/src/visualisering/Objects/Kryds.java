package visualisering.Objects;

import javafx.collections.transformation.SortedList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.Space.Vector2D;
import visualisering.View.IDrawable;

import java.util.Arrays;

public class Kryds extends SpaceObject implements IDrawable {
    private Color color;
    private Vector2D[] corners;

    /*public void setPoints(Vector2D[] vA, float attack_d, float max_width){
        for (int j = 0; j < 2; j++) {
            if (!(Vector2D.Distance(vA[0], vA[1]) > Vector2D.Distance(vA[1], vA[4]))) {
                Vector2D[] newVa = Arrays.copyOf(vA, vA.length);
                newVa[0] = vA[11];
                for (int i = 1; i < vA.length; i++) {
                    newVa[i] = vA[i - 1];
                }
                vA = newVa;
            }
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

        if (width > max_width)
            return;

        //Sætter hjørnerne
        corners = vA;

        //Sætter parameterne
        setWidth(width);
        setHeight(height);
        setPos(position);
        setRotation(rotation);

    }*/

    public boolean isInside(Vector2D v){
        return Vector2D.Distance(position, v) <= width/2;
    }

    public Vector2D[] getAttackPoint(Vector2D target){
        Vector2D[] vA = {
                Vector2D.Middle(corners[9],corners[11]),
                Vector2D.Middle(corners[0],corners[2]),
                Vector2D.Middle(corners[3],corners[5]),
                Vector2D.Middle(corners[6],corners[6]),
                Vector2D.CopyOf(corners[11]).subtract(corners[0]).toUnit(),
                Vector2D.CopyOf(corners[2]).subtract(corners[3]).toUnit(),
                Vector2D.CopyOf(corners[5]).subtract(corners[6]).toUnit(),
                Vector2D.CopyOf(corners[8]).subtract(corners[9]).toUnit(),
                Vector2D.CopyOf(corners[11]),
                Vector2D.CopyOf(corners[2]),
                Vector2D.CopyOf(corners[5]),
                Vector2D.CopyOf(corners[8])
        };

        for (Vector2D v : vA){
            System.out.println("Cross, vector "+v);
        }

        Vector2D[] attackpoints = new Vector2D[2];
        float dist = Float.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            float d = Vector2D.Distance(vA[i], target);

            if (d < dist){
                dist = d;
                attackpoints[0] = vA[i+4];
                attackpoints[1] = vA[i+8];
            }
        }

        System.out.println("Cross, found dir vector "+attackpoints[0]+
                " with corner "+attackpoints[1]+" and target at "+ target);

        return attackpoints;
    }

    public void setCorners(Vector2D[] corners){
        this.corners = corners;
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
