package blockchain;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Random;

public class Miner implements Runnable{
    private final Blockchain bc;
    public Miner(Blockchain bc) {
        this.bc = bc;
    }

    @Override
    public void run() {
        Random rand = new Random();
        while(!Thread.currentThread().isInterrupted()) {
            long id = bc.getSize() + 1;
            int startingZeros = bc.getStarting_zeros();
            String prevHash = bc.getLastHash();
            String minerId = String.valueOf(Thread.currentThread().getId());
            int magicNumber = rand.nextInt();
            Instant startTime = Instant.now();
            long timeStamp = new Date().getTime();
            String hash = StringUtil.applySha256(String.valueOf(timeStamp) + prevHash + String.valueOf(id) + String.valueOf(magicNumber));
            String zeros = new String(new char[startingZeros]).replace('\0', '0');
            while (!hash.startsWith(zeros)) {
                startingZeros = bc.getStarting_zeros();
                prevHash = bc.getLastHash();
                id = bc.getSize() + 1;
                magicNumber = rand.nextInt();
                hash = StringUtil.applySha256(String.valueOf(timeStamp) + prevHash + String.valueOf(id) + String.valueOf(magicNumber));
                if(Thread.currentThread().isInterrupted())
                    break;
            }
            Instant end_time = Instant.now();
            long time = Duration.between(startTime, end_time).toSeconds();
           // synchronized (this) {
               // Block b = new Block(timeStamp, id, hash, prevHash, time, magicNumber, minerId);
              //  bc.acceptBlock(b);
           // }
        }
    }


}
