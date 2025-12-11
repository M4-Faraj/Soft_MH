package org.Code;

/**
 * Provides administrative operations for managing books in the library system.
 * This includes adding new books and searching for books in the system.
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */

public class AdminControl {

    public void addBook(String Name, String author, String ISBN, boolean borrowed){
        Book e=new Book(Name,author,ISBN,borrowed);
    FileControler.addBookAsync(e);
    }

    /**
     * Searches for a book in the library using a keyword.
     * The method checks if the searched word appears in the book's:
     * <ul>
     *     <li>name</li>
     *     <li>ISBN</li>
     *     <li>author</li>
     * </ul>
     *
     * @param searchedWord the keyword to search for
     * @return the first matching {@link Book}, or {@code null} if no match is found
     */

    public Book searchBook(String searchedWord){
        for(Book book1:FileControler.BooksList){
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
