package ru.otus.chat.server;

import java.util.List;

public class UserService extends AutoCloseable {
    List<User> getAll();
    boolean isAdmin(int userId);

}
