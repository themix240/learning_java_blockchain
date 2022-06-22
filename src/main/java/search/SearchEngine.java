package search;

import java.util.ArrayList;

public class SearchEngine implements SearchEng{
    People[] data;

    public SearchEngine(People[] data) {
        this.data = data;
    }

    public ArrayList<People> Search(String keyword,String strategy) {
        keyword=keyword.toUpperCase();
        ArrayList<People> output= new ArrayList<People>();
        for(People p : data) {
          if((p.getName().toUpperCase().contains(keyword))||
             (p.getLast_name().toUpperCase().contains(keyword))||
             (p.getEmail().toUpperCase().contains(keyword))){
              output.add(p);
          }
        }
        return output;

    }
}
