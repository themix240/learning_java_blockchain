package node;

import utils.User;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;


public class ConnectionHandler implements Runnable {
    private final ServerSocket server,serverSocket;
    private List<User> users = new ArrayList<>();
    private final ExecutorService executorService;

    private final Blockchain bc;
    private final String path;
    private final String ips_path;
    private HashMap<InetSocketAddress, Boolean> connectedNodes = new HashMap<>();
    public ConnectionHandler(int port, int p2pPort, Blockchain bc, String path,String ips_path) {
        try {
            this.bc = bc;
            executorService = Executors.newFixedThreadPool(8);
            server = new ServerSocket(1337);
            serverSocket = new ServerSocket(6000);
            server.setSoTimeout(1000);
            this.path = path;
            this.ips_path = ips_path;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

    }

    @Override
    public void run() {
        Thread p2pHandler =new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()){
                    try {
                        Socket p2p= serverSocket.accept();
                        executorService.submit(new NodeServerThread(bc,p2p));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Trying to connect to other nodes");
                    initConnectionsToNodes();
            }
        },10000,100000);

        p2pHandler.start();
        while (!Thread.interrupted()) {
            try {
                Socket socket = server.accept();
                loadUsers();
                executorService.submit(new ClientThread(socket, users, bc, path));
            } catch (IOException ex) {
                //System.out.prinddtln("IOException" + ex);
            }
        }
    }

    private void initConnectionsToNodes() { //Trying to implement p2p networking
        try(Stream<String> input = Files.lines(Path.of(ips_path))){
            input.map(s -> s.split(":"))
                    .map(split -> new InetSocketAddress(split[0],Integer.parseInt(split[1])))
                    .forEach(Adress -> {
                        try {
                            if(connectedNodes.get(Adress)!=null) {
                                System.out.println("Already connected!");
                                return;
                            }
                            Socket clientSocket = new Socket(Adress.getAddress(),6001 );
                            connectedNodes.put(Adress,true);
                            executorService.submit(new NodeClientThread(bc, clientSocket));
                        } catch (ConnectException e) {
                           System.out.println("Connection refused at "+Adress.getAddress()+":"+Adress.getPort());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void loadUsers() {
        File f = new File(path);
        FileInputStream fileInputStream;
        ObjectInputStream objectInput;
        if (!f.exists()) return;
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
