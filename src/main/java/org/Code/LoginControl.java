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
    public boolean isAdmin(String username, String password) {
        String fileName = "src/main/infoBase/Admin.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine(); // read first line

            if (line != null) {
                String[] parts = line.split(","); // split by comma
                String fileUsername = parts[0];
                String filePassword = parts[1];
                if(fileUsername.equals(this.username) && filePassword.equals(this.password)){
                    return true;
                }
            }

        } catch (IOException e) {
            return false;
        }
    return false;
    }
    public boolean isRegisteredUser(String username, String password) {
        String fileName = "src/main/infoBase/Users.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;

            // Read all lines
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(","); // split by comma
                if (parts.length < 2) continue;   // skip invalid lines

                String fileUsername = parts[0].trim();
                String filePassword = parts[1].trim();

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

/// ////////////////////////////a called class to log in
    public LoginControl(){
        Scanner input = new Scanner(System.in);
        Admin MH=new Admin("MH","1234");
        System.out.print("Choose a Number: \n(1)-Login \n(2)-Signup\n");
        String readNumber=input.nextLine();

        if(readNumber.equals("1")){
            System.out.println("Enter Username or Email:");
            this.username=input.nextLine();

            System.out.println("Enter Password:");
            this.password=input.nextLine();
            if(isAdmin(this.username,this.password)){

            System.out.println("You are an Admin");

        }else if(isRegisteredUser(this.username,this.password)){
            System.out.println("You are an Registered User");
        }

        }else if(readNumber.equals("2")){

            System.out.println("Not Yet");
        }
    }

}
