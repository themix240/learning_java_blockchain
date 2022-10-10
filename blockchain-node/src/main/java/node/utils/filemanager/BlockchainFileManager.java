package node.utils.filemanager;

import node.core.Blockchain;

/**
 * Manages loading and saving <code>Blockchain</code> to file.
 */
public interface BlockchainFileManager {
    /**
     * Saves blockchain to file.
     * @param blockchain reference to <code>Blockchain</code>.
     */
    void saveBlockchain(Blockchain blockchain);

    /**
     * Loads <code>Blockchain</code> from file.
     * @return Loaded <code>Blockchain</code>
     */
    Blockchain loadBlockchain();
}
