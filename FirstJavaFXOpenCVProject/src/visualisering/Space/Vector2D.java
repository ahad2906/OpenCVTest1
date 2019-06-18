package visualisering.Space;

/**
 * A class that represents vectors in 2D space and can handle different vector calculations.
 * @author DFallingHammer
 * @version 1.1.2
 */
public class Vector2D {

    public static Vector2D DOWN(){
        return new Vector2D(0,1);
    }
    public static Vector2D UP(){
        return new Vector2D(0,-1);
    }
    public static Vector2D LEFT(){
        return new Vector2D(-1,0);
    }
    public static Vector2D RIGHT(){
        return new Vector2D(1,0);
    }
    public static Vector2D ONE(){
        return new Vector2D(1,1);
    }
    public static Vector2D ZERO(){
        return new Vector2D(0,0);
    }

    private float x, y;

    /**
     * Constructor
     * @param x int x component value.
     * @param y int y component value.
     */
    public Vector2D(float x, float y){
        this.x = x;
        this.y = y;
    }

    /**
     * Constructor
     * @param v Vector2D which will be copied.
     */
    public Vector2D(Vector2D v){
        this.x = v.getX();
        this.y = v.getY();
    }

    //Static methods:
    /**
     * Returns a copy (new instance) of the given Vector2D.
     * @param v Vector2D to be copied.
     * @return Vector2D copy.
     */
    public static Vector2D CopyOf(Vector2D v){
        Vector2D copy = new Vector2D(v);
        return copy;
    }

    /**
     * Returns the angle in degrees between two vectors
     * @param a starting point
     * @param b end point
     * @return float angle in degrees
     */
    public static float Angle(Vector2D a, Vector2D b){
        Vector2D direction = Vector2D.CopyOf(b).subtract(a);

        Vector2D v;
        int d;
        if (b.getY() < a.getY()){
            v = LEFT();
            d = 0;
        }
        else {
            v = RIGHT();
            d = 180;
        }

        float cos0 = Vector2D.DotProduct(v,direction) /
                (v.getMagnitude() * direction.getMagnitude());

        return (float)Math.toDegrees(Math.acos(cos0))+d;
    }

    /**
     * Returns the middle point between to points
     * @param a point a
     * @param b point b
     * @return Vector2D middle
     */
    public static Vector2D Middle(Vector2D a, Vector2D b){
        return new Vector2D(
                (a.getX()+b.getX())/2,
                (a.getY()+b.getY())/2
        );
    }

    /**
     * Returns the float distance between two Vector2D.
     * @param a first Vector2D
     * @param b second Vector2D
     * @return
     */
    public static float Distance(Vector2D a, Vector2D b){
        return CopyOf(a).subtract(b).getMagnitude();
    }

    /**
     * Returns a scaled Vector2D of the scaling between the Vector2D a and Vector2D b.
     * Calculated as such: "a.x *= b.x, a.y *= b.y".
     * @param a first Vector2D
     * @param b second Vector2D
     * @return The scaled Vector2D
     */
    public static Vector2D Scale(Vector2D a, Vector2D b){
        return CopyOf(a).scale(b);
    }

    /**
     * Returns a scaled Vector2D of the scaling between the Vector2D a and the scalar.
     * Calculated as such: "a.x *= scalar, a.y *= scalar".
     * @param a first Vector2D
     * @param scalar second Vector2D
     * @return The scaled Vector2D
     */
    public static Vector2D Scale(Vector2D a, float scalar){
        return CopyOf(a).scale(scalar);
    }

    /**
     * Returns a Vector2D with the lowest two components from the two Vector2D parameters.
     * @param a first Vector2D
     * @param b second Vector2D
     * @return Vector2D min
     */
    public static Vector2D Min (Vector2D a, Vector2D b) {
        float minX = (a.getX() < b.getX())? a.getX() : b.getX();
        float minY = (a.getY() < b.getY())? a.getY() : b.getY();

        return new Vector2D(minX, minY);
    }

