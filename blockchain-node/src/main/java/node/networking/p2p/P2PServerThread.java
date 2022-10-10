package node.networking.p2p;

import node.core.Blockchain;
import node.utils.BlockchainUtils;
import utils.blockchain.MinedBlock;
import utils.blockchain.NewBlock;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * Class used in p2p networking.
 *
 */
public class P2PServerThread extends Thread {
    private Blockchain bc;
    private final Socket socket;

    public P2PServerThread(Blockchain bc, Socket serverSocket) {
        this.bc = bc;
        this.socket = serverSocket;
    }

    @Override
    public void run() {
        String socketInfo = socket.getInetAddress() + ":" + socket.getPort();
        System.out.println("Server started at " + socketInfo);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
            Blockchain newBlockchian = (Blockchain) objectInputStream.readObject();
            if (newBlockchian.getSize() > bc.getSize() && BlockchainUtils.validate(newBlockchian.getBlocks())) {
                bc.replaceBlockchain(newBlockchian);
                System.out.println("Blockchain replaced!");
            }

            while (!Thread.currentThread().isInterrupted()) {
                MinedBlock receivedBlock = (MinedBlock) objectInputStream.readObject();
                System.out.println("New block received from " + socket.getInetAddress() + ":" + socket.getPort());
                if (bc.acceptBlock(new NewBlock(receivedBlock.getTransactions(), receivedBlock.getPrevHash(), receivedBlock.getMagicNumber()))) {
                    System.out.println("New block added to blockchain!");
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
