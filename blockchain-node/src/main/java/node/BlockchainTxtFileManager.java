package node;

import utils.MinedBlock;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class BlockchainTxtFileManager implements BlockchainFileManager {
    String PATH;


    public BlockchainTxtFileManager(String PATH) {
        this.PATH = PATH;
    }

    public List<MinedBlock> loadBlockchain() throws IOException, ClassNotFoundException {
        ObjectInputStream ois = null;
        //FIXME ClassNotFound exception handle
            File f = new File(PATH);
            if (!f.isFile() || !f.canRead()) return Collections.emptyList();
            FileInputStream fis = new FileInputStream(PATH);
            ois = new ObjectInputStream(fis);
            List<MinedBlock> read = (List<MinedBlock>) ois.readObject();
            return (Collections.synchronizedList(read));
    }

    public void saveBlockchain(List<MinedBlock> minedBlocks) {
        ObjectOutputStream oos = null;
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(PATH);
            oos = new ObjectOutputStream(fout);
            oos.writeObject(minedBlocks);
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
