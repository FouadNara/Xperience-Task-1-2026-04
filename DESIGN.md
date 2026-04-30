# Event RSVP Manager — Design Document

---

## 1. Facts (Known and Given)

### Raw Feature Brief
- Users can create events with: title, description, date/time, location, optional max-capacity
- Creators become **hosts**
- Hosts invite people by email
- Invitees get unique links to RSVP: Yes/No/Maybe
- Hosts see live attendance dashboard (counts + attendee list)
- Capacity management: Yes responses → Waitlist when full
- Automatic waitlist promotion when someone changes to No
- Hosts can cancel or close events
- Invitees can change RSVP before event starts
- After event start time: all RSVPs are locked

### Tech Stack
- Backend: Java 17, Spring Boot 4, Spring MVC, Spring Data JPA
- Database: PostgreSQL (local, port 5432, schema: `hero`)
- Frontend: React 19, TypeScript, Vite

### Confirmed Requirements
- **Authentication**: Users must log in to create/host events
- **Email Notifications**: Yes, required (invitations, confirmations, waitlist promotions)
- **Event Privacy**: Events are private (only visible to host + invited guests)

---

## 2. Assumptions (To Confirm)

**Authentication & Users**
- [ ] Are registered users required, or guest accounts (email-only)?
- [ ] What login method? (Email/password, OAuth, social login?)
- [ ] Can invitees RSVP without creating an account? (Magic link, or must register?)

**Invitations & Links**
- [ ] How long are RSVP links valid? (Forever, or expire after event date?)
- [ ] Can a link be used multiple times, or one-time?
- [ ] What happens if the same person gets invited twice to same event?

**Waitlist Behavior**
- [ ] Promotion order: FIFO or other criteria?
- [ ] Should waitlisted people see their position number?
- [ ] Email notification when promoted from waitlist?

**Event Updates & Cancellation**
- [ ] Can hosts edit event details (date, location, capacity) after creation?
- [ ] When event is cancelled, what happens to attendees? (Email notification?)

**Post-Event & Data**
- [ ] Can hosts view final RSVP list after event ends?
- [ ] Data retention: keep old events indefinitely or archive/delete?

---

## 3. High-Level Architecture (No-Code)

### Three Main Entities
1. **User** (Hosts & system users)
   - Authentication required
   - Can create multiple events
   - Can view only their own events (+ invitations)

2. **Event** (Created by host)
   - Private to host + invited guests
   - Has capacity, dates, location
   - Host can cancel or close to new RSVPs

3. **RSVP / Invitation**
   - Unique link per invitee per event
   - RSVP status: Yes / No / Maybe / Waitlisted
   - Can change status before event start time

### Key Workflows

**Create Event**
- Host logs in → Creates event with details → System stores in DB → Host gets dashboard link

**Invite Guests**
- Host provides email list → System generates unique links → Emails sent → Invitees receive & can click

**Attend Event (Host POV)**
- Host sees live dashboard: counts by status, attendee list, waitlist

**Attend Event (Invitee POV)**
- Invitee clicks unique link → Views event details → Selects response (Yes/No/Maybe) → Confirmation sent via email

**Waitlist Management**
- When event is full: new Yes → Waitlist
- When attendee → No: first Waitlist person → Confirmed (with notification)

---

## 4. Data Model (High-Level)

### Core Tables (PostgreSQL)

| Entity | Key Fields | Notes |
|--------|-----------|-------|
| **User** | id, email, password_hash, name, created_at | Hosts & registered users |
| **Event** | id, host_id (FK), title, description, date_time, location, max_capacity, is_cancelled, is_closed, created_at | Private events |
| **Invitation** | id, event_id (FK), invitee_email, unique_token, rsvp_status, position_if_waitlisted, responded_at | One per invitee per event |

### Status Values
- RSVP Status: `PENDING`, `YES`, `NO`, `MAYBE`, `WAITLISTED`
- Event Status: `OPEN`, `CLOSED_TO_RSVP`, `CANCELLED`

---

## 5. User Flows (High-Level)

### Flow 1: Host Creates Event
```
Host logs in → Fill event form → Submit → Event stored → Get dashboard link → Ready to invite
```

### Flow 2: Host Invites Guests
```
Host enters email list → Click "Invite" → System generates unique tokens → Emails sent with RSVP links
```

### Flow 3: Invitee RSVPs
```
Invitee clicks email link → Lands on event details page → Selects Yes/No/Maybe → Submits → DB updated → Email confirmation
```

### Flow 4: Waitlist Promotion
```
Attendee changes from Yes → No → System checks waitlist → First waitlisted → Promoted to Yes → Email notification sent
```

---

## 6. Open Questions (Still Need Answers)

- **Invitee Account Creation**: Do invitees need to register as users, or just respond via link?
- **Event Discovery**: Can guests see a public event list, or only invited events?
- **Edit Event**: Can host modify event after creation? (Title, date, capacity?)
- **Email Provider**: Will you use a mail service (SendGrid, AWS SES) or local SMTP?
- **Front-end Design**: Any wireframes or specific UI components already chosen?
- **Role Permissions**: Can an invitee be added as a co-host?

---

## 7. [To Continue...]

Once the above questions are answered, we will define:
- Detailed API endpoints
- Database schema (with foreign keys, constraints)
- Authentication flow (JWT, sessions, etc.)
- Email template structure
- Error handling & validation rules
- Deployment & performance considerations
