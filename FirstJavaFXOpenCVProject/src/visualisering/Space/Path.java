package visualisering.Space;

import com.sun.istack.internal.NotNull;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.omg.PortableInterceptor.DISCARDING;
import org.opencv.core.Mat;
import visualisering.Objects.Kryds;
import visualisering.Objects.Mål;
import visualisering.Objects.SpaceObject;
import visualisering.View.IDrawable;
import visualisering.View.Kort;

import java.awt.*;
import java.lang.annotation.Target;
import java.util.*;
import java.util.List;

public class Path implements IDrawable {
    private final Kort map;
    private List<Vector2D> path;
    private List<Vector2D> drawpath;
    private SpaceObject target;
    private boolean isCloseEdge, inCorner;
    private int b_dir; //0 er bakhøjre og 1 er bakvenstre
    private final int WIDTH = 2;
    private Color color;

    public Path (@NotNull Vector2D startPoint, Kort map){
        this.map = map;
        path = new ArrayList<>();
        path.add(startPoint);
    }

    public boolean isCloseEdge(){
        return isCloseEdge;
    }

    public boolean isInCorner(){
        return inCorner;
    }

    public int getB_dir(){
        return b_dir;
    }

    public void setTarget(SpaceObject obj){
        this.target = obj;

        /*if (target instanceof Mål){
            //TODO
        }*/

        Grid grid = map.getGrid();
        Kryds cross = map.getCross();

        Vector2D target = this.target.getPos(),
                pos = path.get(0), attackPoint = null,
                correction = new Vector2D(0,5),
                cross_pos = cross.getPos();

        float d = grid.translateLengthToScale(120), space = grid.translateLengthToScale(80);
        int scale = 10, v_scale = 5;

        /*if (cross.isInside(target)){
            isCloseEdge = true;
            //TODO: vælg tilhørende angrebspunkt
            attackPoint = target;//TODO
        }
        else*/ if (target.getX() < space){
            isCloseEdge = true;
            if (target.getY() < space){ //Top left
                attackPoint = Vector2D.RIGHT().scale(scale).add(Vector2D.DOWN());
                target.add(correction);
                inCorner = true;
                b_dir = 1;
            }
            else if (target.getY() > grid.HEIGHT-space){// Bottom left
                attackPoint = Vector2D.RIGHT().scale(scale).add(Vector2D.UP());
                target.subtract(correction);
                inCorner = true;
            }
            else {
                attackPoint = Vector2D.RIGHT().scale(v_scale);
            }

            attackPoint.scale(d).add(target);
        }
        else if (target.getX() > grid.WIDTH-space) {
            isCloseEdge = true;
            if (target.getY() < space) { //Top right
                attackPoint = Vector2D.LEFT().scale(scale).add(Vector2D.DOWN());
                target.add(correction);
                inCorner = true;
            } else if (target.getY() > grid.HEIGHT-space) { //Bottom right
                attackPoint = Vector2D.LEFT().scale(scale).add(Vector2D.UP());
                target.subtract(correction);
                inCorner = true;
                b_dir = 1;
            } else {
                attackPoint = Vector2D.LEFT().scale(v_scale);
            }

            attackPoint.scale(d).add(target);
        }
        else if (target.getY() < space){
            isCloseEdge = true;

            attackPoint = Vector2D.DOWN().scale(v_scale).scale(d).add(target);
        }
        else if (target.getY() > grid.HEIGHT-space) {
            isCloseEdge = true;

            attackPoint = Vector2D.UP().scale(v_scale).scale(d).add(target);
        }
        else {
            float D = Vector2D.Distance(pos, target);
            float x1 = pos.getX(), x2 = target.getX(), y1 = pos.getY(), y2 = target.getY();
            attackPoint = new Vector2D(
                    x2-(d/D)*(x2-x1),
                    y2-(d/D)*(y2-y1)
            );
        }

        path.add(target);

        System.out.println("Path, target is at: "+target+" and attackpoint at: "+attackPoint);
        System.out.println("Path, isClosesEdge = "+isCloseEdge);

        path.add(1, attackPoint);

        //Tjekker om stien krydset krydset
        float a, b, c, s, A, h;
        a = Vector2D.Distance(pos, cross_pos);
        b = Vector2D.Distance(cross_pos, attackPoint);
        c = Vector2D.Distance(pos, attackPoint);

        s = (a+b+c)/2;
        A = (float)Math.sqrt(s*(s-a)*(s-b)*(s-c));
        h = 2*A/c;

        System.out.println("Path, h-val: "+h);

        if (h < grid.translateLengthToScale(350)){
            System.out.println("Path goes through cross");
            float B;
            A = (float)Math.asin((Math.sin(Math.toRadians(90))*h)/a);
            B = (float)Math.toRadians(90)-A;
            b = (float)((Math.sin(B)*a)/Math.sin(A));
            s = grid.translateLengthToScale(400);

            float x1 = pos.getX(), x2 = attackPoint.getX(), y1 = pos.getY(), y2 = attackPoint.getY();
            Vector2D detour = new Vector2D(
                    x1 + (b/a)*(x2-x1),
                    y1 + (b/a)*(y2-y1)
            );

            detour.subtract(cross_pos).toUnit().scale(s);
            path.add(1, detour);

            //Alternativ
            /*float s = grid.translateLengthToScale(400);
            Vector2D middle = Vector2D.Middle(pos, attackPoint).subtract(cross_pos).scale(s);

            path.add(1, middle);*/
        }

        drawpath = new LinkedList<>(path);

    }

    private boolean goesThroughCross(Vector2D pos, Vector2D target, Vector2D cross){
        float a, b, c, s, A, h;
        a = Vector2D.Distance(pos, cross);
        b = Vector2D.Distance(cross, target);
        c = Vector2D.Distance(pos, target);

        s = (a+b+c)/2;
        A = (float)Math.sqrt(s*(s-a)*(s-b)*(s-c));
        h = 2*A/c;

        System.out.println("Path, h-val: "+h);

        return h < 65;
    }

    public float getLength(){
        if (path.size() < 2) //Return if path size is lower than 2.
            return 0;

        Vector2D start, end;
        start = drawpath.get(0);
        float lenght = 0;
        for (int i = 1; i < drawpath.size(); i++){
            end = drawpath.get(i);
            lenght += Vector2D.Distance(start, end);
            start = end;
        }

        return lenght;
    }

    public void add(Vector2D v){
        path.add(1, v);
    }

    public Vector2D getLast(){
        return path.get(path.size()-1);
    }

    public Vector2D getNext(){
        if (path.size() >= 2)
            return path.remove(1);
        else return null;
    }

    public int size(){
        return path.size();
    }

    @Override
    public void draw(GraphicsContext context) {
        if (drawpath.size() < 2) //Return if path size is lower than 2.
            return;

        context.setStroke(color);
        context.setLineWidth(WIDTH);
        Vector2D start, end;
        start = path.get(0);
        for (int i = 1; i < drawpath.size(); i++){
            end = drawpath.get(i);
            context.strokeLine(start.getX(), start.getY(),
                    end.getX(), end.getY());
            start = end;
        }
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
