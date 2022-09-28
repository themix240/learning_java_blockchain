package node.utils.filemanager;

import node.core.Blockchain;

public interface BlockchainFileManager {
    void saveBlockchain(Blockchain blockchain);
    Blockchain loadBlockchain();
}
