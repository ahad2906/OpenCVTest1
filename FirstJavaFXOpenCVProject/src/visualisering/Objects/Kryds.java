package visualisering.Objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.Space.Vector2D;
import visualisering.View.IDrawable;

import java.util.Arrays;

public class Kryds extends SpaceObject implements IDrawable {
    private Color color;
    private Vector2D[] corners;
    private Vector2D[] attackPoints;

    public void setPoints(Vector2D[] vA, float attack_d){
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

        //Sætter hjørnerne
        corners = vA;

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

        /*//Beregner angrebspunkterne
        float D = Vector2D.Distance(vA[1], vA[0]);
        float x1 = vA[1].getX(), x2 = vA[0].getX(), y1 = vA[1].getY(), y2 = vA[0].getY();
        Vector2D bottom = new Vector2D(
                x1+(attack_d/D)*(x2-x1),
                y1+(attack_d/D)*(y2-y1)
        );


        //Sætter angrebspunkterne
        attackPoints = {
                new Vector2D()

        };*/
    }

    public boolean intersects(Vector2D a, Vector2D b){
        float cross_a1 = Vector2D.CrossProduct(a, corners[0].subtract(corners[5]));
        float cross_b1 = Vector2D.CrossProduct(b, corners[0].subtract(corners[5]));
        float cross_a2 = Vector2D.CrossProduct(a, corners[11].subtract(corners[6]));
        float cross_b2 = Vector2D.CrossProduct(b, corners[11].subtract(corners[6]));
        float cross_a3 = Vector2D.CrossProduct(a, corners[9].subtract(corners[2]));
        float cross_b3 = Vector2D.CrossProduct(b, corners[9].subtract(corners[2]));
        float cross_a4 = Vector2D.CrossProduct(a, corners[8].subtract(corners[3]));
        float cross_b4 = Vector2D.CrossProduct(b, corners[8].subtract(corners[3]));


        return ((cross_a1 < 0) != (cross_b1 < 0)) || ((cross_a2 < 0) != (cross_b2 < 0))
                || ((cross_a3 < 0) != (cross_b3 < 0)) || ((cross_a4 < 0) != (cross_b4 < 0));
    }

    public boolean isInside(Vector2D v){
        return Vector2D.Distance(position, v) <= width/2;
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
