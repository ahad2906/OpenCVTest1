package visualisering;

import visualisering.Space.Vector2D;

import java.util.Random;

public class TestData {
    public static final Vector2D[] corners = {
            new Vector2D(40, 366), //Top left
            new Vector2D(1044, 357), //Top right
            new Vector2D(1051, 987), //Bottom right
            new Vector2D(45, 993) // Bottom left
    };
    public static final Vector2D[] cross = {
            new Vector2D(535, 730),
            new Vector2D(535, 680),
            new Vector2D(485, 680),
            new Vector2D(485, 660),
            new Vector2D(535, 660),
            new Vector2D(535, 610),
            new Vector2D(555, 610),
            new Vector2D(555, 660),
            new Vector2D(605, 660),
            new Vector2D(605, 680),
            new Vector2D(555, 680),
            new Vector2D(555, 730)
    };

    public static final Vector2D robot = new Vector2D(97, 403);
    public static final Vector2D[] robotPos = {new Vector2D(97, 403), new Vector2D(107,413), new Vector2D(130, 425)};

    public static Vector2D[] getBalls(){
        Vector2D[] balls = new Vector2D[10];
        Random random = new Random();
        for (int i = 0; i < balls.length; i++){
            balls[i] = new Vector2D(
                    random.nextInt(1051-45) + 45,
                    random.nextInt(993-366) + 366
            );
        }

        return balls;
    }

}
