package node;

import utils.MinedBlock;
import utils.Transaction;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

public class BlockchainUtils {
    public static int getWallet(PublicKey pk,List<MinedBlock> minedBlocks) {
        int wallet = 0;
        for(MinedBlock b : minedBlocks){
            for(Transaction t : b.getTransactions()){
                if(Objects.equals(t.getReciver(), pk)){
                    wallet += t.getAmmount();
                }
                if(Objects.equals(t.getSender(), pk)){
                    wallet-= t.getAmmount();
                }
            }
        }
        return wallet;
    }
    public static boolean verifyTransaction(Transaction t) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,t.getSender());
        String first = Base64.getEncoder().encodeToString(t.getHash().getBytes());
        String second = Base64.getEncoder().encodeToString(cipher.doFinal(t.getSignature()));
        return first.equals(second);
    }
    public static boolean validate(List<MinedBlock> minedBlocks) {
        for(int i = 1; i< minedBlocks.size(); i++){
            if(!Objects.equals(minedBlocks.get(i).getPrevHash(), minedBlocks.get(i - 1).getHash())){
                return false;
            }
        }
        return true;
    }
    public static boolean checkBlock(List<MinedBlock> minedBlocks, MinedBlock toCheck, int nonce){
        String zeros = new String(new char[nonce]).replace('\0', '0');
        String prevHash = (minedBlocks.size() - 1 >= 0) ? minedBlocks.get(minedBlocks.size() - 1).getHash() : "0";
        if (!toCheck.getHash().startsWith(zeros)) {
            System.out.println("ZLA LICZBA ZER");
            return false;
        } else if (!toCheck.getPrevHash().equals(prevHash)) {
            System.out.println("ZLY POPRZEDNI HASH");
            return false;
        } else if (toCheck.getId() != minedBlocks.size() + 1) {
            System.out.println(toCheck.getId());
            System.out.println("ZLE ID");
            return false;
        }
        return true;
    }

}
