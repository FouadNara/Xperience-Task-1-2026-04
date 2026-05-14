package com.xperience.hero.dto;

import com.xperience.hero.entity.RSVP;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RSVPResponse {
    private String id;
    private String eventId;
    private String inviteeEmail;
    private String status;
    private Integer positionInWaitlist;
    private LocalDateTime updatedAt;
    
    public static RSVPResponse fromEntity(RSVP rsvp) {
        RSVPResponse response = new RSVPResponse();
        response.setId(rsvp.getId());
        response.setEventId(rsvp.getEvent().getId());
        response.setInviteeEmail(rsvp.getInviteeEmail());
        response.setStatus(rsvp.getStatus().toString());
        response.setPositionInWaitlist(rsvp.getPositionInWaitlist());
        response.setUpdatedAt(rsvp.getUpdatedAt());
        return response;
    }
}
