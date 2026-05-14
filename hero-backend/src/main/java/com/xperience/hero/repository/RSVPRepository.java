package com.xperience.hero.repository;

import com.xperience.hero.entity.RSVP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RSVPRepository extends JpaRepository<RSVP, String> {
    Optional<RSVP> findByEventIdAndInviteeEmail(String eventId, String inviteeEmail);
    List<RSVP> findByEventId(String eventId);
    List<RSVP> findByEventIdAndStatusOrderByCreatedAtAsc(String eventId, RSVP.RSVPStatus status);
    
    @Query("SELECT COUNT(r) FROM RSVP r WHERE r.event.id = :eventId AND r.status = 'YES'")
    int countConfirmedRsvps(@Param("eventId") String eventId);
}
