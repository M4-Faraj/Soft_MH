package org.Code;

import java.io.*;
import java.util.ArrayList;

public class FileControler {
    public static ArrayList<Book> BooksList = new ArrayList<Book>();
    public static ArrayList<User> UserList = new ArrayList<User>();

public FileControler(){
    fillBooksData();
}

public static void fillBooksData(){
    try (BufferedReader br = new BufferedReader(new FileReader("src/main/infoBase/Books.txt"))) {
        String line;
        while ((line = br.readLine()) != null) {
            // Skip empty lines
            if (line.trim().isEmpty()) continue;

            // Split by comma
            String[] parts = line.split(",");

            // Check if we have all 4 fields
            if (parts.length == 4) {
                String name = parts[0].trim();
                String author = parts[1].trim();
                String ISBN = parts[2].trim();
                Boolean borrowed = Boolean.parseBoolean(parts[3].trim());

                // Add new Book object to ArrayList
                BooksList.add(new Book(name, author, ISBN, borrowed));
            } else {
                System.out.println("Invalid line format: " + line);
            }
        }
        System.out.println("Loaded " + BooksList.size() + " books from file.");
    } catch (IOException e) {
        System.out.println("Error reading file: " + e.getMessage());
    }
}

    public static void fillUsersData(){
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/infoBase/Users.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Split by commas
                String[] parts = line.split(",");

                if (parts.length == 6) {
                    String firstName = parts[0].trim();
                    String lastName = parts[1].trim();
                    String username = parts[2].trim();
                    String email = parts[3].trim();
                    String password = parts[4].trim();

                    // Books separated by semicolons
                    String[] books = parts[5].trim().split(";");
                    for (int i = 0; i < books.length; i++) {
                        books[i] = books[i].trim();
                    }

                    UserList.add(new User(firstName, lastName, username, email, password, books));
                } else {
                    System.out.println("Invalid line format: " + line);
                }
            }
            System.out.println("Loaded " + UserList.size() + " users from file.");
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
    public static void addBook(Book book) {
        String fileName = "src/main/infoBase/Books.txt";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            // Format: Name,Author,ISBN,Borrowed
            bw.write(book.getName() + "," + book.getAuthor() + "," + book.getISBN() + "," + book.isBorrowed());
            bw.newLine(); // move to the next line
            System.out.println("Book added successfully!");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        fillBooksData();
        fillUsersData();

        // Example: print all loaded books
        for (Book b : BooksList) {
            System.out.println(b.getISBN());
        }
        for (User u : UserList) {
            System.out.println(u.getEmail());
        }
    }
}
