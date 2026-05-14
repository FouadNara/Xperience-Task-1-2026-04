package com.xperience.hero.service;

import com.xperience.hero.dto.RSVPRequest;
import com.xperience.hero.dto.RSVPResponse;
import com.xperience.hero.entity.Event;
import com.xperience.hero.entity.Invitation;
import com.xperience.hero.entity.RSVP;
import com.xperience.hero.exception.ResourceNotFoundException;
import com.xperience.hero.repository.RSVPRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class RSVPService {
    
    private final RSVPRepository rsvpRepository;
    private final InvitationService invitationService;
    private final EventService eventService;
    private final EmailService emailService;
    private final InvitationRepository invitationRepository;
    
    public RSVPService(RSVPRepository rsvpRepository,
                      InvitationService invitationService,
                      EventService eventService,
                      EmailService emailService,
                      InvitationRepository invitationRepository) {
        this.rsvpRepository = rsvpRepository;
        this.invitationService = invitationService;
        this.eventService = eventService;
        this.emailService = emailService;
        this.invitationRepository = invitationRepository;
    }
    
    public RSVPResponse submitRSVP(String token, RSVPRequest request) {
        // Get invitation by token
        Invitation invitation = invitationService.getInvitationByToken(token);
        Event event = invitation.getEvent();
        
        // Check if event is cancelled
        if (event.getStatus() == Event.EventStatus.CANCELLED) {
            throw new IllegalArgumentException("Cannot RSVP to a cancelled event");
        }
        
        // Check if RSVPs are locked (after event start time)
        if (isEventLocked(event)) {
            throw new IllegalArgumentException("This event has started and RSVPs are no longer accepted");
        }
        
        // Parse status
        RSVP.RSVPStatus status;
        try {
            status = RSVP.RSVPStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid RSVP status. Must be YES, NO, or MAYBE");
        }
        
        // Check if RSVP already exists
        Optional<RSVP> existingRsvp = rsvpRepository
            .findByEventIdAndInviteeEmail(event.getId(), invitation.getInviteeEmail());
        
        RSVP rsvp;
        if (existingRsvp.isPresent()) {
            rsvp = existingRsvp.get();
            
            // If changing from YES to something else, handle waitlist promotion
            if (rsvp.getStatus() == RSVP.RSVPStatus.YES && 
                status != RSVP.RSVPStatus.YES) {
                promoteFromWaitlist(event);
            }
            
            rsvp.setStatus(status);
            rsvp.setUpdatedAt(LocalDateTime.now());
        } else {
            // New RSVP - check capacity if status is YES
            rsvp = new RSVP();
            rsvp.setEvent(event);
            rsvp.setInviteeEmail(invitation.getInviteeEmail());
            rsvp.setCreatedAt(LocalDateTime.now());
            rsvp.setUpdatedAt(LocalDateTime.now());
            
            if (status == RSVP.RSVPStatus.YES) {
                // Check capacity with pessimistic lock
                int confirmedCount = countConfirmedRsvpsForUpdate(event.getId());
                
                if (event.getMaxCapacity() != null && confirmedCount >= event.getMaxCapacity()) {
                    // At capacity - add to waitlist
                    status = RSVP.RSVPStatus.WAITLISTED;
                    rsvp.setPositionInWaitlist(getNextWaitlistPosition(event.getId()));
                }
            }
            
            rsvp.setStatus(status);
        }
        
        RSVP savedRsvp = rsvpRepository.save(rsvp);
        
        // Send confirmation email
        emailService.sendConfirmationEmail(invitation, savedRsvp);
        
        return RSVPResponse.fromEntity(savedRsvp);
    }
    
    public RSVPResponse getRSVP(String token) {
        Invitation invitation = invitationService.getInvitationByToken(token);
        
        RSVP rsvp = rsvpRepository
            .findByEventIdAndInviteeEmail(invitation.getEvent().getId(), invitation.getInviteeEmail())
            .orElseThrow(() -> new ResourceNotFoundException("RSVP not found"));
        
        return RSVPResponse.fromEntity(rsvp);
    }
    
    public List<RSVPResponse> getEventAttendees(String eventId, String userId) {
        Event event = eventService.getEventEntity(eventId);
        
        // Verify host authorization
        if (!event.getHost().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        
        List<RSVP> rsvps = rsvpRepository.findByEventId(eventId);
        return rsvps.stream()
            .map(RSVPResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    public AttendanceStats getAttendanceStats(String eventId, String userId) {
        Event event = eventService.getEventEntity(eventId);
        
        // Verify host authorization
        if (!event.getHost().getId().equals(userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }
        
        List<RSVP> rsvps = rsvpRepository.findByEventId(eventId);
        
        long yesCount = rsvps.stream().filter(r -> r.getStatus() == RSVP.RSVPStatus.YES).count();
        long noCount = rsvps.stream().filter(r -> r.getStatus() == RSVP.RSVPStatus.NO).count();
        long maybeCount = rsvps.stream().filter(r -> r.getStatus() == RSVP.RSVPStatus.MAYBE).count();
        long waitlistedCount = rsvps.stream().filter(r -> r.getStatus() == RSVP.RSVPStatus.WAITLISTED).count();
        
        return new AttendanceStats(yesCount, noCount, maybeCount, waitlistedCount);
    }
    
    private boolean isEventLocked(Event event) {
        return event.isRsvpsLocked() || LocalDateTime.now().isAfter(event.getDateTime());
    }
    
    private int countConfirmedRsvpsForUpdate(String eventId) {
        // In a production system, this would use SELECT FOR UPDATE
        // For now, we use a simple count
        return rsvpRepository.countConfirmedRsvps(eventId);
    }
    
    private void promoteFromWaitlist(Event event) {
        List<RSVP> waitlisted = rsvpRepository
            .findByEventIdAndStatusOrderByCreatedAtAsc(event.getId(), RSVP.RSVPStatus.WAITLISTED);
        
        if (!waitlisted.isEmpty()) {
            RSVP firstWaitlisted = waitlisted.get(0);
            firstWaitlisted.setStatus(RSVP.RSVPStatus.YES);
            firstWaitlisted.setPositionInWaitlist(null);
            firstWaitlisted.setUpdatedAt(LocalDateTime.now());
            
            RSVP savedRsvp = rsvpRepository.save(firstWaitlisted);
            
            // Send promotion email
            Invitation invitation = invitationService.getInvitationByToken(
                getTokenForInvitee(event.getId(), firstWaitlisted.getInviteeEmail())
            );
            emailService.sendWaitlistPromotionEmail(invitation, savedRsvp);
        }
    }
    
    private int getNextWaitlistPosition(String eventId) {
        List<RSVP> waitlisted = rsvpRepository
            .findByEventIdAndStatusOrderByCreatedAtAsc(eventId, RSVP.RSVPStatus.WAITLISTED);
        return waitlisted.size() + 1;
    }
    
    private String getTokenForInvitee(String eventId, String inviteeEmail) {
        // This is a helper to get token from invitation
        // Get all invitations for the event directly from repository
        List<Invitation> invitations = invitationRepository.findByEventId(eventId);
        return invitations.stream()
            .filter(inv -> inv.getInviteeEmail().equals(inviteeEmail))
            .findFirst()
            .map(Invitation::getToken)
            .orElseThrow(() -> new ResourceNotFoundException("Token not found"));
    }
    
    public static class AttendanceStats {
        public long yesCount;
        public long noCount;
        public long maybeCount;
        public long waitlistedCount;
        
        public AttendanceStats(long yesCount, long noCount, long maybeCount, long waitlistedCount) {
            this.yesCount = yesCount;
            this.noCount = noCount;
            this.maybeCount = maybeCount;
            this.waitlistedCount = waitlistedCount;
        }
    }
}
