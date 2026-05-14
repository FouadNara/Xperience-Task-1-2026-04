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
            String subject = "You're invited to " + invitation.getEvent().getTitle();
            String rsvpLink = "http://localhost:3000/rsvp/" + invitation.getToken();
            String body = "Hello,\n\n" +
                "You are invited to: " + invitation.getEvent().getTitle() + "\n" +
                "Date: " + invitation.getEvent().getDateTime() + "\n" +
                "Location: " + invitation.getEvent().getLocation() + "\n\n" +
                "Please respond to this invitation by clicking the link below:\n" +
                rsvpLink + "\n\n" +
                "Thank you!";
            
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
            String subject = "RSVP Confirmation - " + invitation.getEvent().getTitle();
            String body = "Hello,\n\n" +
                "Your RSVP response has been recorded.\n" +
                "Status: " + rsvp.getStatus() + "\n\n" +
                "Event: " + invitation.getEvent().getTitle() + "\n" +
                "Date: " + invitation.getEvent().getDateTime() + "\n" +
                "Location: " + invitation.getEvent().getLocation() + "\n\n" +
                "Thank you!";
            
            logger.info("Sending confirmation email to {} for event {}", 
                invitation.getInviteeEmail(), invitation.getEvent().getTitle());
            
        } catch (Exception e) {
            logger.error("Failed to send confirmation email to {}", invitation.getInviteeEmail(), e);
        }
    }
    
    public void sendWaitlistPromotionEmail(Invitation invitation, RSVP rsvp) {
        try {
            String subject = "You're confirmed! - " + invitation.getEvent().getTitle();
            String body = "Great news!\n\n" +
                "A spot has opened up and you've been promoted from the waitlist!\n" +
                "Your new status: CONFIRMED\n\n" +
                "Event: " + invitation.getEvent().getTitle() + "\n" +
                "Date: " + invitation.getEvent().getDateTime() + "\n" +
                "Location: " + invitation.getEvent().getLocation() + "\n\n" +
                "See you there!";
            
            logger.info("Sending waitlist promotion email to {}", invitation.getInviteeEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send promotion email to {}", invitation.getInviteeEmail(), e);
        }
    }
    
    public void sendEventCancellationEmail(String inviteeEmail, String eventTitle) {
        try {
            String subject = "Event Cancelled - " + eventTitle;
            String body = "Hello,\n\n" +
                "The following event has been cancelled:\n" +
                "Event: " + eventTitle + "\n\n" +
                "We apologize for any inconvenience.\n\n" +
                "Thank you!";
            
            logger.info("Sending cancellation email to {}", inviteeEmail);
            
        } catch (Exception e) {
            logger.error("Failed to send cancellation email to {}", inviteeEmail, e);
        }
    }
}
