package org.Code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class LoginControl {
    private String username;
    private String password;
    public LoginControl(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public static boolean isAdmin(String username, String password) {
        String fileName = "src/main/infoBase/Admin.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String fileUsername = parts[0].trim();
                    String filePassword = parts[1].trim();
                    if (fileUsername.equals(username) && filePassword.equals(password)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isRegisteredUser(String username, String password) {
        String fileName = "src/main/infoBase/Users.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            // Read all lines
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(","); // split by comma
                if (parts.length < 6) continue;   // skip invalid lines

                String fileUsername = parts[2].trim();
                String filePassword = parts[4].trim();

                // Check if username and password match
                if (fileUsername.equals(username) && filePassword.equals(password)) {
                    return true; // found a match
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false; // no match found
    }
    public LoginControl(){

    }

/// ////////////////////////////a called class to log in


}
