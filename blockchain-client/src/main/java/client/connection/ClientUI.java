package client.connection;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.Scanner;

/**
 * Console user interface for client logic.
 * Allows interacting with blockchain.
 * @see Client
 * @author Mikołaj Morozowski
 */
public class ClientUI {
    Scanner input;
    Client client;

    /**
     *
     * Creates new <code>ClientUI</code> and load parameters from config.properties file.
     * Loads port and path from config file.
     * <p>Starts {@link Client#Client(int, String)  Client()} with port and path loaded from <code>config.properties.</code></p>
     * Also creates new {@link Scanner#Scanner(InputStream) <code>Scanner</code>} used for reading user input from standard input.
     *
     * @throws IOException  parsing config file went wrong.
     */
    public ClientUI() throws IOException {
        int port;
        String path;
        try (InputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            port = Integer.parseInt(prop.getProperty("node_port")); //Port of blockchain node - in future ip:port
            path = prop.getProperty("keys_path"); // Path to folder where the keys will be stored
        }
        client = new Client(port, path);
        input = new Scanner(System.in);
    }

    /**
     * Prints login menu to standard output.
     */
    private void printLoginMenu() {
        System.out.println("---MENU---\n" +
                "1.Log in\n2.Register\n");
    }

    /**
     * Prints main menu and checks option selected by user.
     * @throws IOException something with user input goes wrong.
     * @throws GeneralSecurityException something with transaction goes wrong.
     */

    private void MainMenu() throws IOException, GeneralSecurityException, ClassNotFoundException {
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

    /**
     * Gets user credentials from standard input.
     * @return 2 element array with credentials.
     */
    private String[] getCredentials() {
        String[] output = new String[2];
        System.out.println("Username:");
        output[0] = input.nextLine();
        System.out.println("Password:");
        output[1] = input.nextLine();
        return output;
    }

    /**
     * Starts UI with login menu then calls selected Client methods.
     * @throws Exception something with Client method goes wrong.
     * @see Client
     */
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
