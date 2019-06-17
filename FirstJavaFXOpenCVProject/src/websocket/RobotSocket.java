package websocket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;

public class RobotSocket {
    Socket clientSocket;
    DataOutputStream outToServer;
    BufferedReader inFromServer;
    private static final String
            FORWARD = "fremkor ",
            SLOW_FORWARD = "langfremkor ",
            BACKWARD = "tilbagekor ",
            TURN = "drej ",
            SUCK = "e",
            BLOW = "r",
            MUSIC = "sound";

    public void start() throws IOException {
        while (true){
            try {
                clientSocket = new Socket("192.168.137.81", 6789);
                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                break;
            }
            catch (ConnectException e){
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String msg) throws IOException {
        start();
        outToServer.writeBytes(msg + "\n");
        System.out.println("from server : "+inFromServer.readLine().toString());
    }

    public void driveForward(float dist) throws IOException {
        System.out.println("Socket, drive norm: "+dist+"cm");
        sendMessage(FORWARD+dist);
    }

    public void driveSlowForward(float dist) throws IOException {
        System.out.println("Socket, drive slow: "+dist+"cm");
        sendMessage(SLOW_FORWARD+dist);
    }

    public void driveBackward(float dist) throws  IOException {
        System.out.println("Socket, backup: "+dist+"cm");
        sendMessage(BACKWARD+dist);
    }

    public void turn(float angle) throws IOException {
        sendMessage((TURN+angle));
    }

    public void suck() throws IOException {
        sendMessage(SUCK);
    }

    public void blow() throws IOException {
        sendMessage(BLOW);
    }

    public void stop() throws IOException {
        blow();
        start();
        outToServer.writeBytes("exit\n");
    }


    public void close() throws IOException {
        stop();
        outToServer.close();
        clientSocket.close();
        inFromServer.close();
    }
}
