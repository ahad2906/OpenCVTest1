package websocket;

import visualisering.Objects.Robot;
import visualisering.Objects.SpaceObject;
import visualisering.Space.Vector2D;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RobotController {
    private Robot robot;
    private RobotSocket robotSocket;
    private boolean isTargeting, motorsStarted;

    public void start(){
        //Starter forbindelse til roboten
        robotSocket = new RobotSocket();
        try {
            robotSocket.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRobot(Robot robot){
        this.robot = robot;
    }

    public void setRobotTarget(Vector2D target){
        if (!isTargeting && robot != null){
            isTargeting = true;
            robot.setTarget(target);

            ScheduledExecutorService s = Executors.newScheduledThreadPool(1);
            s.schedule(new Runnable() {
                           @Override
                           public void run() {
                               try {
                                   robotSocket.turn(robot.getAngleToTarget());
                               } catch (IOException e) {
                                   e.printStackTrace();
                               }
                               try {
                                   wait(2000);
                               } catch (InterruptedException e) {
                                   e.printStackTrace();
                               }

                               //Kører mod target
                               float dist = robot.getDIstToTarget();
                               while (dist > 2){
                                   try {
                                       robotSocket.driveForward(2);
                                   } catch (IOException e) {
                                       e.printStackTrace();
                                   }
                                   try {
                                       wait(500);
                                   } catch (InterruptedException e) {
                                       e.printStackTrace();
                                   }
                                   dist = robot.getDIstToTarget();
                               }
                               isTargeting = false;
                           }
                       },
                    1, TimeUnit.MINUTES);
            s.shutdown();
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
