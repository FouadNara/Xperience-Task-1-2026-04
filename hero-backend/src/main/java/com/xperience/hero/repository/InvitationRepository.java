package com.xperience.hero.repository;

import com.xperience.hero.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, String> {
    Optional<Invitation> findByToken(String token);
    List<Invitation> findByEventId(String eventId);
    Optional<Invitation> findByEventIdAndInviteeEmail(String eventId, String inviteeEmail);
}
