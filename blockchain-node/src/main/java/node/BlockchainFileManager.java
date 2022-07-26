package node;

import utils.MinedBlock;

import java.util.List;

public interface BlockchainFileManager {
    void saveBlockchain(List<MinedBlock> minedBlocks);
    List<MinedBlock> loadBlockchain();
}
