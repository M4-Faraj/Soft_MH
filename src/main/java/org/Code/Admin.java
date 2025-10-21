package org.Code;

public class Admin {
    private String username;
    private String password;
    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean login(String username, String password) {
        if(username.equals(this.username) && password.equals(this.password)) {
            System.out.println("You are now an admin");
            return true;
        }
        System.out.println("you r not an admin");
        return false;
    }
}
