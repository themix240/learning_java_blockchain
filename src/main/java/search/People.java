package search;

public class People {
    String name;
    String last_name;
    String email;

    public People(String name) {
        this.name = name;
        this.last_name = "";
        this.email = "";
    }

    @Override
    public String toString() {
        return name + (last_name.equals("") ? "" : " "+ last_name)+(email.equals("") ? "" : " "+ email);
    }

    public People(String name, String last_name) {
        this.name = name;
        this.last_name = last_name;
        this.email = "";
    }

    public People(String name, String last_name, String email) {
        this.name = name;
        this.last_name = last_name;
        this.email = email;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


}
