package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        //TODO JUnit tests
        //TODO configuration file
        int port;
        String path;
        try (InputStream input= new FileInputStream("config.properties")){
            Properties prop = new Properties();
            prop.load(input);
            port = Integer.parseInt(prop.getProperty("node_port"));
            path = prop.getProperty("keys_path");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Client client = new Client(port,path);
        client.start();
    }
}
