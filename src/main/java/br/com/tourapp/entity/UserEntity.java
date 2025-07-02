package br.com.tourapp.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "google_id", unique = true)
    private String googleId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "subscription_plan")
    private String subscriptionPlan;

    @Column(name = "subscription_expiry")
    private LocalDateTime subscriptionExpiry;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    // Constructors, getters and setters
    public UserEntity() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getSubscriptionPlan() { return subscriptionPlan; }
    public void setSubscriptionPlan(String subscriptionPlan) { this.subscriptionPlan = subscriptionPlan; }

    public LocalDateTime getSubscriptionExpiry() { return subscriptionExpiry; }
    public void setSubscriptionExpiry(LocalDateTime subscriptionExpiry) { this.subscriptionExpiry = subscriptionExpiry; }

    public Set<RoleEntity> getRoles() { return roles; }
    public void setRoles(Set<RoleEntity> roles) { this.roles = roles; }
}
