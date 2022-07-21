package client;

import utils.BlockchainData;
import utils.NewBlock;
import utils.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MinerMaster implements Runnable {
    Client client;

    @Override
    public void run() {
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                  return;
                }
            }
        }
    }

    public MinerMaster(Client client) {
        this.client = client;
    }

    private ExecutorService initExecutorService() {
        return Executors.newFixedThreadPool(8,
                runnable -> {
                    Thread n = Executors.defaultThreadFactory().newThread(runnable);
                    n.setDaemon(true);
                    return n;
                });
    }
}
