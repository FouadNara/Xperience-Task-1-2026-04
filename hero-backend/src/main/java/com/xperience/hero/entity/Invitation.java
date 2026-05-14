package com.xperience.hero.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "invitations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "invitee_email"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Invitation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
    
    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;
    
    @Column(unique = true, nullable = false)
    private String token;
    
    @Column(name = "email_sent_at")
    private LocalDateTime emailSentAt;
    
    @Column(name = "email_failed")
    private boolean emailFailed = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
