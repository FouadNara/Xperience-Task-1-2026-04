package com.xperience.hero.controller;

import com.xperience.hero.dto.RSVPRequest;
import com.xperience.hero.dto.RSVPResponse;
import com.xperience.hero.service.RSVPService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/rsvp")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class RSVPController {
    
    private final RSVPService rsvpService;
    
    public RSVPController(RSVPService rsvpService) {
        this.rsvpService = rsvpService;
    }
    
    @PostMapping("/{token}")
    public ResponseEntity<RSVPResponse> submitRSVP(
            @PathVariable String token,
            @RequestBody RSVPRequest request) {
        RSVPResponse response = rsvpService.submitRSVP(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{token}")
    public ResponseEntity<RSVPResponse> getRSVP(@PathVariable String token) {
        RSVPResponse response = rsvpService.getRSVP(token);
        return ResponseEntity.ok(response);
    }
}

@RestController
@RequestMapping("/api/events/{eventId}/attendees")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
class AttendanceController {
    
    private final RSVPService rsvpService;
    
    public AttendanceController(RSVPService rsvpService) {
        this.rsvpService = rsvpService;
    }
    
    @GetMapping
    public ResponseEntity<List<RSVPResponse>> getAttendees(
            @PathVariable String eventId,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<RSVPResponse> response = rsvpService.getEventAttendees(eventId, userId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<RSVPService.AttendanceStats> getStats(
            @PathVariable String eventId,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        RSVPService.AttendanceStats stats = rsvpService.getAttendanceStats(eventId, userId);
        return ResponseEntity.ok(stats);
    }
}
