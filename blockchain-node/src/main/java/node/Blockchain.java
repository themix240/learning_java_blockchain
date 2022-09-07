package node;

import utils.BlockchainData;
import utils.MinedBlock;
import utils.NewBlock;
import utils.Transaction;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.Math.min;
import static node.BlockchainUtils.checkBlock;
import static node.BlockchainUtils.verifyTransaction;

public class Blockchain implements Serializable {
    private static Blockchain instance;
    private transient BlockchainFileManager blockchainFileManager;
    private List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    private List<MinedBlock> minedBlocks = Collections.synchronizedList(new ArrayList<>());
    private int nonce;


    public BlockingQueue<MinedBlock> blockToSend = new ArrayBlockingQueue<>(1);

    public Blockchain(BlockchainFileManager blockchainFileManager, List<MinedBlock> minedBlocks, int nonce) {
        this.blockchainFileManager = blockchainFileManager;
        this.minedBlocks = minedBlocks;
        this.nonce = nonce;
        instance = this;
    }

    private Blockchain() { //GS throws IOException, ClassNotFoundException {
        try (InputStream inputStream = new FileInputStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String PATH = properties.getProperty("blockchain_path");//path where blockchain is stored - required in jetbrains project
            String fileManagerType = properties.getProperty("blockchain_file_type");
            blockchainFileManager = new BlockchainTxtFileManager(PATH);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Blockchain toLoad = blockchainFileManager.loadBlockchain();
        this.nonce = toLoad.nonce;
        this.minedBlocks = toLoad.minedBlocks;
        this.transactions = toLoad.transactions;
    }

    public static Blockchain getInstance() { //GS throws IOException, ClassNotFoundException {
        if (instance == null) {
            instance = new Blockchain();
        }
        return instance;
    }

    @Override
    public String toString() {
        StringJoiner sb = new StringJoiner("\n");
        for (int i = 0; i < min(15, minedBlocks.size()); i++) {
            MinedBlock b = minedBlocks.get(i);
            sb.add("Block:");
            sb.add("Created by " + b.getOwner()); //change to username based on coinbase transaction
            sb.add("Miner gets 10 VC");
            sb.add("Id: " + b.getId());
            sb.add("Timestamp:" + b.getTimeStamp());
            sb.add("Magic number: " + b.getMagicNumber());
            sb.add("Hash of the previous block: ");
            sb.add(b.getPrevHash());
            sb.add("Hash of the block: ");
            sb.add(b.getHash());
            sb.add("Block data: " + (b.getTransactions().isEmpty() ? "no messages" : ""));
            b.getTransactions().stream().map(Transaction::toString).forEach(sb::add);
            sb.add("\n");
        }
        return sb.toString();
    }

    public int getSize() {
        return minedBlocks.size();
    }

    public int getNonce() {
        return nonce;
    }

    public String getLastHash() {
        return minedBlocks.size() > 0 ? minedBlocks.get(minedBlocks.size() - 1).getHash() : "0";
    }

    synchronized boolean acceptBlock(NewBlock b) throws InterruptedException {
        MinedBlock mb = new MinedBlock(b, getSize() + 1);
        if (checkBlock(minedBlocks, mb, nonce)) {
            minedBlocks.add(mb);
            blockToSend.put(mb);
            System.out.println("Added block to blockchain:\n" + mb.toPrettyString());
//            System.out.println("Current blockchain state:");
//            System.out.println(this);
            blockchainFileManager.saveBlockchain(this);
            updateNonce();
            return true;
        }
        return false;
    }

    public List<MinedBlock> getBlocks() {
        return minedBlocks;
    }

    public synchronized void appendMessage(Transaction t) { //GS throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (verifyTransaction(t)) {
            t.setId(transactions.size() + 1);
            transactions.add(t);
        }
    }

    public BlockchainData getBlockchainData() {
        return new BlockchainData(nonce, transactions, minedBlocks.size(), getLastHash());
    }

    private void updateNonce() {
        if (minedBlocks.size() < 2) nonce += 1; //GS or 'nonce++'
        else {
            long minedTimeMs = minedBlocks.get(minedBlocks.size() - 1).getTimeStamp() - minedBlocks.get(minedBlocks.size() - 2).getTimeStamp(); //GS
            System.out.println("minedTimeMs: " + minedTimeMs);//GS-for debugging only
            if (minedTimeMs < 2000)
                nonce++;
            else if (minedTimeMs > 5000)
                nonce--;
            if (nonce < 0) nonce = 0;
        }
    }

    public synchronized void replaceBlockchain(Blockchain newBlockchain) {
        instance = newBlockchain;
        minedBlocks = newBlockchain.getBlocks();
        blockchainFileManager.saveBlockchain(this);
    }
}
