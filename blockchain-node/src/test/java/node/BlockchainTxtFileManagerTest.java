package node;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utils.MinedBlock;
import utils.NewBlock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockchainTxtFileManagerTest {
    @TempDir
    private static Path tempDir;

    @Test
    @Order(1)
    void saveBlockchainTest() throws IOException, ClassNotFoundException {
        assertTrue(Files.exists(tempDir));
        File file = new File(String.valueOf(tempDir), "blockchain.txt");
        List<MinedBlock> minedBlocks = new ArrayList<>();
        BlockchainFileManager blockchainFileManager = new BlockchainTxtFileManager(file.toString());
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 0));
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 1));
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 2));
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 3));
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 4));
        blockchainFileManager.saveBlockchain(new Blockchain(blockchainFileManager, minedBlocks, 0));
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        Blockchain inputFromFile = (Blockchain) ois.readObject();
        assertTrue(file.exists());
        assertIterableEquals(minedBlocks, inputFromFile.getBlocks());
    }

    @Test
    @Order(2)
    void loadBlockchainTest() {
        assertTrue(Files.exists(tempDir));
        File file = new File(String.valueOf(tempDir), "blockchain.txt");
        List<MinedBlock> minedBlocks = new ArrayList<>();
        BlockchainFileManager blockchainFileManager = new BlockchainTxtFileManager(file.toString());
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 0));
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 1));
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 2));
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 3));
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), "0", 122), 4));
        blockchainFileManager.saveBlockchain(new Blockchain(blockchainFileManager, minedBlocks, 0));
        Blockchain inputFromFile = blockchainFileManager.loadBlockchain();
        assertTrue(file.exists());
        assertIterableEquals(minedBlocks, inputFromFile.getBlocks());
    }

}