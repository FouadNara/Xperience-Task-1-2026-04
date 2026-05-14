package com.xperience.hero.service;

import com.xperience.hero.entity.Invitation;
import com.xperience.hero.entity.RSVP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    public void sendInvitationEmail(Invitation invitation) {
        try {
            // In production, integrate with actual email service
            logger.info("Sending invitation email to {} for event {}", 
                invitation.getInviteeEmail(), invitation.getEvent().getTitle());
            
            // Simulate email send
            invitation.setEmailSentAt(LocalDateTime.now());
            // Would save here in production
            
        } catch (Exception e) {
            logger.error("Failed to send invitation email to {}", invitation.getInviteeEmail(), e);
            invitation.setEmailFailed(true);
        }
    }
    
    public void sendConfirmationEmail(Invitation invitation, RSVP rsvp) {
        try {
            logger.info("Sending confirmation email to {} for event {}", 
                invitation.getInviteeEmail(), invitation.getEvent().getTitle());
            
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to {}", invitation.getInviteeEmail(), e);
        }
    }
    
    public void sendWaitlistPromotionEmail(Invitation invitation, RSVP rsvp) {
        try {
            logger.info("Sending waitlist promotion email to {}", invitation.getInviteeEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send promotion email to {}", invitation.getInviteeEmail(), e);
        }
    }
    
    public void sendEventCancellationEmail(String inviteeEmail, String eventTitle) {
        try {
            logger.info("Sending cancellation email to {}", inviteeEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send cancellation email to {}", inviteeEmail, e);
        }
    }
}
