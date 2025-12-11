package org.Code;
/**
 * Represents an administrator in the library system.
 * This class stores basic authentication information such as username and password.
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
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

  
}
