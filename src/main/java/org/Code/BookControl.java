package org.Code;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookControl {
    private Books books;
    private List<Loan> loans = new ArrayList<>();

    public BookControl(Books books){
        this.books = books;
    }

    public void borrowBook (User user , String searchedWord){
        Book book = books.searchBook(searchedWord);
        if(book == null){
            System.out.println("Book not found!");
            return;
        }
        if(book.isborrowed()){
            System.out.println("Book is already borrowed");
            return;
        }

        Loan loan = new Loan(book, user, LocalDate.now(), 28);
        loans.add(loan);
        book.updateBorrowed(true);

        System.out.println("Borrow successful! Due date: " + loan.getDueDate());
    }

    public List<Loan> getOverDueBooks(LocalDate currentDate){
        List<Loan> overdueBooks = new ArrayList<>();
        for (Loan loan : loans) {
            if (!loan.isReturned() && loan.getDueDate().isBefore(currentDate)) {
                overdueBooks.add(loan);
            }
        }

        return overdueBooks;
    }

    public void payFine(User user, double amount) {
        user.payFine(amount);
    }


}
