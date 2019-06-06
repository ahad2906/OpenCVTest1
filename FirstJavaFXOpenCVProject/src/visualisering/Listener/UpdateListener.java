package visualisering.Listener;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface for an update listener. <br>
 * Can be used to update an object for every frame for example.
 * @author DFallingHammer
 * @version 1.0.0
 */
public interface UpdateListener {

    /**
     *
     */
    void OnUpdate(GraphicsContext context);

}
