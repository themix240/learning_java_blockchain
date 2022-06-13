package blockchain;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ServerThread implements Runnable {
    private final ServerSocket server;
    private final List<String> transactions = new ArrayList<>();
    private List<User> users = new ArrayList<>();
    private final ExecutorService executorService;

    private final Blockchain bc;

    public ServerThread(int port, Blockchain bc) {
        try {
            this.bc = bc;
            executorService = Executors.newFixedThreadPool(8);
            server = new ServerSocket(port);
            server.setSoTimeout(1000);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Socket socket = server.accept();
                loadUsers();
                executorService.submit(new ClientThread(socket, transactions, users, bc));
            } catch (IOException ex) {
                //System.out.prinddtln("IOException" + ex);
            }
        }
    }

    private void loadUsers() {
        String path = "/Users/themix240/blockchain/server_data/userDB.txt";
        File f = new File(path);
        FileInputStream fileInputStream = null;
        ObjectInputStream objectInput = null;
        if (!f.exists())
            return;
        try {
            fileInputStream = new FileInputStream(f);
            objectInput = new ObjectInputStream(fileInputStream);
            users = (List<User>) objectInput.readObject();


        } catch (EOFException e) {
            System.out.println(e);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
