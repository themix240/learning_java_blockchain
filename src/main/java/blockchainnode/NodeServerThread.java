package blockchainnode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class NodeServerThread implements Runnable {
    private Blockchain bc;
    private final Socket serverSocket;
    private final ObjectInputStream objectInputStream;
    @Override
    public void run() {
        System.out.println("Server started!");
        try {
            Blockchain nowy = (Blockchain) objectInputStream.readObject();
            System.out.println(nowy.getSize());
            if(nowy.getSize() >= bc.getSize() && nowy.validate()) {
                System.out.println("Blockchain verified");
            }
            //TODO wait for blocks and if correct append them to blockchain
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
