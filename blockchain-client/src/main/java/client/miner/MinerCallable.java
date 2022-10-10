package client.miner;

import utils.blockchain.BlockchainData;
import utils.blockchain.NewBlock;
import utils.blockchain.User;

import java.util.concurrent.Callable;

/**
 * Miner functionality using Callable.
 * @see Miner
 * @author Morozowski Mikołaj
 * @version 1.0-SNAPSHOT
 */
public class MinerCallable implements Callable<NewBlock> {
    Miner miner;
    BlockchainData data;
    User user;

    /**
     * Creates new <code>Miner</code> with specified parameters.
     * @param data <code>BlockchainData</code> got from <code>Blockchain</code> at particular moment.
     * @param user User owner of the Miner.
     */
    public MinerCallable(BlockchainData data, User user) {
        this.data = data;
        this.user = user;
        miner = new Miner(data, user);
    }

    /**
     * Calls <code>Miner</code> method mine.
     * @return <code>New Block</code> which satisfies requirements specified in <code>BlockchainData</code>
     */
    @Override
    public NewBlock call() {
        return miner.Mine();
    }
}
