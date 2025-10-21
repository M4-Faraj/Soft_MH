package org.Code;

public class User {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String []books;

    public User(String firstName, String lastName,String username, String email, String password, String[] books) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.books = books;
        this.username = username;
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
}
