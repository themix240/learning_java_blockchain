package node.utils;

import utils.blockchain.MinedBlock;
import utils.blockchain.Transaction;

import javax.crypto.Cipher;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * Class with static utility methods for <code>Blockchain</code>.
 * @see node.core.Blockchain
 */
public class BlockchainUtils {
    /**
     * Calculates current wallet value for given public key of user.
     * Searches for transactions in minedBlocks which include given publicKey then adds or subtracts value saved in transaction.
     * @param pk publicKey of user.
     * @param minedBlocks List of all blocks in <code>Blockchain</code>
     * @return amount of currency in user wallet.
     */
    public static int getWallet(PublicKey pk, List<MinedBlock> minedBlocks) {
        int wallet = 0;
        for (MinedBlock b : minedBlocks) {
            for (Transaction t : b.getTransactions()) {
                if (Objects.equals(t.getReceiver(), pk)) {
                    wallet += t.getAmount();
                }
                if (Objects.equals(t.getSender(), pk)) {
                    wallet -= t.getAmount();
                }
            }
        }
        return wallet;
    }

    /**
     * Checks if <code>Transaction</code> is signed properly.
     * @param t Transaction to check
     * @return <code>true</code> if transaction is valid; <code>false</code> otherwise.
     */
    public static boolean verifyTransaction(Transaction t) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, t.getSender());
            String first = Base64.getEncoder().encodeToString(t.getHash().getBytes());
            String second = Base64.getEncoder().encodeToString(cipher.doFinal(t.getSignature()));
            return first.equals(second);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validates <code>Blockchain</code> checks if all blocks contains correct previousHash.
     * @param minedBlocks list of all minedBlocks in <code>Blockchain</code>
     * @return <code>true</code> if <code>Blockchain</code> has valid blocks; <code>false</code> otherwise.
     */
    public static boolean validate(List<MinedBlock> minedBlocks) {
        for (int i = 1; i < minedBlocks.size(); i++) {
            if (!Objects.equals(minedBlocks.get(i).getPrevHash(), minedBlocks.get(i - 1).getHash())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if block satisfy requirements given by <code>Blockchain</code>.
     * Checks if blocks starts with correct amount of zeros, has proper previousHash and id is valid.
     *
     * @param minedBlocks list of all blocks in <code>Blockchain</code>.
     * @param toCheck block to be checked.
     * @param nonce amount of starting zeros.
     * @return <code>true</code> if block satisfy all requirements; <code>false</code> otherwise.
     */
    public static boolean checkBlock(List<MinedBlock> minedBlocks, MinedBlock toCheck, int nonce) {
        String zeros = new String(new char[nonce]).replace('\0', '0');
        String prevHash = (minedBlocks.size() - 1 >= 0) ? minedBlocks.get(minedBlocks.size() - 1).getHash() : "0";
        if (!toCheck.getHash().startsWith(zeros)) {
            return false;
        } else if (!toCheck.getPrevHash().equals(prevHash)) {
            return false;
        } else if (toCheck.getId() != minedBlocks.size() + 1) {
            return false;
        }
        return true;
    }

}
