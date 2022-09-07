package node;

import utils.MinedBlock;

import java.util.List;

public interface BlockchainFileManager {
    void saveBlockchain(Blockchain blockchain);
    Blockchain loadBlockchain();
}
