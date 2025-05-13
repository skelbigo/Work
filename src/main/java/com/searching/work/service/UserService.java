package com.searching.work.service;

import com.searching.work.model.User;

public interface UserService {
    User register(String username, String password, String roleStr);
    User authenticate(String username, String password);
    void logout();
}
