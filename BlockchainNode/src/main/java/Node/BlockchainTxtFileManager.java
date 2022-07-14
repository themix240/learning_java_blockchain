package Node;

import utils.Block;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class BlockchainTxtFileManager implements BlockchainFileManager {
    String PATH;
    List<Block> blocks;

    public BlockchainTxtFileManager(String PATH, List<Block> blocks) {
        this.PATH = PATH;
        this.blocks = blocks;
    }

    public void loadBlockchain() {
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


}
