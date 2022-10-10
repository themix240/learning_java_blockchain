package client.miner;

import utils.blockchain.BlockchainData;
import utils.blockchain.NewBlock;
import utils.blockchain.Transaction;
import utils.blockchain.User;

import java.util.ArrayList;
import java.util.Random;

/**
 * Mines new block for blockchain.
 * Gets current state of blockchain and mines block which satisfies these requirements.
 * Mined block is not guaranteed to be accepted by blockchain (state of blockchain can change in time of mining).
 * @author Mikołaj Morozowski
 * @version 1.0-SNAPSHOT
 */
public class Miner {
    final BlockchainData data;
    final User user;
/**
 * Constructor of <code>Miner</code>Class
 * @param data Blockchain data required for mining block
 * @param user user which started mining "owner" of the miner, blocks mined by Miner will generate currency for this user wallet.
 * @see BlockchainData
 * @see User
 */
    public Miner(BlockchainData data, User user) {
        this.data = data;
        this.user = user;
    }

    /**
     * Hash of block consist transactions, hash of previous block and
     * magicNumber - randomized number added to satisfy number of starting zeros required
     * for valid block by blockchain.
     * Mining randomize this number until number of starting zeros equals nonce in blockchain.
     * @return mined block which satisfies requirements in blockchain data.
     */
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

    /**
     * Method which generates coinbase transaction - transaction which introduces new currency into blockchain,
     * added automatically, always is first transaction in mined block. Currency is added to owner (user who started mining) wallet.
     * @param b freshly mined block without any transactions
     */
    private void generateCoinbase(NewBlock b) {
        Transaction coinbase = new Transaction(null, user.getPublicKey(), 100);
        coinbase.setId(0);
        b.appendTransaction(coinbase);
    }
}
