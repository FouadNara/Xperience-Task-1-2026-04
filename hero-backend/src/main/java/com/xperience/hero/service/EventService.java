package com.xperience.hero.service;

import com.xperience.hero.dto.EventRequest;
import com.xperience.hero.dto.EventResponse;
import com.xperience.hero.entity.Event;
import com.xperience.hero.entity.User;
import com.xperience.hero.exception.ResourceNotFoundException;
import com.xperience.hero.exception.UnauthorizedException;
import com.xperience.hero.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {
    
    private final EventRepository eventRepository;
    private final AuthService authService;
    
    public EventService(EventRepository eventRepository, AuthService authService) {
        this.eventRepository = eventRepository;
        this.authService = authService;
    }
    
    public EventResponse createEvent(String userId, EventRequest request) {
        User host = authService.getUserById(userId);
        
        // Validate date is in the future
        if (request.getDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event date must be in the future");
        }
        
        Event event = new Event();
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setDateTime(request.getDateTime());
        event.setLocation(request.getLocation());
        event.setMaxCapacity(request.getMaxCapacity());
        event.setHost(host);
        event.setStatus(Event.EventStatus.ACTIVE);
        event.setRsvpsLocked(false);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        
        Event savedEvent = eventRepository.save(event);
        return EventResponse.fromEntity(savedEvent);
    }
    
    public EventResponse getEvent(String eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return EventResponse.fromEntity(event);
    }
    
    public EventResponse getEventWithAuth(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        // Verify host authorization
        if (!event.getHost().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to view this event");
        }
        
        return EventResponse.fromEntity(event);
    }
    
    public List<EventResponse> getEventsByHost(String userId) {
        User host = authService.getUserById(userId);
        List<Event> events = eventRepository.findByHost(host);
        return events.stream()
            .map(EventResponse::fromEntity)
            .collect(Collectors.toList());
    }
    
    public EventResponse updateEvent(String eventId, String userId, EventRequest request) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        // Verify host authorization
        if (!event.getHost().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to update this event");
        }
        
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setLocation(request.getLocation());
        event.setUpdatedAt(LocalDateTime.now());
        
        Event updatedEvent = eventRepository.save(event);
        return EventResponse.fromEntity(updatedEvent);
    }
    
    public void cancelEvent(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        // Verify host authorization
        if (!event.getHost().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to cancel this event");
        }
        
        event.setStatus(Event.EventStatus.CANCELLED);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
    }
    
    public void closeEventToResponses(String eventId, String userId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        // Verify host authorization
        if (!event.getHost().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to close this event");
        }
        
        event.setStatus(Event.EventStatus.CLOSED);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
    }
    
    public Event getEventEntity(String eventId) {
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
    }
    
    public void lockEvent(String eventId) {
        Event event = getEventEntity(eventId);
        event.setRsvpsLocked(true);
        event.setUpdatedAt(LocalDateTime.now());
        eventRepository.save(event);
    }
}
