package utils.blockchain;

import utils.crypto.StringUtil;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Consists data about transaction, sender, receiver, amount.
 * Needs to be signed by owner to be valid.
 */
public class Transaction implements Serializable {
    private final PublicKey sender;
    private final PublicKey receiver;
    private final int amount;
    private int id;
    private byte[] signature;

    public Transaction(PublicKey sender, PublicKey receiver, int amount) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getAmount() {
        return amount;
    }

    public byte[] getSignature() {
        return signature;
    }

    public String getHash() {
        return StringUtil.applySha256(Base64.getEncoder().encodeToString(sender.getEncoded()) +
                Base64.getEncoder().encodeToString(receiver.getEncoded()) +
                amount);
    }


    public PublicKey getReceiver() {
        return receiver;
    }

    public PublicKey getSender() {
        return sender;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "Transaction:" + id;

    }
}