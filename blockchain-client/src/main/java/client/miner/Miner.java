package client.miner;

import utils.blockchain.BlockchainData;
import utils.blockchain.NewBlock;
import utils.blockchain.Transaction;
import utils.blockchain.User;

import java.util.ArrayList;
import java.util.Random;

public class Miner {
    final BlockchainData data;
    final User user;

    public Miner(BlockchainData data, User user) {
        this.data = data;
        this.user = user;
    }

    public NewBlock Mine() {
        Random random = new Random();
        int magicNumber = random.nextInt();
        NewBlock block = new NewBlock(new ArrayList<>(data.getTransactions()), data.getPrevHash(), magicNumber);
        generateCoinbase(block);
        String startingZeros = "0".repeat(data.getNonce());
        while (!block.getHash().startsWith(startingZeros)) {
            magicNumber = random.nextInt();
            block.magicNumber = magicNumber;
        }
        return block;
    }

    private void generateCoinbase(NewBlock b) {
        Transaction coinbase = new Transaction(null, user.getPublicKey(), 100);
        coinbase.setId(0);
        b.appendTransaction(coinbase);
    }
}
