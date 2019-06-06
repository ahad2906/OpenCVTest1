package visualisering.View;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * JavaFX drawable object
 * @author DFallingHammer
 * @version 1.0.1
 */
public interface IDrawable {

    /**
     * Draws the object in the given Group
     */
    void draw(GraphicsContext context);

    /**
     * Returns the javaFx color of the drawable
     * @return Color
     */
    Color getColor();

    /**
     * Sets the javaFx color of the drawable
     * @param col the new Color
     */
    void setColor(Color col);

}
