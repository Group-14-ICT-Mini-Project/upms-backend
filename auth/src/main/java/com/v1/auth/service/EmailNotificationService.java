package com.v1.auth.service;

import com.v1.auth.model.User;

public interface EmailNotificationService {
    void sendAccessGrantedEmail(User user);
}
