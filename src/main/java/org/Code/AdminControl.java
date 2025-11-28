package org.Code;

public class AdminControl {

    public void addBook(String Name, String author, String ISBN, boolean borrowed){
        Book e=new Book(Name,author,ISBN,borrowed);
    FileControler.addBook(e);
    }

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
