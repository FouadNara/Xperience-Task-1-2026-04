package com.xperience.hero.controller;

import com.xperience.hero.dto.InvitationBatchRequest;
import com.xperience.hero.entity.Invitation;
import com.xperience.hero.service.InvitationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class InvitationController {
    
    private final InvitationService invitationService;
    
    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }
    
    @PostMapping("/{eventId}/invitations")
    public ResponseEntity<Void> sendInvitations(
            @PathVariable String eventId,
            @RequestBody InvitationBatchRequest request,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        invitationService.sendInvitations(eventId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/{eventId}/invitations")
    public ResponseEntity<List<Invitation>> getInvitations(
            @PathVariable String eventId,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<Invitation> invitations = invitationService.getInvitations(eventId, userId);
        return ResponseEntity.ok(invitations);
    }
}
