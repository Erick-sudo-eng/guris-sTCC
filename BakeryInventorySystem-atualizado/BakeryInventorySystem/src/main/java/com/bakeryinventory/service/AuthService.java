package com.bakeryinventory.service;

import com.bakeryinventory.dao.UserDao;
import com.bakeryinventory.model.User;
import com.bakeryinventory.util.Validation;
import com.bakeryinventory.util.ValidationException;

import java.sql.SQLException;
import java.util.List;

public class AuthService {
    private final UserDao userDao = new UserDao();

    public User login(String username, String password) throws SQLException, ValidationException {
        Validation.requireUsername(username);
        Validation.requirePassword(password);
        User user = userDao.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Usuario ou senha invalidos."));
        if (!PasswordService.verifyPassword(password, user.getPasswordHash(), user.getSalt())) {
            throw new ValidationException("Usuario ou senha invalidos.");
        }
        return user;
    }

    public void createUser(User currentUser, String username, String password) throws SQLException, ValidationException {
        if (currentUser == null || !currentUser.isManager()) {
            throw new ValidationException("Somente o gerente pode criar contas de usuario.");
        }
        Validation.requireUsername(username);
        Validation.requirePassword(password);
        if ("gerente".equalsIgnoreCase(username.trim())) {
            throw new ValidationException("O usuario gerente e permanente e nao pode ser substituido.");
        }
        if (userDao.usernameExists(username)) {
            throw new ValidationException("Este nome de usuario ja esta em uso.");
        }
        PasswordService.HashResult hash = PasswordService.hashPassword(password);
        userDao.createUser(username, hash.hash(), hash.salt(), "USER");
    }

    public List<User> listUsers() throws SQLException {
        return userDao.findAll();
    }

    public void deleteUser(User currentUser, int userId) throws SQLException, ValidationException {
        if (currentUser == null || !currentUser.isManager()) {
            throw new ValidationException("Somente o gerente pode excluir contas de usuario.");
        }
        if (currentUser.getId() == userId) {
            throw new ValidationException("A conta gerente permanente nao pode ser excluida.");
        }
        userDao.deleteUser(userId);
    }
}
