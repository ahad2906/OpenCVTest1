package websocket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class RobotSocket {
    Socket clientSocket;
    DataOutputStream outToServer;
    private static final String
            FORWARD = "fremkor %d",
            SLOW_FORWARD = "langfremkor %d",
            BACKWARD = "tilbagekor %d",
            TURN = "drej %d",
            SUCK = "e",
            BLOW = "r",
            MUSIC = "sound";

    public void start() throws IOException {
        clientSocket = new Socket("172.20.10.9", 6789);
        outToServer = new DataOutputStream(clientSocket.getOutputStream());
        sendMessage(SUCK);
    }

    private void sendMessage(String msg) throws IOException {
        outToServer.writeBytes(msg + "\n");
    }

    public void driveForward(float dist) throws IOException {
        sendMessage(String.format(FORWARD, dist));
    }

    public void driveSlowForward(float dist) throws IOException {
        sendMessage(String.format(SLOW_FORWARD, dist));
    }

    public void driveBackward(float dist) throws  IOException {
        sendMessage(String.format(BACKWARD, dist));
    }

    public void turn(float angle) throws IOException {
        sendMessage((String.format(TURN, angle)));
    }

    public void close() throws IOException {
        outToServer.close();
        clientSocket.close();
    }
}
