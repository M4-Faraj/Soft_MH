package org.Code;

public class User {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String []books;
    private double fine = 0;

    public User(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
    /// /////////////////////////////////////F name
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    /// /////////////////////////////////////L name
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    /// /////////////////////////////////////User Name
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    /// /////////////////////////////////////email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    /// /////////////////////////////////////pass
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public boolean allowedBooks(){

        if(books.length>2){
            return true;

        }else{
            return false;
        }
    }
    public double getFine() {
        return fine;
    }

    public void addFine(double amount) {
        fine += amount;
    }

    public void payFine(double amount) {
        if (amount <= 0) {
            System.out.println("Invalid payment amount!");
            return;
        }

        fine -= amount;
        if (fine < 0) fine = 0;

        System.out.println("Fine paid. Remaining balance: " + fine);
    }

    public boolean hasOutstandingFine() {
        return fine > 0;
    }
}
