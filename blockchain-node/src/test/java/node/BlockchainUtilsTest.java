package node;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import utils.CryptoUtils;
import utils.MinedBlock;
import utils.NewBlock;
import utils.Transaction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static org.junit.jupiter.api.Assertions.*;

class BlockchainUtilsTest {
    @TempDir
    static Path tempDir;

    @BeforeEach
    void beforeEach() {
        assertTrue(Files.isDirectory(tempDir));
    }

    @Test
    void getWalletTest() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, NoSuchProviderException {
        Path file = tempDir.resolve("test");
        PublicKey firstUser = CryptoUtils.keyGeneration(file.toAbsolutePath().toString(), "firstUser", "test");
        PublicKey secondUser = CryptoUtils.keyGeneration(file.toAbsolutePath().toString(), "secondUser", "test");
        List<MinedBlock> minedBlockList = new ArrayList<>();
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(null, firstUser, 100));
        transactions.add(new Transaction(null, secondUser, 100));
        minedBlockList.add(new MinedBlock(new NewBlock(transactions, "0", 123), 0));
        int ammount = BlockchainUtils.getWallet(firstUser, minedBlockList);
        assertEquals(100, ammount);
        List<Transaction> transactionsSecond = new ArrayList<>();
        transactionsSecond.add(new Transaction(firstUser, secondUser, 25));
        minedBlockList.add(new MinedBlock(new NewBlock(transactionsSecond, "0", 123), 1));
        ammount = BlockchainUtils.getWallet(firstUser, minedBlockList);
        assertEquals(75, ammount);
        ammount = BlockchainUtils.getWallet(secondUser, minedBlockList);
        assertEquals(125, ammount);
    }

    @Test
    void verifyTransactionTest() throws Exception {
        Path file = tempDir.resolve("test");
        PublicKey firstUser = CryptoUtils.keyGeneration(file.toString() + '/', "firstUser", "test");
        PublicKey secondUser = CryptoUtils.keyGeneration(file.toString() + '/', "secondUser", "test");
        Transaction transaction = new Transaction(firstUser, secondUser, 100);
        transaction.setSignature(Base64.getDecoder().decode
                (CryptoUtils.signString(CryptoUtils.getPrivate(file.toString() + "/firstUser/privatekey.txt"), transaction.getHash()))); // correct transaction
        assertTrue(BlockchainUtils.verifyTransaction(transaction));
        Transaction secondTransaction = new Transaction(firstUser, secondUser, 100);
        secondTransaction.setSignature(Base64.getDecoder().decode
                (CryptoUtils.signString(CryptoUtils.getPrivate(file.toString() + "/secondUser/privatekey.txt"), secondTransaction.getHash()))); // bad signature
        assertFalse(BlockchainUtils.verifyTransaction(secondTransaction));
        Transaction withoutSignature = new Transaction(firstUser,secondUser, 50); // no signature
        assertFalse(BlockchainUtils.verifyTransaction(withoutSignature));

    }

    @Test
    void validateTest() {
        List<MinedBlock> minedBlocks = new ArrayList<>();
        MinedBlock temp = new MinedBlock(new NewBlock(new ArrayList<>(), null, 128),1);
        minedBlocks.add(temp);
        minedBlocks.add(new MinedBlock(new NewBlock(new ArrayList<>(), temp.getHash(), 125),2));
        assertTrue(BlockchainUtils.validate(minedBlocks));
        List<MinedBlock> minedBlocksSecond = new ArrayList<>();
        minedBlocksSecond.add(temp);
        minedBlocksSecond.add(new MinedBlock(new NewBlock(new ArrayList<>(), "1222252345", 127), 2));
        assertFalse(BlockchainUtils.validate(minedBlocksSecond));
    }

    @Test
    void checkBlockTest() {
        List<MinedBlock> minedBlocks = new ArrayList<>();
        MinedBlock first = new MinedBlock(new NewBlock(new ArrayList<>(), "0", 125), 1);
        assertTrue(BlockchainUtils.checkBlock(minedBlocks,first,0));
        minedBlocks.add(first);
        MinedBlock validSecond = new MinedBlock(new NewBlock(new ArrayList<>(), first.getHash(), 125), 2);
        assertTrue(BlockchainUtils.checkBlock(minedBlocks,validSecond, 0));
        MinedBlock invalidSecond = new MinedBlock(new NewBlock(new ArrayList<>(), validSecond.getHash(), 125 ), 2);
        assertFalse(BlockchainUtils.checkBlock(minedBlocks,invalidSecond, 0 ));

    }
}