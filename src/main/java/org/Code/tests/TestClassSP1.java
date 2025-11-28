package org.Code.tests;

import org.Code.Book;
import org.Code.LoginControl;

public class TestClassSP1 {

    public static void adminLoginTest () {
        String testUserName="MOF";
        String testPassword="123";

    LoginControl MH=new LoginControl(testUserName,testPassword);
    if(MH.isAdmin("MOF","123")||MH.isRegisteredUser("MOF","123")){
        System.out.println("Testing for false admin input:true");
    }
    else{
        System.out.println("Testing for false admin input: false ");
    }

    if(MH.isAdmin("MH","1234")||MH.isRegisteredUser("MOF","123")){
            System.out.println("Testing for true admin input: true");
        }
        else{
            System.out.println("Testing for true admin input: false");
        }




////we will test for right input but not from the username or email type shi
    }



    public void adminLogout () {

    }
   static public void userLogin () {
        String testUserName="MOF";
        String testPassword="123";

        LoginControl MH=new LoginControl(testUserName,testPassword);

        if(MH.isAdmin("LOLO","555")||MH.isRegisteredUser("LOLO","555")){
            System.out.println("Testing for false user input: true");
        }
        else{
            System.out.println("Testing for false user input: false");
        }
        if(MH.isAdmin("johndoe","1234")||MH.isRegisteredUser("johndoe","1234")){
            System.out.println("Testing for true user input: true");
        }
        else{
            System.out.println("Testing for true user input: false");
        }


    }
    public void userLogout () {

    }
    private boolean isAddedBook(Book book) {
return false;
    }
    public void adminAddBookTest(){

    }
    public boolean existsBook(String searchedWord){
return false;
    }
    
    public void userSearchBookTest(){
    }


    public static  void main(String[] args) {
        adminLoginTest();
        userLogin();
    }
}
