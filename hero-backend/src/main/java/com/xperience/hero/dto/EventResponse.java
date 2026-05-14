package com.xperience.hero.dto;

import com.xperience.hero.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private String id;
    private String title;
    private String description;
    private LocalDateTime dateTime;
    private String location;
    private Integer maxCapacity;
    private String status;
    private boolean rsvpsLocked;
    private LocalDateTime createdAt;
    
    public static EventResponse fromEntity(Event event) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setDateTime(event.getDateTime());
        response.setLocation(event.getLocation());
        response.setMaxCapacity(event.getMaxCapacity());
        response.setStatus(event.getStatus().toString());
        response.setRsvpsLocked(event.isRsvpsLocked());
        response.setCreatedAt(event.getCreatedAt());
        return response;
    }
}
