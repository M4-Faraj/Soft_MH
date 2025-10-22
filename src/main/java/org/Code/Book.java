package org.Code;

public class Book {
     private String name;
     private String author;
    private String ISBN;
    private boolean borrowed;

    public Book(String Name, String author, String ISBN, boolean borrowed) {
        this.name = Name;
        this.author = author;
        this.ISBN = ISBN;
        this.borrowed = borrowed;
    }
    public void updateName(String Name){
        this.name=Name;
    }
    public void updateAuthor(String author){
        this.author=author;
    }
    public void updateISBN(String ISBN){
        this.ISBN=ISBN;
    }
    public void updateborrowed(boolean borrowed){
        this.borrowed=borrowed;
    }

    public String getName(){
        return name;
    }
    public String getAuthor(){
        return author;
    }
    public String getISBN(){
        return ISBN;
    }
    public boolean isBorrowed(){
        return borrowed;
    }


}
