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

    public boolean isInside(Vector2D v){
        return Vector2D.Distance(position, v) <= width/1.6;
    }

    public Vector2D[] getAttackPoint(Vector2D target, float scale){
        //float scale = 3.5f;
        Vector2D[] vA = {
                Vector2D.Middle(corners[9],corners[11]).subtract(position).toUnit(),
                Vector2D.Middle(corners[0],corners[2]).subtract(position).toUnit(),
                Vector2D.Middle(corners[3],corners[5]).subtract(position).toUnit(),
                Vector2D.Middle(corners[6],corners[6]).subtract(position).toUnit(),
                Vector2D.CopyOf(corners[9]).subtract(corners[2]).toUnit(), //0 til 11
                Vector2D.CopyOf(corners[0]).subtract(corners[5]).toUnit(), //3 til 2
                Vector2D.CopyOf(corners[3]).subtract(corners[8]).toUnit(), //6 til 5
                Vector2D.CopyOf(corners[6]).subtract(corners[11]).toUnit(), //9 til 8
                Vector2D.CopyOf(corners[11]).subtract(corners[10]).toUnit().scale(scale).add(position),
                Vector2D.CopyOf(corners[2]).subtract(corners[1]).toUnit().scale(scale).add(position),
                Vector2D.CopyOf(corners[5]).subtract(corners[4]).toUnit().scale(scale).add(position),
                Vector2D.CopyOf(corners[8]).subtract(corners[7]).toUnit().scale(scale).add(position)
        };

        for (Vector2D v : vA){
            System.out.println("Cross, vector "+v);
        }

        //Retningsvektoren fra krydset til bolden
        Vector2D b_dir = Vector2D.CopyOf(target).subtract(position);

        //Vi prøver at finde hvilket indhak i krydset bolden ligger
        Vector2D[] attackpoints = new Vector2D[2];
        float degrees = Float.MAX_VALUE;
        int j = 0;
        for (int i = 0; i < 4; i++) {

            //Beregner vinklen mellem boldens retningsvektor og retningsvektoren for et af krydsets "huller"
            float cos0 = Vector2D.DotProduct(b_dir, vA[i]) /
                    (b_dir.getMagnitude() * vA[i].getMagnitude());
            float d = (float)Math.toDegrees(Math.acos(cos0));

            System.out.println("Cross, degrees: "+d+", dir vector: "+vA[i]+", b_dir: "+b_dir);

            //Hvis denne er lavere end forrige, gør den til den nye vinkel og vælg dette hul
            if (d < degrees){
                j = i;
                degrees = d;
                attackpoints[0] = vA[i+4];
                attackpoints[1] = vA[i+8];
            }
        }

        System.out.println("Cross, found unit vector: "+attackpoints[0]+
                " where dir vector: "+vA[j]+" had an angle of "+degrees+
                " degrees from target direction vector: "+b_dir);

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
