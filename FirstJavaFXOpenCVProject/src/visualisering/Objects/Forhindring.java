package visualisering.Objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.View.IDrawable;

/**
 * Represents an obstacle
 * @author DFallingHammer
 * @version 1.0.1
 */
public class Forhindring extends SpaceObject implements IDrawable {
    private Color color;

    @Override
    public void draw(GraphicsContext context) {
        context.setFill(color);
        context.fillRect(
                position.getX()-width/2,
                position.getY()-height/2,
                width, height
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
