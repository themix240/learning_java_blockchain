package search;

public class ArgsParser {
    String input_file = null;
    public ArgsParser(String[] args) {
        String stringBefore="";
        for (String x : args) {
            switch (stringBefore) {
                case "--data":
                    input_file= x;
                    break;
            }
            stringBefore = x;
        }
    }
}
