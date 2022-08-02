package node;

import utils.MinedBlock;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockchainTxtFileManager implements BlockchainFileManager, Serializable {
    String PATH;


    public BlockchainTxtFileManager(String PATH) {
        this.PATH = PATH;
    }

    public List<MinedBlock> loadBlockchain() {
        try {
            ObjectInputStream ois = null;
            File f = new File(PATH);
            if (!f.isFile() || !f.canRead()) {
                f.createNewFile();
                return Collections.emptyList();
            }
            FileInputStream fis = new FileInputStream(PATH);
            ois = new ObjectInputStream(fis);
            List<MinedBlock> read = (List<MinedBlock>) ois.readObject();
            return (Collections.synchronizedList(read));
        }
        catch(Exception ex){
            return (List<MinedBlock>) new ArrayList<MinedBlock>();
        }
    }

    public void saveBlockchain(List<MinedBlock> minedBlocks)  {
        ObjectOutputStream oos = null;
        FileOutputStream fout;
        if(!Files.exists(Path.of(PATH))){
            File f = new File(PATH);
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
