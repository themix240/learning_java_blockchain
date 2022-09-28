package client.miner;

import utils.blockchain.BlockchainData;
import utils.blockchain.NewBlock;
import utils.blockchain.User;

import java.util.concurrent.Callable;

public class MinerCallable implements Callable<NewBlock> {
    Miner miner;
    BlockchainData data;
    User user;

    public MinerCallable(BlockchainData data, User user) {
        this.data = data;
        this.user = user;
        miner = new Miner(data, user);
    }

    @Override
    public NewBlock call() throws Exception {
        return miner.Mine();
    }
}
