package client;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Miner implements Runnable {
    AtomicLong id;
    AtomicInteger startingZeros;
    AtomicReference<String>  prevHash;
    private BlockingQueue<Block> minersQueue;

    public Miner(AtomicLong id, AtomicInteger starting_zeros, AtomicReference<String> prevHash, BlockingQueue<Block> minersQueue) {
        this.id = id;
        this.startingZeros = starting_zeros;
        this.prevHash = prevHash;
        this.minersQueue = minersQueue;
    }

    @Override
    public void run() {
       while(!Thread.currentThread().isInterrupted()) {

        Random rand = new Random();
        int magicNumber = rand.nextInt();
        long timeStamp = System.currentTimeMillis();
        String hash = StringUtil.applySha256(String.valueOf(timeStamp) + prevHash + String.valueOf(id) + String.valueOf(magicNumber));
        String zeros = new String(new char[startingZeros.get()]).replace('\0', '0');
        while (!hash.startsWith(zeros)) {
            //objectOutputStream.writeObject(init);
            zeros = new String(new char[startingZeros.get()]).replace('\0', '0');
            magicNumber = rand.nextInt();
            hash = StringUtil.applySha256(String.valueOf(timeStamp) + prevHash.get() + String.valueOf(id.get()) + String.valueOf(magicNumber));
            //System.out.println(hash);
            if (Thread.currentThread().isInterrupted())
                break;
        }
        Block b = new Block(timeStamp, hash, magicNumber);
        try {
           // System.out.println("WYKOPALEM");
            minersQueue.put(b);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    }
}
