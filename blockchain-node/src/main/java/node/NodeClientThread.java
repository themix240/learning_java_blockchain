package node;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NodeClientThread implements Runnable {
private Blockchain bc;
private final Socket socket;
private final ObjectOutputStream objectOutputStream;

    public NodeClientThread(Blockchain bc, Socket socket) {
        this.bc = bc;
        this.socket = socket;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        System.out.println("Client started!");
        //TODO when new block occurs send it to other peers
    }
}
