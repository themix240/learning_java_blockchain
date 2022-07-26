package node;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class NodeServerThread implements Runnable {
    private Blockchain bc;
    private final Socket serverSocket;
    private final ObjectInputStream objectInputStream;

    @Override
    public void run() {
        System.out.println("Server started at " + serverSocket.getInetAddress() +":"+serverSocket.getPort());
        try {
            while(!Thread.currentThread().isInterrupted()) {
            Blockchain newBlockchain = (Blockchain) objectInputStream.readObject();
            if(BlockchainUtils.validate(newBlockchain.getBlocks()) && newBlockchain.getSize() > bc.getSize()) {
                bc.replaceBlockchain(newBlockchain);
                System.out.println("Blockchain replaced!");
            }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public NodeServerThread(Blockchain bc, Socket serverSocket) {
        this.bc = bc;
        this.serverSocket = serverSocket;
        try {
            objectInputStream = new ObjectInputStream(serverSocket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
