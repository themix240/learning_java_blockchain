package node.networking;

import node.core.Blockchain;
import node.networking.p2p.P2PClientThread;
import node.networking.p2p.P2PServerThread;
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
import java.util.stream.Stream;

/**
 * Manages all upcoming connections - from clients and from other nodes (p2p networking).
 * <p>Tries to connect to ip:port pairs listed in file with ips.</p>
 * Main class for managing networking.
 *
 * @see P2PClientThread
 * @see P2PServerThread
 * @see ClientNetworkingLogic
 */
public class ConnectionHandler implements Runnable {
    private final ServerSocket clientServerSocket, p2pServerSocket;
    private List<User> users = new ArrayList<>();

    private Blockchain bc;
    private final String path;
    private final String ipsPath;
    private Set<InetSocketAddress> connectedNodes = new HashSet<>();

    private BlockingQueue<MinedBlock> blocksToSend;
    private List<BlockingQueue<MinedBlock>> connectedClientsBlockingQueues;
    private List<Thread> runningThreads = new ArrayList<>();
    private Timer timer;

    /**
     * Creates <code>ConnectionHandler</code> with 2 new <code>ServerSockets</code>
     * with given ports. First for client connections, second one for p2p connections.
     * <p>Also initializes list of blocking queues for upcoming connections from other nodes.</p>
     *
     * @param port    port on which clients can connect.
     * @param p2pPort port on which p2p nodes can connect.
     * @param bc      reference to blockchain which data will be exchanged with other nodes and clients.
     * @param path    path for file with list of users and public keys.
     * @param ipsPath path for file with ips of other nodes.
     */
    public ConnectionHandler(int port, int p2pPort, Blockchain bc, String path, String ipsPath) {
        try {
            this.bc = bc;
            clientServerSocket = new ServerSocket(port);
            p2pServerSocket = new ServerSocket(p2pPort);
            this.path = path;
            this.ipsPath = ipsPath;
            blocksToSend = bc.blockToSend;
            connectedClientsBlockingQueues = new ArrayList<>();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

    }

    /**
     * Receives upcoming connections, starts multiple Threads:
     * <ul>
     * <li>Thread for refreshing blocks in <code>BlockingQueues</code> for p2p networking. {@link #getMinedBlocksRefresher()}</li>
     * <li>Thread for managing p2p upcoming connections. {@link #getP2pHandler()}</li>
     * <li>Timer thread which tries to connect to listed ips in path file every 10000 ms. {@link #getTask()}</li>
     * </ul>
     * <p>
     * Then this thread is managing client connections to node.
     * <code>ServerThread</code> listens on port specified  in constructor then accepts upcoming connections and starts new <code>Thread</code>
     * with {@link node.core.ClientLogic} which handles communication between node and Client.
     * </p>
     */
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
                Socket socket = clientServerSocket.accept();
                loadUsers();
                Thread newClientConnectionThread = new Thread(
                        new ClientNetworkingLogic(bc, socket, path, users)
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
                initConnectionsToNodes();
            }
        };
    }

    /**
     * Returns <code>Thread</code> which manages upcoming p2p connections.
     * <code>SocketServer</code> listens for connections and if any occur, accepts them and starts new {@link P2PServerThread}
     * then adds this <code>Thread</code> to list of running Threads.
     * @return <code>Thread which manages p2p connections.</code>
     */
    private Thread getP2pHandler() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket p2p = p2pServerSocket.accept();
                        Thread p2pServerThread = new Thread(new P2PServerThread(bc, p2p));
                        p2pServerThread.setName("P2P Server: " + p2p.getInetAddress() + " Thread");
                        runningThreads.add(p2pServerThread);
                        p2pServerThread.start();
                    } catch (IOException e) {
                        return;
                    }
                }
            }
        }, "Blockchain P2P Handler");
    }

    /**
     * Refreshes <code>minedBlocks</code> to be sent in each of running <code>Threads</code>.
     *<p>
     * Each {@link P2PClientThread P2PClientThread} has own <code>BlockingQueue</code> with blocks and <code>Blockchain</code> has only one.
     * To integrate <code>Blockchain BlockingQueue</code> with this in each {@link P2PClientThread P2PClientThread} this <code>Thread</code> takes <code>minedBlock</code>
     * from <code>BlockingQueue</code> shared with <code>Blockchain</code> and puts this block to <code>BlockingQueue</code> of any running {@link P2PClientThread P2PClientThread}.
     *</p>
     * @return Thread which updates every Blocking Queue in p2p networking with value from blocking queue shared with blockchain.
     */
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

    /**
     * Takes content from file with ips and tries to {@link #initClientThread(InetSocketAddress) start client Thread} with given address.
     */
    private void initConnectionsToNodes() {
        if (checkIpsFile()) return;
        try (Stream<String> input = Files.lines(Path.of(ipsPath))) {
            input.map(s -> s.split(":"))
                    .map(split -> new InetSocketAddress(split[0], Integer.parseInt(split[1])))
                    .forEach(this::initClientThread);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts {@link P2PClientThread} with given address.
     * Adds this address to <code>HashSet</code> to avoid trying to connect to already connected p2p server.
     * Creates new <code>BlockingQueue</code> for this <code>Thread</code> to use and adds it to list of ClientBlockingQueues.
     * Adds this <code>Thread</code> to list of running <code>Threads</code>.
     * @param address
     */
    private void initClientThread(InetSocketAddress address) {
        try {
            if (connectedNodes.contains(address)) {
                return;
            }
            Socket clientSocket = new Socket(address.getAddress(), address.getPort());
            connectedNodes.add(address);
            BlockingQueue<MinedBlock> minedBlocksQueueForNewThread = new ArrayBlockingQueue<>(1);
            connectedClientsBlockingQueues.add(minedBlocksQueueForNewThread);
            Thread p2pClient = new Thread(new P2PClientThread(bc, clientSocket, minedBlocksQueueForNewThread));
            runningThreads.add(p2pClient);
            p2pClient.setName("p2pClient " + address + " Thread");
            p2pClient.start();
        } catch (ConnectException e) {
            // System.out.println("Connection refused at " + Adress.getAddress() + ":" + Adress.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks if file with ips exists. If not creates new one.
     * @return <code>false</code> if File exists or is successfully created; <code>true</code>
     */
    private boolean checkIpsFile() {
        if (!Files.exists(Path.of(ipsPath))) {
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

    /**
     * Loads users from path specified in constructor.
     */
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

    /**
     * Shutdowns all running Threads.
     * Then closes <code>ServerSockets</code>.
     */
    public void shutdown() {

        try {
            for (Thread runningThread : runningThreads) {
                runningThread.join(10);
            }
            timer.cancel();
            clientServerSocket.close();
            p2pServerSocket.close();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
