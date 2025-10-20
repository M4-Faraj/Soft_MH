package org.Code;

public class User {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String []books;

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
}
