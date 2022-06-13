package blockchain;

import java.security.PublicKey;

public class Transaction {
    private PublicKey sender;
    private PublicKey reciver;
    private int ammount;

    @Override
    public String toString() {
        return "Transaction{" +
                "sender=" + sender +
                ", reciver=" + reciver +
                ", ammount=" + ammount +
                '}';
    }

    public Transaction(PublicKey sender, PublicKey reciver, int ammount) {
        this.sender = sender;
        this.reciver = reciver;
        this.ammount = ammount;
    }
}
