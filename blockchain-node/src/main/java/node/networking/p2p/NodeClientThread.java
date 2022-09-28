package node.networking.p2p;

import node.core.Blockchain;
import utils.blockchain.MinedBlock;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

public class NodeClientThread extends Thread {
    private Blockchain bc;
    private final Socket socket;
    private BlockingQueue<MinedBlock> blocksToSend;

    public NodeClientThread(Blockchain bc, Socket socket, BlockingQueue<MinedBlock> blocksToSend) {
        this.bc = bc;
        this.socket = socket;
        this.blocksToSend = blocksToSend;
    }

    @Override
    public void run() {
        String socketInfo = socket.getInetAddress() + ":" + socket.getPort();
        System.out.println("New node connection at " + socketInfo);
        System.out.println("Sending blockchain to " + socketInfo);

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            objectOutputStream.writeObject(bc);
            while(!Thread.currentThread().isInterrupted()) {
                MinedBlock toSend = blocksToSend.take();
                System.out.println("Sending new block to " + socketInfo);
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
