package org.Code;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    boolean Admin;
    boolean RegisteredUser;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        LoginControl MH = new LoginControl("1");
        Admin MHO=new Admin("MH","1234");
        System.out.print("enter UserName:");
        String readUsername=input.nextLine();

        System.out.print("enter Password:");
        String readPassword=input.nextLine();
        MHO.login(readUsername, readPassword);

    }
}