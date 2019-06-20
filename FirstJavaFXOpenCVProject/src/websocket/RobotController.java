package websocket;

import com.sun.istack.internal.NotNull;
import visualisering.Objects.Robot;
import visualisering.Space.Grid;
import visualisering.Space.Path;
import visualisering.Space.Vector2D;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private Robot robot;
    private Grid grid;
    private Path path;
    private RobotSocket robotSocket;
    private Thread t;
    private boolean isTargeting, isDone;
    private ScheduledExecutorService schedule;
    private final float MIN_DIST = 4f, OFFSET = 0.92f, BACK_DIST = 18f, MIN_ANGLE = 5f;

    public RobotController(Grid grid){
        this.grid = grid;
    }

    public void start(){
        //Starter forbindelse til roboten
        robotSocket = new RobotSocket();

        Runnable runnable = this::update;

        this.schedule = Executors.newSingleThreadScheduledExecutor();
        // Her sættes framerate (Runnable, initialDelay, framerate, tidsenhed )
        this.schedule.scheduleAtFixedRate(runnable, 0, 120, TimeUnit.MILLISECONDS);
    }

    public void update(){
        if (isDone){
            try{
                while (path.size() >= 2){
                    robot.setTarget(path.getNext());

                    System.out.println("Target is at "+robot.getTarget());

                    //Beregner vinklen til target
                    float angle = robot.getAngleToTarget();

                    //Beregner afstanden til target
                    float dist = grid.translateLengthToMilimeters(robot.getDistToTarget()) / 10;

                    //Drejer mod target og retter op hvis det er forkert
                    int i = 0;
                    while (i < 2 && angleCheck(Math.abs(angle), Math.abs(dist))) {
                        if (i < 1) {
                            robotSocket.turn(angle);
                        }
                        else {
                            robotSocket.turnSlow(angle);
                        }
                        angle = robot.getAngleToTarget();
                        i++;
                    }

                    //Hvis path'en er lavere end 2, er det en bold og derfor køres der langsomt
                    if (path.size() < 2){
                        //Hvis det drejer sig om et hjørne skal der bare køres fuld smader
                        robotSocket.driveSlowForward(dist
                                    - grid.translateLengthToMilimeters(robot.getHeight()) / 13);
                    }
                    //Ellers køres der alm hastighed mod target
                    else {
                        robotSocket.driveForward(dist*OFFSET);

                        //Tjekker om den har kørt langt nok, hvis ikke, så tilføjes punktet på igen
                        if (path.isCloseEdge()) {
                            dist = (grid.translateLengthToMilimeters(robot.getDistToTarget()) -
                                    grid.translateLengthToMilimeters(robot.getHeight()) / 2f) / 10;
                            if (dist > MIN_DIST) {
                                path.add(robot.getTarget());
                            }
                        }
                    }
                }

                //Beregner vinklen til target
                float angle = robot.getAngleToTarget();
                int i = 0;
                while (i < 2 && angle > 4) {
                    if (i < 1) {
                        robotSocket.turn(angle);
                    }
                    else {
                        robotSocket.turnSlow(angle);
                    }
                    angle = robot.getAngleToTarget();
                    i++;
                }

                //Spytter bolde ud
                robotSocket.blow();

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //
                robotSocket.driveBackward(20);

                isDone = false;
                isTargeting = false;
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
        else if (isTargeting) {

            try {
                //Sug de bolde
                robotSocket.suck();

                Vector2D target;
                while (path.size() >= 2){
                    robot.setTarget(path.getNext());

                    System.out.println("Target is at "+robot.getTarget());

                    //Beregner vinklen til target
                    float angle = robot.getAngleToTarget();

                    //Beregner afstanden til target
                    float dist = grid.translateLengthToMilimeters(robot.getDistToTarget()) / 10;

                    //Drejer mod target og retter op hvis det er forkert
                    int i = 0;
                    while (i < 2 && angleCheck(Math.abs(angle), Math.abs(dist))) {
                        if (!path.isInCross()) {
                            if (i < 1) {
                                robotSocket.turn(angle);
                            }
                            else {
                                robotSocket.turnSlow(angle);
                            }
                        }
                        else {
                            robotSocket.turnSlow(angle);
                        }
                        angle = robot.getAngleToTarget();
                        i++;
                        try {
                            Thread.sleep(80);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //Hvis path'en er lavere end 2, er det en bold og derfor køres der langsomt
                    if (path.size() < 2){
                        //Hvis det drejer sig om et hjørne skal der bare køres fuld smader
                        if (path.isInCorner()){
                            robotSocket.driveForward(dist);
                        }
                        //Hvis det drejer sig om en rute til krydset skal vi lige køre mindre fremad
                        else if (path.isInCross()){
                            robotSocket.driveSlowForward(dist
                                    - grid.translateLengthToMilimeters(robot.getHeight()) / 20 - 5f);
                        }
                        //Ellers kør normal langsom afstand
                        else {
                            robotSocket.driveSlowForward(dist
                                    - grid.translateLengthToMilimeters(robot.getHeight()) / 20 - 1f);
                        }
                    }
                    //Ellers køres der alm hastighed mod target
                    else {
                        robotSocket.driveForward(dist*OFFSET);

                        //Tjekker om den har kørt langt nok, hvis ikke, så tilføjes punktet på igen
                        if (path.isCloseEdge() || path.isInCross() || path.isInCorner()) {
                            dist = (grid.translateLengthToMilimeters(robot.getDistToTarget()) -
                                    grid.translateLengthToMilimeters(robot.getHeight()) / 2f) / 10;
                            if (dist > MIN_DIST) {
                                path.add(robot.getTarget());
                            }
                        }
                    }
                }

                //Hvis robotten har lavet en manøvre tæt på banderet bakker den
                if (path.isCloseEdge() || path.isInCross() || path.isInCorner()){
                    //Bak hurtigt ud hvis man var i et hjørne
                    if (path.isInCorner()){
                        if (path.getB_dir() == 1){
                            robotSocket.backLeft();
                        }
                        else {
                            robotSocket.backRight();
                        }
                    }
                    //Ellers bak langsomt ud
                    else
                        robotSocket.driveForward(-BACK_DIST);
                }

                isTargeting = false;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean angleCheck(float angle, float dist){
        return (dist < MIN_DIST && angle > MIN_ANGLE) || angle > 1;
    }

    public void setRobot(Robot robot){
        this.robot = robot;
    }

    public void target(@NotNull Path path){
        if (!isTargeting && robot != null){
            this.path = path;
            isDone = path.isGoal();
            isTargeting = true;
        }
    }

    public boolean isTargeting(){
        return isTargeting;
    }

    public void close(){
        try {
            robotSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        schedule.shutdown();
        try {
            schedule.awaitTermination(33, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        robotSocket = null;
    }
}
