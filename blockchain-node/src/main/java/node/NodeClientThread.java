package node;

import utils.MinedBlock;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class NodeClientThread implements Runnable {
    private Blockchain bc;
    private final Socket socket;
    private final ObjectOutputStream objectOutputStream;
    private BlockingQueue<MinedBlock> blocksToSend;

    public NodeClientThread(Blockchain bc, Socket socket, BlockingQueue<MinedBlock> blocksToSend) {
        this.bc = bc;
        this.socket = socket;
        this.blocksToSend = blocksToSend;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        System.out.println("New node connection at " + socket.getInetAddress() + ":" + socket.getPort());
        System.out.println("Sending blockchain");
        try {
            objectOutputStream.writeObject(bc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            while(!Thread.currentThread().isInterrupted()) {
                MinedBlock toSend = blocksToSend.take();
                System.out.println("New Block sended");
                objectOutputStream.writeObject(toSend);
                objectOutputStream.flush();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
