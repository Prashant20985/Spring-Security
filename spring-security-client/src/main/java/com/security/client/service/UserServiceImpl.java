package com.security.client.service;
import com.security.client.entity.PasswordResetToken;
import com.security.client.entity.User;
import com.security.client.entity.VerificationToken;
import com.security.client.model.UserModel;
import com.security.client.repository.IPasswordResetTokenRepository;
import com.security.client.repository.IUserRepository;
import com.security.client.repository.IVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IVerificationTokenRepository verificationTokenRepository;

    @Autowired
    private IPasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public User registerUser(UserModel userModel) {
        User user = new User();
        user.setEmail(userModel.getEmail());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));

        userRepository.save(user);
        return user;
    }

    @Override
    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken verificationToken = new VerificationToken(user, token);

        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public String validateVerificationToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByToken(token);
        if (verificationToken == null) {
            return "invalid";
        }

        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();

        if (verificationToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
           verificationTokenRepository.delete(verificationToken);
           return "Expired";
        }

        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository
                .findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user, token);

        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public String validatePasswordRestToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(token);
        if (passwordResetToken == null) {
            return "invalid";
        }

        User user = passwordResetToken.getUser();
        Calendar calendar = Calendar.getInstance();

        if (passwordResetToken.getExpirationTime().getTime() - calendar.getTime().getTime() <= 0) {
            passwordResetTokenRepository.delete(passwordResetToken);
            return "Expired";
        }

        return "valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}
