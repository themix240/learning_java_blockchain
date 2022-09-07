package node;

import utils.MinedBlock;
import utils.NewBlock;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NodeServerThread extends Thread { //GS-wrong class name, it's not a thread
    private Blockchain bc;
    private final Socket serverSocket; // GS - ta zmienna jest wykorzystywana tylko do wypisania info w metodzie 'run', może nie warto tu przekazywać?

    public NodeServerThread(Blockchain bc, Socket serverSocket) {
        this.bc = bc;
        this.serverSocket = serverSocket;
    }

    @Override
    public void run() {
        String socketInfo = serverSocket.getInetAddress() + ":" + serverSocket.getPort();
        System.out.println("Server started at " + socketInfo);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(serverSocket.getInputStream())) {
            Blockchain newBlockchian = (Blockchain) objectInputStream.readObject();
            if(newBlockchian.getSize() > bc.getSize()&& BlockchainUtils.validate(newBlockchian.getBlocks())){
                bc.replaceBlockchain(newBlockchian);
                System.out.println("Blockchain replaced!");
            }

            while(!Thread.currentThread().isInterrupted()) {
                MinedBlock receivedBlock = (MinedBlock) objectInputStream.readObject();
                System.out.println("New block received from " + serverSocket.getInetAddress() + ":" + serverSocket.getPort());
                if(bc.acceptBlock(new NewBlock(receivedBlock.getTransactions(), receivedBlock.getPrevHash(), receivedBlock.getMagicNumber()))){
                    System.out.println("New block added to blockchain!");
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
