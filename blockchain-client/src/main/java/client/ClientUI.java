package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;

public class ClientUI {
    Scanner input;
    Client client;


    public ClientUI() throws IOException {
        int port;
        String path;
        //Create config properties in project folder
        try (InputStream input = new FileInputStream("blockchain-client/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            port = Integer.parseInt(prop.getProperty("node_port")); //Port of blockchain node - in future ip:port
            path = prop.getProperty("keys_path"); // Path to folder where the keys will be stored
        }
        client = new Client(port, path);
        input = new Scanner(System.in);
    }

    private void printLoginMenu() {
        System.out.println("---MENU---\n" +
                "1.Log in\n2.Register\n");
    }

    private void MainMenu() throws Exception {
        System.out.println("1. Switch mining\n" +
                "2. Check Wallet\n" +
                "3. Make transaction\n" +
                "4. Quit");
        int option = Integer.parseInt(input.nextLine());
        while (true) {
            switch (option) {
                case 1:
                    client.switchMining();
                    break;
                case 2:
                    System.out.println(client.checkWallet());
                    break;
                case 3:
                    System.out.println("Username of reciver:");
                    String username = input.nextLine();
                    System.out.println("Amount to transfer: ");
                    int amount = Integer.parseInt(input.nextLine());
                    System.out.println("Passphrase: ");
                    String passphrase = input.nextLine();
                    if(client.transaction(username, amount, passphrase))
                        System.out.println("Transaction successful");
                    else
                        System.out.println("Transaction unsuccessful");
                    break;
                case 4:
                    client.quit();
                    return;
            }
            System.out.println("1. Switch mining\n" +
                    "2. Check Wallet\n" +
                    "3. Make transaction\n" +
                    "4. Quit");
            option = Integer.parseInt(input.nextLine());
        }
    }

    private String[] getCredentials() {
        String[] output = new String[2];
        System.out.println("Username:");
        output[0] = input.nextLine();
        System.out.println("Password:");
        output[1] = input.nextLine();
        return output;
    }

    public void start() throws Exception {
        printLoginMenu();
        int option = Integer.parseInt(input.nextLine());
        switch (option) {
            case 1:
                String[] loginCredentials = getCredentials();
                if (client.loginUser(loginCredentials[0], loginCredentials[1])) {
                    System.out.println("Login successful!");
                    MainMenu();
                } else
                    System.out.println("Login failed");
                break;
            case 2:
                String[] registerCredentials = getCredentials();
                if (client.registerUser(registerCredentials[0], registerCredentials[1]))
                    System.out.println("Registration successful!");
                else
                    System.out.println("Registration failed");
                break;

        }

    }
}
