package com.security.client.controller;

import com.security.client.entity.User;
import com.security.client.entity.VerificationToken;
import com.security.client.event.RegistrationCompleteEvent;
import com.security.client.model.PasswordModel;
import com.security.client.model.UserModel;
import com.security.client.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request){
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user,
                applicationUrl(request)
        ));
        return (userModel.getFirstName()+" added successfully");
    }

    @GetMapping("verifyRegistration")
    public String verifyRegistration(@RequestParam String token){
        String result = userService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")){
            return "User verified Successfully";
        }
        else {
            return "Bad User";
        }
    }

    @GetMapping("/resendVerificationToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request){
        VerificationToken verificationToken = userService
                .generateNewVerificationToken(oldToken);

        User user = verificationToken.getUser();

        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification link sent";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request){
        User user = userService.findByEmail(passwordModel.getEmail());
        String url = "";
        if(user != null){
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,token);
            url = passwordResetTokenMail(user,applicationUrl(request), token);
        }
        return url;
    }

    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token, @RequestBody PasswordModel passwordModel){
        String result = userService.validatePasswordRestToken(token);

        if(!result.equalsIgnoreCase("valid")){
            return "Invalid token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);

        if(user.isPresent()){
            userService.changePassword(user.get(), passwordModel.getNewPassword());
            return "Password reset successful";
        }
        else {
            return "invalid token";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel){
        User user = userService.findByEmail(passwordModel.getEmail());
        if(!userService.checkIfValidOldPassword(user, passwordModel.getOldPassword())){
            return "Invalid Old Password";
        }
        //save new Password
        userService.changePassword(user, passwordModel.getNewPassword());
        return "Password Changed successfully";
    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url = applicationUrl
                + "/savePassword?token="
                +token;
        log.info("Click the link to reset your password: {}",url);

        return url;
    }

    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url = applicationUrl
                + "/verifyRegistration?token="
                +verificationToken.getToken();
        log.info("Click the link to verify your account: {}",url);
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                ":" +
                request.getServerPort() +
                request.getContextPath();
    }
}
