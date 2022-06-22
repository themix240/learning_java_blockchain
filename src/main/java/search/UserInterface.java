package search;

import java.util.List;
import java.util.Scanner;

public class UserInterface {
    static SearchEng se;
    People[] data;
    List<String> full_data;
    Scanner input;
    public UserInterface(SearchEng se, People[] data,List<String> full_data) {
        this.se = se;
        this.data = data;
        this.full_data = full_data;
         input= new Scanner(System.in);
    }

    public void interfaceLoop()
    {
        printMenu();
        int option = Integer.parseInt(input.nextLine());
        while(option != 0) {
            switch(option){
                case 1:
                    option_1();
                    break;
                case 2:
                    option_2();
                    break;
                default:
                    System.out.println("\nIncorrect option! Try again.");
            }
            printMenu();
            option = Integer.parseInt(input.nextLine());
        }
        System.out.println("\nBye!");}
    private  void option_2() {
        System.out.println("\n===List of all people===");
        full_data.forEach(System.out::println);
    }

    private  void option_1() {
        System.out.println("Select a matching strategy: ALL, ANY, NONE");
        String strategy = input.nextLine();
        System.out.println("\nEnter a name or email to search all suitable people.");
        List<People> match= se.Search(input.nextLine(),strategy);
        if ((match.size() == 0)) {
            System.out.println("\nNo matching people foud.");
        } else {
            System.out.println(match.size()+" persons found:");
            match.forEach(System.out::println);
        }

    }

    public static void printMenu(){
        System.out.println("\n=== Menu ===");
        System.out.println("1. Find a person");
        System.out.println("2. Print all people");
        System.out.println("0. Exit");
    }
}
