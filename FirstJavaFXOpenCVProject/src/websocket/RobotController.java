package websocket;

import visualisering.Objects.Robot;
import visualisering.Objects.SpaceObject;
import visualisering.Space.Grid;
import visualisering.Space.Vector2D;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private Robot robot;
    private Grid grid;
    private RobotSocket robotSocket;
    private Thread t;
    private boolean isTargeting, motorsStarted;

    public RobotController(Grid grid){
        this.grid = grid;
    }

    public void start(){
        //Starter forbindelse til roboten
        robotSocket = new RobotSocket();
        try {
            robotSocket.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        t = new Thread(() -> {
            if (robot.getTarget() != null) {
                //Sug de bolde
                try {
                    robotSocket.suck();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Drejer mod target
                float angle = robot.getAngleToTarget();
                System.out.println("Angle to target: " + angle);
                try {
                    robotSocket.turn(angle);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //Kører mod target
                float dist = grid.translateLengthToMilimeters(robot.getDIstToTarget()) / 10;
                System.out.println("Distance to target:  " + dist);
                try {
                    robotSocket.driveForward(dist);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isTargeting = false;
            }
            try {
                t.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t.start();
    }

    public void setRobot(Robot robot){
        this.robot = robot;
    }

    public void setRobotTarget(final Vector2D target){
        if (!isTargeting && robot != null){
            System.out.println("Robot is targeting target at pos: "+target);
            isTargeting = true;
            robot.setTarget(target);
            t.notify();
        }
    }


    public void close(){
        try {
            robotSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
