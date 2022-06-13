package blockchain;

import java.io.*;
import java.security.*;
import java.time.LocalDateTime;
import java.util.*;

import static java.lang.Math.min;

public class Blockchain {
    private static Blockchain instance;
    private List<Transaction> transactions = Collections.synchronizedList(new ArrayList<Transaction>());

    private List<Block> blocks = Collections.synchronizedList(new ArrayList<>());
    private int startingZeros;
    private final String PATH = "/Users/themix240/blockchain/blockchain.txt";

    private Blockchain() {
        loadFromFile();
        startingZeros = 5;
        if(blocks.isEmpty())
            blocks.add(new Block(System.currentTimeMillis(), 1,"0",0,"GENESIS")); //genesis Block
    }

    public static Blockchain getInstance() {
        if (instance == null) {
            instance = new Blockchain();
        }
        return instance;
    }

    @Override
    public String toString() {
        StringJoiner sb = new StringJoiner("\n");
        for (int i = 0; i < min(15, blocks.size()); i++) {
            Block b = blocks.get(i);
            sb.add("Block:");
            sb.add("Created by miner "+ b.getSignature());
            sb.add("Miner gets 10 VC");
            sb.add("Id: " + b.getId());
            sb.add("Timestamp:" + String.valueOf(b.getTimeStamp()));
            sb.add("Magic number: " + String.valueOf(b.getMagicNumber()));
            sb.add("Hash of the previous block: ");
            sb.add(b.getPrevHash());
            sb.add("Hash of the block: ");
            sb.add(b.getHash());
            sb.add("Block data: " + (b.getMessages().isEmpty() ? "no messages" : ""));
            b.getMessages().stream().map(t -> t.toString()).forEach(sb::add);
            //sb.add("Block was generating for " + b.getTime() + " seconds");
            //sb.add(b.getTime() > 4 ? b.getTime() < 15 ? "N stays the same" : "N was decreased" : "N was increased");
            sb.add("\n");
        }
        return sb.toString();
    }

    public int getSize() {
        return blocks.size();
    }

    public int getStarting_zeros() {
        return startingZeros;
    }

    public String getLastHash() {
        return blocks.size() > 0 ? blocks.get(blocks.size() - 1).getHash() : "0";
    }

    synchronized boolean acceptBlock(Block b) {
        String zeros = new String(new char[startingZeros]).replace('\0', '0');
        String prevHash = (blocks.size() - 1 >= 0) ? blocks.get(blocks.size() - 1).getHash() : "0";
        //String lastHash = getLastHash();
        // System.out.println(prevHash);
        // System.out.println(lastHash);
        long lastTimeStamp = (blocks.size() - 1 >= 0) ? blocks.get(blocks.size() - 1).getTimeStamp() : 0;
        if (!b.getHash().startsWith(zeros)) {
            System.out.println("ZLA LICZBA ZER");
            return false;
        } else if (!b.getPrevHash().equals(prevHash)) {
            System.out.println("ZLY POPRZEDNI HASH");
            return false;
        } else if (b.getId() != blocks.size() + 1) {
            System.out.println(b.getId());
            System.out.println("ZLE ID");
            return false;
        } else {
            if (b.getId() > 1) {
                b.setMessages(transactions);
                transactions.clear();
            }
            blocks.add(b);
            saveBlockchain();
            return true;
        }
    }

    private void saveBlockchain() {
        ObjectOutputStream oos = null;
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(PATH);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(blocks);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public boolean validate() {
     for(int i = 1; i< blocks.size();i++){
         if(!Objects.equals(blocks.get(i).getPrevHash(), blocks.get(i - 1).getHash())){
             return false;
         }
     }
     return true;
    }

    public synchronized void appendMessage(Transaction t) {
        transactions.add(t);
    }
    public int getWallet(String username) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        int wallet = 0;
        for(Block b : blocks){
           if(b.getSignature().equals(username))
               wallet+=10;
        }
        return wallet;
    }

    private void loadFromFile() {

        ObjectInputStream ois = null;
        try {
            File f = new File(PATH);
            if (!f.isFile() || !f.canRead()) return;
            FileInputStream fis = new FileInputStream(PATH);

            ois = new ObjectInputStream(fis);

            List<Block> read = (List<Block>) ois.readObject();
            blocks = Collections.synchronizedList(read);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }
}
