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
    private boolean isCloseEdge, inCorner, inCross, isGoal;
    private int b_dir; //0 er bakhøjre og 1 er bakvenstre
    private final int WIDTH = 2;
    private Color color;

    public Path(@NotNull Kort map) {
        this.map = map;
        path = new ArrayList<>();
    }

    public boolean isCloseEdge() {
        return isCloseEdge;
    }

    public boolean isInCorner() {
        return inCorner;
    }

    public boolean isInCross() {
        return inCross;
    }

    public boolean isGoal() {
        return isGoal;
    }

    public int getB_dir() {
        return b_dir;
    }

    public void setTarget(SpaceObject obj) {
        Grid grid = map.getGrid();
        Kryds cross = map.getCross();

        Vector2D target = obj.getPos(),
                pos = map.getRobot().getPos(), attackPoint = null,
                correction = new Vector2D(0, 5),
                cross_pos = cross.getPos();

        //Tilføj robottens placering
        path.add(pos);

        //Definerer de forskellige afstands variabler
        float d = grid.translateLengthToScale(160), space = grid.translateLengthToScale(80),
                space_corner = grid.translateLengthToScale(120);
        float norm_d = grid.translateLengthToScale(200), corner_d = grid.translateLengthToScale(340);
        int scale = 10;

        if (obj instanceof Mål) {
            isGoal = true;
            attackPoint = Vector2D.RIGHT().scale(grid.translateLengthToScale(100)).add(target);
            path.add(1, attackPoint);
            attackPoint = Vector2D.RIGHT().scale(grid.translateLengthToScale(300)).add(target);
        } else {
            //Tjekker hvor bolden befinder sig
            if (cross.isInside(target)) { //I krydset
                inCross = true;
                Vector2D[] points = cross.getAttackPoint(target);
                attackPoint = points[0];
                attackPoint.scale(corner_d);
                attackPoint.add(points[1]);
                target = points[1];
                //Tæt på øvre venstre hjørne
            } else if (Vector2D.Distance(target, Vector2D.ZERO()) <= space_corner) {
                attackPoint = Vector2D.RIGHT().scale(scale).add(Vector2D.DOWN()).toUnit().scale(corner_d);
                target.add(correction);
                attackPoint.add(target);
                inCorner = true;
                b_dir = 1;
                //Tæt på nedre venstre hjørne
            } else if (Vector2D.Distance(target, new Vector2D(0, grid.HEIGHT)) <= space_corner) {
                attackPoint = Vector2D.RIGHT().scale(scale).add(Vector2D.UP()).toUnit().scale(corner_d);
                target.subtract(correction);
                attackPoint.add(target);
                inCorner = true;
                //Tæt på øvre højre hjørne
            } else if (Vector2D.Distance(target, new Vector2D(grid.WIDTH, 0)) <= space_corner) {
                attackPoint = Vector2D.LEFT().scale(scale).add(Vector2D.DOWN()).toUnit().scale(corner_d);
                target.add(correction);
                attackPoint.add(target);
                inCorner = true;
                //Tæt på nedre højre hjørne
            } else if (Vector2D.Distance(target, new Vector2D(grid.WIDTH, grid.HEIGHT)) <= space_corner) {
                attackPoint = Vector2D.LEFT().scale(scale).add(Vector2D.UP()).toUnit().scale(corner_d);
                target.subtract(correction);
                attackPoint.add(target);
                inCorner = true;
                b_dir = 1;
            } else if (target.getX() < space) { //Tæt på venstre bander
                isCloseEdge = true;

                attackPoint = Vector2D.RIGHT().scale(norm_d).add(target);
            } else if (target.getX() > grid.WIDTH - space) { //Tæt på højre bander
                isCloseEdge = true;

                attackPoint = Vector2D.LEFT().scale(norm_d).add(target);
            } else if (target.getY() < space) { //Tæt på øverste bander
                isCloseEdge = true;

                attackPoint = Vector2D.DOWN().scale(norm_d).add(target);
            } else if (target.getY() > grid.HEIGHT - space) { //Tæt på nederste bander
                isCloseEdge = true;

                attackPoint = Vector2D.UP().scale(norm_d).add(target);
            } else { //Eller frit
                float D = Vector2D.Distance(pos, target);
                float x1 = pos.getX(), x2 = target.getX(), y1 = pos.getY(), y2 = target.getY();
                attackPoint = new Vector2D(
                        x2 - (d / D) * (x2 - x1),
                        y2 - (d / D) * (y2 - y1)
                );
            }
        }

        path.add(target);

        //Tjekker om stien krydset krydset
        float a, b, c, s, A, h, ct;
        a = Vector2D.Distance(pos, cross_pos);
        b = Vector2D.Distance(cross_pos, attackPoint);
        c = Vector2D.Distance(pos, attackPoint);
        ct = Vector2D.Distance(pos,target);

        s = (a + b + c) / 2;
        A = (float) Math.sqrt(s * (s - a) * (s - b) * (s - c));
        h = 2 * A / c;

        System.out.println("Path, h-val: " + h + ", c-val: " + c + ", a-val: " + a + ", ct-val: "+ct);

        //Hvis stien går igennem krydset
        if (h < grid.translateLengthToScale(200) && (c >= a || ct >= a)) {
            //Er der tale om en bold tæt på banderet?
            if (isCloseEdge || isGoal || inCross || inCorner) {
                path.add(1, attackPoint); //Tilføj det tidligere udregnet angrebspunkt
            } else {
                attackPoint = target; //Sæt angræbspunkt til boldens position
                c = Vector2D.Distance(pos, attackPoint); //Beregn den ny c afstand mellem robot og angrebspunkt
            }

            System.out.println("Path goes through cross");
            //Beregner detour, punktet hvor en retvinklet linje fra krydset
            // skærer igennen linjen mellem robot og angrebspunkt
            A = (float) Math.asin((Math.sin(Math.toRadians(90)) * h) / a);
            b = (float) Math.cos(A) * a;
            s = grid.translateLengthToScale(300);

            float x1 = pos.getX(), x2 = attackPoint.getX(), y1 = pos.getY(), y2 = attackPoint.getY();
            Vector2D detour = new Vector2D(
                    x1 + (b / c) * (x2 - x1),
                    y1 + (b / c) * (y2 - y1)
            );

            //Udregner enhedsvektoren af retningsvektoren fra krydset til detour punktet
            detour.subtract(cross_pos).toUnit();
            //Skalerer denne og flytter den i forhold til krydsets placering
            detour.scale(s).add(cross_pos);

            System.out.println("Path, detour is: "+detour);

            //Hvis bolden ikke er tæt på kanten, beregnes et nyt angrebspunkt med
            // udgangspunkt i linjen fra detour punktet til bolden
            if (!isCloseEdge && !isGoal && !inCross && !inCorner) {
                float D = Vector2D.Distance(detour, target);
                x1 = detour.getX();
                x2 = target.getX();
                y1 = detour.getY();
                y2 = target.getY();
                attackPoint = new Vector2D(
                        x2 - (d / D) * (x2 - x1),
                        y2 - (d / D) * (y2 - y1)
                );
                path.add(1, attackPoint);
            }

            //Tilføjer detour som vores næste punkt i stien
            path.add(1, detour);

        } else { //hvis nu vi ikke går igennem krydset, så tilføj det tidligere udregnet angrebspunkt
            path.add(1, attackPoint);
        }

        // sætter vores drawpath liste til en kopi af vores path liste
        drawpath = new ArrayList<>(path);

        System.out.println("Path, point order: ");
        for (Vector2D v : path){
            System.out.println(v+",");
        }
        System.out.println("Path, drawpath size: "+drawpath.size());

    }

    public float getLength() {
        if (path.size() < 2) //Return if path size is lower than 2.
            return 0;

        Vector2D start, end;
        start = path.get(0);
        float length = 0;
        for (int i = 1; i < path.size(); i++) {
            end = path.get(i);
            length += Vector2D.Distance(start, end);
            start = end;
        }

        return length;
    }

    public void add(Vector2D v) {
        path.add(1, v);
    }

    public Vector2D getLast() {
        return path.get(path.size() - 1);
    }

    public Vector2D getNext() {
        if (path.size() >= 2)
            return path.remove(1);
        else return null;
    }

    public int size() {
        return path.size();
    }

    @Override
    public void draw(GraphicsContext context) {
        if (drawpath.size() < 2) //Return if path size is lower than 2.
            return;

        context.setStroke(color);
        context.setLineWidth(WIDTH);
        Vector2D start, end;
        start = drawpath.get(0);
        for (int i = 1; i < drawpath.size(); i++) {
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
