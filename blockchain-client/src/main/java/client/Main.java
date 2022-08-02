package client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws Exception {
        if(!Files.exists(Path.of("config.properties"))){
            createDefaultConfig();
        }
            ClientUI ui = new ClientUI();
            ui.start();
    }

    private static void createDefaultConfig() throws IOException {
            File file = new File("config.properties");
            if (file.createNewFile()) {
                Properties properties = new Properties();
                properties.setProperty("node_port", "1337");
                properties.setProperty("keys_path", "blockchain/user_data/");
                properties.setProperty("ip_address", "localhost");
                properties.store(new FileOutputStream(file), null);
            }



    }
}
