package org.Code;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controls borrowing, fines, and user-related operations
 * for books and other media in the library system.
 * <p>
 * This class coordinates between {@link Books}, {@link MediaCollection},
 * {@link Loan}, and {@link Users} to:
 * <ul>
 *   <li>Borrow media (books or CDs)</li>
 *   <li>Check overdue items</li>
 *   <li>Calculate fines</li>
 *   <li>Unregister users (with conditions)</li>
 * </ul>
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
public class BookControl {
    private Books books;
    private MediaCollection mediaCollection;
    private List<Loan> loans = new ArrayList<>();
    private Users users;

    /**
     * Creates a new {@code BookControl} using the given {@link Books}
     * and {@link MediaCollection} instances.
     *
     * @param books           the books manager
     * @param mediaCollection the media collection used for searching items
     */
    public BookControl(Books books, MediaCollection mediaCollection) {
        this.books = books;
        this.mediaCollection = mediaCollection;
    }
    /**
     * Creates a new {@code BookControl} with a given {@link Books} instance
     * and a default empty {@link MediaCollection}.
     *
     * @param books the books manager
     */
    public BookControl(Books books) {
        this.books = books;
        this.mediaCollection = new MediaCollection(); // fallback
    }

    /**
     * Counts how many borrowed items are currently recorded in
     * {@code src/main/InfoBase/Borrowed_Books.txt}.
     * <p>
     * If the file does not exist or an error occurs, this method returns {@code 0}.
     *
     * @return the number of borrowed records found in the file
     */
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
    /**
     * Attempts to borrow a media item (book or CD) for the given user,
     * based on a search keyword.
     * <p>
     * Borrowing will be blocked if:
     * <ul>
     *   <li>The user has unpaid fines</li>
     *   <li>The user has any overdue loans</li>
     *   <li>The item cannot be found</li>
     *   <li>The item is already borrowed</li>
     * </ul>
     * On success, a new {@link Loan} is created, the item is marked as borrowed,
     * and a message with the due date is printed to the console.
     *
     * @param user         the user who is borrowing the item
     * @param searchedWord keyword used to search for the media in {@link MediaCollection}
     */
    public void borrowMedia(User user, String searchedWord){
        if (user.hasOutstandingFine()) {
            System.out.println("Cannot borrow until fine is fully paid. Outstanding balance: "
                    + user.getFine());
            return;
        }

        List<Loan> overdueLoans = getOverDueBooks(LocalDate.now());
        for (Loan l : overdueLoans) {
            if (l.getUser().equals(user)) {
                System.out.println("Cannot borrow because you have overdue items. Please return them first.");
                return;
            }
        }

        Media item = mediaCollection.searchItem(searchedWord); // Book or CD
        if(item == null){
            System.out.println("Item not found!");
            return;
        }
        if(item.isBorrowed()){
            System.out.println("Item is already borrowed");
            return;
        }

        int loanDays = item.getLoanDuration(); // Book: 28, CD: 7
        Loan loan = new Loan(item, user, LocalDate.now(), loanDays);
        loans.add(loan);
        item.updateBorrowed(true);

        System.out.println("Borrow successful! Due date: " + loan.getDueDate());
    }

    /**
     * Returns a list of all loans that are overdue relative
     * to the provided date.
     *
     * @param currentDate the date used to check whether a loan is overdue
     * @return list of overdue {@link Loan} objects
     */
    public List<Loan> getOverDueBooks(LocalDate currentDate){
        List<Loan> overdueBooks = new ArrayList<>();
        for (Loan loan : loans) {
            if (!loan.isReturned() && loan.getDueDate().isBefore(currentDate)) {
                overdueBooks.add(loan);
            }
        }

        return overdueBooks;
    }
    /**
     * Allows a user to pay part or all of their fine balance.
     * This simply delegates to {@link User#payFine(double)}.
     *
     * @param user   the user who is paying
     * @param amount the amount to pay
     */
    public void payFine(User user, double amount) {
        user.payFine(amount);
    }
    /**
     * Unregisters a target user from the system, if allowed.
     * <p>
     * A user can be unregistered only if:
     * <ul>
     *     <li>The caller is an admin ({@link User#isAdmin()})</li>
     *     <li>The target user has no unpaid fines</li>
     *     <li>The target user has no active or overdue loans</li>
     * </ul>
     * If one of these conditions is not met, an explanatory message is printed
     * and the operation is aborted.
     *
     * @param admin      the admin user requesting the operation
     * @param targetUser the user to unregister
     */
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
    /**
     * Calculates the total overdue fine for a specific user
     * based on all loans managed by this controller.
     * <p>
     * For each loan that is overdue as of {@code today}, the fine is:
     * {@code daysOverdue * item.getFinePerDay()}.
     *
     * @param user  the user whose fines should be calculated
     * @param today the date used to determine overdue days
     * @return total fine amount for the user
     */
    public double calculateOverdueFine(User user, LocalDate today) {
        double totalFine = 0;
        for (Loan loan : loans) {
            if (loan.getUser().equals(user) && loan.daysOverdue(today) > 0) {
                totalFine += loan.daysOverdue(today) * loan.getItem().getFinePerDay();
            }
        }
        return totalFine;
    }
    /**
     * Returns all overdue loans for the given user as of the specified date.
     *
     * @param user  the user whose overdue media should be fetched
     * @param today the date used as the reference point for overdue calculation
     * @return list of overdue {@link Loan} objects for the user
     */
    public List<Loan> getAllOverdueMedia(User user, LocalDate today) {
        List<Loan> overdue = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.getUser().equals(user) && loan.daysOverdue(today) > 0) {
                overdue.add(loan);
            }
        }
        return overdue;
    }
}
