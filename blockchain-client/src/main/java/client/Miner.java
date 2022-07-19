package client;

import utils.BlockchainData;
import utils.MinedBlock;
import utils.NewBlock;
import utils.StringUtil;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class Miner {
 final BlockchainData data;


    public Miner(BlockchainData data) {
        this.data = data;
    }
    public NewBlock Mine() {
        Random random = new Random();
        int magicNumber = random.nextInt();
        NewBlock block = new NewBlock(data.getTransactions(),data.getPrevHash(),magicNumber);
        String startingZeros = "0".repeat(data.getNonce());
        while(!block.getHash().startsWith(startingZeros)) {
            magicNumber = random.nextInt();
            block.magicNumber = magicNumber;
        }
        return block;
    }
}
