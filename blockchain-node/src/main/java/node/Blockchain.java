package node;

import utils.BlockchainData;
import utils.MinedBlock;
import utils.NewBlock;
import utils.Transaction;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static node.BlockchainUtils.checkBlock;
import static node.BlockchainUtils.verifyTransaction;
import static java.lang.Math.min;

public class Blockchain implements Serializable {
    private static Blockchain instance;
    private BlockchainFileManager blockchainFileManager;
    private final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    private  List<MinedBlock> minedBlocks = Collections.synchronizedList(new ArrayList<>());
    private int nonce;
    BlockingQueue<MinedBlock> sendNewMinedBlock = new ArrayBlockingQueue<>(1);

    private Blockchain() throws IOException, ClassNotFoundException {
        try (InputStream inputStream = new FileInputStream("blockchain-node/config.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String PATH = properties.getProperty("blockchain_path");//path where blockchain is stored - required in jetbrains project
            String fileManagerType = properties.getProperty("blockchain_file_type");
            if (fileManagerType.equals("txt"))
                blockchainFileManager = new BlockchainTxtFileManager(PATH);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        minedBlocks.addAll(blockchainFileManager.loadBlockchain());
        nonce = 0;
    }

    public static Blockchain getInstance() throws IOException, ClassNotFoundException {
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

    synchronized boolean acceptBlock(NewBlock b) {
        MinedBlock mb = new MinedBlock(b, getSize() + 1);
        if (checkBlock(minedBlocks, mb, nonce)) {
            minedBlocks.add(mb);
            blockchainFileManager.saveBlockchain(minedBlocks);
            updateNonce();
            return true;
        }
        return false;
    }

    public List<MinedBlock> getBlocks() {
        return minedBlocks;
    }

    public synchronized void appendMessage(Transaction t) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if (verifyTransaction(t)) {
            t.setId(transactions.size() + 1);
            transactions.add(t);
        }
    }

    public BlockchainData getBlockchainData() {
        return new BlockchainData(nonce, transactions, minedBlocks.size(), getLastHash());
    }

    private void updateNonce() {
        if (minedBlocks.size() < 2) nonce += 1;
        else {
            if(minedBlocks.get(minedBlocks.size()-2).getTimeStamp() - minedBlocks.get(minedBlocks.size()-1).getTimeStamp() < 2000) nonce+=1;
            else if(minedBlocks.get(minedBlocks.size()-2).getTimeStamp() - minedBlocks.get(minedBlocks.size()-1).getTimeStamp() > 5000) nonce -=1;
        }
        if(nonce < 0) nonce = 0;

    }

}
