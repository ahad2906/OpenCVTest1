package visualisering;

import visualisering.Space.Grid;
import visualisering.Space.Vector2D;

public class MainTest {

    public static void main(String[] args) {
        Grid grid = new Grid(600, 400);
        Vector2D[] corners = {
                new Vector2D(45, 993), // Bottom left
                new Vector2D(40, 366), //Top left
                new Vector2D(1051, 987), //Bottom right
                new Vector2D(1044, 357) //Top right
        };
        grid.setScale(corners);
    }
}
