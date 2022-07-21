package utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public final class MinedBlock implements Serializable {
    private static final long serialVersionUID = 3L;
    private final long timeStamp;
    private final long id;
    private final String hash;
    private final String prevHash;
    private final int magicNumber;

    private String applyHash() {
        return StringUtil.applySha256(this.toString());
    }
    @Override
    public String toString() {
        return prevHash + transactions + magicNumber;
    }

    private final List<Transaction> transactions;


    public long getTimeStamp() {
        return timeStamp;
    }

    public long getId() {
        return id;
    }

    public String getHash() {
        return hash;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public MinedBlock(NewBlock block, long id) {
        this.timeStamp = System.currentTimeMillis();
        this.id = id;
        this.prevHash = block.hashOfPrev;
        this.magicNumber = block.magicNumber;
        this.transactions = Collections.unmodifiableList(block.transactions);
        this.hash = applyHash();
    }

    public String getOwner() {
        return transactions.size() > 0 ? Base64.getEncoder().encodeToString(transactions.get(0).getReciver().getEncoded()) : "null";
    }
}
