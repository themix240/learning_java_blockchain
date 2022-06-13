package client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Block  {
    long timeStamp;
    String hash;
    int magicNumber;

    public Block(long timeStamp, String hash, int magicNumber) {
        this.timeStamp = timeStamp;
        this.hash = hash;
        this.magicNumber = magicNumber;
    }
}

