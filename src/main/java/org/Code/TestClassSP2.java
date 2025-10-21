package org.Code;

public class TestClassSP2 {

    public static void adminLogin () {
        String TestUserName="MOF";
        String TestPassword="123";

    LoginControl MH=new LoginControl(TestUserName,TestPassword);
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




////we will test for right input but not from the username or email type shi
    }



    public void adminLogout () {

    }
    public void userLogin () {

    }
    public void userLogout () {
    }

    public void bookBorrow(){

    }
    public void bookOverDue(){

    }
    public void bookPayFine(){

    }

    public static  void main(String[] args) {
        adminLogin();
    }
}
