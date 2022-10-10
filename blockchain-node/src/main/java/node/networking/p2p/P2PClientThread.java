package node.networking.p2p;

import node.core.Blockchain;
import utils.blockchain.MinedBlock;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Class used in p2p networking.
 * It is thread which sends new block to other <code>Blockchain</code> nodes after <code>Blockchain</code> get one from miner client.
 */
public class P2PClientThread extends Thread {
    private Blockchain bc;
    private final Socket socket;
    private BlockingQueue<MinedBlock> blocksToSend;

    /**
     * Creates p2p Client Thread.
     * @param bc reference to blockchain.
     * @param socket socket on which is connected to p2p server.
     * @param blocksToSend <code>blockingQueue</code> with new blocks accepted by <code>Blockchain</code>
     */
    public P2PClientThread(Blockchain bc, Socket socket, BlockingQueue<MinedBlock> blocksToSend) {
        this.bc = bc;
        this.socket = socket;
        this.blocksToSend = blocksToSend;
    }

    /**
     * Main loop of Client Thread.
     * Waits for blocks to occur in blockingQueue, then take one and sends it to connected p2p node server.
     */
    @Override
    public void run() {
        String socketInfo = socket.getInetAddress() + ":" + socket.getPort();
        System.out.println("New node connection at " + socketInfo);
        System.out.println("Sending blockchain to " + socketInfo);

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())) {
            objectOutputStream.writeObject(bc);
            while (!Thread.currentThread().isInterrupted()) {
                MinedBlock toSend = blocksToSend.take();
                System.out.println("Sending new block to " + socketInfo);
                objectOutputStream.writeObject(toSend);
                objectOutputStream.flush();
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
