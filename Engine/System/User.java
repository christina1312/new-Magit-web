
package System;

public class User {

    private String name = "Administrator";

    public String getName() { return name; }
    public void setName(String s) { this.name=s;  }

    public User(String name){this.name=name;}
    public User(){}

    @Override
    public String toString() {
        return name;
    }
}