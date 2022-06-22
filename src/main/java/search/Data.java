package search;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Data {
    List<People> data= new ArrayList<>();
    List<String> full_out = new ArrayList<>();
    public Data(String path) {
        try {
            BufferedReader input = new BufferedReader(new FileReader(path));
            String line;
            File out_1 = new File("out_1.txt");
            out_1.createNewFile();
            FileWriter line_writer = new FileWriter(out_1);
            while((line = input.readLine())!=null){
                String[] items = line.split(" ");
                line_writer.append(line+'\n');
                full_out.add(line);
                switch (items.length){
                    case 1:
                        data.add(new People(items[0]));
                        break;
                    case 2:
                        data.add(new People(items[0],items[1]));
                        break;
                    case 3:
                        data.add(new People(items[0],items[1],items[2]));
                        break;
                }
            }
            line_writer.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
