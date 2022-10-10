package node;

import node.core.Blockchain;
import node.networking.ConnectionHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws /*InterruptedException,*/ IOException/*, ClassNotFoundException*/ {
        if (!Files.exists(Path.of("config.properties"))) {
            createDefaultConfig();
        }
        Blockchain bc = Blockchain.getInstance();
        int port = 0;
        int p2pPort = 0;
        String path;
        String userDBPath = null;
        //Create properties file in project dir

        try (InputStream inputStream = new FileInputStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            port = Integer.parseInt(properties.getProperty("client_port")); //port on which node is listening to client connections
            userDBPath = properties.getProperty("userDB"); // path to txt file which store username-publickey credentials (to more friendly use)
            path = properties.getProperty("nodes_ips"); //path to file with other nodes in p2p network (format of node_ip - ip:port)
            p2pPort = Integer.parseInt(properties.getProperty("p2pPort")); //port on which node is listening to p2p connections
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ConnectionHandler connectionHandler = new ConnectionHandler(port, p2pPort, bc, userDBPath, path);
        //GS Thread t = new Thread(connectionHandler);
        Thread t = new Thread(connectionHandler, "Blockchain Connection Handler"); //GS -naming threads is a good practice
        t.start();
        //GS System.out.println(bc);
        //GS System.out.println(bc.getSize());
        //GS System.out.println(validate(bc.getBlocks()));
    }

    private static void createDefaultConfig() throws IOException {
        File file = new File("config.properties");
        if (file.createNewFile()) {
            Properties properties = new Properties();
            properties.setProperty("client_port", "1337");
            properties.setProperty("userDB", "blockchain/serverData/userDB.txt");
            properties.setProperty("nodes_ips", "blockchain/nodes.txt");
            properties.setProperty("p2pPort", "1338");
            properties.setProperty("blockchain_path", "blockchain/blockchain.txt");
            properties.setProperty("blockchain_file_type", "txt");
            properties.store(new FileOutputStream(file), null);
        }
        File data = new File("blockchain/serverData/userDB.txt");
        data.getParentFile().mkdirs();
        data.createNewFile();


    }
}
