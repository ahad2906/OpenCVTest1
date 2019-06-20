package visualisering.View;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class Text implements IDrawable {
    Color color;
    String text = "Tid: ";

    public void setText(String text){
        this.text = "Tid: "+text;
    }

    @Override
    public void draw(GraphicsContext context) {
        context.setFill(color);
        context.setTextAlign(TextAlignment.LEFT);
        context.setTextBaseline(VPos.TOP);
        context.fillText(
                text,
                5,5
        );
        context.getCanvas().setAccessibleText(text);
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
