package node;

import utils.MinedBlock;
import utils.User;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;


public class ConnectionHandler implements Runnable {
    private final ServerSocket server, serverSocket;
    private List<User> users = new ArrayList<>();
    private final ExecutorService executorService;

    private final Blockchain bc;
    private final String path;
    private final String ips_path;
    private HashMap<InetSocketAddress, Boolean> connectedNodes = new HashMap<>();

    private final BlockingQueue<MinedBlock> blocksToSend;
    private List<BlockingQueue<MinedBlock>> clientBlocksToSend;
    private Thread minedBlocksRefresher;
    private Timer timer;
    private Thread p2pHandler;

    public ConnectionHandler(int port, int p2pPort, Blockchain bc, String path, String ips_path) {
        try {
            this.bc = bc;
            executorService = Executors.newFixedThreadPool(8);
            server = new ServerSocket(port);
            serverSocket = new ServerSocket(p2pPort);
            this.path = path;
            this.ips_path = ips_path;
            blocksToSend = bc.blockToSend;
            clientBlocksToSend = new ArrayList<>();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

    }
    public void shutdown() {

        executorService.shutdownNow();
        try {
            server.close();
            p2pHandler.interrupt();
            minedBlocksRefresher.interrupt();
            p2pHandler.join();
            minedBlocksRefresher.join();
            timer.cancel();
            serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {
         minedBlocksRefresher = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        MinedBlock block = blocksToSend.take();
                        clientBlocksToSend.forEach(bq -> {
                            try {
                                bq.put(block);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (InterruptedException e) {
                        shutdown();
                        return;
                    }
                }
            }
        });
         p2pHandler = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket p2p = serverSocket.accept();
                        executorService.submit(new NodeServerThread(bc, p2p));
                    } catch (IOException e) {
                        return;
                    }
                }
            }
        });
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Trying to connect to other nodes");
                initConnectionsToNodes();
            }
        }, 10000, 100000);
        p2pHandler.setDaemon(true);
        minedBlocksRefresher.setDaemon(true);
        p2pHandler.start();
        minedBlocksRefresher.start();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = server.accept();
                loadUsers();
                executorService.submit(new ClientSocketCommunicationHandler(bc, socket, path, users));
            } catch (IOException ex) {
                return;
            }
        }
    }

    private void initConnectionsToNodes()  { //Trying to implement p2p networking
        if(!Files.exists(Path.of(ips_path))){
            File ips = new File(ips_path);
            ips.getParentFile().mkdirs();
            try {
                ips.createNewFile();
            } catch (IOException e) {
               return;
            }
        }
        try (Stream<String> input = Files.lines(Path.of(ips_path))) {
            input.map(s -> s.split(":"))
                    .map(split -> new InetSocketAddress(split[0], Integer.parseInt(split[1])))
                    .forEach(Adress -> {
                        try {
                            if (connectedNodes.get(Adress) != null) {
                                System.out.println("Already connected!");
                                return;
                            }
                            Socket clientSocket = new Socket(Adress.getAddress(), Adress.getPort());
                            connectedNodes.put(Adress, true);
                            BlockingQueue<MinedBlock> x = new ArrayBlockingQueue<MinedBlock>(1);
                            clientBlocksToSend.add(x);
                            executorService.submit(new NodeClientThread(bc, clientSocket, x));
                        } catch (ConnectException e) {
                            System.out.println("Connection refused at " + Adress.getAddress() + ":" + Adress.getPort());
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
