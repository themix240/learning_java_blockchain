package Client;

import utils.Block;
import utils.StringUtil;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Miner implements Runnable {
    AtomicLong id;
    AtomicInteger startingZeros;
    AtomicReference<String> prevHash;
    private final BlockingQueue<Block> minersQueue;

    public Miner(AtomicLong id, AtomicInteger starting_zeros, AtomicReference<String> prevHash, BlockingQueue<Block> minersQueue) {
        this.id = id;
        this.startingZeros = starting_zeros;
        this.prevHash = prevHash;
        this.minersQueue = minersQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {

            Random rand = new Random();
            int magicNumber = rand.nextInt();
            long timeStamp = System.currentTimeMillis();
            String hash = StringUtil.applySha256(String.valueOf(timeStamp) + prevHash + id + magicNumber);
            String zeros = new String(new char[startingZeros.get()]).replace('\0', '0');
            while (!hash.startsWith(zeros)) {
                zeros = new String(new char[startingZeros.get()]).replace('\0', '0');
                magicNumber = rand.nextInt();
                hash = StringUtil.applySha256(timeStamp + prevHash.get() + id.get() + magicNumber);
                if (Thread.currentThread().isInterrupted())
                    break;
            }
            Block b = new Block(timeStamp,id.get(), prevHash.get(), magicNumber);
            try {
                minersQueue.put(b);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
