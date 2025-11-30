package org.Code;

import java.time.LocalDate;

public class User {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private String[] books;
    private double fine = 0;
    private boolean isAdmin = false;
    private boolean isLibrarian = false;


    // === Constructors ===

    // Constructor الأساسي اللي بتستخدمه في SignUp
    public User(String firstName, String lastName, String username,
                String email, String password, String[] books) {
        this.firstName = firstName;
        this.lastName  = lastName;
        this.username  = username;
        this.email     = email;
        this.password  = password;
        this.books     = (books != null) ? books : new String[0];
    }

    // Constructor بدون books → يخلق مصفوفة فاضية
    public User(String firstName, String lastName, String username,
                String email, String password) {
        this(firstName, lastName, username, email, password, new String[0]);
    }

    // Constructor فاضي لو حابب تنشئ وبعدين تعبّي بالـ setters
    public User() {
        this("", "", "", "", "", new String[0]);
    }

    // === Getters & Setters ===

    // First name
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    // Last name
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // Username
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    // Email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public boolean hasOutstandingFine() {
        return FileControler.hasOverdueBooks(this.username);
    }

    // Password
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    // Books
    public String[] getBooks() {
        return books;
    }
    public void setBooks(String[] books) {
        this.books = (books != null) ? books : new String[0];
    }

    // === Fine & Admin stuff ===

    public double getFine() {
        return fine;
    }

    public void addFine(double amount) {
        fine += amount;
    }
    public boolean isLibrarian() { return isLibrarian; }
    public void setLibrarian(boolean librarian) { isLibrarian = librarian; }

    public void payFine(double amount) {
        if (amount <= 0) {
            System.out.println("Invalid payment amount!");
            return;
        }

        fine -= amount;
        if (fine < 0) fine = 0;

        System.out.println("Fine paid. Remaining balance: " + fine);
    }
    public void applyFineForLoan(Loan loan, LocalDate currentDate) {
        if (loan.isOverdue()) {
            Media item = loan.getItem(); // Media can be Book, CD, etc.
            int finePerDay = item.getOverdueFine(); // Book = 10, CD = 20
            long daysOverdue = loan.daysOverdue(currentDate);
            addFine(finePerDay * daysOverdue);
        }
    }


    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    // === Helper ===

    // هل عدد الكتب أكبر من 2؟
    public boolean allowedBooks() {
        return books != null && books.length > 2;
    }
}
