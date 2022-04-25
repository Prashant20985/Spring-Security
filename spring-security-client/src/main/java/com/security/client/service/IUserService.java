package com.security.client.service;

import com.security.client.entity.User;
import com.security.client.entity.VerificationToken;
import com.security.client.model.UserModel;

import java.util.Optional;

public interface IUserService {

    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(String token, User user);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    String validatePasswordRestToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkIfValidOldPassword(User user, String oldPassword);
}

