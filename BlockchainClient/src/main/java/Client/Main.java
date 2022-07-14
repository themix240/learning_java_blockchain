package Client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        int port;
        String path;
        //Create config properties in project folder
        try (InputStream input= new FileInputStream("BlockchainClient/config.properties")){
            Properties prop = new Properties();
            prop.load(input);
            port = Integer.parseInt(prop.getProperty("node_port")); //Port of blockchain node - in future ip:port
            path = prop.getProperty("keys_path"); // Path to folder where the keys will be stored

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Client client = new Client(port,path);
        client.start();
    }
}
