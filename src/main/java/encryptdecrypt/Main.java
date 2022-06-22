package encryptdecrypt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;


public class Main {
    public static void main(String[] args) {
        ArgsParser arguments = new ArgsParser(args);
        EncyptorDecryptor encryptor = arguments.alg.equals("shift") ? new ShiftEncryptor() : new UnicodeEncryptor();
        try {
            arguments.data = arguments.data.equals("") ? Files.readString(Path.of(arguments.in_path)) : arguments.data;
        } catch (IOException e) {
            System.out.println("Error - input file does not exist!");
        }
        if (arguments.out_path == null)
            System.out.println(arguments.mode.equals("enc") ? encryptor.encrypt(arguments.data, arguments.key) : encryptor.decrypt(arguments.data, arguments.key));
        else {
            File output_file = new File(arguments.out_path);
            try {
                output_file.createNewFile();
            } catch (IOException e) {
                System.out.println("Error");
            }
            try {
                PrintWriter pw = new PrintWriter(arguments.out_path);
                pw.println(arguments.mode.equals("enc") ? encryptor.encrypt(arguments.data, arguments.key) : encryptor.decrypt(arguments.data, arguments.key));
                pw.close();
            } catch (FileNotFoundException e) {
                System.out.println("Error - output file does not exist!");
            }
        }
    }
}
