package client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utils.BlockchainData;
import utils.GenerateKeys;
import utils.NewBlock;
import utils.User;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MinerTest {

    private Miner miner;
    private PublicKey publicKey;
    @BeforeEach
    void setUp() {
        BlockchainData data = new BlockchainData(2, new ArrayList<>(), 3, "6981be29c36bb201f7fbbb26ac324c85b3115df96043882cdd7072147e1e9d3c\n" +
                "31a8e3c132a5eb3f950f942d1936cbdb276fb7276cb59c9b87c848d496232768\n" +
                "ae198f884c5d6d558de5e0af8a336f76fa38c4235cb64b1e25447ec60961647a\n" +
                "7d83ea6fd4d46a4b66f4ed707875cbbeff031a99450a6ba5e26ff8675ffe22da\n" +
                "9150ce2dbc6a722265275a9c060a3c7b12fdad3dd116a2b90b850b84298a4bfb\n" +
                "ca9dfb81ca5d6cc48c4e5c280ecd00341da697323a09d100920ca6ecbf074675\n" +
                "9ba75f903fc7d02ab6e0126c79e653d50c2e58815ea94988fad987236c3a0c6f\n" +
                "7cd132c39e48f0a6b9b1b589aafa0eb93da3b3af88e9a6da1a26d9e9b79b35d0\n" +
                "7476e867595b6dbbfab5e6eff8c24c6088b2cff2671736132efcc475c2d42ab7\n" +
                "f6f305124f07e73fc90ae24bd50502f81e908903f68ffc0d6b709dadaae6e63f");
        try {
            GenerateKeys generateKeys = new GenerateKeys(1024);
            generateKeys.createKeys();
            publicKey = generateKeys.getPublicKey();
            miner = new Miner(data, new User("testUser", publicKey));
        } catch (NoSuchAlgorithmException e) {
            fail(e.getMessage());
        }


    }

    @Test
    void testMining() {
        NewBlock mined = miner.Mine();
        assertEquals(mined.getHashOfPrev(), "6981be29c36bb201f7fbbb26ac324c85b3115df96043882cdd7072147e1e9d3c\n" +
                "31a8e3c132a5eb3f950f942d1936cbdb276fb7276cb59c9b87c848d496232768\n" +
                "ae198f884c5d6d558de5e0af8a336f76fa38c4235cb64b1e25447ec60961647a\n" +
                "7d83ea6fd4d46a4b66f4ed707875cbbeff031a99450a6ba5e26ff8675ffe22da\n" +
                "9150ce2dbc6a722265275a9c060a3c7b12fdad3dd116a2b90b850b84298a4bfb\n" +
                "ca9dfb81ca5d6cc48c4e5c280ecd00341da697323a09d100920ca6ecbf074675\n" +
                "9ba75f903fc7d02ab6e0126c79e653d50c2e58815ea94988fad987236c3a0c6f\n" +
                "7cd132c39e48f0a6b9b1b589aafa0eb93da3b3af88e9a6da1a26d9e9b79b35d0\n" +
                "7476e867595b6dbbfab5e6eff8c24c6088b2cff2671736132efcc475c2d42ab7\n" +
                "f6f305124f07e73fc90ae24bd50502f81e908903f68ffc0d6b709dadaae6e63f");
        assertEquals(1, mined.transactions.size());
        assertEquals(publicKey, mined.transactions.get(0).getReciver());
        assertTrue(mined.getHash().startsWith("00"));
    }
}