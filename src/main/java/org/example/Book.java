package org.example;

import javax.swing.*;

public class Books {
     private String name;
     private String author;
    private String ISBN;

    public Books(String Name, String author, String ISBN) {
        this.name = Name;
        this.author = author;
        this.ISBN = ISBN;
    }
    puplic void updateName(String Name){
        this.name=Name;
    }
    puplic void updateAuthor(String author){
        this.author=author;
    }
    puplic void updateISBN(String ISBN){
        this.ISBN=ISBN;
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

}
