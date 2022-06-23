package blockchainnode;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.Math.min;

public class Blockchain implements Serializable{
    private static Blockchain instance;
    private final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());

    private List<Block> blocks = Collections.synchronizedList(new ArrayList<>());
    private final int nonce;
    private final String PATH;
    BlockingQueue<Block> sendNewBlock = new ArrayBlockingQueue<>(1);
    private Blockchain() {
        try  (InputStream inputStream = new FileInputStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            PATH = properties.getProperty("blockchain_path"); //path where blockchain is stored - required in jetbrains project
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        loadFromFile();
        nonce = 6;
        if(blocks.isEmpty()) {
            blocks.add(new Block(System.currentTimeMillis(), 1, "0", 0, "GENESIS")); //genesis Block
            saveBlockchain();
        }
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
        return blocks.size();
    }

    public int getNonce() {
        return nonce;
    }

    public String getLastHash() {
        return blocks.size() > 0 ? blocks.get(blocks.size() - 1).getHash() : "0";
    }

    synchronized boolean acceptBlock(Block b) {
        String zeros = new String(new char[nonce]).replace('\0', '0');
        String prevHash = (blocks.size() - 1 >= 0) ? blocks.get(blocks.size() - 1).getHash() : "0";
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
                transactions.forEach(b::appendTransaction);
                transactions.clear();
            }
            blocks.add(b);
            sendNewBlock.add(b);
            saveBlockchain();
            return true;
        }
    }

    public void saveBlockchain() {
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

    public synchronized void appendMessage(Transaction t) throws NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        if(verifyTransaction(t)) {
            t.setId(transactions.size()+1);
            transactions.add(t);
        }
    }
    public int getWallet(PublicKey pk) {
        int wallet = 0;
        for(Block b : blocks){
            for(Transaction t : b.getTransactions()){
                if(Objects.equals(t.getReciver(), pk)){
                    wallet += t.getAmmount();
                }
                if(Objects.equals(t.getSender(), pk)){
                    wallet-= t.getAmmount();
                }
            }
        }
        return wallet;
    }
    public boolean verifyTransaction(Transaction t) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,t.getSender());
        String first = Base64.getEncoder().encodeToString(t.getHash().getBytes());
        String second = Base64.getEncoder().encodeToString(cipher.doFinal(t.getSignature()));
        return first.equals(second);
    }



    private void loadFromFile() {

        ObjectInputStream ois = null;
        //FIXME ClassNotFound exception handle
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
