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
    private boolean isTargeting, motorsStarted;
    private ScheduledExecutorService schedule;
    private final float MIN_DIST = 5f, OFFSET = 4f, BACK_DIST = 8f;

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

            try {
                //Sug de bolde
                robotSocket.suck();

                Vector2D target;
                while (path.size() >= 2){
                    robot.setTarget(path.getNext());


                    //Beregner vinklen til target
                    float angle = robot.getAngleToTarget();
                    //Drejer mod target
                    robotSocket.turn(angle);

                    //Beregner afstanden til target
                    float dist = (grid.translateLengthToMilimeters(robot.getDistToTarget()) -
                            grid.translateLengthToMilimeters(robot.getHeight())/2) / 10;
                    //Hvis path'en er lavere end 2, er det en bold og derfor køres der langsomt
                    if (path.size() < 2){
                        robotSocket.driveSlowForward(dist);
                    }
                    //Ellers køres der alm hastighed mod target
                    else {
                        robotSocket.driveForward(dist);
                    }
                }

                //Hvis robotten har lavet en manøvre tæt på banderet bakker den
                if (path.isCloseEdge()){
                    robotSocket.driveBackward(BACK_DIST);
                }

                isTargeting = false;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void setRobot(Robot robot){
        this.robot = robot;
    }

    public void target(@NotNull Path path){
        if (!isTargeting && robot != null){
            this.path = path;
            isTargeting = true;
        }
    }

    public boolean isTargeting(){
        return isTargeting;
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
