package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Block implements Serializable {
    private static final long serialVersionUID = 3L;
    private final long timeStamp;
    private final long id;
    private final String hash;
    private final String prevHash;
    private final int magicNumber;
    private List<Transaction> transactions = new ArrayList<>();


    public Block(long timeStamp, long id, String hash_of_prev, int magicNumber) {
        this.timeStamp = timeStamp;
        this.id = id;
        this.prevHash = hash_of_prev;
        this.magicNumber = magicNumber;
        this.hash = Hash();
    }


    @Override
    public String toString() {
        return timeStamp + prevHash + id + magicNumber;
    }

    public String Hash() {
        return StringUtil.applySha256(this.toString());
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
    public void appendTransaction(Transaction t){
        transactions.add(t);
    }
}
