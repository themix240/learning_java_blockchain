package utils.blockchain;

import utils.crypto.StringUtil;

import java.io.Serializable;
import java.security.PublicKey;
import java.util.Base64;

public class Transaction implements Serializable {
    private PublicKey sender;
    private PublicKey reciver;
    private int ammount;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    byte[] signature;

    public PublicKey getReceiver() {
        return reciver;
    }

    public int getAmmount() {
        return ammount;
    }

    public byte[] getSignature() {
        return signature;
    }

    public PublicKey getSender() {
        return sender;
    }

    public Transaction(PublicKey sender, PublicKey reciver, int ammount){
        this.sender = sender;
        this.reciver = reciver;
        this.ammount = ammount;
    }
    public String getHash(){
        return StringUtil.applySha256(Base64.getEncoder().encodeToString(sender.getEncoded())+
                Base64.getEncoder().encodeToString(reciver.getEncoded())+
                String.valueOf(ammount));
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "Transaction:" + String.valueOf(id);

    }
}