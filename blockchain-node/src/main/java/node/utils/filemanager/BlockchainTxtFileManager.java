package node.utils.filemanager;

import node.core.Blockchain;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manages saving and loading <code>Blockchain</code> to txt file.
 */
public class BlockchainTxtFileManager implements BlockchainFileManager, Serializable {
    String PATH;

    /**
     * Creates TxtFileManager with specified path to file.
     *
     * @param PATH path to file.
     */
    public BlockchainTxtFileManager(String PATH) {
        this.PATH = PATH;
    }

    /**
     * Loads <code>Blockchain</code> from specified path.
     *
     * @return loaded <code>Blockchain</code>
     */
    public Blockchain loadBlockchain() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PATH))) {
            return (Blockchain) ois.readObject();
        } catch (EOFException e) {
            return null;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Saves <code>Blockchain</code> to specified path.
     * If file or directories not exists method creates them.
     *
     * @param blockchain reference to <code>Blockchain</code>.
     */
    public void saveBlockchain(Blockchain blockchain) {
        ObjectOutputStream oos = null;
        FileOutputStream fout;
        if (!Files.exists(Path.of(PATH))) {
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
