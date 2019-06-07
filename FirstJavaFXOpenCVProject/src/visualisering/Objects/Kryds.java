package visualisering.Objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.Space.Vector2D;
import visualisering.View.IDrawable;

public class Kryds extends SpaceObject implements IDrawable {
    private Vector2D[] corners;
    private Color color;
    private float thickness;

    public Kryds (Vector2D[] corners){
        this.corners = corners;
        Vector2D[] hor = {
                Vector2D.Middle(corners[2],corners[3]),
                Vector2D.Middle(corners[8],corners[9])
        };

        Vector2D[] ver = {
                Vector2D.Middle(corners[5],corners[6]),
                Vector2D.Middle(corners[0],corners[11])
        };

        width = Vector2D.Distance(hor[0], hor[1]);
        height = Vector2D.Distance(ver[0], ver[1]);
        thickness = (
                Vector2D.Distance(corners[2], corners[3])
                + Vector2D.Distance(corners[8], corners[9])
                + Vector2D.Distance(corners[5], corners[6])
                + Vector2D.Distance(corners[0], corners[11])
        )/4;

        position = Vector2D.Middle(hor[0], hor[1]);

        rotation = Vector2D.Angle(hor[0], hor[1]);

    }


    @Override
    public void draw(GraphicsContext context) {
        context.setFill(color);
        context.fillRect(
                position.getX()-width/2,
                position.getY()-thickness/2,
                width, thickness
        );
        context.fillRect(
                position.getX()-thickness/2,
                position.getY()-height/2,
                thickness, height
        );
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
