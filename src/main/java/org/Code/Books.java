package org.Code;

import java.util.ArrayList;
import java.util.List;
/**
 * Manages a collection of available {@link Book} objects in the library system.
 * <p>
 * This class provides basic search functionality that allows lookup by:
 * <ul>
 *   <li>Book name</li>
 *   <li>Book ISBN</li>
 *   <li>Book author</li>
 * </ul>
 * It returns the first matching book found in the collection.
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
public class Books {
    List<Book> availableBooks = new ArrayList<>();
    /**
     * Searches for a {@link Book} whose name, ISBN, or author
     * contains the given search keyword.
     * <p>
     * The search is case-sensitive and returns the **first match only**.
     * If no match is found, {@code null} is returned.
     *
     * @param searchedWord the keyword used for matching name, ISBN, or author
     * @return the first matching {@link Book}, or {@code null} if no match exists
     */
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
