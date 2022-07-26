package node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static node.BlockchainUtils.validate;

public class Main {
    public static void main(String[] args) throws InterruptedException, IOException, ClassNotFoundException {
        Blockchain bc = Blockchain.getInstance();
        int port = 0;
        int p2pPort = 0;
        String path;
        String userDBPath = null;
        //Create properties file in project dir
        try  (InputStream inputStream = new FileInputStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            port = Integer.parseInt(properties.getProperty("client_port")); //port on which node is listening to client connections
            userDBPath = properties.getProperty("userDB"); // path to txt file which store username-publickey credentials (to more friendly use)
            path = properties.getProperty("nodes_ips"); //path to file with other nodes in p2p network (format of node_ip - ip:port)
            p2pPort = Integer.parseInt(properties.getProperty("p2pPort")); //port on which node is listening to p2p connections
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        ConnectionHandler connectionHandler = new ConnectionHandler(port,p2pPort, bc,userDBPath,path);
        Thread t = new Thread(connectionHandler);
        t.start();
        System.out.println(bc);
        System.out.println(bc.getSize());
        System.out.println(validate(bc.getBlocks()));
    }
}