    /**
     * Returns a Vector2D with the highest two components from the two Vector2D parameters.
     * @param a first Vector2D
     * @param b second Vector2D
     * @return Vector2D max
     */
    public static Vector2D Max (Vector2D a, Vector2D b){
        float maxX = (a.getX() > b.getX())? a.getX() : b.getX();
        float maxY = (a.getY() > b.getY())? a.getY() : b.getY();

        return new Vector2D(maxX, maxY);
    }

    /**
     * Returns the dot product of two vectors, (ax*bx + ay*by)
     * @param a first Vetor2D
     * @param b second Vector2D
     * @return float dot product
     */
    public static float DotProduct(Vector2D a, Vector2D b){
        return a.getX()*b.getX()+a.getY()*b.getY();
    }

    public static float CrossProduct(Vector2D a, Vector2D b){
        return a.getX()*b.getY() - b.getX()*a.getY();
    }


    //Public methods:
    /**
     * Add a Vector2D to this Vector2D
     * @param v Vector2D to be added
     * @return Vector2D result
     */
    public Vector2D add(Vector2D v){
        x += v.getX();
        y += v.getY();

        return this;
    }

    /**
     * Subtract a Vector2D from this Vector2D
     * @param v Vector2D to be subtracted
     * @return Vector2D result
     */
    public Vector2D subtract(Vector2D v){
        x -= v.getX();
        y -= v.getY();

        return this;
    }

    /**
     * Scales the Vector2D by another Vector2D. Calculated as such: "this.x *= other.x, this.y *= other.y".
     * @param v The scaling Vector2D
     * @return The scaled Vector2D result
     */
    public Vector2D scale(Vector2D v){
        x *= v.getX();
        y *= v.getY();

        return this;
    }

    /**
     * Scales the Vector2D by a scalar Calculated as such: "this.x *= scalar, this.y *= scalar".
     * @param scalar The scalar
     * @return The scaled Vector2D result
     */
    public Vector2D scale(float scalar){
        x *= scalar;
        y *= scalar;

        return this;
    }

    /**
     * Clamps the vector to the given min and max bounds
     * @param min Vector2D of minimum bound
     * @param max Vector2D of maximum bound
     * @return Vector2D inside the bounds
     */
    public Vector2D clamp(Vector2D min, Vector2D max){
        if (x < min.getX())
            x = min.getX();
        else if (x > max.getX())
            x = max.getX();

        if (y < min.getY())
            y = min.getY();
        else if (y > max.getY())
            y = max.getY();

        return this;
    }

    /**
     * Sets the vector to it's unit vector
     * @return Vector2D unit
     */
    public Vector2D toUnit(){
        float m = getMagnitude();

        x /= m;
        y /= m;

        return this;
    }


    //Getters:
    /**
     * Returns the magnitude(length) of the Vector2D.
     * @return float magnitude.
     */
    public float getMagnitude (){
        return Math.abs((float)Math.sqrt(getSqrMagnitude()));
    }

    /**
     * Returns the squared magnitude of the Vector2D. (x*x + y*y).
     * @return float squared magnitude.
     */
    public float getSqrMagnitude(){
        return (x*x + y*y);
    }

    /**
     * Returns the x component of the Vector2D.
     * @return float x component.
     */
    public float getX(){
        return x;
    }

    /**
     * Returns the y component of the Vector2D.
     * @return float y component.
     */
    public float getY(){
        return y;
    }


    //Setters:
    /**
     * Used to set the x and y components of the Vector2D.
     * @param x float new x component value.
     * @param y float new y component value.
     */
    public void set(float x, float y){
        this.x = x;
        this.y = y;
    }

    /**
     * Used to set the x component of the Vector2D.
     * @param x float new x component value.
     */
    public void setX(float x){
        this.x = x;
    }

    /**
     * Used to set the y component of the Vector2D.
     * @param y float new y component value.
     */
    public void setY(float y){
        this.y = y;
    }

    public String toString(){
        return "["+x+", "+y+"]";
    }

}
