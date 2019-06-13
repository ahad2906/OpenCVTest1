package websocket;

import visualisering.Objects.Robot;
import visualisering.Space.Grid;
import visualisering.Space.Vector2D;

import java.io.IOException;

public class RobotController {
    private Robot robot;
    private Grid grid;
    private RobotSocket robotSocket;
    private Thread t;
    private boolean isTargeting, motorsStarted;

    public RobotController(Grid grid){
        this.grid = grid;
        this.robotSocket = new RobotSocket();
    }

    /*public void start(){
        //Starter forbindelse til roboten
        robotSocket = new RobotSocket();

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                if (robot != null && robot.getTarget() != null) {

                }
                try {
                    System.out.println("Thread is waiting...");
                    t.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t.start();
    }*/

    public void setRobot(Robot robot){
        this.robot = robot;
    }

    public void setRobotTarget(final Vector2D target){
        if (!isTargeting && robot != null){
            System.out.println("Robot is targeting target at pos: "+target);
            isTargeting = true;
            robot.setTarget(target);
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
            float dist = grid.translateLengthToMilimeters(robot.getDistToTarget()) / 10;
            System.out.println("Distance to target:  " + dist);
            try {
                robotSocket.driveForward(dist);
            } catch (IOException e) {
                e.printStackTrace();
            }
            isTargeting = false;
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
