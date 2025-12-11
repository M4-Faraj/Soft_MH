package org.Code;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/**
 * A lightweight in-memory container for managing registered users
 * within the library system.
 *
 * <p>This class does NOT handle file storage. It only stores users in
 * a local list and provides operations for adding, removing, and retrieving
 * all registered users.
 *
 * <p>It is typically used together with controllers such as
 * {@link UserControl} or {@link BookControl}.
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
public class Users {
    List<User> registeredUser = new ArrayList<>();

    public void fill(){

    }
    /**
     * Adds a new user to the in-memory list.
     *
     * @param user the user to add
     */
    public void addUser(User user) {
        registeredUser.add(user);
    }
    /**
     * Removes a user from the in-memory list.
     *
     * @param user the user to remove
     */
    public void removeUser(User user) {
        registeredUser.remove(user);
    }

    public List<User> getAllUsers() {
        return registeredUser;
    }
}
