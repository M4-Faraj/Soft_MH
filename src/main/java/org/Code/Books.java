package org.Code;

import java.util.ArrayList;
import java.util.List;

public class Books {
    List<Book> availableBooks = new ArrayList<>();

    public Book searchBook(String searchedWord){
        for(Book book1:availableBooks){
            if(book1.getName().contains(searchedWord)){
                return book1;
            }

            if(book1.getISBN().contains(searchedWord)){
                return book1;
            }

            if(book1.getAuthor().contains(searchedWord)){
                return book1;
            }
        }

            return null;
    }


}
