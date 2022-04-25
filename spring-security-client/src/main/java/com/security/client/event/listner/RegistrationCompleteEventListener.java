package com.security.client.event.listner;

import com.security.client.entity.User;
import com.security.client.event.RegistrationCompleteEvent;
import com.security.client.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private IUserService userService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {
        //CreateVerification token for the user with link
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(token,user);
        //send mail to user
        String url = event.getApplicationUrl()
                + "/verifyRegistration?token="
                +token;
        log.info("Click the link to verify your account: {}",url);
    }
}
