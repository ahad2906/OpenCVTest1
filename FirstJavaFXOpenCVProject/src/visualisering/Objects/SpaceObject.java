package visualisering.Objects;

import visualisering.Space.Vector2D;

/**
 * Represents an object in 2d space
 * @author DFallingHammer
 * @version 1.0.1
 */
public abstract class SpaceObject {
    protected Vector2D position;
    protected float rotation, height, width;
    private final int FULL_ROT = 360;

    /**
     * Returns the current x,y position as a Vector2D
     * @return Vector2D of current position
     */
    public Vector2D getPos() {
        return this.position;
    }

    /**
     * Sets the x,y positon to the given Vector2D
     * @param pos the new Vector2D position
     */
    public void setPos(Vector2D pos) {
        this.position = pos;
    }

    /**
     * Returns the current rotation as a float value
     * @return float the rotation
     */
    public float getRotation() {
        return this.rotation;
    }

    /**
     * Set the rotation to the given rotation
     * @param rot the new rotation
     */
    public void setRotation(float rot) {
        this.rotation = rot%FULL_ROT;
        if (this.rotation < 0)
            this.rotation += FULL_ROT;
    }

    /**
     * Returns the float height
     * @return float height
     */
    public float getHeight(){
        return this.height;
    }

    /**
     * Sets the float height
     * @param height the new height
     */
    public void setHeight(float height){
        this.height = height;
    }

    /**
     * Returns the float width
     * @return float width
     */
    public float getWidth(){
        return this.width;
    }

    /**
     * Sets the float width
     * @param width the new width
     */
    public void setWidth(float width){
        this.width = width;
    }
}
