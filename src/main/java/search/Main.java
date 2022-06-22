package search;

public class Main {
    public static void main(String[] args) {
        ArgsParser argsParser = new ArgsParser(args);
        Data dane = new Data(argsParser.input_file);
        SearchEng se = new InvertedIndexSearchEngine(new SearchEngine(dane.data.toArray(People[]::new)));
        UserInterface ui = new UserInterface(se,dane.data.toArray(new People[0]),dane.full_out);
        ui.interfaceLoop();
    }


}