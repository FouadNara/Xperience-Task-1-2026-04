package com.xperience.hero.scheduler;

import com.xperience.hero.entity.Event;
import com.xperience.hero.repository.EventRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class EventLockingScheduler {
    
    private final EventRepository eventRepository;
    
    public EventLockingScheduler(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }
    
    /**
     * Runs every minute to lock events that have started
     */
    @Scheduled(fixedRate = 60000) // 60 seconds
    public void lockStartedEvents() {
        try {
            LocalDateTime now = LocalDateTime.now();
            
            // Find all active events that have started
            List<Event> events = eventRepository.findAll();
            
            events.stream()
                .filter(event -> event.getStatus() == Event.EventStatus.ACTIVE)
                .filter(event -> !event.isRsvpsLocked())
                .filter(event -> event.getDateTime().isBefore(now) || event.getDateTime().equals(now))
                .forEach(event -> {
                    event.setRsvpsLocked(true);
                    event.setUpdatedAt(LocalDateTime.now());
                    eventRepository.save(event);
                });
        } catch (Exception e) {
            // Log the error but don't crash the scheduler
            System.err.println("Error locking events: " + e.getMessage());
        }
    }
}
