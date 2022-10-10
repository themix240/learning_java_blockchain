package utils.blockchain;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Contains data from blockchain at current state.
 * Miner can have outdated data at time of mining.
 * <p>Immutable object</p>
 */
public final class BlockchainData implements Serializable {
    final  private int nonce;
    final private List<Transaction> transactions;
    final private int size;
    final private String prevHash;

    /**
     * Creates immutable object with given parameters.
     * @param nonce amount of required starting zeros in hash of <code>minedBlock</code>.
     * @param transactions immutable list of waiting <code>transactions</code> in blockchain.
     * @param size current size of blockchain blocks.
     * @param prevHash hash of previous block in <code>Blockchain</code>.
     * @see Transaction
     */
    public BlockchainData(int nonce, List<Transaction> transactions, int size, String prevHash) {
        this.nonce = nonce;
        this.transactions = Collections.unmodifiableList(transactions);
        this.size = size;
        this.prevHash = prevHash;
    }

    /**
     * Nonce is amount of required starting zeros of hash in blockchain. Must be equal or greater than zero.
     * Set automatically by <code>Blockchain</code>, based on time between two last mined blocks.
     * Nonce is name suggested by <em>Satoshi Nakamoto</em> in his blockchain white paper.
     * @return number of required starting zeros.
     */
    public int getNonce() {
        return nonce;
    }

    /**
     * List of <code>Transactions</code> waiting to be added to next accepted block.
     * <p>Transactions are added to block after coinbase transaction in order in which they appeared.
     * After adding <code>Transactions</code> to accepted block waiting list in <code>Blockchain</code> is cleared.
     * List can be empty. Coinbase transaction is generated at mining stage.</p>
     * @return Immutable list of transactions.
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * Number of blocks in <code>Blockchain</code> at time of creating <code>BlockchainData</code>.
     * @return number of blocks in <code>Blockchain</code>.
     */
    public int getSize() {
        return size;
    }

    /**
     * SHA-256 hash of previous block is needed for generating hash of next block.
     * <p>If block has wrong hash of previous block it will be rejected by <code>Blockchain</code>.
     * It prevents attacks with staged blockchain blocks, because attacker will have to recreate all previous blocks to match their hashes.
     * Which is computationally expensive, and requires from attacker more compute power than all <code>Blockchain</code> nodes and it's clients combined.</p>
     *
     * @return SHA-256 hash of previous block.
     */
    public String getPrevHash() {
        return prevHash;
    }
}
