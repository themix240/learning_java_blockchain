package client;

import utils.BlockchainData;
import utils.NewBlock;

import java.util.concurrent.Callable;

public class MinerCallable implements Callable {
    Miner miner;
    BlockchainData data;

    public MinerCallable(BlockchainData data) {
        this.data = data;
        miner = new Miner(data);
    }

    @Override
    public NewBlock call() throws Exception {
        return miner.Mine();
    }
}
