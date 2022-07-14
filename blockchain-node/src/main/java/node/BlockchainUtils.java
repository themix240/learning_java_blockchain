package node;

import utils.Block;
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
    public static int getWallet(PublicKey pk,List<Block> blocks) {
        int wallet = 0;
        for(Block b : blocks){
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
    public static boolean validate(List<Block> blocks) {
        for(int i = 1; i< blocks.size();i++){
            if(!Objects.equals(blocks.get(i).getPrevHash(), blocks.get(i - 1).getHash())){
                return false;
            }
        }
        return true;
    }
    public static boolean checkBlock(List<Block> blocks, Block toCheck,int nonce){
        String zeros = new String(new char[nonce]).replace('\0', '0');
        String prevHash = (blocks.size() - 1 >= 0) ? blocks.get(blocks.size() - 1).getHash() : "0";
        if (!toCheck.getHash().startsWith(zeros)) {
            System.out.println("ZLA LICZBA ZER");
            return false;
        } else if (!toCheck.getPrevHash().equals(prevHash)) {
            System.out.println("ZLY POPRZEDNI HASH");
            return false;
        } else if (toCheck.getId() != blocks.size() + 1) {
            System.out.println(toCheck.getId());
            System.out.println("ZLE ID");
            return false;
        }
        return true;
    }

}
