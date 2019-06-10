package visualisering.Objects;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import visualisering.Space.Vector2D;
import visualisering.View.IDrawable;

public class Kryds extends SpaceObject implements IDrawable {
    private Color color;

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
