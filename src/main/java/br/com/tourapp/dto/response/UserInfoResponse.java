package br.com.tourapp.dto.response;


import java.time.LocalDateTime;
import java.util.List;

public class UserInfoResponse {
    private Long id;
    private String email;
    private String fullName;
    private String profilePicture;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private List<String> roles;
    private String subscriptionPlan;
    private LocalDateTime subscriptionExpiry;
    private boolean hasActiveSubscription;

    // Constructors
    public UserInfoResponse() {}

    public UserInfoResponse(Long id, String email, String fullName, String profilePicture, LocalDateTime createdAt, LocalDateTime lastLogin, List<String> roles, String subscriptionPlan, LocalDateTime subscriptionExpiry, boolean hasActiveSubscription) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.profilePicture = profilePicture;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.roles = roles;
        this.subscriptionPlan = subscriptionPlan;
        this.subscriptionExpiry = subscriptionExpiry;
        this.hasActiveSubscription = hasActiveSubscription;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public LocalDateTime getSubscriptionExpiry() { return subscriptionExpiry; }
    public void setSubscriptionExpiry(LocalDateTime subscriptionExpiry) {
        this.subscriptionExpiry = subscriptionExpiry;
    }

    public boolean isHasActiveSubscription() { return hasActiveSubscription; }
    public void setHasActiveSubscription(boolean hasActiveSubscription) {
        this.hasActiveSubscription = hasActiveSubscription;
    }
}