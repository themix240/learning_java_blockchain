package client.miner;

import client.connection.Client;
import utils.blockchain.BlockchainData;
import utils.blockchain.NewBlock;
import utils.blockchain.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class which manages Miners.
 * Uses ExecutorService
 * @see ExecutorService
 * @author Morozowski Mikołaj
 * @version 1.0-SNAPSHOT
 */
public class MinerMaster implements Runnable {
    Client client;

    /**
     * Default constructor.
     * @param client client connected to blockchain, owner of MinerMaster.
     */
    public MinerMaster(Client client) {
        this.client = client;
    }

    /**
     * Starts mining if mining flag is <code>true</code>.
     * <p>Inits <code>ExecutorService</code> then adds 8 <code>MinerCallable</code> objects, if any of
     * <code>Callable</code> returns <code>NewBlock</code>, shutdowns
     * <code>ExecutorService</code> and sends this <code>NewBlock</code> to blockchain</p>
     * @see MinerCallable
     * @see ExecutorService
     */
    @Override
    public void run()  {
        while (!Thread.currentThread().isInterrupted()) {
            if (client.mining) {
                try {
                    ExecutorService es = initExecutorService();
                    BlockchainData data = client.startMining();
                    User user = client.getUser();
                    NewBlock minedBlock = es.invokeAny(Arrays.asList(
                            new MinerCallable(data, user),
                            new MinerCallable(data, user),
                            new MinerCallable(data, user),
                            new MinerCallable(data, user),
                            new MinerCallable(data, user),
                            new MinerCallable(data, user),
                            new MinerCallable(data, user),
                            new MinerCallable(data, user)
                    ));
                    client.writeBlockData(minedBlock);
                    es.shutdownNow();
                } catch (IOException | ClassNotFoundException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Initializes new <code>ExecutorService</code> with 8 <code>Threads</code>
     * @return Initialized <code>ExecutorService</code>
     */
    private ExecutorService initExecutorService() {
        return Executors.newFixedThreadPool(8,
                runnable -> {
                    Thread n = Executors.defaultThreadFactory().newThread(runnable);
                    n.setName("Blockchain Miner Master Executor Thread");
                    return n;
                });
    }
}
