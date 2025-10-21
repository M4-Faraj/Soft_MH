package org.Code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LoginCotrol {
    private String username;
    private String password;
    public LoginCotrol(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public boolean isAdmin(){
        String fileName = "Admin.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine(); // read first line

            if (line != null) {
                String[] parts = line.split(","); // split by comma
                String username = parts[0];
                String password = parts[1];
                if(username.equals(this.username) && password.equals(this.password)){
                    return true;
                }
            }

        } catch (IOException e) {
            return false;
        }
    return false;
    }
    public boolean isRegisteredUser() {
        String fileName = "Users.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            // Read all lines
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(","); // split by comma
                if (parts.length < 2) continue;   // skip invalid lines

                String username = parts[0].trim();
                String password = parts[1].trim();

                // Check if username and password match
                if (username.equals(this.username) && password.equals(this.password)) {
                    return true; // found a match
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false; // no match found
    }

}
