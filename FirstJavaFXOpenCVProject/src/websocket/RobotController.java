package websocket;

import com.sun.istack.internal.NotNull;
import visualisering.Objects.Robot;
import visualisering.Space.Grid;
import visualisering.Space.Vector2D;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private Robot robot;
    private Grid grid;
    private RobotSocket robotSocket;
    private Thread t;
    private boolean isTargeting, motorsStarted;
    private ScheduledExecutorService schedule;
    private final float MIN_DIST = 8f, OFFSET = 2f;

    public RobotController(Grid grid){
        this.grid = grid;
    }

    public void start(){
        //Starter forbindelse til roboten
        robotSocket = new RobotSocket();

        Runnable runnable = this::update;

        this.schedule = Executors.newSingleThreadScheduledExecutor();
        // Her sættes framerate (Runnable, initialDelay, framerate, tidsenhed )
        this.schedule.scheduleAtFixedRate(runnable, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void update(){
        if (isTargeting) {
            //Sug de bolde
            try {
                robotSocket.suck();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Drejer mod target
            float angle = robot.getAngleToTarget();
            System.out.println("Angle to target: " + angle+ " robot angle: "+robot.getRotation());
            try {
                robotSocket.turn(angle);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Kører mod target
            float dist = (grid.translateLengthToMilimeters(robot.getDistToTarget()) -
                    grid.translateLengthToMilimeters(robot.getHeight())/2) / 10 - OFFSET;
            System.out.println("Distance to target:  " + dist);
            try {
                if (dist > MIN_DIST){
                    robotSocket.driveForward(dist-MIN_DIST);
                    robotSocket.driveSlowForward(MIN_DIST);
                }
                else {
                    robotSocket.driveSlowForward(dist);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            isTargeting = false;
        }
    }

    public void setRobot(Robot robot){
        this.robot = robot;
    }

    public void setRobotTarget(@NotNull final Vector2D target){
        if (!isTargeting && robot != null){
            System.out.println("Robot is targeting target at pos: "+target);
            robot.setTarget(target);
            isTargeting = true;
        }
    }


    public void close(){
    schedule.shutdown();
        try {
            schedule.awaitTermination(33, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            robotSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
