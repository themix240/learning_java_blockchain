package node.networking;

import node.core.Blockchain;
import node.networking.p2p.NodeClientThread;
import utils.blockchain.MinedBlock;
import utils.blockchain.User;

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
import java.util.function.Consumer;
import java.util.stream.Stream;


public class ConnectionHandler implements Runnable {
    private final ServerSocket server, serverSocket;
    private List<User> users = new ArrayList<>();

    private Blockchain bc;
    private final String path;
    private final String ipsPath;
    private Set<InetSocketAddress> connectedNodes = new HashSet<>();

    private  BlockingQueue<MinedBlock> blocksToSend;
    private List<BlockingQueue<MinedBlock>> connectedClientsBlockingQueues;
    private List<Thread> runningThreads = new ArrayList<>();
    private Timer timer;

    public ConnectionHandler(int port, int p2pPort, Blockchain bc, String path, String ipsPath) {
        try {
            this.bc = bc;
            server = new ServerSocket(port);
            serverSocket = new ServerSocket(p2pPort);
            this.path = path;
            this.ipsPath = ipsPath;
            blocksToSend = bc.blockToSend;
            connectedClientsBlockingQueues = new ArrayList<>();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

    }
    @Override
    public void run() {
        Thread minedBlocksRefresherThread = getMinedBlocksRefresher();
        minedBlocksRefresherThread.start();
        runningThreads.add(minedBlocksRefresherThread);
        Thread p2pHandlerThread = getP2pHandler();
        p2pHandlerThread.start();
        runningThreads.add(p2pHandlerThread);
        timer = new Timer();
        timer.scheduleAtFixedRate(getTask(), 10000, 100000);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = server.accept();
                loadUsers();
                Thread newClientConnectionThread = new Thread(
                        new ClientSocketCommunicationHandler(bc,socket,path, users)
                );
                newClientConnectionThread.setName("Client Communication Thread");
                newClientConnectionThread.start();
            } catch (IOException ex) {
                return;
            }
        }
    }

    private TimerTask getTask() {
        return new TimerTask() {
            @Override
            public void run() {
                //GS System.out.println("Trying to connect to other nodes");
                initConnectionsToNodes();
            }
        };
    }

    private Thread getP2pHandler() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket p2p = serverSocket.accept();
                    } catch (IOException e) {
                        return;
                    }
                }
            }
        }, "Blockchain P2P Handler");
    }

    private Thread getMinedBlocksRefresher() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        MinedBlock block = blocksToSend.take();
                        connectedClientsBlockingQueues.forEach(bq -> {
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
        }, "Blockchain Mined Blocks Refresher");
    }

    private void initConnectionsToNodes()  { //Trying to implement p2p networking
        if (checkNodesFile()) return;
        try (Stream<String> input = Files.lines(Path.of(ipsPath))) {
            input.map(s -> s.split(":"))
                    .map(split -> new InetSocketAddress(split[0], Integer.parseInt(split[1])))
                    .forEach(getInetSocketAddressConsumer()); //GS the results of 'getInetSocketAddressConsumer()' are not used
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Consumer<InetSocketAddress> getInetSocketAddressConsumer() { //GS-wynik nie jest uzywany do niczego
        return address -> { //GS - use lowercase name
            try {
                if (connectedNodes.contains(address)) {
                    //GS System.out.println("Already connected!");
                    return;
                }
                Socket clientSocket = new Socket(address.getAddress(), address.getPort());
                connectedNodes.add(address);
                BlockingQueue<MinedBlock> minedBlocksQueueForNewThread = new ArrayBlockingQueue<>(1);
                connectedClientsBlockingQueues.add(minedBlocksQueueForNewThread);
                Thread nodeClient = new Thread(new NodeClientThread(bc, clientSocket, minedBlocksQueueForNewThread));
            } catch (ConnectException e) {
                //GS System.out.println("Connection refused at " + Adress.getAddress() + ":" + Adress.getPort());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    private boolean checkNodesFile() {
        if(!Files.exists(Path.of(ipsPath))){
            File ips = new File(ipsPath);
            ips.getParentFile().mkdirs();
            try {
                ips.createNewFile();
            } catch (IOException e) {
                return true;
            }
        }
        return false;
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

    public void shutdown() {

        try {
            server.close();
            for (Thread runningThread : runningThreads) {
                runningThread.join(10);
            }
            timer.cancel();
            serverSocket.close();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
