package blockchain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Block implements Serializable {
    private static final long serialVersionUID = 3L;
    private long timeStamp;
    private long id;
    private String hash;
    private String prevHash;
    private int magicNumber;
    private String signature;
    private List<Transaction> messages = new ArrayList<>();

    public String getSignature() {
        return signature;
    }

    public Block(long timeStamp, long id, String hash_of_prev, int magicNumber, String signature) {
        this.timeStamp = timeStamp;
        this.id = id;
        this.prevHash = hash_of_prev;
        this.magicNumber = magicNumber;
        this.signature = signature;
        this.hash = Hash();
    }


    public String Hash() {
        return StringUtil.applySha256(this.toString());
    }

    @Override
    public String toString() {
        return String.valueOf(timeStamp) + prevHash + String.valueOf(id) + String.valueOf(magicNumber);
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




    public void setMessages(List<Transaction> messages) {
        this.messages = messages;
    }

    public List<Transaction> getMessages() {
        return messages;
    }
}
