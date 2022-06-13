package blockchain;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class Main {
    //private static final long NUMBER_OF_MINERS = 10L;

    public static void main(String[] args) throws InterruptedException {
//TODO peer-to-peer networking
        Blockchain bc = Blockchain.getInstance();
        ServerThread serverThread = new ServerThread(1337, bc);
        Thread t = new Thread(serverThread);
        t.start();
        System.out.println(bc);
        System.out.println(bc.getSize());
        System.out.println(bc.validate());
    }
}
