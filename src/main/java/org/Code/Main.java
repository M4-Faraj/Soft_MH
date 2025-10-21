package org.Code;
import java.util.Scanner;
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        Admin MH=new Admin("MH","1234");
        System.out.print("enter UserName:");
        String readUsername=input.nextLine();

        System.out.print("enter Password:");
        String readPassword=input.nextLine();
        MH.login(readUsername, readPassword);

    }
}