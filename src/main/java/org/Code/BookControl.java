package org.Code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookControl {
    private Books books;
    private List<Loan> loans = new ArrayList<>();
    private Users users;


    public BookControl(Books books){
        this.books = books;
    }

    public int numberOfBorrowedBooks() {
        String filePath = "src/main/InfoBase/Borrowed_Books.txt";
        Path path = Paths.get(filePath).toAbsolutePath();

        if (!Files.exists(path)) {
            return 0; // لو الملف مش موجود اعتبره فاضي
        }

        try {
            return (int) Files.lines(path).count();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void borrowBook (User user , String searchedWord){
        if (user.hasOutstandingFine()) {
            System.out.println("Cannot borrow until fine is fully paid. Outstanding balance: "
                    + user.getFine());
            return;
        }



        List<Loan> overdueLoans = getOverDueBooks(LocalDate.now());
        for (Loan l : overdueLoans) {
            if (l.getUser().equals(user)) {
                System.out.println("Cannot borrow because you have overdue books. Please return them first.");
                return;
            }
        }

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

    public void unregisterUser(User admin, User targetUser) {

        if (!admin.isAdmin()) {
            System.out.println("Only admins can unregister users.");
            return;
        }

        if (targetUser.getFine() > 0) {
            System.out.println("Cannot unregister user. They have unpaid fines: "
                    + targetUser.getFine());
            return;
        }

        for (Loan loan : loans) {
            if (loan.getUser().equals(targetUser) && !loan.isReturned()) {
                System.out.println("Cannot unregister user. They have active or overdue loans.");
                return;
            }
        }

        users.removeUser(targetUser);
        System.out.println("User successfully unregistered.");
    }

    public List<Loan> getLoans() {
        return loans;
    }
    public void setUsers(Users users) {
        this.users = users;
    }
}
