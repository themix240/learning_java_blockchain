package node.utils.filemanager;

import node.core.Blockchain;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class BlockchainTxtFileManager implements BlockchainFileManager, Serializable {
    String PATH;


    public BlockchainTxtFileManager(String PATH) {
        this.PATH = PATH;
    }

    public Blockchain loadBlockchain() {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PATH)))
        {
                return (Blockchain) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveBlockchain(Blockchain blockchain)  {
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
            oos.writeObject(blockchain);
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
