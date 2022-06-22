package search;

import java.util.*;

public class InvertedIndexSearchEngine implements SearchEng{
    SearchEngine se;
    People[] data;
    Map<String, Set<Integer>> data_map;
    public InvertedIndexSearchEngine(SearchEngine se) {
        this.se = se;
        data_map = new HashMap<>();
        data = se.data;
        List<People> data_list = Arrays.asList(se.data);
        for(int i= 0; i<data_list.size();i++){
            String[] splitedPerson = data_list.get(i).toString().split(" ");
            for(String s : splitedPerson){
              List<People> match= se.Search(s,"");
              Set<Integer> values = new HashSet<>();
              match.forEach(p -> values.add(data_list.indexOf(p)));

              data_map.put(s,values);
            }
        }
    }
    public ArrayList<People> Search(String keyword,String strategy)
    {
        keyword = keyword.toUpperCase();
        ArrayList<People> output = new ArrayList<>();
        String[] keywords = keyword.split(" ");
        switch (strategy){
            case "ANY":
                for(String k : keywords) {
                    for (String s : data_map.keySet()) {
                        if (s.toUpperCase().equals(k)) {
                            for (Integer i : data_map.get(s)) {
                                output.add(se.data[i]);
                            }
                        }
                    }
                }
                break;
            case "ALL":
                for(String k : keywords) {
                    for (String s : data_map.keySet()) {
                        if (s.toUpperCase().equals(k)) {
                            for (Integer i : data_map.get(s)) {
                                output.add(se.data[i]);
                            }
                        }
                    }
                }
                ArrayList<People>to_remove = new ArrayList<>();
                for(People p : output){
                    for(String k : keywords){
                        if(!p.toString().toUpperCase().contains(k)) {
                           to_remove.add(p);
                        }
                    }
                }
                output.removeAll(to_remove);
                break;
            case "NONE":
                output = new ArrayList<>(List.of(data));
                for(String k : keywords) {
                    for (String s : data_map.keySet()) {
                        if (s.toUpperCase().equals(k)) {
                            for (Integer i : data_map.get(s)) {
                                output.remove(se.data[i]);
                            }
                        }
                    }
                }

                break;

        }


        return output;
    }
}
