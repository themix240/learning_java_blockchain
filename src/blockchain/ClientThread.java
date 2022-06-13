package blockchain;


import javax.crypto.*;
import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

import static blockchain.HEADERS.*;

public class ClientThread implements Runnable {
    private final Socket socket;
    private final List<String> t;
    private SecretKey sessionKey;

    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;
    private final List<User> users;
    private User user=null;
    private final String path = "/Users/themix240/blockchain/server_data/userDB.txt";
    private final Blockchain bc;

    public ClientThread(Socket socket, List<String> t, List<User> users, Blockchain bc) {
        this.t = t;
        this.socket = socket;
        this.users = users;
        this.bc = bc;
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try {
            int x = objectInputStream.readByte();
            switch (x) {
                case 9:
                    logowanie();
                    break;
                case 10: //TODO integrate constants
                    rejestracja();
                    break;

            }
            return;
        } catch (IOException | ClassNotFoundException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    private void rejestracja() {
        try {
            Object recived = objectInputStream.readObject();
            String username = (String) recived;
            System.out.println(username);
            //TODO integrate search engine
            user = findUser(username);
            if (user==null) {
                objectOutputStream.writeByte(REGISTRATION_SUCCESFULL.data);
                objectOutputStream.flush();
                //String passphrase = (String) objectInputStream.readObject();
                PublicKey pk = (PublicKey) objectInputStream.readObject();
                User usr = new User(username, pk);
                saveUser(usr);
            } else
                objectOutputStream.writeByte(REGISTRATION_UNSUCCESFULL.data);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveUser(User userToSave) {
        users.add(userToSave);
        saveChanges();
    }

    private void saveChanges() {
        File f = new File(path);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(f);
            ObjectOutputStream outputStream1 = new ObjectOutputStream(fileOutputStream);
            outputStream1.writeObject(users);
            outputStream1.close();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void userInterface() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        String options;
        while (!Thread.currentThread().isInterrupted()) {
            options = (String) objectInputStream.readObject();
            if (options.equals("mining")) {
                writeBlockchainData();
            } else if (options.equals("wykopano")) {
                long timeStamp = objectInputStream.readLong();
                String hash = (String) objectInputStream.readObject();
                //long time = objectInputStream.readLong(); //TODO cleanup unnecesarry variables
                int magicNumber = objectInputStream.readInt();
                String signature = (String) objectInputStream.readObject();
                Block b = new Block(timeStamp, bc.getSize()+1, bc.getLastHash(), magicNumber,signature);
                if (bc.acceptBlock(b)) {
                    saveChanges();
                }
            } else if (options.equals("checkWallet")) {
                objectOutputStream.writeInt(bc.getWallet(user.username));
                objectOutputStream.flush();
            } else if (options.equals("initTransaction")) {
                transaction();
                //TODO verify transaction with signature and add it to the block

            }
        }
    }

    private void transaction() throws IOException, ClassNotFoundException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        List<String> usernames = users.stream().map(User::getUsername).collect(Collectors.toList());
        objectOutputStream.writeObject(usernames);
        String selectedUser = (String) objectInputStream.readObject();
        int ammount = objectInputStream.readInt();
        //System.out.println(ammount);
        if (calculateWallet() - ammount < 0) {
            objectOutputStream.writeByte(NO_ENOUGH_MONEY.data);
        } else {
            writeTransaction(selectedUser,ammount);
            objectOutputStream.writeByte(TRANSACTION_SUCCESFULL.data);
        }
        objectOutputStream.flush();
    }

    private int calculateWallet() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
       return bc.getWallet(user.username);
    }
    private void writeTransaction(String selectedUser,int ammount){
//TODO generate transaction
    }

    private void writeBlockchainData() throws IOException {
        objectOutputStream.writeLong(bc.getSize()+1);
        objectOutputStream.flush();
        objectOutputStream.writeObject(bc.getLastHash());
        objectOutputStream.flush();
        objectOutputStream.writeObject(bc.getStarting_zeros());
        objectOutputStream.flush();
    }

    private void logowanie() throws IOException, GeneralSecurityException, ClassNotFoundException {
            Object recived = objectInputStream.readObject();
            String username = (String) recived;
            //TODO session key verification initHandshake();
            //TODO proper login
            user = findUser(username);
            if (user.username.equals(username)) {
                byte[] challange = new byte[64];
                SecureRandom sr = new SecureRandom();
                sr.nextBytes(challange);
                byte[] encrypted = CryptoUtils.encryptBytes(challange,user.publicKey);
                objectOutputStream.writeObject(Base64.getEncoder().encodeToString(encrypted));
                byte[] decrypted =  objectInputStream.readNBytes(64);
                if (Arrays.equals(challange,decrypted)) {
                    System.out.println("ZALOGOWOWANO " + user.username);
                    objectOutputStream.writeByte(LOGIN_SUCCESFULL.data);
                    objectOutputStream.flush();
                    userInterface();
                }
            }
    }
    private User findUser(String username) {
        for (User u : users) {
            if (username.equals(u.username)) {
                return u;
            }
        }
        return null;
    }
}
