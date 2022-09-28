package utils.blockchain;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public final class BlockchainData implements Serializable {
    final int nonce;
    final List<Transaction> transactions;
    final int size;
    final String prevHash;

    public BlockchainData(int nonce, List<Transaction> transactions, int size, String prevHash) {
        this.nonce = nonce;
        this.transactions = Collections.unmodifiableList(transactions);
        this.size = size;
        this.prevHash = prevHash;
    }

    public int getNonce() {
        return nonce;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public int getSize() {
        return size;
    }

    public String getPrevHash() {
        return prevHash;
    }
}
