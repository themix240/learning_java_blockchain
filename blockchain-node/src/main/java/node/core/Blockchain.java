package node.core;

import node.utils.BlockchainUtils;
import node.utils.filemanager.BlockchainFileManager;
import node.utils.filemanager.BlockchainTxtFileManager;
import utils.blockchain.BlockchainData;
import utils.blockchain.MinedBlock;
import utils.blockchain.NewBlock;
import utils.blockchain.Transaction;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.Math.min;
import static node.utils.BlockchainUtils.checkBlock;
import static node.utils.BlockchainUtils.verifyTransaction;

/**
 * Implements logic of blockchain.
 * Manages transactions and blocks coming from clients.
 *
 * @see BlockchainData
 */
public class Blockchain implements Serializable {
    private static Blockchain instance;
    private transient BlockchainFileManager blockchainFileManager;
    private List<Transaction> waitingList = Collections.synchronizedList(new ArrayList<>());

    private List<MinedBlock> minedBlocks = Collections.synchronizedList(new ArrayList<>());
    private int nonce;


    public BlockingQueue<MinedBlock> blockToSend = new ArrayBlockingQueue<>(1);

    /**
     * Public constructor used only for testing.
     *
     * @param blockchainFileManager manager which saves state of blockchain.
     * @param minedBlocks           list of blocks in blockchain.
     * @param nonce                 starting value of nonce.
     */
    public Blockchain(BlockchainFileManager blockchainFileManager, List<MinedBlock> minedBlocks, int nonce) {
        this.blockchainFileManager = blockchainFileManager;
        this.minedBlocks = minedBlocks;
        this.nonce = nonce;
        instance = this;
    }

    /**
     * Loads properties from config.properties.
     * <p>Loads path to blockchain and file type, then <code>BlockchainFileManager</code> loads blockchain
     * from file.</p>
     *
     * @see BlockchainFileManager
     */
    private Blockchain() {
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
            if(toLoad!=null) {
                this.nonce = toLoad.nonce;
                this.minedBlocks = toLoad.minedBlocks;
                this.waitingList = toLoad.waitingList;
            }
            else {
                this.nonce = 0;
                this.minedBlocks = new ArrayList<>();
                this.waitingList = new ArrayList<>();
            }
    }

    /**
     * Blockchain is singleton.
     * Creates new instance if it not exists. Otherwise, returns already existing one.
     *
     * @return instance of Blockchain.
     */
    public static Blockchain getInstance() {
        if (instance == null) {
            instance = new Blockchain();
        }
        return instance;
    }

    /**
     * Text information about first 15 blocks in blockchain.
     *
     * @return Data of first 15 blocks in blockchain.
     */
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

    /**
     * Method to decide if block will be attached to blockchain or rejected.
     * After acceptance it saves blockchain and updates nonce (number of zeros at start of hash required).
     *
     * @param b Block to check
     * @return True if block is added to blockchain, False otherwise
     * @throws InterruptedException
     * @see BlockchainUtils#checkBlock(List, MinedBlock, int)
     */
    public synchronized boolean acceptBlock(NewBlock b) throws InterruptedException {
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

    /**
     * Returns all accepted and stored blocks in blockchain with their all data.
     * @return List of minedBlocks.
     * @see MinedBlock
     */
    public List<MinedBlock> getBlocks() {
        return minedBlocks;
    }

    /**
     * Checks transaction and appends it to waiting list.
     * If new block occurs all transactions in waiting list are added to this block and then waiting list is cleared.
     * @param t Transaction to be appended to waiting list.
     */
    public synchronized void appendTransaction(Transaction t) {
        if (verifyTransaction(t)) {
            t.setId(waitingList.size() + 1);
            waitingList.add(t);
        }
    }

    /**
     * Returns essential data of blockchain for mining.
     * @return Blockchain data required for mining block.
     */
    public BlockchainData getBlockchainData() {
        return new BlockchainData(nonce, waitingList, minedBlocks.size(), getLastHash());
    }

    /**
     * Updates nonce after adding block.
     * It calculates time required for mining the block and then decides to increment or decrement nonce.
     * Higher nonce means the next block will take exponentially longer time to mine.
     */
    private void updateNonce() {
        if (minedBlocks.size() < 2) nonce += 1;
        else {
            long minedTimeMs = minedBlocks.get(minedBlocks.size() - 1).getTimeStamp() - minedBlocks.get(minedBlocks.size() - 2).getTimeStamp(); //GS
            System.out.println("minedTimeMs: " + minedTimeMs);
            if (minedTimeMs < 2000)
                nonce++;
            else if (minedTimeMs > 5000)
                nonce--;
            if (nonce < 0) nonce = 0;
        }
    }

    /**
     * Replaces Blockchain with different blockchain.
     * <p>Used in p2p networking</p>
     * Nodes at start of connection exchange their local blockchains and Blockchain with more mined blocks replace Blockchain with less mined blocks.
     *
     * @param newBlockchain blockchain which replace current blockchain.
     */
    public synchronized void replaceBlockchain(Blockchain newBlockchain) {
        instance = newBlockchain;
        minedBlocks = newBlockchain.getBlocks();
        blockchainFileManager.saveBlockchain(this);
    }
}
