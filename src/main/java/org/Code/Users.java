package org.Code;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Users {
    List<User> registeredUser = new ArrayList<>();

    public void addUser(User user) {
        registeredUser.add(user);
    }

    public void removeUser(User user) {
        registeredUser.remove(user);
    }

    public List<User> getAllUsers() {
        return registeredUser;
    }
}
