package com.v1.auth.service.impl;

import com.v1.auth.model.User;
import com.v1.auth.service.EmailNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MicrosoftGraphEmailNotificationService implements EmailNotificationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.mail.graph.tenant-id:}")
    private String tenantId;

    @Value("${app.mail.graph.client-id:}")
    private String clientId;

    @Value("${app.mail.graph.client-secret:}")
    private String clientSecret;

    @Value("${app.mail.graph.sender:}")
    private String sender;

    @Value("${app.frontend.login-url:http://localhost:5173/login}")
    private String loginUrl;

    @Override
    public void sendAccessGrantedEmail(User user) {
        if (isBlank(tenantId) || isBlank(clientId) || isBlank(clientSecret) || isBlank(sender)) {
            throw new IllegalStateException("Microsoft Graph mail settings are not configured");
        }

        String accessToken = requestAccessToken();
        String displayName = buildDisplayName(user);
        String body = """
                Dear %s,

                Your UPMS access has been granted. You can now sign in with your username and password.

                Login: %s
                Username: %s

                University Procurement Management System
                """.formatted(displayName, loginUrl, user.getUsername());

        Map<String, Object> payload = Map.of(
                "message", Map.of(
                        "subject", "UPMS access granted",
                        "body", Map.of(
                                "contentType", "Text",
                                "content", body
                        ),
                        "toRecipients", List.of(Map.of(
                                "emailAddress", Map.of("address", user.getEmail())
                        ))
                ),
                "saveToSentItems", true
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String url = "https://graph.microsoft.com/v1.0/users/%s/sendMail".formatted(sender);
        restTemplate.postForEntity(url, new HttpEntity<>(payload, headers), Void.class);
        log.info("Access approval email sent to {}", user.getEmail());
    }

    private String requestAccessToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("scope", "https://graph.microsoft.com/.default");
        form.add("client_secret", clientSecret);
        form.add("grant_type", "client_credentials");

        String url = "https://login.microsoftonline.com/%s/oauth2/v2.0/token".formatted(tenantId);
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(url, new HttpEntity<>(form, headers), Map.class);
        if (response == null || response.get("access_token") == null) {
            throw new IllegalStateException("Microsoft Graph did not return an access token");
        }

        return response.get("access_token").toString();
    }

    private String buildDisplayName(User user) {
        String firstName = user.getFirstName() == null ? "" : user.getFirstName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String fullName = (firstName + " " + lastName).trim();
        return fullName.isBlank() ? user.getUsername() : fullName;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
