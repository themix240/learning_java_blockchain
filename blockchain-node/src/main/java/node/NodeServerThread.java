package node;

import utils.MinedBlock;
import utils.NewBlock;

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
            Blockchain newBlockchian = (Blockchain) objectInputStream.readObject();
            if(newBlockchian.getSize() > bc.getSize()&& BlockchainUtils.validate(newBlockchian.getBlocks())){
                bc.replaceBlockchain(newBlockchian);
                System.out.println("Blockchain replaced!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            while(!Thread.currentThread().isInterrupted()) {
                MinedBlock newBlock = (MinedBlock) objectInputStream.readObject();
                System.out.println("New block recived");
                if(bc.acceptBlock(new NewBlock(newBlock.getTransactions(), newBlock.getPrevHash(), newBlock.getMagicNumber()))){
                    System.out.println("New block added to blockchain!");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
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
