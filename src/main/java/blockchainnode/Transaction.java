package blockchainnode;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;

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

    public PublicKey getReciver() {
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