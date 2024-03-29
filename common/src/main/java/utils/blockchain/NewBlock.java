package utils.blockchain;

import utils.crypto.StringUtil;

import java.io.Serializable;
import java.util.List;

/**
 * Mutable Block used in Blockchain.
 */
public class NewBlock implements Serializable {
     public List<Transaction> transactions;

    String hashOfPrev;

    public int magicNumber;
    public NewBlock(List<Transaction> transactions, String hashOfPrev, int magicNumber) {
        this.transactions = transactions;
        this.hashOfPrev = hashOfPrev;
        this.magicNumber = magicNumber;
    }

    public void appendTransaction(Transaction t){
        transactions.add(t);
    }
    @Override
    public String toString() {
        return hashOfPrev + transactions + magicNumber;
    }

    public String getHash() {
        return StringUtil.applySha256(this.toString());
    }
    public String getHashOfPrev() {
        return hashOfPrev;
    }
}
