package visualisering.Objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.View.IDrawable;

/**
 * Represents a goal
 * @author DFallingHammer
 * @version 1.0.0
 */
public class Mål extends SpaceObject implements IDrawable {
    Color color;


    @Override
    public void draw(GraphicsContext context) {
        context.setStroke(color);
        context.setLineWidth(width);
        context.strokeLine(position.getX(),position.getY()-height/2,position.getX(),position.getY()+height/2);
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
