package com.xperience.hero.service;

import com.xperience.hero.dto.InvitationBatchRequest;
import com.xperience.hero.entity.Event;
import com.xperience.hero.entity.Invitation;
import com.xperience.hero.exception.ResourceNotFoundException;
import com.xperience.hero.exception.UnauthorizedException;
import com.xperience.hero.repository.InvitationRepository;
import com.xperience.hero.util.TokenGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class InvitationService {
    
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    
    private final InvitationRepository invitationRepository;
    private final EventService eventService;
    private final EmailService emailService;
    
    public InvitationService(InvitationRepository invitationRepository, 
                            EventService eventService,
                            EmailService emailService) {
        this.invitationRepository = invitationRepository;
        this.eventService = eventService;
        this.emailService = emailService;
    }
    
    public void sendInvitations(String eventId, String userId, InvitationBatchRequest request) {
        Event event = eventService.getEventEntity(eventId);
        
        // Verify host authorization
        if (!event.getHost().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to send invitations for this event");
        }
        
        // Validate event is not cancelled
        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot send invitations for a cancelled event");
        }
        
        // Validate emails and create invitations
        for (String email : request.getEmails()) {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException("Invalid email format: " + email);
            }
            
            // Check if invitation already exists
            Optional<Invitation> existing = invitationRepository
                .findByEventIdAndInviteeEmail(eventId, email);
            
            if (existing.isEmpty()) {
                Invitation invitation = new Invitation();
                invitation.setEvent(event);
                invitation.setInviteeEmail(email);
                invitation.setToken(TokenGenerator.generateSecureToken());
                invitation.setCreatedAt(LocalDateTime.now());
                invitation.setUpdatedAt(LocalDateTime.now());
                
                invitationRepository.save(invitation);
                
                // Queue email sending (asynchronously in real app)
                emailService.sendInvitationEmail(invitation);
            }
        }
    }
    
    public Invitation getInvitationByToken(String token) {
        return invitationRepository.findByToken(token)
            .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invitation link"));
    }
    
    public List<Invitation> getInvitations(String eventId, String userId) {
        Event event = eventService.getEventEntity(eventId);
        
        // Verify host authorization
        if (!event.getHost().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view invitations for this event");
        }
        
        return invitationRepository.findByEventId(eventId);
    }
    
    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
}
