package encryptdecrypt;

public class ArgsParser {
    String mode = "enc";
    int key = 0;
    String data = "";
    String in_path = null;
    String out_path = null;
    String stringBefore = "";
    String alg = "shift";
    public ArgsParser(String[] args) {
        for (String x : args) {
            switch (stringBefore) {
                case "-mode":
                    mode = x;
                    break;
                case "-key":
                    key = Integer.parseInt(x);
                    break;
                case "-data":
                    data = x;
                    break;
                case "-in":
                    in_path = x;
                    break;
                case "-out":
                    out_path = x;
                    break;
                case "-alg":
                    alg = x;
                    break;
            }
            stringBefore = x;
        }
    }
}
