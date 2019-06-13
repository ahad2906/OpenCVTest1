package websocket;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private boolean sucking;

    public void start() throws IOException {
        clientSocket = new Socket("10.42.0.176", 6789);
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    private void sendMessage(String msg) throws IOException {
        start();
        outToServer.writeBytes(msg + "\n");
        System.out.println("from server : "+inFromServer.readLine().toString());
    }

    public void driveForward(float dist) throws IOException {
        sendMessage(FORWARD+dist);
    }

    public void driveSlowForward(float dist) throws IOException {
        sendMessage(SLOW_FORWARD+dist);
    }

    public void driveBackward(float dist) throws  IOException {
        sendMessage(BACKWARD+dist);
    }

    public void turn(float angle) throws IOException {
        sendMessage((TURN+angle));
    }

    public void suck() throws IOException {
        if (!sucking){
            sendMessage(SUCK);
            sucking = true;
        }
    }

    public void blow() throws IOException {
        sucking = false;
        sendMessage(BLOW);
    }

    public void close() throws IOException {
        outToServer.close();
        clientSocket.close();
        inFromServer.close();

        sucking = false;
    }
}
