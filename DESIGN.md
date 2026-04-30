# Event RSVP Manager — Design Document

---

## 1. Raw Feature Brief (Structured)

### What Is It?
A small web application for hosting events and collecting RSVPs from invitees. Users can create events, invite guests via email, and manage attendance through a live dashboard with capacity and waitlist management.

### Who Uses It?
- **Event Hosts**: Users who create events, invite guests, and monitor attendance
- **Invitees**: People invited to events who respond with attendance status (Yes/No/Maybe)

### Why It Exists
To provide a simple, centralized way to:
- Organize events without manual email tracking
- Collect attendance commitments with clear response options
- Manage event capacity and automatically handle overflow via waitlist
- Give hosts real-time visibility into attendance

### Core Capabilities
1. **Event Creation**: Host can create event with title, description, date/time, location, and optional max-capacity
2. **Guest Invitations**: Host invites people by email; each invitee gets a unique link
3. **RSVP Responses**: Invitees respond Yes / No / Maybe via unique link
4. **Attendance Dashboard**: Host sees live counts by response status and list of attendees
5. **Capacity Management**: When max-capacity is reached, new "Yes" responses go to waitlist
6. **Waitlist Promotion**: Waitlisted attendee automatically moves to confirmed when a confirmed attendee changes to "No"
7. **Event Management**: Host can cancel event or close it to further responses at any time
8. **Response Flexibility**: Invitees can change RSVP at any point **before** event start time
9. **Response Lock**: After event start time, all RSVPs are locked (immutable)

### Touched System Areas
- **User Management**: Host authentication and identification
- **Event Management**: Create, retrieve, update, cancel events
- **Invitation System**: Generate and send unique links
- **RSVP Tracking**: Store and manage response statuses
- **Capacity Logic**: Track capacity usage and manage waitlist
- **Notifications**: Email invitations and confirmations
- **Time-based Rules**: Enforce RSVP locks post-event start

### Unclear Points (To Clarify)
- Do invitees need user accounts, or respond anonymously via link?
- What happens when an event is cancelled? (Refund? Notification?)
- Can hosts edit event details after creation?
- How is the unique link generated and managed?
- What email service is used for sending invitations?
- Can one person be invited multiple times to the same event?
- Does the host see invitee identity in the final attendee list?

---

## 2. Tech Stack (Given)
- Backend: Java 17, Spring Boot 4, Spring MVC, Spring Data JPA
- Database: PostgreSQL (local, port 5432, schema: `hero`)
- Frontend: React 19, TypeScript, Vite

---

## 3. Problem Statement

**The Problem**
Event hosts lack a centralized, automated way to collect and track attendance commitments from invitees. Currently, they rely on manual emails, unclear responses, and spreadsheet tracking—resulting in:
- No real-time visibility into who is attending
- Unclear response counts and unclear response semantics (email chains are ambiguous)
- Manual, error-prone capacity management when event limits apply
- No automatic overflow handling; hosts must manually manage waitlists
- Inability to lock responses after the event starts, risking last-minute RSVPs for past events

**What Solves This Problem**
The application is considered successful if it enables:
1. Hosts can create an event with key details (title, date/time, location, capacity) in < 2 minutes
2. Hosts can invite multiple guests by email and receive confirmation of sent invitations
3. Each invitee receives a unique, traceable link and submits a clear response (Yes / No / Maybe)
4. Hosts see a **live, real-time dashboard** showing:
   - Count of responses by status (Yes / No / Maybe / Waitlisted)
   - Full list of attendees with their response status
5. When capacity is reached, new "Yes" responses automatically go to a waitlist (no manual action required)
6. When an attendee changes from "Yes" → "No", the first waitlisted person automatically promotes to "Yes" with notification
7. Invitees can change their RSVP up until the event start time; after that, responses are immutable
8. Hosts can cancel or close an event to further responses at any time
9. Hosts and invitees receive email notifications for key actions (invitations, confirmations, promotions)

---

## 4. Goals and Non-Goals

### Goals (In Scope)
- Enable event hosts to create private events and invite guests via email
- Provide invitees with a simple, single-click RSVP interface (Yes/No/Maybe)
- Display a real-time dashboard showing attendance counts and attendee list
- Automatically manage capacity and waitlist without manual intervention
- Allow invitees to change RSVP up until event start time
- Lock all RSVPs after event start time
- Send email notifications for invitations, confirmations, and waitlist promotions
- Allow hosts to cancel or close events to new responses
- Require host login and private event visibility

### Non-Goals (Out of Scope)
**Do NOT build:**
- Event discovery or public event listings (no "browse all events")
- Guest list publishing (invitees cannot see who else is invited unless explicitly shown in feature)
- Advanced guest roles or permissions (e.g., co-hosts, moderators, guest+1 management)
- Recurring or multi-date events
- Calendar integrations (Google Calendar, Outlook, iCal sync)
- Payment processing or ticketing
- Custom fields or questionnaires (dietary restrictions, t-shirt sizes, etc.)
- Advanced reporting or analytics beyond the live dashboard
- Mobile app or SMS notifications
- Video conferencing or meeting room integration
- Guest communication tools (in-app chat, comments)
- Venue/location database or map integration
- Bulk event import/export
- API access for third-party integrations
- Complex workflow approvals (e.g., manager approval before RSVP)

**Why these boundaries matter:** They keep the MVP focused on core RSVP tracking and capacity management. If capacity or invitee identification logic changes later, these non-goals can become future phases.

---

## 5. Context and Constraints

### Technical Constraints
- **Language & Framework**: Backend must be Java 17 with Spring Boot 4 (Spring MVC, Spring Data JPA)
- **Frontend**: React 19 with TypeScript and Vite build tooling
- **Database**: PostgreSQL (version not specified; assumes modern/standard)
- **Database Access**: Spring Data JPA (implies ORM-based data access with Hibernate)
- **Existing Schema**: Database `hero` with schema `hero` already created, ready for table definitions

**Design Impact**: 
- JPA/Hibernate drives entity modeling and relationship patterns
- Spring Boot conventions shape REST endpoints, dependency injection, and lifecycle
- PostgreSQL enables ACID transactions for capacity/waitlist atomicity

### Product Constraints
- **Authentication Required**: Hosts must log in; gates event creation and host access to dashboards
- **Event Privacy**: Events are private; only host + invited guests can view/access event details
- **Email Notifications**: Mandatory for invitations, confirmations, and waitlist promotions
- **RSVP Lock Post-Event**: RSVPs immutable after event start time (critical business rule)
- **Capacity & Waitlist**: Automatic overflow to waitlist when max-capacity reached; automatic promotion on cancellation

**Design Impact**:
- Requires authentication layer (session/JWT tokens)
- Requires email service integration
- Requires time-based business logic (comparing current time to event start time)
- Requires atomic transaction handling for capacity checks and waitlist promotion

### Operational Constraints
- **Deployment Target**: Local development environment (PostgreSQL on localhost:5432 implied)
- **Email Service**: Unspecified provider; needs configuration-driven approach
- **Data Persistence**: PostgreSQL only; no caching or external data stores mentioned
- **Scalability**: No performance targets or concurrency requirements stated

**Design Impact**:
- Use environment variables/config files for email credentials and database connection
- Plan for single-instance deployment initially; no distributed transaction handling needed yet

### Organizational Constraints
- **Scaffold Provided**: Full-stack Maven + npm/Vite project structure already exists
- **Learning Context**: This is an educational task (Xperience program); design clarity prioritized over arbitrary optimizations
- **Scope Boundary**: MVP focus (see Goals/Non-Goals) to support learning objectives

---

## 6. Confirmed Facts, Working Assumptions, and Open Questions

### Confirmed Facts (Constraints + Requirements Locked In)
- Tech stack: Java 17, Spring Boot 4, React 19, TypeScript, PostgreSQL
- Hosts MUST log in to create events and access dashboards
- Events are PRIVATE (only host + invited guests can see)
- Email notifications are REQUIRED (invitations, confirmations, waitlist promotions)
- RSVPs are LOCKED after event start time (immutable, non-changeable)
- Capacity overflow → Waitlist (automatic, not manual)
- Waitlist auto-promotion when a confirmed attendee changes to "No"
- Database `hero` with schema `hero` already exists
- Database is PostgreSQL, accessed via Spring Data JPA

### Working Assumptions (Unconfirmed, but Guiding Design)
- Invitees can RSVP via unique link WITHOUT creating a user account (guest RSVP)
- Each invitation generates ONE unique, single-use or long-lived token per invitee per event
- Hosts receive email confirmation when they invite a batch of people
- When an event is CANCELLED, all invitees are notified via email
- Hosts CAN edit non-critical event details (title, description) but NOT date/time/capacity after invitations are sent
- The dashboard is accessible only by the event host (no co-hosts, no guest access)
- Email service is configured externally (env vars, config file); Spring Boot sends via configured provider
- RSVP responses are stored by invitee email address (primary identifier for non-logged-in guests)
- The same person (email address) can be invited to multiple different events

### Open Questions (Materially Affect Design)
**Authentication & Guest Access**
- [ ] Can an invitee view their own past RSVPs if they create a user account later?
- [ ] Is there a "login via email link" option, or password required?
- [ ] Can guests resend themselves a forgotten RSVP link?

**Invitations & Links**
- [ ] How long are RSVP links valid? (Forever? Expire after event date?)
- [ ] Can the same email be invited twice to the same event? (Duplicate check?)
- [ ] What if an email bounces during invite send? (Retry? Mark as failed?)

**Event Editing**
- [ ] If host changes event date/time after invitations sent, are guests notified?
- [ ] Can host reduce capacity after people have RSVP'd "Yes"? (Could force demotions)
- [ ] Can host change max-capacity after event is created?

**Waitlist & Capacity**
- [ ] When promoting from waitlist, does the newly confirmed person get notified immediately or in batch?
- [ ] Can someone on waitlist delete their RSVP entirely, or only change to No/Maybe?
- [ ] If capacity increases (host raises max), do waitlisted people auto-promote?

**Event Cancellation & Data**
- [ ] When event is cancelled, what happens to RSVP records? (Keep for audit? Delete?)
- [ ] Can host "uncancel" a cancelled event?

**Email & Notifications**
- [ ] Who is the "from" email address in notifications? (noreply@? host's email?)
- [ ] Can invitees unsubscribe from emails for a specific event?
- [ ] Are reminders sent before event date? (Not in spec, but often expected)

---

---

## 7. Actors and Key Workflows

### Actors
- **Host (Authenticated User)**: Creates events, invites guests, views dashboard, manages event
- **Invitee (Guest / Unregistered User)**: Receives email with RSVP link, submits response, can change RSVP before event starts
- **Email Service**: Sends invitations, confirmations, and promotion notifications
- **System / Background Jobs**: Monitors time, locks RSVPs at event start, promotes waitlist, handles capacity

---

### User-Facing Workflows

| Workflow | Actor | Trigger | Key Checks | Output |
|----------|-------|---------|-----------|--------|
| Create Event | Host | Submit event form | Authenticated, date > now, inputs validated | Event created in DB, host is owner |
| Invite Guests | Host | Upload/enter emails | Valid format, duplicates rejected, host authorized | Invitations created, tokens generated, emails queued |
| Submit RSVP | Invitee | Click token link + select response | Token valid, time < event.date_time, capacity available | RSVP saved; if full → Waitlisted |
| Change RSVP | Invitee | Submit new response | Token valid, time < event.date_time, recalc capacity | RSVP updated, waitlist auto-promoted if applicable |
| View Dashboard | Host | Navigate to event | Host authorized (user_id == event.host_id) | Live response counts + attendee list |
| Cancel/Close Event | Host | Confirm action | Host authorized | Event marked cancelled/closed, invitees notified |

---

### System Logic (Capacity, Auth, Time Locking)

**WF7: Capacity & Waitlist Check (Inside RSVP Submit Transaction)**
- Count confirmed Yes responses
- If count < capacity: set status = Yes
- Else: set status = Waitlisted (record creation order)
- **🔴 Unresolved**: Concurrency control (pessimistic lock vs. optimistic vs. serializable isolation)

**WF8: Waitlist Auto-Promotion (Yes → No)**
- Decrement capacity, query oldest waitlisted (by created_at ASC)
- If exists: promote to Yes, queue promotion email
- **🔴 Unresolved**: Sync vs. async promotion timing

**WF9: Authorization Check (Every Protected Request)**
- Validate session/JWT, extract user_id
- Host endpoints: verify user_id == event.host_id
- RSVP endpoints: verify invitation.token exists

**WF10: RSVP Time Lock (Every RSVP Attempt)**
- Check: current_time >= event.date_time
- If true: reject with "event locked" error
- **🔴 Unresolved**: Single source of truth (flag vs. code-based check)

---

### Asynchronous Workflows

**Email Sending** (WF11–WF13): Invitations, confirmations, and waitlist promotions are queued asynchronously. Background worker renders templates, sends via email provider, updates `invitation.email_sent_at` on success. **🔴 Unresolved**: Idempotency strategy (prevent duplicate sends on job retry).

**RSVP Lock Scheduling** (WF14): Scheduled job runs at/after `event.date_time` to set `event.rsvps_locked = true`. Fallback: RSVP Service also validates time on every submit. **🔴 Unresolved**: Which is authoritative—the flag or the time check?

---

### Failure Cases (Error Handling)

| Error Case | Behavior | Control |
|-----------|----------|---------|
| Invalid/expired token (WF15) | Return error page + offer "request new link" | Validate token exists in Invitation table before serving form |
| Event is cancelled | Reject RSVP with "event cancelled" | Check event.status before accepting any RSVP |
| Email send failure (WF16) | Mark `invitation.email_failed = true`, log error, show host warning | Implement retry job + alerting for failures |
| Duplicate invitation (WF17) | Reject or upsert existing record (design TBD) | Enforce unique constraint `(event_id, invitee_email)` in DB |
| Capacity reduced after RSVPs (WF18) | Reject or require manual demotion (design TBD) | Validate `new_capacity >= confirmed_count` before allowing edit |
| **Race: Simultaneous Yes at capacity (WF19)** | **🔴 Risk of overbooking** | **Must choose concurrency strategy (see Section 9.4)** |

---

---

## 8. Invariants (Safety Guarantees That Must Always Hold)

### Business Invariants (Domain Rules)

**BI1: Capacity Constraint**
```
Invariant: confirmed_count(event) <= event.max_capacity (if max_capacity is set)
```
- **What violates it**: Two invitees submit "Yes" simultaneously when event is at capacity
- **Control needed**: 
  - Database-level CHECK constraint: `confirmed_rsvps <= max_capacity`
  - Pessimistic locking (SELECT FOR UPDATE) during capacity check
  - Alternative: optimistic locking with retry logic
  - Row-level serialization isolation level

**BI2: Waitlist Only at Capacity**
```
Invariant: If a response is WAITLISTED, then confirmed_count(event) >= max_capacity
```
- **What violates it**: Manually creating waitlist entries when capacity exists, or race condition
- **Control needed**:
  - Application logic: only set status=WAITLISTED if confirmed_count >= max_capacity
  - Database CHECK constraint if feasible
  - Unit tests verifying this on RSVP submit

**BI3: Unique Response per Invitee per Event**
```
Invariant: For each (event_id, invitee_email), at most ONE rsvp record exists
```
- **What violates it**: Duplicate invitations leading to multiple RSVP records per person
- **Control needed**:
  - Unique constraint on (event_id, invitee_email, status) or (event_id, invitee_email)
  - Application logic: upsert on RSVP submit, never insert if exists
  - Validation before accepting invite email list

**BI4: RSVP Locked Post-Event Start**
```
Invariant: If current_time >= event.date_time AND event.rsvps_locked=true, then RSVP.status is immutable
```
- **What violates it**: Invitee submitting changes after event start; code forgetting time check
- **Control needed**:
  - Time check on EVERY RSVP change endpoint
  - Database trigger or CHECK constraint on timestamps
  - Scheduled job to set event.rsvps_locked=true at event start time
  - **CURRENTLY: Likely protected only by application logic (hope)**

**BI5: Only Host Can Manage Event**
```
Invariant: Only user == event.host_id can cancel, close, invite, or edit event
```
- **What violates it**: Invitee or another user accessing host endpoints
- **Control needed**:
  - Authorization check on every event management endpoint
  - Row-level security if supported, or explicit checks in queries
  - Unit tests for authorization

**BI6: Host Identity on Event Creation**
```
Invariant: event.host_id == authenticated_user_id (at creation time)
```
- **What violates it**: Passing a forged host_id in request
- **Control needed**:
  - Always use authenticated user from session/JWT, never trust client input
  - Ignore any host_id in request payload

---

### Data Integrity Invariants

**DI1: Foreign Key Integrity**
```
Invariant: Every event_id in rsvp table references existing event record
Invariant: Every host_id in event table references existing user record
```
- **What violates it**: Deleting events without cascading RSVP cleanup
- **Control needed**:
  - Database foreign key constraints with ON DELETE CASCADE or ON DELETE RESTRICT
  - Never delete events in code; mark as cancelled instead

**DI2: Unique Invitation Tokens**
```
Invariant: Each invitation.token is globally unique and not reused
```
- **What violates it**: Token collision or token reuse across invitations
- **Control needed**:
  - Use cryptographically secure random generation (UUID or SecureRandom)
  - Unique constraint on invitation.token column
  - Never re-generate or reassign tokens

**DI3: Immutable Invitation Link**
```
Invariant: Once invitation.token is issued, it never changes (even if invitee re-invites)
```
- **What violates it**: Changing token on resend, or creating new record instead of updating
- **Control needed**:
  - On resend: update invitation record; do not create new record
  - Application logic: check invitee exists before creating new record

**DI4: Timestamp Consistency**
```
Invariant: created_at <= updated_at <= current_time (for all records)
Invariant: event.date_time is in the future (or past, but consistent)
```
- **What violates it**: System clock skew, timezone confusion, manual data edits
- **Control needed**:
  - Always use server time, never client time
  - Store timestamps in UTC
  - Use TIMESTAMP WITH TIME ZONE in PostgreSQL
  - Validation: updated_at >= created_at

---

### Authorization Invariants

**AI1: Only Authenticated Users Can Create Events**
```
Invariant: Every event.host_id references a valid, authenticated user
```
- **What violates it**: Unauthenticated requests creating events
- **Control needed**:
  - Spring Security @PreAuthorize("isAuthenticated()") on event creation endpoint
  - Check session/JWT on request; reject if missing

**AI2: Host-Only Dashboard Access**
```
Invariant: event.host_id == authenticated_user_id is required to view event dashboard
```
- **What violates it**: Invitee or other user accessing host dashboard
- **Control needed**:
  - Authorization check: `if (user.id != event.host_id) throw unauthorized`
  - Row-level security or query filtering

**AI3: Invitee Can Only RSVP to Events They're Invited To**
```
Invariant: RSVP submit requires (event_id, invitee_email) pair to exist in invitation table
```
- **What violates it**: Invitee guessing event IDs, or submitting RSVP without invitation
- **Control needed**:
  - Verify invitation record exists before accepting RSVP
  - Use unique token (not guessable event ID) in RSVP URLs
  - Check invitation.email matches submitter

**AI4: Only Host Can Send Invitations**
```
Invariant: Only event.host_id can invoke the "invite" endpoint
```
- **What violates it**: Another user adding guests to an event
- **Control needed**:
  - Authorization check on invite endpoint
  - Verify authenticated user == event.host_id

---

### Concurrency Invariants

**CI1: No Overbooking on Simultaneous RSVPs**
```
Invariant: confirmed_count <= max_capacity despite concurrent RSVP submissions
```
- **What violates it**: Two people submit Yes at same time, both see available slot, both get confirmed
- **Control needed**:
  - **Currently: Likely only protected by hope**
  - Pessimistic locking: SELECT count(*) FOR UPDATE NOWAIT
  - Serializable isolation level or explicit row locks
  - OR: Accept the risk and fix manually via batch job
  - Test with concurrent submission simulation

**CI2: Waitlist Promotion Happens Exactly Once**
```
Invariant: When a confirmed attendee changes to No, exactly ONE waitlisted person promotes (not zero, not two)
```
- **What violates it**: 
  - Two people simultaneously change to No, both try to promote the same waitlisted person
  - Code bug: forgetting to update the promoted record
  - Database transaction not isolated
- **Control needed**:
  - Serializable isolation or pessimistic SELECT FOR UPDATE
  - Atomically: update one record (changed to No), update one waitlist record (promote to Yes)
  - Test: change two people to No simultaneously

**CI3: No Double Email Sends**
```
Invariant: Exactly one promotion email per promoted invitee (not zero, not two)
```
- **What violates it**: Email job runs twice, network retry sends twice, no idempotency check
- **Control needed**:
  - Idempotency keys on email job (store sent timestamp)
  - Idempotent email service (dedup by content hash or message ID)
  - Check email_sent_at before queuing
  - **Currently: Likely only protected by hope**

**CI4: No Duplicate Email Invitations**
```
Invariant: Host sends one batch invite with 100 emails; exactly 100 invitation records created (not 200, not 50)
```
- **What violates it**: Retry logic sending batch twice, code uploading same CSV twice
- **Control needed**:
  - Unique constraint on (event_id, invitee_email)
  - Check for duplicates in uploaded list before processing
  - Idempotent invite job with dedup on email

---

### Tenant Isolation Invariants (If Multi-Tenant; Currently Single-Tenant)

**TI1: Host Cannot See Other Hosts' Events**
```
Invariant: User A's event list only contains events where user_id == host_id
```
- **What violates it**: SQL injection, authorization bypass
- **Control needed**:
  - Always filter queries: `WHERE event.host_id = current_user.id`
  - Never trust user_id from request; use authenticated session

**TI2: RSVP Data is Private**
```
Invariant: RSVP list for event X is only visible to event X's host
```
- **What violates it**: Invitee seeing other invitees' responses; another user accessing event dashboards
- **Control needed**:
  - Authorization check before returning RSVP list
  - Row-level security in queries

---

### Which Invariants Are Currently Protected Only by Hope?

🚨 **High Risk (No Strong Control Yet)**:

1. **BI4 (RSVP Lock Post-Event)**: Time-based check likely only in code, not enforced at DB level. If code is forgotten, RSVPs could change after event starts.
   - *Fix*: Add DB trigger or CHECK constraint; add tests; audit logs

2. **CI1 (No Overbooking on Concurrent RSVPs)**: No pessimistic locking or serialization level set. Two simultaneous submits could both see capacity available.
   - *Fix*: Add SELECT FOR UPDATE during capacity check, or switch to SERIALIZABLE isolation

3. **CI3 (No Double Email Sends)**: Email job probably has no idempotency check. Network retries could send promotion emails twice.
   - *Fix*: Store email_sent_at, check before queuing; use idempotency keys

4. **CI4 (No Duplicate Invitations on Retry)**: If invite batch is retried, could create duplicate invitation records.
   - *Fix*: Add unique constraint (event_id, invitee_email); upsert logic

⚠️ **Medium Risk (Partially Protected)**:
- **BI1 (Capacity Constraint)**: If you use optimistic locking, it's robust; if only application logic, it's fragile.
- **BI3 (Unique Response per Invitee)**: Protected by unique constraint, but only if constraint is in place.

✅ **Low Risk (Well Protected)**:
- **AI1-AI4 (Authorization)**: Spring Security framework helps, but requires checks on every endpoint.
- **DI1-DI4 (Data Integrity)**: Foreign keys and unique constraints are database-enforced.

---

---

## 9. High-Level Architecture (No-Code)

### Core Principle
Separate concerns by responsibility: event hosting, RSVP collection, capacity management, notifications, and authentication each own their domain, with clear boundaries and data flow.

---

### Components & Responsibilities

**1. Authentication & Authorization Layer**
- **Responsibility**: User login/logout, session/JWT token management, permission checks
- **Owns**: User table (credentials), session/JWT handling, authorization rules
- **Triggered by**: Login request, every protected endpoint request
- **Output**: Authenticated user context (user_id) for downstream services

---

**2. Event Service**
- **Responsibility**: Event lifecycle (create, view, edit, cancel, close to responses)
- **Owns**: Event table (title, date/time, location, max_capacity, status), business rules (host-only modify), state transitions
- **Triggered by**: Host creates/cancels event
- **Output**: Event record; publishes event cancellation event
- **🔴 Unresolved**: Can host edit event after invitations sent?

---

**3. Invitation Service**
- **Responsibility**: Generate unique RSVP links, create/resend invitation records
- **Owns**: Invitation table (event_id, invitee_email, unique_token, status), token generation (secure, unique), email batch validation
- **Triggered by**: Host uploads email list and clicks "Invite"
- **Output**: Invitation records; publishes "invitations_created" event
- **Boundary**: Validates email format, prevents duplicates, enforces unique (event_id, invitee_email)

---

**4. RSVP Service** ⚠️ **Carries Blocking Decisions** ⚠️
- **Responsibility**: Accept RSVP responses, enforce capacity/waitlist, manage transitions, trigger auto-promotions
- **Owns**: RSVP table (event_id, invitee_email, status, created_at, updated_at, position_in_waitlist), capacity logic, waitlist promotion
- **Triggered by**: Invitee submits/changes RSVP
- **Output**: Updated RSVP record; publishes "rsvp_submitted" and "waitlist_promoted" events
- **🔴 Blocking Decisions** (see decision table above): Concurrency control, promotion timing, state transitions, email integration

---

**5. Time / Scheduler Service**
- **Responsibility**: Lock RSVPs at event start time
- **Owns**: Scheduled job that sets `event.rsvps_locked = true` at/after event.date_time
- **Triggered by**: Clock reaching event start time (cron task)
- **Output**: Sets rsvps_locked flag; publishes "event_locked" event
- **Note**: Fallback: RSVP Service also validates time on every submit (defense-in-depth)

---

**6. Dashboard Query Service**
- **Responsibility**: Aggregate RSVP counts and attendee list for host dashboard
- **Owns**: Attendance count queries (Yes/No/Maybe/Waitlisted), attendee list queries (ordered, filtered)
- **Triggered by**: Host views event dashboard
- **Output**: Live response counts + attendee list
- **Boundary**: Host-only access (verified by Auth layer)

---

**7. Email/Notification Service** (Async, Queued)
- **Responsibility**: Send emails asynchronously (invitations, confirmations, promotions)
- **Owns**: Email templates, email provider integration, retry logic, delivery status tracking
- **Triggered by**: "invitations_created", "rsvp_submitted", "waitlist_promoted" events
- **Output**: Email sent, delivery status recorded in database
- **🔴 Unresolved**: Idempotency strategy (prevent duplicate sends on retry)

---

**8. Persistence Layer** (Spring Data JPA)
- **Responsibility**: ORM mapping, query execution, enforce database constraints
- **Owns**: Entity definitions (User, Event, Invitation, RSVP), repositories, transaction management, constraints

---

**9. REST API Layer** (Spring MVC Controllers)
- **Responsibility**: HTTP handling, input validation, routing
- **Owns**: Controller methods, request mapping, validation, HTTP responses

---

**10. Frontend Layer** (React, TypeScript, Vite)
- **Responsibility**: UI rendering, user interactions, form handling
- **Owns**: Component structure, user flows, client-side validation

---

### Data Flow: Event Creation to RSVP

```
1. Host logs in
   → Auth Service validates credentials, creates session
   
2. Host creates event
   → API Layer validates form → Event Service stores event → Event owns record
   
3. Host uploads invitee emails
   → API Layer routes to Invitation Service
   → Invitation Service generates tokens, stores invitation records
   → Publishes "invitations_created" event
   
4. Email Service picks up event
   → Renders email template, sends invitation link to each invitee
   → Records email_sent_at timestamp
   
5. Invitee clicks link
   → Frontend renders RSVP form (read-only event details)
   → No authentication required; link validates invitee
   
6. Invitee submits Yes
   → API validates invitation token exists
   → RSVP Service checks: current_time < event.date_time (via Time Service)
   → RSVP Service checks: confirmed_count < capacity
   → If yes: set status=Yes, increment count
   → If no: set status=Waitlisted
   → Publishes "rsvp_submitted" event
   
7. Email Service picks up event
   → Sends confirmation email to invitee
   
8. Host views dashboard
   → Auth layer verifies user == event.host_id
   → Dashboard Query Service aggregates counts from RSVP table
   → Returns live attendance snapshot
```

---

### Ownership Boundaries (Clear Data Isolation)

| Component | Owns Table(s) | Queries | Mutates | Does NOT Touch |
|-----------|---------------|---------|---------|-----------------|
| Auth | User | Read user by email | Create user, update password | Event, RSVP |
| Event | Event | Read by id/host | Create, update, cancel | Invitation, RSVP |
| Invitation | Invitation | Read by token, event_id/email | Create, resend | Event, RSVP |
| RSVP | RSVP | Read by event/email, count | Create, update status | Event, User |
| Time | (none) | Read Event.date_time | Write Event.rsvps_locked | Invitation, RSVP |
| Dashboard | (none) | Aggregate RSVP + Event | None | Any mutation |
| Email | (none) | Read Invitation, RSVP | Update email_sent_at | Event state |

---

### Key Design Choices Made (Not Yet Confirmed)

1. **Email is Async & Queued**: Do not block user on email send; use background jobs or message queue
2. **Invitees RSVP Without Login**: Use unique token instead of user account; email is identifier
3. **Host-Only Dashboard**: No invitee visibility into other attendees
4. **Time Lock in Two Places**: Scheduler sets flag at event start; RSVP Service also checks on every submit (defense in depth)
5. **Capacity Check Inside Transaction**: RSVP Service must lock/serialize capacity check to prevent overbooking

---

### 🚨 Blocking Decisions in RSVP Service

Before implementation starts, these four design choices must be made:

| Decision | Options | Tradeoffs | Unresolved |
|----------|---------|-----------|-----------|
| **Concurrency Control** | Pessimistic lock (SELECT FOR UPDATE) vs. Serializable isolation vs. Optimistic retry | Lock: safe, slower; Serializable: clean but may impact perf; Optimistic: complex but lighter load | Which matches constraints? |
| **Waitlist Promotion** | Sync (block until email queued) vs. Async (queue and return) | Sync: simple, blocking; Async: faster but harder to guarantee exactly-once | How to guarantee no double-promotes? |
| **Email Delivery** | Guaranteed (transactional outbox) vs. Best-effort (just queue and hope) | Guaranteed: complex, reliable; Best-effort: simple, risky | Retry strategy + failure alerting? |
| **State Transitions** | Explicit state machine vs. Implicit rules (enforce in code) | Explicit: clear, hard to change; Implicit: flexible, easy to miss invalid transitions | Which transitions are legal? |

---

---

## 10. Data Ownership and State Model

### Entity: User
| Aspect | Value |
|--------|-------|
| **Source of Truth** | User table (PostgreSQL) |
| **Owner (Mutates)** | Auth Service only (registration, password reset) |
| **Readers** | Auth Service (login), Event Service (host lookup), Authorization layer (on every request) |
| **Derived State** | None |
| **Consistency Risk** | LOW: Single owner, straightforward reads |

---

### Entity: Event
| Aspect | Value |
|--------|-------|
| **Source of Truth** | Event table (PostgreSQL) |
| **Owner (Mutates)** | Event Service only (create, update, cancel) |
| **Readers** | Event Service, Dashboard Query, RSVP Service (for date/time check), Email Service (for context) |
| **Derived State** | rsvps_locked flag (see separate section below) |
| **Consistency Risk** | **MEDIUM**: Event data (date/time, capacity) is read by RSVP Service. If date_time is stale in cache, RSVP locking could fail. |

---

### Entity: Invitation
| Aspect | Value |
|--------|-------|
| **Source of Truth** | Invitation table (PostgreSQL) |
| **Owner (Mutates)** | Invitation Service only (create, resend) |
| **Readers** | RSVP Service (validate invitee exists), Email Service (render template) |
| **Derived State** | email_sent_at (performance flag, not business logic) |
| **Consistency Risk** | **MEDIUM**: Resend operation could create duplicate record instead of updating. Must use upsert/update-not-insert logic. |

---

### Entity: RSVP
| Aspect | Value |
|--------|-------|
| **Source of Truth** | RSVP table (PostgreSQL) |
| **Owner (Mutates)** | RSVP Service only (create, update status) |
| **Readers** | RSVP Service (count for capacity check), Dashboard Query (aggregation) |
| **Derived State** | None; status is explicit |
| **Consistency Risk** | **HIGH**: RSVP state transitions are implicit. No explicit state machine enforces valid transitions (Yes→No→Maybe vs. Waitlisted→Yes only via promotion). Code must enforce rules correctly. |

---

### Derived Concept: Confirmed Attendee Count
| Aspect | Value |
|--------|-------|
| **Source of Truth** | Aggregate of RSVP table: `SELECT COUNT(*) WHERE event_id = X AND status = 'Yes'` |
| **Owner (Reads)** | RSVP Service (to decide if new Yes → Waitlist), Dashboard (to display) |
| **Owner (Mutates)** | No direct mutation; derived from RSVP records |
| **Derived State** | **YES**: Computed on-demand, never stored |
| **Consistency Risk** | **CRITICAL**: Stale-read race condition. Two invitees submit Yes simultaneously, both query count and see X < capacity, both get confirmed. Capacity overbooked by 1. |
| **Why It Matters** | This is the **single biggest concurrency risk** in the system. |
| **Mitigation Required** | Pessimistic lock (SELECT COUNT FOR UPDATE) or Serializable isolation, OR optimistic retry with conflict detection. Must be decided in RSVP Service. |

---

### Derived Concept: Waitlist Position / Order
| Aspect | Value |
|--------|-------|
| **Source of Truth** | Implicit: Ordering of RSVP records with status='Waitlisted', ordered by `created_at ASC` |
| **Owner (Reads)** | RSVP Service (find "first waitlisted" on promotion) |
| **Owner (Mutates)** | No explicit position column; order is implicit in creation time |
| **Derived State** | **YES**: Position is computed on each query (no stored position column) |
| **Consistency Risk** | **MEDIUM**: If "first waitlisted" is always defined as oldest created_at, order is stable. But no explicit enforcement. If position column were added and stored, it would require expensive reordering on each update. |
| **Design Choice Made** | Implicit ordering by created_at (simpler, no update overhead). |

---

### Stateful Concept: Event RSVP Lock Status
| Aspect | Value |
|--------|-------|
| **Source of Truth (Location 1)** | Event.rsvps_locked (boolean flag in Event table) |
| **Source of Truth (Location 2)** | Current time vs. Event.date_time (implicit check in RSVP Service code) |
| **Owner (Mutates Flag)** | Time Service / Scheduler (scheduled job at event start) |
| **Owner (Mutates Time Check)** | RSVP Service (reads Event.date_time on every RSVP submit) |
| **Readers** | RSVP Service (checks both before allowing status change) |
| **Consistency Risk** | **CRITICAL & BLURRED OWNERSHIP**: Two sources of truth instead of one. |
| **Scenario A: Time Service Fails** | Job fails to set rsvps_locked=true. But RSVP Service still checks current_time, so RSVPs still lock. System works, but flag is stale. |
| **Scenario B: Code Forgets Time Check** | RSVP Service is refactored, time check is removed. Only rsvps_locked flag is checked. If flag is never set (job never runs), RSVPs are NOT locked. **Silent failure.** |
| **Scenario C: Time Check Passes, Flag Not Set** | Event starts, RSVP Service checks time correctly, but flag is never set. Flag is inconsistent with reality. Confusing for debugging. |
| **Problem** | **Ownership is blurred**: Is Time Service authoritative, or is the code-based time check? Should there be one source of truth? |
| **Mitigation Option 1** | Keep both as defense-in-depth: job sets flag, code checks time. But make explicit in code: `if (rsvps_locked OR time >= event.date_time)` and document why. |
| **Mitigation Option 2** | Single source of truth: only flag. Job MUST run; if it fails, system is broken. Add monitoring/alerting for job failure. |
| **Mitigation Option 3** | Single source of truth: only code-based time check. Remove flag entirely; derive it from Event.date_time. Simpler, no job needed. |

---

### Stateful Concept: Email Send Status
| Aspect | Value |
|--------|-------|
| **Source of Truth** | Invitation.email_sent_at (timestamp) + Invitation.email_failed (boolean) |
| **Owner (Mutates)** | Email Service (sets email_sent_at after successful send, or email_failed if failure) |
| **Readers** | Host (sees in invitation list), Email Service (checks before resending) |
| **Derived State** | No; explicitly stored |
| **Consistency Risk** | **MEDIUM**: What if email sends successfully, but DB write of email_sent_at fails? Status is "not sent" but email was actually sent. Duplicate emails on retry. |
| **Mitigation** | Idempotency key: Email Service generates unique key per invitation and sends to email provider. Provider dedupes on same key. OR: Check email_sent_at before every send. |

---

### Stateful Concept: Host Identity on Event
| Aspect | Value |
|--------|-------|
| **Source of Truth** | Event.host_id (foreign key to User table) |
| **Owner (Mutates)** | Event Service (set at creation time, never changed) |
| **Readers** | Authorization layer (verify event.host_id == authenticated_user_id) |
| **Immutability** | Yes; host cannot be changed after event creation |
| **Consistency Risk** | **LOW**: Immutable once set. But if Event is deleted and cascade is broken, orphaned RSVP records could reference deleted event. Foreign key constraint should prevent this. |

---

### High-Level Mutation Authority Map

| Entity | Who Can Create | Who Can Update | Who Can Delete | Risk |
|--------|---|---|---|---|
| User | Auth Service | Auth Service | None (no delete) | LOW |
| Event | Event Service | Event Service | Event Service (soft-cancel) | LOW |
| Invitation | Invitation Service | Invitation Service | Never | LOW |
| RSVP | RSVP Service | RSVP Service | Never (soft-delete via status) | MEDIUM (no explicit state machine) |
| Email records | Email Service | Email Service | Never | MEDIUM (send status sync) |
| Event.rsvps_locked | Time Service | Time Service | RSVP Service (implicitly via time check) | **CRITICAL (blurred)** |

---

### 🚨 Ownership Blurs & Truth Distortions

**#1: Capacity Count (Derived, Not Stored)**
- **What is the truth?** Count query at the moment of read
- **When is it wrong?** At the exact millisecond between two concurrent submissions. First reads count=4, second reads count=4, both think capacity=5 is available. Both submit Yes. Count becomes 6; capacity violated.
- **Who owns it?** RSVP Service owns the check, but database isolation level owns the guarantee.
- **Mitigation**: Explicit concurrency control (pessimistic lock, serializable isolation).

**#2: Event RSVP Lock Status (Two Sources of Truth)**
- **What are the sources?** 
  1. Event.rsvps_locked flag (owned by Time Service)
  2. Code-based time check (owned by RSVP Service)
- **When are they inconsistent?** When flag is not set but time check is correct (job failed), or vice versa (code doesn't check time).
- **Who owns it?** Currently: no one. Both services assume the other is responsible.
- **Mitigation**: Choose one source as authoritative, and document why the other exists (if at all). Use defense-in-depth intentionally, not by accident.

**#3: Waitlist Position (Implicit, Not Stored)**
- **What is the truth?** The order of RSVP records by created_at.
- **When is it wrong?** If someone assumes explicit position column exists and reads stale position; or if ordering query is wrong.
- **Who owns it?** RSVP Service assumes created_at order is correct. Called nowhere else.
- **Mitigation**: Document this assumption in code. Consider storing explicit position if querying becomes expensive.

**#4: Email Send Status (Sync Risk Between Services)**
- **What is the truth?** Invitation.email_sent_at timestamp.
- **When is it wrong?** Email sends, DB write fails. Status is "unsent" but email was sent. Resend sends duplicate.
- **Who owns it?** Email Service writes, Invitation Service reads. No coordination.
- **Mitigation**: Idempotency key on email side, or check before resend.

**#5: RSVP State Transitions (Implicit, Not Enforced)**
- **What is the truth?** RSVP.status field.
- **When is it wrong?** State transitions are not enforced. Code allows Waitlisted→Yes directly (should only be via promotion). Code allows Yes→Waitlisted (should not happen).
- **Who owns it?** RSVP Service owns the logic, but no explicit state machine.
- **Mitigation**: Define and enforce valid transitions in code (FSM pattern) or database trigger.

---

### Summary: Where Truth Lives, Where Ownership Is Blurred

| Concept | Truth Location | Ownership | Blurred? |
|---------|---|---|---|
| User credentials | User table | Auth Service | No |
| Event metadata | Event table | Event Service | No |
| Invitations | Invitation table | Invitation Service | No |
| RSVP status | RSVP table | RSVP Service | No |
| **Capacity count** | RSVP sum (derived) | RSVP Service + Database isolation | **YES** (concurrency) |
| **Event lock status** | Event flag + time check (dual) | Time Service + RSVP Service | **YES** (dual authority) |
| Waitlist position | RSVP order by created_at (implicit) | RSVP Service | Somewhat (implicit vs explicit) |
| Email status | Invitation.email_sent_at | Email Service + Invitation Service | **YES** (sync between services) |
| RSVP transitions | Code logic (implicit) | RSVP Service | **YES** (no FSM) |

---

---

## 11. Trust Boundaries and Security Notes

### Trust Entry Points (Where Untrusted Input Enters)

**TP1: User Login**
- **Trust source**: Username + password from client
- **Trust decision**: Auth Service validates credentials and creates session/JWT
- **Risk**: Weak passwords, credential reuse. But password is hashed; we don't over-trust.
- **Control**: Hash algorithm (bcrypt, Argon2), session timeout, optional 2FA (out of scope)
- **Status**: ✅ Low risk; standard auth pattern

**TP2: RSVP Link Click**
- **Trust source**: Unique token in URL from invitee email
- **Trust decision**: Any URL holder can RSVP using that link
- **Risk**: Link is forwarded to someone else (intentionally or by accident). That person can now RSVP as the invitee. System won't know it wasn't the intended person.
- **Control**: No identity verification; link-based auth only
- **Status**: ⚠️ **Over-trust here**: No confirmation token belongs to the actual invitee. Anyone with link can respond.

**TP3: Event Creation Form**
- **Trust source**: Event details from host (title, description, date, location, capacity)
- **Trust decision**: Believe the host entered valid data
- **Risk**: Host enters invalid date (past), malicious title, capacity=0, negative capacity
- **Control**: Input validation (date > now, capacity >= 0 or NULL), length limits on strings
- **Status**: ⚠️ **Over-trust here**: If validation is weak or host is hostile, could create confusion

**TP4: Invitation Email List Upload**
- **Trust source**: CSV/list of email addresses from host
- **Trust decision**: Believe these are valid emails to invite
- **Risk**: Host invites "ceo@company.com" by mistake, sends wrong person RSVP link. Or invites fake emails to inflate headcount.
- **Control**: Email validation (format), dedup in batch, warn on large imports
- **Status**: ⚠️ **Over-trust here**: No verification that email belongs to intended person. System sends invitation to whatever email is provided.

**TP5: Email Service Output**
- **Trust source**: Email service sends invitation/confirmation emails
- **Trust decision**: Believe emails arrive and are not tampered with
- **Risk**: Email service is down, emails are lost, or SMTP is insecure and emails are intercepted
- **Control**: Use TLS/STARTTLS for SMTP, trusted email provider (SendGrid/AWS SES), test email delivery, retry logic
- **Status**: ⚠️ **Over-trust here**: No confirmation emails were received. "Sent" ≠ "delivered". Could mark as invited but invitee never sees link.

---

### Authorization Enforcement Points (Where Privilege Must Be Verified)

**AEP1: Event Creation**
```
Requirement: Only authenticated users can create events
Control: Check authenticated user exists (session/JWT)
Implemented: Yes (Spring Security @PreAuthorize or manual check)
Risk: LOW - straightforward authentication check
```

**AEP2: Event Dashboard Access**
```
Requirement: Only event.host_id can view dashboard/attendance list
Control: Verify request.user_id == event.host_id
Implemented: Yes (authorization check in controller/service)
Risk: MEDIUM if check is forgotten in some endpoint
Scope: Single host per event; multi-host co-hosts are out of scope
```

**AEP3: Event Management (Invite, Cancel, Close)**
```
Requirement: Only event.host_id can send invitations or manage event
Control: Verify request.user_id == event.host_id
Implemented: Yes (assumed)
Risk: MEDIUM - must be checked on EVERY endpoint (invite, cancel, close)
Missing check: Could a host do a POST to /events/{OTHER_EVENT}/invite and bypass this?
```

**AEP4: RSVP Submission**
```
Requirement: RSVP can only be submitted if invitation exists for (event_id, email)
Control: Verify Invitation record exists for provided token
Implemented: Yes (required per workflows)
Risk: LOW - no one can RSVP without valid token
But: Anyone with the token can RSVP (see TP2 over-trust)
```

**AEP5: Dashboard Attendance List**
```
Requirement: Invitee cannot see other invitees' responses
Control: Only host can query RSVP list
Implemented: Assumed in Dashboard Query Service
Risk: MEDIUM - ensure query filters by event and includes authorization
Scenario: Could invitee guess /api/events/{id}/attendees and get attendee list?
```

---

### Sensitive Data and Exposure Points

**SD1: Event Details (Title, Description, Date, Location, Capacity)**
- **Classification**: Sensitive (private event, only for host + invitees)
- **Access**: Host sees full details. Invitees see details at RSVP time (not before, not modifiable).
- **Risk**: Exposed to anyone with event ID if event ID is guessable (sequential)
- **Control**: Use UUIDs for event ID (not sequential). Only serve details to authorized host or valid invitee.
- **Scope**: Single-tenant (host's own events only)

**SD2: Attendee List (Email + Response Status)**
- **Classification**: Highly sensitive (who is attending? who might not come?)
- **Access**: Only host can see
- **Risk**: Exposed to anyone if dashboard endpoint has no authorization check
- **Control**: Authorization check on dashboard endpoint; row-level security at DB
- **Scope**: Single-tenant (host's own event attendees only)

**SD3: Invitee Email Addresses**
- **Classification**: Personal data (PII) under GDPR/privacy laws
- **Access**: Host sees when inviting. Email Service sees to send emails. Invitees see their own email in confirmation.
- **Risk**: Exposed if invitation list is leaked or database is breached
- **Control**: Encrypt email in invitation table if required by privacy law. Audit who downloads invitee lists. Comply with data retention policies.
- **Scope**: Each invitee's email is private to that event only

**SD4: Email Addresses in Sent Messages**
- **Classification**: PII in transit
- **Access**: Email service sees all invitee emails. Email provider sees sender + recipients.
- **Risk**: Email is unencrypted in transit (TLS helps but doesn't guarantee end-to-end encryption)
- **Control**: Use TLS for SMTP. Consider E2E encryption if high-security event (out of scope for MVP).
- **Scope**: Transient; not stored beyond event

**SD5: User Passwords**
- **Classification**: Extremely sensitive (authentication credential)
- **Access**: Auth Service only (never logged, never sent in email, never displayed)
- **Risk**: Exposed if hashed password is cracked, or database is breached
- **Control**: Use strong hash (bcrypt, Argon2, NOT MD5/SHA1). Salted hash. Password reset via secure link. No password recovery (user can reset only).
- **Scope**: Not event-specific; user-wide

---

### Privileged Operations (Authority Required)

**PO1: Event Creation**
- **Authority**: Authenticated user only
- **Over-trust risk**: No rate limiting. Authenticated user could create 10,000 events. No quotas.
- **Mitigation**: Add rate limit or quota per user (out of scope, but should be noted)

**PO2: Sending Invitations**
- **Authority**: Event host only
- **Over-trust risk**: Host is trusted to invite legitimate emails. No validation that emails are real people.
- **Mitigation**: Email validation (format), optional: SMTP verification (risky, could mark valid emails as invalid)

**PO3: Viewing Attendance Dashboard**
- **Authority**: Event host only
- **Over-trust risk**: No audit log of who viewed the dashboard or when. Host could be fishing for personal data.
- **Mitigation**: Audit log (out of scope for MVP, but should be noted)

**PO4: Cancelling Event**
- **Authority**: Event host only
- **Over-trust risk**: Immediate cancellation with no confirmation. Could be accidents. No soft-delete period.
- **Mitigation**: Require confirmation, or implement soft-delete with grace period before cleanup

**PO5: Changing RSVP**
- **Authority**: Invitee with valid token only
- **Over-trust risk**: Anyone with link can change. No rate limiting on changes.
- **Mitigation**: Rate limit RSVP changes per token (e.g., max 10 changes per invitee). Log all changes.

---

### Tenant Scope (Single-Tenant, But Must Be Enforced)

**TS1: Host Sees Only Own Events**
```
Query: SELECT * FROM event WHERE host_id = current_user_id
Risk: SQL injection or forgot WHERE clause → all events leaked
Control: Use parameterized queries (JPA does this). Review all event queries.
```

**TS2: Host Sees Only Own Event's Attendees**
```
Query: SELECT * FROM rsvp WHERE event_id = ? AND event.host_id = current_user_id
Risk: Invitee could guess event ID and query /api/events/{ID}/attendees
Control: Verify author on every request. Use event.host_id in WHERE clause.
```

**TS3: Invitee Sees Only Invited Events**
```
Rule: Invitee can only RSVP to events they are invited to
Risk: Invitee could guess event ID and submit RSVP without invitation
Control: Verify Invitation record exists before accepting RSVP
```

---

### 🚨 Places Where Design Over-Trusts

**OT1: RSVP Link is Passwordless** (HIGH IMPACT)
```
Current: Anyone with RSVP link can submit response
Risk: Link gets forwarded or leaked. Attacker responds as someone else.
Assumption: Link is secret and unguessable. But long links are annoying to share.
Over-trust: System assumes link holder is the intended invitee
Mitigation Options:
  a) Require invitee to enter their email + name to verify identity (adds friction)
  b) Send link via SMS instead of email (requires phone number)
  c) Use one-time codes + email verification (more secure but complex)
  d) Accept the risk for MVP (simplest, but document assumption)
```

**OT2: Email Address is Invitee ID** (MEDIUM IMPACT)
```
Current: System identifies invitee by email alone
Risk: If email is mistyped, wrong person gets invited. If email is shared (family/team), multiple people could RSVP.
Over-trust: Host is trusted to provide correct email
Mitigation: Email domain validation (if org-specific event), webhook to verify email ownership
```

**OT3: Host-Provided Event Date is Trusted** (MEDIUM IMPACT)
```
Current: Host can enter any date/time, including past dates
Risk: Host could set event to past date to bypass RSVP locks (would allow unlimited changes)
Over-trust: Host is trusted to enter valid, future date
Mitigation: Validate date > now at creation time. Prohibit editing date after invitations sent.
```

**OT4: No Audit Log of Actions** (MEDIUM IMPACT)
```
Current: No record of who created event, who invited whom, when host cancelled, when dashboard was viewed
Risk: Cannot investigate disputes ("I didn't RSVP Yes!"), cannot recover deleted data, cannot audit access
Over-trust: System trusts we won't need to audit
Mitigation: Add audit log table (event_id, user_id, action, timestamp, details). De-scope for MVP, but flag as future requirement.
```

**OT5: Capacity Logic is Code-Based, Not DB-Enforced** (MEDIUM IMPACT)
```
Current: Capacity check happens in RSVP Service code, not in database
Risk: Code has bug, or concurrent access race condition. Overbooking happens.
Over-trust: System trusts code is always correct
Mitigation: Add database CHECK constraint to enforce confirmed_count <= max_capacity
```

**OT6: Email Service is Trusted to Send Without Failure** (MEDIUM IMPACT)
```
Current: Email send queued; if job fails quietly, invitee never gets notification
Risk: Invitee doesn't receive RSVP link. Thinks they weren't invited.
Over-trust: System trusts email job always succeeds or retries
Mitigation: Email job must have retry logic, dead-letter queue, and alerting for failures
```

**OT7: No Invitation Token Expiry Confirmed** (MEDIUM IMPACT)
```
Current: Unknown if RSVP links expire
Risk: If links are permanent, an invitee can RSVP a year after event ended
Over-trust: System trusts invitations never expire (or assumes they do via event lock)
Mitigation: Confirm token expiry policy. Either: (a) tokens expire after event ends, or (b) event lock prevents RSVP anyway
```

**OT8: No Rate Limiting on RSVP Submissions** (LOW IMPACT)
```
Current: Anyone with valid token can submit RSVP changes unlimited times
Risk: Denial of service: spam RSVP changes to log database, or test for token brute-force
Over-trust: System trusts invitees won't abuse endpoint
Mitigation: Rate limit per token (e.g., max 100 changes per invitee). Log suspicious patterns.
```

**OT9: Event ID Could Be Guessable** (LOW IMPACT, IF SEQUENTIAL)
```
Current: Event ID strategy not specified (UUID assumed, but not confirmed)
Risk: If event ID is sequential, attacker could guess event IDs and try accessing/RSVP'ing
Over-trust: System trusts event ID is non-guessable
Mitigation: Use UUID for event ID, not sequential integer
```

**OT10: No Invitee Can Revoke Invitation Link** (LOW IMPACT)
```
Current: Once invitation sent, invitee has RSVP link forever (or until event ends)
Risk: If link is accidentally shared, invitee cannot ask host to invalidate it
Over-trust: System trusts link won't be misused
Mitigation: Add "stop accepting RSVPs from this link" option. Or host can remove individual invitee and re-invite.
```

---

### Summary: Risk Heat Map

| Risk | Component | Severity | Mitigation |
|------|-----------|----------|-----------|
| Overbooking on concurrent RSVPs | RSVP Service | CRITICAL | Pessimistic lock or Serializable isolation |
| RSVP link can be forwarded | Invitation Service | HIGH | Accept for MVP; document assumption |
| Capacity check only in code | RSVP Service | HIGH | Add DB CHECK constraint |
| Email send failures not retried | Email Service | HIGH | Implement retry + alerting |
| Dual source of truth for lock | Event Service + Time Service | HIGH | Pick one authoritative source |
| Event ID could be guessable | Event Service | MEDIUM | Use UUID, not sequential |
| No audit log | All services | MEDIUM | Out of scope for MVP; flag for future |
| Host can set past event date | Event Service | MEDIUM | Validate date > now |
| Email address mistyped | Invitation Service | MEDIUM | Validate format; consider verification |
| No rate limiting | RSVP Service | LOW | Add rate limit per token |

---

---

## 12. Concurrency and Correctness Notes

### Vulnerable Areas & What Can Go Wrong

---

### VA1: Duplicate RSVP Submission (Same Invitee Submits Twice)

**What Can Happen**
```
1. Invitee clicks submit for response "Yes"
2. Network timeout / page refresh
3. Invitee clicks submit again (or browser auto-retry)
4. Two RSVP records created, or first is overwritten
5. Capacity count is now wrong, or duplicate email sent
```

**Why It Matters**: Violates **BI3 (Unique response per invitee per event)** and **CI3 (no double email sends)**

**Current Control**: Unknown (likely only application-level upsert, not database-level)

**Required Control**: 
- **Idempotency Key**: Invitee generates unique submission ID (client-side UUID). API checks "have I seen this submission ID before?"
- **OR Unique Constraint**: Database constraint on (event_id, invitee_email) prevents second insert, returns error
- **OR Upsert Logic**: Application uses INSERT ... ON CONFLICT UPDATE (Postgres) pattern

**Risk Classification**: **MEDIUM** — Possible if network is flaky; most users won't hit this

---

### VA2: Concurrent RSVP Submissions (Capacity Race Condition)

**What Can Happen**
```
Timeline:
T1: Person A submits Yes, queries: capacity=5, confirmed=4, ✓ space available
T2: Person B submits Yes, queries: capacity=5, confirmed=4, ✓ space available
T3: Person A inserts Yes → confirmed becomes 5
T4: Person B inserts Yes → confirmed becomes 6 (OVERBOOKING!)
```

**Why It Matters**: Violates **BI1 (capacity constraint)** and **CI1 (no overbooking)**

**Current Control**: **NONE** (only hope that two submissions don't happen simultaneously)

**Required Control** (pick one):
- **Pessimistic Locking**: `SELECT count(*) WHERE status='Yes' FOR UPDATE NOWAIT` locks row, prevents second reader from seeing stale count
  - Pros: Guaranteed safety
  - Cons: Can cause lock contention at high concurrency
- **Serializable Isolation Level**: Set `SET TRANSACTION ISOLATION LEVEL SERIALIZABLE` at connection level
  - Pros: Transparent, no code changes
  - Cons: May cause transaction rollbacks, requires retry logic
- **Optimistic Locking**: Store version number on event, increment on each update. Reject if version mismatch.
  - Pros: No locks, can handle high concurrency
  - Cons: Requires retry loop in application
- **Document the Risk**: Accept limit of ~1% overbooking, fix via batch job
  - Pros: Simplest
  - Cons: Terrible for capacity-critical events

**Our Recommendation for MVP**: Pessimistic lock (simplest, most reliable). Can upgrade to Serializable later if performance needed.

**Risk Classification**: **CRITICAL** — Happens at scale; testing won't catch it easily (race conditions are timing-dependent)

---

### VA3: Waitlist Promotion Race (Multiple People Change to No Simultaneously)

**What Can Happen**
```
Scenario: Event capacity=2, both confirmed attendees are on call and both cancel
T1: Person A changes Yes → No
T2: Person B changes Yes → No (at nearly same time)
T3: A's transaction: decrement count, find first Waitlisted, promote to Yes
T4: B's transaction: decrement count, find first Waitlisted, promote to Yes
T5: Both promote SAME waitlisted person (count increments twice, same person confirmed twice)
```

**Why It Matters**: Violates **CI2 (exactly one person promotes, not two)**

**Current Control**: **NONE** (application logic is not transaction-serialized)

**Required Control**:
- **Serializable Isolation** or **Pessimistic SELECT FOR UPDATE** on the waitlist promotion query
- Atomic transaction: decrement count, SELECT first waitlisted FOR UPDATE, update status, increment count
- Ensure transaction is ordered (first writer wins, second gets "no one to promote" result)

**Alternative Control**: Use message queue to serialize all capacity-mutating operations for an event (all RSVPs for event X go through queue, processed in order)

**Risk Classification**: **CRITICAL** — Small window, but possible; causes guest to lose spot without knowing

---

### VA4: Stale Event Data (Host Edits Event While RSVP in Flight)

**What Can Happen**
```
T1: RSVP Service reads event.date_time = "2026-05-10 18:00", not locked
T2: Host changes event.date_time to "2026-05-08 18:00" (past date)
T3: RSVP Service checks if current_time < event.date_time (still using old time from T1)
T4: RSVP allowed mistakenly (because we checked against old date)
T5: Event is now "in the past" but RSVP was allowed
```

**Why It Matters**: Violates **BI4 (RSVPs locked post-event start)**

**Current Control**: 
- **Defense 1**: Event.rsvps_locked flag (but set by scheduler, not immediate)
- **Defense 2**: RSVP Service checks time (but may use stale cache)

**Required Control**:
- **Read Fresh Data**: RSVP Service must SELECT event.date_time, event.rsvps_locked FOR UPDATE (or with lock)
- **Prohibit Date Edits**: Once event invitations are sent, lock event.date_time from further edits
- **OR**: Always read event inside transaction scope, don't cache

**Risk Classification**: **MEDIUM** — Requires host to edit date during narrow time window; unlikely but possible

---

### VA5: Email Send Failure & Silent Loss

**What Can Happen**
```
T1: RSVP.status updated to "Yes", transaction commits
T2: Event published: "rsvp_submitted"
T3: Email Service picks up event, starts sending confirmation email
T4: Email provider is down / network fails
T5: Email job fails, but error is not retried
T6: RSVP is updated in DB ("Yes"), but invitee never sees confirmation
T7: Invitee thinks RSVP didn't work, tries again
```

**Why It Matters**: Violates **user expectation** (not a correctness invariant, but a UX disaster)

**Current Control**: **NONE** (assuming email job is async and fire-and-forget)

**Required Control**:
- **Retry Logic**: Email job must retry on failure (exponential backoff, max retries)
- **Dead Letter Queue**: Failed jobs go to DLQ after max retries, trigger alert
- **Idempotency**: Email must be idempotent (same email ID won't send twice even if retried)
- **Feedback to RSVP**: Ideally, mark RSVP.email_sent = true only after email actually sends
- **OR Accept Risk**: Document that transient email failures are possible

**Risk Classification**: **HIGH** — Can strand users in unreliable state; no alerting means silent failure

---

### VA6: Database Transaction Isolation Issues

**What Can Happen**
```
Current Setting (assumed): READ_COMMITTED (PostgreSQL default)

Scenario: Capacity check is not serialized
T1: Transaction A: SELECT count(*) WHERE status='Yes' → 4
T2: Transaction B: SELECT count(*) WHERE status='Yes' → 4
T3: Transaction A: INSERT RSVP status='Yes' → count becomes 5
T4: Transaction B: INSERT RSVP status='Yes' → count becomes 6 (OVERBOOKING)
```

**Why It Matters**: **Non-serializable execution** violates **BI1 (capacity constraint)**

**Current Control**: Unknown (depends on database default isolation level)

**Required Control**:
- **Set Isolation Level**: Explicitly configure `SERIALIZABLE` or `REPEATABLE_READ` at transaction start
- **OR**: Add `FOR UPDATE` locks to capacity check queries
- **OR**: Document capacity constraint as "soft" (allows ±1% overbooking)

**Risk Classification**: **CRITICAL** — Depends entirely on isolation level; testing may not catch it

---

### VA7: Scheduled Job Duplicate Runs (Event Lock Job Runs Twice)

**What Can Happen**
```
Event start time: 2026-05-10 18:00:00

T1: System clock = 2026-05-10 18:00:05
T2: Scheduler Job #1 wakes up, does: UPDATE event SET rsvps_locked=true WHERE date_time <= now()
T3: Scheduler Job #2 wakes up (misconfigured, or platform restarts job), does same UPDATE
T4: Both write rsvps_locked=true (idempotent, harmless, but not ideal)
```

**Why It Matters**: **Depends on implementation**. If job also sends "event locked" notifications, could send duplicate emails.

**Current Control**: **NONE** (job is run by scheduler, no dedup)

**Required Control**:
- **Idempotent Job**: UPDATE should be idempotent (setting flag to true twice is fine)
- **Lease/Lock**: Job acquires distributed lock before running, prevents concurrent execution
- **Last-Run Timestamp**: Check if job already ran in last hour; skip if yes
- **Event Flag**: Set "locked_by_job" timestamp; only run job if timestamp < now

**Risk Classification**: **LOW** (idempotent by nature, but adds noise to logs)

---

### VA8: Unique Token Collision (Two Invitations Get Same Token)

**What Can Happen**
```
T1: Host invites alice@example.com → generates token "abc123def456"
T2: Later, host invites bob@example.com → generates token "abc123def456" (collision!)
T3: Database unique constraint on token rejects second insert
T4: Bob never gets invited (user faces error)
```

**Why It Matters**: Violates **DI2 (unique tokens)**

**Current Control**: 
- **Randomness**: Assume cryptographic randomness (UUID) makes collisions astronomically unlikely
- **Unique Constraint**: Database prevents actual duplicates

**Required Control**:
- **Use UUID**: Guaranteed unique per invocation; no collision risk with proper randomness
- **Retry on Constraint Violation**: If token collision happens (should never), generate new token and retry
- **Avoid Sequential Tokens**: Do NOT use sequential or predictable tokens

**Risk Classification**: **NEGLIGIBLE** (with UUID, impossible; with weak RNG, ~0.01% risk)

---

### VA9: Concurrent Event Edits (Race Condition on Event Update)

**What Can Happen**
```
Host opens event editor in two browser windows
Window 1: Changes title from "Birthday" to "Birthday Party"
Window 2: Changes capacity from 20 to 30
Both click submit (nearly simultaneous)
T1: Window 1 UPDATE event SET title='Birthday Party' WHERE id=X
T2: Window 2 UPDATE event SET capacity=30 WHERE id=X
Result: Title is updated, capacity is updated (both applied, no conflict)

But what if:
Window 1: Changes title, capacity (both fields)
Window 2: Changes location
Result: Last write wins (Window 2's location is applied, Window 1's location is lost)
```

**Why It Matters**: **Lost update**: one user's edit is silently lost

**Current Control**: **NONE** (no version checking or locking)

**Required Control**:
- **Optimistic Locking**: Add version column to event. On update, check version; if mismatch, reject.
- **Pessimistic Locking**: Host locks event while editing (prevents other edits)
- **Merge/3-Way Merge**: Complex, usually not worth it for this domain

**Risk Classification**: **MEDIUM** — Unlikely (users rarely edit simultaneously), but possible

---

### VA10: Worker Crash After Update, Before Email Send

**What Can Happen**
```
T1: RSVP Service updates RSVP.status = 'Yes', commits transaction
T2: Event published: "rsvp_submitted"
T3: Background worker picks up event, starts async email send
T4: Worker code: send email, then mark invitation.email_sent_at = now()
T5: Email sends successfully
T6: Worker CRASHES before updating email_sent_at
T7: Email was sent, but DB says "not sent"
T8: Retry logic: sees email_sent_at is NULL, sends AGAIN (duplicate)
```

**Why It Matters**: Violates **CI3 (no double email sends)**

**Current Control**: **NONE** (no transactional coordination between send and record)

**Required Control**:
- **Transactional Outbox Pattern**: 
  1. Publish event to outbox table in same transaction as RSVP update
  2. Worker reads from outbox, sends email, marks outbox row as processed
  3. Failure between steps 2-3 means retry will resend (acceptable if email is idempotent)
- **Idempotent Emails**: Email provider accepts same email ID twice, dedupes on server
- **Save Status BEFORE Send**: Mark email_sent_at in DB, then send email. If crash during send, at least DB is accurate.

**Risk Classification**: **MEDIUM** — Crash is rare, but if it happens, duplicate email is likely

---

### VA11: Invitee Bulk-Submits Many RSVP Changes (Rate Limiting)

**What Can Happen**
```
Invitee's script submits RSVP yes/no/maybe 1000 times per second
T1-T1000: 1000 status changes in 1 second
Result: Database loaded with 1000 updates, dashboard is slow
```

**Why It Matters**: **Denial of service** (not a correctness invariant, but availability)

**Current Control**: **NONE** (no rate limiting)

**Required Control**:
- **Rate Limit Per Token**: Max N changes per invitee per hour (e.g., 10)
- **IP-Based Rate Limit**: Max M requests from IP per minute
- **Token Bucket**: Allow burst but average rate is limited

**Risk Classification**: **LOW** (requires adversarial invitee; unlikely in MVP)

---

### VA12: Event Lock Check & RSVP Lock Flag (Timing Luck)

**What Can Happen**
```
Current Design: RSVP Service checks BOTH time AND flag

Scenario: Job is broken, never sets rsvps_locked=true
Event start time: 2026-05-10 18:00:00
T1: Current time = 2026-05-09 23:00:00 (before event) — RSVP allowed ✓
T2: Current time = 2026-05-10 18:00:30 (after event) — Time check says lock, ✓ RSVP blocked
T3: Current time = 2026-05-11 09:00:00 — Time check still blocks ✓

This works! But only because time check is in code.

What if code is refactored and time check is removed?
T1: Current time = 2026-05-11 09:00:00 — Flag is still false (job never ran) — RSVP NOT blocked ✗
```

**Why It Matters**: Violates **BI4 (RSVPs locked post-event)**

**Current Control**: 
- **Defense 1**: Code-based time check (working by luck if job fails)
- **Defense 2**: Event.rsvps_locked flag (working if job runs)

**Dependency**: System depends on BOTH being in place. If one is removed, system breaks **silently**.

**Required Control**:
- **Choose One Source of Truth**:
  - Option A: Job sets flag; code always checks flag. Remove time check from code. (Job must be reliable, alertable)
  - Option B: Code checks time; ignore flag. (Simpler, no job needed, but flag becomes dead data)
  - Option C: Check both; document why. (Defensive, but confusing)
- **Make it Explicit**: If you keep both, add a comment explaining why, and add tests for both paths

**Risk Classification**: **CRITICAL & TIMING-DEPENDENT** — System currently depends on "timing luck": both checks happen to be in place. Refactor could break silently.

---

### VA13: Out-of-Order Email Delivery (Early Arrival)

**What Can Happen**
```
Host sends 100 invitations
Email provider delivers them out of order
Person #50 receives email, clicks link, submits RSVP before person #1 even gets email
T1: Person #50: clicks RSVP link → invitation token valid — accepted ✓
T2: Person #1: email arrives 30 minutes later

No correctness violation, but: Person #50 might not realize they were early inviter.
```

**Why It Matters**: **Not a correctness issue**, but affects UX expectations

**Current Control**: **Inherent to email** (no control possible)

**Required Control**: None needed for correctness; document that email delivery is not ordered.

**Risk Classification**: **LOW** (expected email behavior, not a bug)

---

### Summary: Which Correctness Risk Depends on Timing Luck?

🎲 **Timing-Luck Risks (System Works Only If Timing Happens Right)**:

1. **VA2 - Capacity Race Condition**: Works only if two RSVP submissions are never truly concurrent. Luck.
2. **VA3 - Waitlist Promotion Race**: Works only if two No changes never happen simultaneously. Luck.
3. **VA12 - Event Lock Timing**: Works only if BOTH time check AND flag are in place. Refactoring removes one → breaks silently. **Biggest risk.**

🎲 **Most Dangerous**: **VA12 (Event Lock Status)** — System has two independent controls (time check + flag) as if they're defense-in-depth, but they're actually *assumptions*. If one is removed, no one notices until someone RSVPs after event starts. This is a **silent correctness violation**.

---

### Control Types Needed (Summary Table)

| VA | Risk | Control Type | Effort |
|---|---|---|---|
| VA1 | Duplicate submission | Idempotency key OR upsert | MEDIUM |
| VA2 | Capacity race | Pessimistic lock OR isolation level | MEDIUM |
| VA3 | Waitlist race | Serializable isolation OR message queue | HIGH |
| VA4 | Stale event data | Fresh read + lock OR prohibit edits | MEDIUM |
| VA5 | Email send failure | Retry + DLQ + idempotency | MEDIUM |
| VA6 | Isolation level | Set isolation level explicitly | LOW |
| VA7 | Scheduled job duplicate | Idempotent job OR distributed lock | MEDIUM |
| VA8 | Token collision | Use UUID | LOW |
| VA9 | Concurrent event edits | Optimistic locking OR prevent edits | MEDIUM |
| VA10 | Worker crash after update | Transactional outbox OR idempotent email | HIGH |
| VA11 | Rate limiting | Token bucket rate limit | LOW |
| VA12 | Lock timing luck | Choose ONE source of truth | HIGH |

---

---

## 13. Scalability and Multi-Tenancy Notes

### Growth Axes (What Could Scale)

1. **Number of Events** (linear): Users create more events over time
2. **Invitees Per Event** (unbounded): One event could invite 10k, 100k people
3. **Concurrent RSVP Submissions** (unpredictable): Small event: 1-5/sec. Large event: 100s/sec. Flash mob event: 1000s/sec.
4. **Daily Email Volume** (linear × event size): 100 events × 500 invites = 50k emails/day = 600 emails/sec peak
5. **Dashboard Query Traffic** (linear × hosts): Hosts refreshing dashboard during event
6. **Storage** (unbounded): Events + RSVPs stored forever; no archival

---

### Likely First Bottlenecks (What Breaks First)

**Bottleneck #1: Database Lock Contention on Capacity Check (PESSIMISTIC LOCKING)**
```
Problem: RSVP Service uses SELECT FOR UPDATE on capacity counter
Scenario: Large event, 500 people RSVP in final 5 minutes
Symptom: Every RSVP submission waits for lock; queue builds up; users see 10-30sec latency spikes
Root cause: Lock is held for entire transaction duration while email job enqueues
Scale: Breaks at ~50-100 concurrent RSVP submissions to same event
Fix Options:
  1. Switch to SERIALIZABLE isolation + optimistic retry (removes lock, adds retry overhead)
  2. Reduce lock hold time: queue email job outside transaction
  3. Shard by event: more complex, overkill for single-host events
  4. Accept it; document "concurrent RSVP limit ~50/sec per event"
Current Design: Assuming pessimistic lock as primary control (VA2); no mention of contention risk
```

**Bottleneck #2: Email Service Latency / Throughput**
```
Problem: Email sending is asynchronous, but job must connect to external email provider
Scenario: Host invites 10k people; 10k emails queued; provider rate-limit is 100/sec
Symptom: Bulk invites take hours to send; invitees don't receive links for hours; confusion
Scale: Breaks at ~5k invites in one batch
Root cause: Email service provider has rate limits (e.g., SendGrid allows ~100/sec)
Fix Options:
  1. Implement backoff / adaptive rate limiting in job
  2. Batch emails: queue 100 at a time, wait before batch
  3. Use higher-tier email provider (costs more)
  4. Implement local mail relay (adds ops complexity)
Current Design: Assuming fire-and-forget job; no rate limiting mentioned
Database will fill with outbox rows if job is slower than queue
```

**Bottleneck #3: Dashboard Aggregation Query (Counts by RSVP Status)**
```
Problem: Dashboard query: SELECT status, COUNT(*) FROM rsvp WHERE event_id=? GROUP BY status
Scenario: Large event with 50k RSVP records; query scans all 50k rows
Symptom: Dashboard takes 500ms-2sec to load; host waits; multiple hosts querying = contention
Scale: Breaks at ~10k RSVPs per event
Root cause: No index on (event_id, status) or no caching of counts
Fix Options:
  1. Add materialized count table: update on每 RSVP change (adds complexity, must be kept in sync)
  2. Cache counts with TTL: Redis cache, invalidate on RSVP change (adds infrastructure)
  3. Accept slow dashboard; document "refresh time may be 1-5sec for large events"
  4. Denormalize: store count columns in event table, increment/decrement atomically
Current Design: No mention of caching or counting strategy; assumes query is fast
```

**Bottleneck #4: Scheduled RSVP Lock Job (Becomes O(n) at Scale)**
```
Problem: Job runs every minute: SELECT * FROM event WHERE date_time <= now() AND rsvps_locked=false
Scenario: System has 100k events; job must scan all to find ones to lock
Symptom: Job takes 5-10 seconds; if it runs every minute, job queue fills; second run starts before first completes
Scale: Breaks at ~10k events with overlap (some past, some future)
Root cause: No efficient way to find "events that should be locked NOW"
Fix Options:
  1. Index on (date_time, rsvps_locked) — helps, but still O(n) worst case
  2. Event queue: when event is created, queue a "lock at date_time" job (DLQ pattern)
  3. Accept slow job; document "lock will be set within 5-10 minutes, not exactly at start time"
Current Design: Assumes job is instant; no mention of job latency at scale
```

**Bottleneck #5: Primary Key Lookup Congestion (Event Table)**
```
Problem: Service layer reads event by ID (SELECT * FROM event WHERE id=?), used in every request
Scenario: Single event with 10k concurrent dashboard + RSVP requests
Symptom: Event row is hot; row-level lock contention on event update
Scale: Breaks at ~500-1000 concurrent requests to same event
Root cause: Event is mutable (host can cancel, close); lock required for updates
Fix Options:
  1. Cache event metadata (title, date, capacity) separately; only lock when mutating
  2. Read-only copy of event for read requests; only lock on write
  3. Event snapshot: store immutable copy at invite time; use snapshot for RSVP logic
Current Design: Assumes event reads are cheap; no caching strategy
```

---

### Multi-Tenancy Concerns (Currently Single-Tenant Design, But Risky)

**MT1: Event Ownership (Currently: One Host)**
```
Current: event.host_id = single user; host can view dashboard, invites, cancellations
Isolation: Event is private to host + invited guests
Noisy Neighbor Risk: One host creates 10k events
  - Dashboard listing all events becomes slow
  - Event deletion cascades to 10k× invitations and RSVPs
  - Scheduler job must lock 10k event rows on same night
Mitigation: None in current design
Risk Level: MEDIUM (possible but requires single malicious/careless user)
```

**MT2: Email Service Shared (Currently: Single Queue)**
```
Current: All invitations, confirmations, promotions share single email job queue
Noisy Neighbor Risk: One large event (10k invites) queues 10k emails
  - Other events' emails wait in queue
  - Promotions for other events delayed hours
  - Job crashes; all email stuck, including small events
Mitigation: Separate queues per priority level? Or higher-throughput provider?
Risk Level: HIGH (single-host events can starve others)
```

**MT3: Database Connections (Currently: Connection Pool)**
```
Current: Spring Boot uses HikariCP (default pool=10 connections)
Noisy Neighbor Risk: One large event with 1000 concurrent RSVPs uses 10 connections
  - Other requests wait for connection
  - Connection pool exhausted; new requests timeout
Mitigation: Increase pool size? Or use queue/backpressure?
Risk Level: MEDIUM (tunable, but not mentioned in architecture)
```

**MT4: Webhook / Event Listener Ordering (Currently: Implicit Async)**
```
Current: RSVP Service publishes "rsvp_submitted" event; Email Service subscribes
Noisy Neighbor Risk: If Email Service is slow, queue of events backs up
  - Later events' emails delayed
  - If service crashes, event is lost
Mitigation: Use message queue (RabbitMQ, Kafka) with separate consumer groups
Risk Level: LOW for MVP (single queue, single subscriber), HIGH for multi-tenant
```

---

### What Is Sufficient Now vs. Later Architectural Change

**✅ Sufficient Now (MVP Scale)**
- Single PostgreSQL database (no replication needed)
- Single email job worker (synchronous or simple async)
- No event partitioning or sharding
- No caching layer
- No message queue (in-memory event publisher OK)
- Event IDs can be sequential or UUID
- Dashboard refreshes every 5-10 seconds (acceptable for small events)

**⚠️ Must Add Before ~1k Concurrent Users**
- Explicit RSVP concurrency control (pessimistic lock or Serializable isolation level)
- Rate limiting on RSVP submissions per event
- Database indexes on (event_id, status) for dashboard query
- Set database connection pool size explicitly (don't rely on defaults)

**🚨 Would Require Architectural Change Before ~100k RSVPs/Concurrent**
- Message queue for email jobs (replace in-memory publisher with RabbitMQ/SQS)
- Denormalized count table or Redis cache for dashboard aggregation
- Event lock scheduler: move from scheduled SELECT-all-events to event-time-queue pattern
- Read replicas for dashboard queries (separate read and write paths)
- Connection pooling strategy (maybe per-tenant connection pool)

**❌ Architectural Changes for Multi-Tenant (Not in Scope Now)**
- Row-level security (RLS) in PostgreSQL per host
- Separate databases per tenant (overengineering for single-host events)
- Event sharding by tenant (unnecessary; events are scoped to host_id already)

---

### What Scale Problem Is Quietly Assumed to Be Far Away?

🚨 **The Quiet Assumption: Viral Event Concurrency**

**Scenario**: A single event goes viral. 10k people all decide to RSVP "Yes" within 5 minutes (e.g., concert venue sells out suddenly, news breaks).

**What Breaks**:
1. Pessimistic lock (SELECT FOR UPDATE) on capacity check → Lock queue grows to 10k requests waiting in database connection pool
2. Each RSVP transaction holds lock for ~500ms (email job enqueue + commit) → throughput is ~2 RSVP/sec per lock holder → 5000 sequential waits
3. Dashboard queries compete with RSVP locks → host can't see real-time attendance

**Why It's Quiet**: 
- The architecture assumes RSVP submissions are spread over hours (realistic for most events)
- If submissions are spread, lock contention is ~1-5 concurrent, manageable
- The design never explicitly states "concurrent RSVP limit is 50-100/sec/event"
- Testing will not catch this (need load test with sustained high concurrency)

**Current Mitigation**: NONE. Design will silently break at ~100-200 concurrent RSVP submissions.

**Explicit Fix Required**:
```
Either:
Option A) Document: "Maximum 100 concurrent RSVPs per event; larger events should use queue system"
Option B) Switch to Serializable isolation + optimistic retry (slower for low-contention case)
Option C) Implement event-specific queue (each event has serialized RSVP queue)
```

---

### Scale Budget (What's Reasonable to Target for MVP)

| Metric | MVP Target | Comment |
|--------|---|---|
| Total users | 100-1000 | Social sharing spreads events |
| Total events | 100-10k | Users create multiple events |
| Invitees per event | 50-500 | Typical use case |
| Concurrent users | 10-100 | Most events are not live at same time |
| Concurrent RSVPs to same event | 5-20 | Typical; breaks at ~100 |
| Daily emails | 10k-100k | 100 events × avg 100 invites |
| QPS (requests/sec) | 10-50 | Mostly dashboard + RSVP; 80/20 rule |
| Event lock job frequency | 1000-10k events/run | Runs every 1min; each is fast |

**Design is optimized for**: ~50-500 attendees per event, ~20 concurrent RSVPs/sec/event, ~100k daily emails

**Design breaks at**: ~2k attendees per event with concurrent submissions, ~500 concurrent RSVPs/sec/event, viral events

---

### Noisy Neighbor Scenarios (Single-Host Events Can Cascade)

**Scenario 1: Large Event Overwhelms Email Queue**
```
Host A: Creates event with 100k invites (conference registration)
Queue builds up: 100k emails at rate of 100/sec = 1000 seconds = 17 minutes
Host B: Creates event with 20 invites during peak
Host B's emails wait in same queue; arrive 17 minutes late
Host B is confused; assumes system is broken
```

**Scenario 2: Pessimistic Lock Starves Other Events**
```
Event X: Large event, 1000 concurrent RSVPs, all waiting for lock
Event Y: Small event, 1 RSVP submission
Event Y's submission waits for connection from pool; all are held by Event X's locks
Event Y's user waits 10-30 seconds; timeout/error
```

**Scenario 3: Scheduler Job Locks All Events at Midnight**
```
Hundred events all start at 2000-01-01 00:00:00
Scheduler runs at 00:00:01
Tries to lock all 100 at once
Row-level lock contention; takes 10 seconds to complete
Second scheduler run starts at 00:01:00 before first completes
Queue fills; system becomes unavailable
```

**Mitigation**: None in current design. Noisy neighbor isolation would require per-tenant queues or rate limiting.

---

### Conclusion: Design Readiness

**Current design is ready for MVP**: 50-500 invitees/event, ~10-100 concurrent users, ~10k-100k daily emails.

**Becomes risky at**: 1k invitees/event with concurrent bulk RSVPs, or viral events with 100s/sec submissions.

**The quiet assumption**: Events are not viral, and RSVPs are spread over hours. If that assumption breaks, system needs re-architecture.

---

---

## 14. Risks and Failure Modes

### Correctness Failure Risks (From Architecture Choices)

**CR1: Capacity Overbooking on Concurrent Submissions**
```
Trigger: Two or more RSVP "Yes" submissions happen simultaneously when event is at capacity
Outcome: Event confirms more attendees than max_capacity
Root Cause: Pessimistic lock (SELECT FOR UPDATE) not used, or isolation level is READ_COMMITTED
How It Becomes Naive: Host discovers event is over capacity; says "This is fraud" or "System is broken"
Production Impact: MEDIUM (capacity=100, actually 102 attend; venue screwed)
Mitigation: Document max concurrent RSVPs per event; add monitoring; use Serializable isolation
Detection: Daily count check: SELECT COUNT(*) WHERE status='Yes' > event.max_capacity
```

**CR2: Silent Capacity Constraint Violation (Race After Safety Control Removed)**
```
Trigger: Code refactored; pessimistic lock or Serializable isolation is removed for "performance"
Outcome: System "worked fine" in tests but breaks at scale; capacity violated silently
Root Cause: No database CHECK constraint to enforce capacity; safety was only in code
How It Becomes Naive: Production runs fine for weeks, then capacity is violated; no one notices until venue is overcrowded
Production Impact: HIGH (silent failure, hard to debug)
Mitigation: Add CHECK constraint: confirmed_rsvps <= max_capacity at database level
Detection: Monitoring query that alerts if COUNT > capacity
```

**CR3: Waitlist Not Promoted After No Response (Race Condition)**
```
Trigger: Two people change from Yes → No simultaneously; both try to promote first waitlisted person
Outcome: Waitlist person is promoted twice (somehow), or not promoted at all
Root Cause: Waitlist promotion is not serialized; no SELECT FOR UPDATE on waitlist query
How It Becomes Naive: Invitee says "I was on waitlist but never got promoted email"; debug shows they're still Waitlisted
Production Impact: MEDIUM (broken expectation; guest missed event)
Mitigation: Serialize promotion: SELECT first waitlist FOR UPDATE, update atomically
Detection: Monitoring: check for Waitlisted people with no promotion timestamp, or duplicated promotions
```

**CR4: RSVP Allowed After Event Start (Silent Loss of Time Check)**
```
Trigger: Code refactored; time check condition is accidentally removed or commented out
Outcome: RSVP Service checks only event.rsvps_locked flag; flag is never set (job broken)
Result: RSVPs are accepted hours after event ends
How It Becomes Naive: Host realizes attendees RSVP'd after event ended; data is confused
Production Impact: MEDIUM (data integrity; post-event analysis is unreliable)
Mitigation: Make BOTH time check + flag non-optional; test both paths; add code comment explaining why both exist
Detection: Alerting: if time > event.date_time but rsvps_locked=false, send alert
```

**CR5: Timezone Handling Breaks Event Lock Logic**
```
Trigger: Event created in one timezone; RSVP check happens in different timezone
Scenario: Host creates event "2026-05-10 18:00 PT" (Pacific Time)
Server stores it as UTC: 2026-05-11 01:00 UTC
RSVP Server checks with UTC clock
Host's expectation: Event starts at 6 PM Pacific
Server's interpretation: Event starts at 1 AM UTC
If RSVP server is in UTC, it locks 7 hours early
How It Becomes Naive: Invitees on West Coast can't RSVP, event appears locked; host is confused
Production Impact: HIGH (event is locked at wrong time; unclear who's responsible)
Mitigation: Store all dates as UTC; document timezone handling; timezone field on event; test with multiple timezones
Detection: Alerts if rsvps_locked set way before/after expected event time
```

**CR6: Atomic Waitlist Promotion Not Guaranteed**
```
Trigger: Waitlist promotion starts: "Decrement confirmed, find first waitlist, promote, increment count"
Failure: Decrement succeeds, find first waitlist succeeds, but UPDATE on waitlist fails
Outcome: One confirmed moved to No; waitlist person is found but NOT promoted
Production Impact: MEDIUM (attendee slot opened but not filled)
Mitigation: Make entire promotion a single transaction; use savepoints if needed
Detection: Monitoring: check for Confirmed count < expected after No changes
```

---

### Dependency Failure Risks (External Services)

**DR1: Email Service Down → No Invitations Sent**
```
Trigger: Email provider (SendGrid, AWS SES, or local SMTP) is down or unreachable
Outcome: Host clicks "Send Invitations"; system says "success"; no emails arrive
Result: Invitees never receive RSVP links; event starts with zero RSVPs; host is furious
How It Becomes Naive: "We sent invitations successfully" (UI message) but nobody got them
Production Impact: CRITICAL (event is broken; host has no workaround)
Mitigation: 
  - Retry logic with exponential backoff
  - Dead-letter queue for failed emails
  - Alert after N consecutive failures
  - Expose email status in UI (show which emails failed)
Detection: Email job fails > 3 times; alert ops team
```

**DR2: Email Provider Rate Limiting → Bulk Invites Delayed 24+ Hours**
```
Trigger: Host invites 50k people (conference registration)
Provider rate limit: 100 emails/sec max
Reality: 50k emails = 500 seconds = 8 minutes minimum
But if retries + other events sharing queue: 50k emails could take hours or days
How It Becomes Naive: Host expects "bulk invitations sent instantly"; they arrive hours later; registration is broken
Production Impact: MEDIUM (user expectation mismatch)
Mitigation: 
  - Document that bulk emails may take time
  - Split large batches internally (send 1k, wait, send 1k)
  - Use higher-tier email provider with higher limits
  - Warn host upfront: "This will send ~8 hours to complete"
Detection: Monitor job queue depth; alert if backing up
```

**DR3: Database Connection Pool Exhaustion**
```
Trigger: Large event with 1000 concurrent RSVP submissions + dashboard queries
Each holds transaction + lock (pessimistic) for seconds
Default pool size = 10; all 10 connections held
New requests queue; hit timeout (30 seconds)
How It Becomes Naive: System stops responding during peak RSVP time; users think system is down
Production Impact: HIGH (event is in progress; system unreachable)
Mitigation: 
  - Explicitly configure connection pool size (not default)
  - Monitor active connections; alert if > 80% utilized
  - Reduce transaction duration (queue email job outside tx)
  - Consider separate read pool vs write pool
Detection: Monitoring: if active connections > pool size, alert
```

**DR4: PostgreSQL Down → Entire System Down**
```
Trigger: Database crash, storage failure, or network partition
Outcome: All requests fail (no cache, no fallback)
How It Becomes Naive: No high-availability setup; single point of failure
Production Impact: CRITICAL (system is completely unavailable)
Mitigation: 
  - Replicate database (standby for failover)
  - Backup + restore procedure (must be tested monthly)
  - Connection retry logic with circuit breaker
Detection: Health check endpoint pings database; alerts if down
```

**DR5: Background Job Scheduler Fails or Never Runs**
```
Trigger: Job scheduler (Spring @Scheduled) is misconfigured or disabled
Outcome: RSVP lock job never runs; event.rsvps_locked is never set
Result: Event passes start time; RSVPs are still accepted (code time-check might catch, but depends on luck—see CR4)
How It Becomes Naive: Event is "locked" in expectation but RSVPs are actually still accepted; data is corrupted
Production Impact: MEDIUM (silent failure; hard to detect)
Mitigation: 
  - Verify scheduler is running on startup
  - Log every job execution (start, end, count of events locked)
  - Alert if job hasn't run in 2 hours
  - Monitor: if any event.date_time < now AND rsvps_locked=false, alert
Detection: Job execution log; alert if last execution > 1 hour ago
```

**DR6: Network Partition Between Services**
```
Trigger: RSVP Service and Email Service are on different networks; partition occurs
Outcome: RSVP Service publishes "rsvp_submitted" event; Email Service never receives
Result: RSVP saved in DB; email never sent; invitee has no confirmation
How It Becomes Naive: "Event was published" but Email Service is unreachable; no fallback
Production Impact: MEDIUM (lost email; user has no feedback)
Mitigation: 
  - Use durable message queue (RabbitMQ, Kafka) with acknowledgments
  - Email Service explicitly acknowledges receipt
  - Retry with timeout; move to dead-letter if failed
Detection: Message queue depth; alert if emails unacknowledged > 5 minutes
```

---

### Operational Failure Risks (Deployments, Monitoring, Ops)

**OR1: Schema Migration Blocks Capacity Check**
```
Trigger: Adding CHECK constraint to ensure capacity is not violated
Initial state: Some events already have confirmed > capacity
Migration tries to add constraint; fails because constraint is violated
Deployment blocks; cannot rollback safely
How It Becomes Naive: "We tried to add safety and broke production"
Production Impact: CRITICAL (deployment stuck; system undeployable)
Mitigation: 
  - Fix data first: identify events with capacity violations, fix manually
  - Add constraint as deferrable (deferred until end of transaction)
  - Test schema migration on production backup first
Detection: Schema migration testing in CI/CD
```

**OR2: Rolling Deploy Leaves System Inconsistent**
```
Trigger: Deploy new RSVP Service code while old code is still running
Old code: Does NOT check capacity when accepting Yes
New code: DOES check capacity
During deploy: Request from old code, response from new
How It Becomes Naive: System behaves unpredictably; some RSVPs bypass new checks
Production Impact: MEDIUM (data inconsistency; hard to debug)
Mitigation: 
  - Use blue-green deployment (not rolling)
  - Or: ensure old + new code are compatible (backward-compatible schema)
  - Or: disable traffic during deploy (not ideal for uptime)
Detection: Monitoring for capacity violations; alert if any occur
```

**OR3: No Monitoring for Critical Jobs → Silent Failure**
```
Trigger: RSVP lock job fails; no monitoring is in place
Outcome: Job has not run for 3 days; nobody notices
Result: Events that started 3 days ago are still unlocked; old RSVPs are accepted
How It Becomes Naive: "We thought the job was running; it's been broken for days"
Production Impact: HIGH (silent data corruption)
Mitigation: 
  - Add monitoring: last execution time; execution count per hour
  - Alert if job hasn't run in 1 hour
  - Dashboard widget to see job health
Detection: Daily health check; alert on missing job executions
```

**OR4: Data Loss on Database Restore**
```
Trigger: Database crashes; restore from backup is 24 hours stale
Outcome: Last 24 hours of RSVP data is lost
Recovery: Can't redo RSVPs; invitees don't remember their responses; event is corrupted
How It Becomes Naive: "We had a backup but lost a day of data"
Production Impact: CRITICAL (permanent data loss)
Mitigation: 
  - Frequent backups (hourly, not nightly)
  - Test restore procedure monthly
  - WAL archiving for point-in-time recovery
  - Verify backup integrity after each backup
Detection: Backup job monitoring; alert if backup fails
```

**OR5: No Audit Log → Can't Investigate Disputes**
```
Trigger: Invitee claims "I never RSVP'd Yes"
Current system: No audit log; can't prove they did or didn't
How It Becomes Naive: "We can't tell you what happened; no logs"
Production Impact: MEDIUM (customer support can't resolve disputes)
Mitigation: 
  - Add audit log: who did what, when
  - Log RSVP changes (old status, new status, timestamp)
  - Log captain access (who viewed dashboard when)
Detection: Not a technical risk; a customer support issue. But no mitigation in current design.
```

---

### Assumption Failure Risks (What If Our Assumptions Break?)

**AF1: Viral Event Assumption Breaks**
```
Assumption: RSVPs are spread over hours/days; not hundreds per second
Reality: Concert ticket release; thousands RSVP in 5 minutes
Outcome: Lock contention; system slows to <1 RSVP/sec; users see 30-60 sec latencies or timeouts
How It Becomes Naive: "Our capacity logic is right; the database just can't handle it"
Production Impact: CRITICAL (event in progress; system is unusable)
Mitigation: 
  - Document limit: "Max 50 concurrent RSVPs/event"
  - Implement event-specific request queue if limit exceeded
  - Pre-provision resources for known viral events
Detection: Monitoring: QPS per event; alert if > threshold
```

**AF2: Email Won't Be Shared / Forwarded Assumption Breaks**
```
Assumption: RSVP link is sent to exactly one person; they alone RSVP
Reality: Invitee forwards email to roommate; roommate also RSVPs
Outcome: Two people RSVP from same link; system thinks it's one person changing response
How It Becomes Naive: Host counts "1 attendee John Lee"; actually John Lee + Jane Lee
Production Impact: MEDIUM (attendance count off; might not matter if capacity allows)
Mitigation: None in current design; this is accepted risk for MVP
Detection: Manual: host reviews attendee list and notices duplicates
```

**AF3: Hosts Won't Abuse Event Creation Assumption Breaks**
```
Assumption: Hosts create legitimate events; system trusts them
Reality: Spammer creates 10k events to spam email addresses in bulk invitations
Outcome: Email provider flags account as spam; all events unable to send emails
How It Becomes Naive: Legitimate host's invitations blocked because spammer abused the system
Production Impact: MEDIUM (legitimate use affected by bad actor)
Mitigation: 
  - Rate limit event creation per user (e.g., 1 event per hour)
  - Require email confirmation before first event
  - Monitor for bulk event creation; alert ops
Detection: Monitor event creation rate per user; alert if > threshold
```

**AF4: Hosts Won't Change Capacity Mid-Event Assumption Breaks**
```
Assumption: Capacity is set at creation time; doesn't change during event
Reality: Host decides "we have standing room, increase capacity from 50 to 75"
Outcome: System allows 75 Yes RSVPs; venue only has chairs for 50
How It Becomes Naive: Made an assumption; didn't enforce it
Production Impact: MEDIUM (venue capacity mismatch)
Mitigation: 
  - Prohibit capacity edits after invitations send
  - Or: allow edits but recompute waitlist (complex)
  - Or: warn host "This will affect waitlist logic"
Detection: Not a technical risk; business rule enforcement
```

**AF5: Event Clock Never Skews / Timezone Never Changes Assumption Breaks**
```
Assumption: System clock is accurate; event dates are in host timezone
Reality: System clock drifts; or host changes timezone between creating and running event
Outcome: Event date logic becomes wrong; RSVP lock timing is off
How It Becomes Naive: "Why did my event lock at 4 PM instead of 6 PM?"
Production Impact: MEDIUM (event timing is unreliable)
Mitigation: 
  - Use NTP to keep system clock in sync
  - Store timezone explicitly on event; interpret event date in that timezone
  - Test with clock skew scenarios
Detection: Monitoring: if system time drifts > 1 minute, alert
```

**AF6: Invitee Won't Try to Exploit Token Expiry Assumption Breaks**
```
Assumption: Invitee won't try to RSVP after event ends if they know better
Reality: Invitee keeps RSVP link and submits a day later "to check if it's open"
Outcome: If lock job failed (CR4), RSVP accepted; if lock job ran, rejected (correct)
How It Becomes Naive: Depends on job health; correctness is luck-based
Production Impact: LOW (rejected invitee is just curious; not a real problem)
Mitigation: Ensure lock job always runs; add tests for post-event rejection
Detection: Monitoring for post-event RSVP attempts; log them
```

---

### 🚨 What Failure Mode Would Make This Design Look Naive in Production?

**The Naive-in-Production Failure**: **Viral Event During First Week of Production**

**Scenario**:
1. System launches Monday, handling small events (50-200 invitees each)
2. Thursday, influencer shares event link; 10k people try to RSVP simultaneously
3. Pessimistic lock (SELECT FOR UPDATE) hits contention → queue of 10k lock requests
4. Database connection pool exhausted → subsequent requests fail
5. Dashboard queries can't run → host can't see what's happening
6. RSVP submissions timeout after 30 seconds; users retry → queue grows
7. System appears completely broken

**Why It Looks Naive**:
- Assumption was "RSVPs arrive over hours" — never stress-tested with concurrent submissions
- Pessimistic lock was chosen for simplicity, not performance
- No monitoring of lock contention or connection pool depth
- No documentation of limits ("This system can handle 50 concurrent RSVPs/event")
- Team is surprised: "We passed local testing; it broke at scale"

**What Would Have Prevented It**:
- Load test with 1000 concurrent RSVP submissions to single event
- Use Serializable isolation + optimistic retry (instead of pessimistic lock)
- Implement circuit breaker on RSVP endpoint (reject fast, don't queue)
- Document limits explicitly: "Max 50-100 concurrent RSVPs/event without redesign"
- Capacity test before launch

**Other Naive-Looking Failures**:

2. **Silent RSVP Lock Failure**: Host doesn't realize RSVPs are still being accepted 2 hours after event; only notices when invitees report they RSVP'd late. Cause: lock job broken (see CR4). Looks naive: "How did this get through testing?"

3. **Email Provider Rate-Limit Cascade**: Host invites 50k people for mass event; emails arrive over 24 hours instead of 1 hour. Looks naive: "Why didn't you tell me bulk invites take this long?" (No UX warning given.)

4. **Waitlist Promotion Lost**: Attendee changes to No; first waitlisted person is NOT promoted (race condition). Looks naive: "Why doesn't auto-promotion work?" (Untested under concurrent conditions.)

5. **Capacity Overbooking**: Event with capacity=100 ends up with 102 Yes RSVP
s (race condition). Looks naive: "Your database is broken; math doesn't add up."

---

### Risk Priority Matrix (What to Fix Before Production)

| Risk | Likelihood | Impact | Effort to Fix | Action |
|------|---|---|---|---|
| Capacity overbooking (CR1) | MEDIUM | CRITICAL | MEDIUM | FIX NOW |
| RSVP lock timing (CR4) | MEDIUM | MEDIUM | LOW | FIX NOW |
| Waitlist race (CR3) | MEDIUM | MEDIUM | MEDIUM | FIX NOW |
| Viral event contention (DR3, AF1) | MEDIUM | CRITICAL | HIGH | DOCUMENT LIMIT |
| Email service down (DR1) | LOW | CRITICAL | MEDIUM | RETIRE + DEAD LETTER |
| Zone timezone handling (CR5) | LOW | MEDIUM | MEDIUM | REFACTOR |
| Rolling deploy inconsistency (OR2) | LOW | MEDIUM | LOW | USE BLUE-GREEN |
| Job scheduler fails (DR5) | LOW | MEDIUM | LOW | ADD MONITORING |

---

---

## 15. Alternative Design Directions

### Current Proposed Design (Final Summary)
- **Capacity Enforcement**: Pessimistic lock (SELECT FOR UPDATE) with Serializable isolation
- **Concurrency Model**: Optimistic retry assumed; one thread wins capacity check
- **Email Delivery**: Async job queue, in-memory event publisher, best-effort send (no transactional guarantee)
- **State Model**: Mutable entities (RSVP status changes in-place); implicit state machine
- **Scalability**: Single database; no sharding; capacity check serializes all RSVP submissions
- **Correctness Guarantee**: Strong (capacity never violated); bought with latency (locks)

---

### Alternative A: Serialized RSVP Queue (Message Queue Pattern)

**Design**:
```
Every event has a queue of RSVP requests.
RSVP submission → enqueue request → worker processes one-at-a-time
Worker: read current count, decide Yes/Waitlist/No, update + send email
All requests for same event are serialized (no concurrency).
```

**Correctness**:
- ✅ Capacity never violated (queue enforces serialization)
- ✅ Waitlist promotion guaranteed (single thread)
- ✅ Email send is part of same transaction
- ✅ No lost updates, no race conditions

**Complexity**:
- Adds new infrastructure: RabbitMQ/Kafka/SQS (must be installed, monitored, backed up)
- Job worker polling + error handling (more code, more edge cases)
- Debugging latency issues (queue depth monitoring, worker CPU)

**Operational Burden**:
- HIGH: RabbitMQ is a separate system to operate
- Must monitor: queue depth, worker latency, message loss
- Upgrades: RabbitMQ version management; cluster setup if HA needed

**Scalability**:
- **Throughput**: Limited by worker speed; if worker takes 100ms per RSVP, max 10/sec per event
- **Concurrency**: Unlimited (different events have different queues)
- **Multi-event**: Scales horizontally (each event queue independent)
- **Burst Handling**: Queue absorbs burst; worker processes at steady rate (good UX: user gets immediate "queued" response, not blocked for 5 seconds)

**Latency**:
- MEDIUM: User clicks submit → returns immediately ("queued"); actual processing happens 100ms-1sec later
- GOOD for big events (no lock wait), BAD for small events (unnecessary latency)

**Future Changeability**:
- ✅ Easy: Add workflow step (e.g., "notify co-host after RSVP") — just add to worker
- ✅ Easy: Change RSVP business logic (waitlist rules, etc.) — worker code only
- ❌ Hard: Remove queue later (legacy code might depend on async contracts)

**Why a Strong Engineer Might Choose This**:
- Proven pattern; no new concurrency tricks needed
- Operational pain is known (been done before)
- Scales to viral events naturally
- Strong audit trail (every RSVP request is a message)

---

### Alternative B: Event Sourcing (Immutable Event Log)

**Design**:
```
All meaningful actions are immutable events: RSVP_SUBMITTED, RSVP_CHANGED, ATTENDEE_WAITLISTED, etc.
Events are appended to log; never deleted.
Read model (current RSVP state) is derived from replaying events.
Capacity check: replay events, count Yes responses, check >= capacity.
```

**Correctness**:
- ✅✅ Maximal correctness: full audit trail; can replay history; can find root causes
- ✅ No lost updates (append-only log)
- ✅ RSVP changes are fully traceable

**Complexity**:
- VERY HIGH: Event model, projection/read model, replay logic, versioning
- Typical complexity: 2x-3x code length compared to CRUD
- Requires mental model of CQRS (command query responsibility segregation)

**Operational Burden**:
- MEDIUM-HIGH: Event log must be immutable and persistent
- Must implement snapshot logic (replaying 50k events every query is slow)
- Monitoring: event log compaction, projection lag

**Scalability**:
- **Throughput**: Excellent (events are appended in parallel)
- **Concurrency**: Very good (no locks; append-only)
- **Query latency**: MEDIUM (must replay events or maintain cached read model)
- **Storage**: 2x-3x larger (storing all events, not just state)

**Latency**:
- Small events: longer latency (less efficient than CRUD)
- Large events: better latency (no lock contention)

**Future Changeability**:
- ✅✅✅ Excellent: Change business logic, replay history, experiment
- ✅ Can debug production issues by replaying old events
- ❌ Very hard to delete old events (audit trail is purpose of this design)

**Why a Strong Engineer Might Choose This**:
- Natural fit if audit trail is critical (financial events, compliance)
- Enables powerful debugging (replay production bug scenario locally)
- Scales to very high concurrency (no pessimistic locking)
- Makes eventual consistency explicit (read model lags event log)

**Why NOT for this Product (Unless Scaling Beyond MVP)**:
- Overkill for a simple RSVP system
- Massive operational complexity for expected scale
- Too slow for small, fast events

---

### Alternative C: Optimistic Locking with Retry (Version Numbers)

**Design**:
```
Add version column to RSVP and Event tables.
RSVP submit does:
  1. SELECT event.version (current capacity, confirmed count) WITH VERSION N
  2. Check: confirmed < capacity
  3. INSERT/UPDATE rsvp WITH version = N
  4. If version mismatch (concurrent update), ROLLBACK and RETRY
```

**Correctness**:
- ✅ Capacity never violated (version check prevents stale reads)
- ⚠️ Waitlist promotion still needs care (atomic transaction required)
- ✅ No deadlocks (unlike pessimistic)

**Complexity**:
- MEDIUM: Retry loop required in application code
- Exponential backoff; give up after N retries
- Must handle retry logic in UI (show error or auto-retry invisibly?)
- Debugging: why did this request fail? Version mismatch is hard to explain to user

**Operational Burden**:
- LOW: No new infrastructure
- Standard database patterns; nothing exotic

**Scalability**:
- **Throughput**: High on low-contention (no lock waits)
- **Throughput**: Lower on high-contention (retries add overhead)
- **Concurrency**: Very good (no row locks)
- **Contention**: Shows up as "retry loops" not "lock timeouts"

**Latency**:
- Low-contention: very fast (no lock wait)
- High-contention: slower (retry loops can add 50-200ms per failure)
- Unpredictable (depends on timing; could be instant or multi-retry)

**Future Changeability**:
- ✅ Easy: Change capacity logic (just update version check)
- ⚠️ Medium: Interaction effects hard to reason about (when do retries happen?)
- ❌ Hard: Optimize away retries (version numbers are foundational)

**Why a Strong Engineer Might Choose This**:
- Fine-grained performance tuning (retry only on contention)
- No locks = scales to high concurrency
- Simpler than event sourcing; less ops burden than message queue

**Why NOT for this Product**:
- Harder to debug (retry behavior is timing-dependent and hard to reproduce)
- Worse latency under contention (current design: predictable 200ms; optimistic: 200-2000ms depending on luck)
- User experience is unclear ("Why was my request delayed?")

---

### Comparison Matrix

| Aspect | Current (Pessimistic) | Alt A (Message Queue) | Alt B (Event Sourcing) | Alt C (Optimistic) |
|--------|---|---|---|---|
| **Correctness** | Guaranteed | Guaranteed | Perfect audit | Guaranteed |
| **Code Complexity** | MEDIUM | MEDIUM | HIGH | MEDIUM |
| **Ops Burden** | LOW | MEDIUM | MEDIUM | LOW |
| **Scalability** | ~50-100 RSVPs/sec | ~10-20 RSVPs/sec | Excellent | ~100-200 RSVPs/sec |
| **Latency** | Predictable, higher | Unpredictable (queue), lower | Unpredictable (replay), medium | Unpredictable (retry), lows/medium |
| **Debugging** | Easy (locks visible) | Medium (queue depth) | Very easy (replay) | Hard (retries invisible) |
| **Ready for prod?** | Yes, with limits | Yes, more effort | No, overkill | Yes, with more testing |
| **Supports viral event?** | No (locks contend) | Yes (queue absorbs) | Yes (parallel appends) | Medium (retries work) |

---

### When Each Alternative Shines

**Current (Pessimistic Lock)**:
- ✅ MVP with limited concurrency stress test budget
- ✅ Small-medium events (50-500 attendees)
- ✅ Team unfamiliar with distributed systems
- ❌ Viral events with 10k concurrent submissions

**Alt A (Message Queue)**:
- ✅ High concurrency (1000+ RSVPs/sec)
- ✅ Team already runs RabbitMQ / Kafka in production
- ✅ Audit trail of all RSVP requests desired
- ❌ "Keep it simple" projects
- ❌ Teams without ops experience

**Alt B (Event Sourcing)**:
- ✅ Regulatory compliance required (banking, healthcare)
- ✅ Debugging production issues is critical (time-travel debugging)
- ✅ Model is complex enough to benefit from immutable log
- ❌ MVP; simple domain; limited budget
- ❌ Teams unfamiliar with CQRS patterns

**Alt C (Optimistic)**:
- ✅ Low-to-medium concurrency (100-500 concurrent)
- ✅ Predictable latency is NOT critical (async framework OK)
- ✅ Team is comfortable with retry logic
- ❌ Simple, predictable latency required (conference registration at gates)
- ❌ Hard-to-debug systems problematic

---

### 🚨 The Reluctant Tradeoff the Current Design Avoids Admitting

**The design is saying**: "We'll use pessimistic locks to guarantee capacity constraints; this is the right choice for correctness."

**What it's NOT saying explicitly**: "We are choosing **correctness at the cost of concurrency scalability**. Viral events will break this design. The system can handle ~50-100 concurrent RSVP submissions per event, not 1000. This is a hidden limit we've never documented."

**The reluctant tradeoff**:
```
Pessimistic Lock (Current Design):
  ✅ Correctness: 100% capacity guaranteed
  ❌ Scalability: ~50-100 concurrent RSVPs/event max
  ❌ Latency: 200-500ms per RSVP under contention (lock waits)

vs.

Optimistic Retry (Alt C):
  ✅ Scalability: ~200-500 concurrent RSVPs/event max
  ⚠️ Correctness: Still guaranteed (via version check)
  ⚠️ Latency: Predictable on low-contention, unpredictable on high-contention

vs.

Message Queue (Alt A):
  ✅ Scalability: Unlimited, scales horizontally
  ✅ Latency: Predictable (user gets "queued" response instantly)
  ❌ Complexity: Multi-system architecture; ops burden
```

**The Current Design is Choosing**: Correctness + Simplicity over Concurrency.

**But it's NOT saying**: "If we ever need to support viral events with 1000 concurrent RSVP submissions, we must completely redesign the RSVP service. This is not a gradual upgrade path; it's a rip-and-replace."

**The Honest Admission Would Be**:
> "We are building for small-to-medium events that receive RSVP submissions over hours/days, not minutes. If event concurrency exceeds 50-100 simultaneous submissions, the system will slow down significantly or fail. Viral events are out of scope for this architecture. To support them, we'd graduate to a message-queue-based design."

But the design document never says this. It just quietly assumes RSVPs arrive spread out, and if they don't, "that's a capacity planning issue, not a design issue."

---

### Recommendation for Implementation

**Use Current Design (Pessimistic Lock)** because:
1. MVP scope; viral events are unlikely day 1
2. Simplest to implement without extra infrastructure
3. Correctness is guaranteed (strong invariant)
4. Can be upgraded to Alt A (Message Queue) later without breaking existing code (add queue as optional path)

**But Document Explicitly**:
- [ ] "Maximum recommended concurrent RSVPs per event: 50-100"
- [ ] "If concurrent submissions exceed this, system may experience 5-30sec latencies"
- [ ] "Viral events with 1000+ simultaneous submissions are not supported without architecture change to message-queue pattern"
- [ ] Load test to find actual breaking point; put number in docs
- [ ] Add monitoring alerts for RSVP latency; if >2sec, investigate

---

---

## 16. Rollout and Migration Notes

### Release Sequencing (Deployment Order Matters)

**Phase 1: Database Schema (Backward Compatible, No Data Yet)**
```
Deploy: Create new tables (user_account, event, invitation, rsvp) but do NOT drop old tables (if any)
Rollback: Drop new tables (safe; no data yet)
Risk: LOW (tables are empty; old system unaffected)
Time: ~30 minutes (schema creation + index creation)
```

**Phase 2: Backend API Endpoints (Hidden by Feature Flag)**
```
Deploy: Backend code with all endpoints, but all protected by feature flag EVENTS_FEATURE_ENABLED=false
Endpoints exist but return 403 or 404 if flag is off
Allow a day of monitoring: logs, metrics, error rates
Rollback: Disable flag; old code path unaffected
Risk: LOW (endpoints hidden; no traffic)
Time: ~1 hour (deploy + monitoring)
Ops Check:
  - Verify all endpoints exist (curl each one with flag OFF, should 403/404)
  - Check error logs for any exceptions
  - Verify database connections work (no connection pool exhaustion)
  - Load test with 10 concurrent requests to /events endpoint
```

**Phase 3: Email Service Integration (Dry Run)**
```
Deploy: Email service configured, but outgoing emails are captured to log file (not sent)
Send 10 test invitations; verify email content in logs
Rollback: No rollback needed; emails are not actually sent
Risk: MEDIUM (first real dependency on external service)
Time: ~2 hours (configure provider, test, verify)
Critical Checks:
  - [ ] Email templates render correctly (no null values, no broken HTML)
  - [ ] Test with real email provider (SendGrid/AWS SES), not localhost SMTP
  - [ ] Verify rate limiting: send 100 emails in 10 seconds; verify no rejections
  - [ ] Test with invalid email address; verify graceful error (logged, not crashed)
  - [ ] Check email logs; verify no sensitive data leaked (passwords, tokens in plain text)
```

**Phase 4: Frontend Components (Feature Flagged, Hidden UI)**
```
Deploy: React components in codebase, but hidden behind feature flag (EVENTS_FEATURE_ENABLED)
If flag OFF, Event menu items don't appear; create event button hidden
Rollback: Disable flag; UI returns to previous state
Risk: LOW (UI hidden; no user interaction possible)
Time: ~30 minutes
Ops Check:
  - [ ] Verify feature flag is actually working (navigate to /events; should 404 or redirect)
  - [ ] Check browser console for errors (should have none; flag prevents component load)
  - [ ] Verify old pages (dashboard, etc.) still work normally
```

**Phase 5: Soft Feature Flag Rollout (To 1% of Users)**
```
Deploy: Set EVENTS_FEATURE_ENABLED=true, but only for 1% of users (canary)
New users see Event RSVP feature; 99% of users don't
Rollback: Reduce percentage to 0%; UI hidden again
Risk: MEDIUM (real usage; bugs could affect 1% of user base)
Time: ~4 hours (observe metrics, check logs)
Critical Monitoring (Every 10 Minutes for First Hour):
  - [ ] Error rate on /events/* endpoints (should be < 1%)
  - [ ] Database query latency (should be < 500ms for 99th percentile)
  - [ ] Email send success rate (should be > 99%)
  - [ ] RSVP submission latency (should be < 2sec)
  - [ ] Check for capacity violations (COUNT(yes) > max_capacity; should be 0)
  - [ ] Check scheduler job: is event locking happening? (query: SELECT COUNT(*) WHERE rsvps_locked=true; should increase)
  - [ ] User reports (monitor support channels; any complaints about events feature)
```

**Phase 6: Full Rollout (100% of Users)**
```
Deploy: Set EVENTS_FEATURE_ENABLED=true for all users
All users can now create events and RSVP
Rollback: Very difficult; users may have created real events with real invitations sent
Risk: HIGH (point of no return; cannot easily undo)
Time: Continuous (production live)
Post-Rollout Monitoring (First 24 Hours):
  - [ ] Every 15 minutes: error rate, latency, success rates
  - [ ] Every hour: capacity violations (should be 0)
  - [ ] Email delivery: spot-check invitations (manually verify email arrives)
  - [ ] RSVP lock: at midnight, verify lock job executed (SELECT WHERE rsvps_locked=true should > 0)
  - [ ] Watch for customer support tickets related to events
```

---

### Database Migration Path (Data Safety)

**Pre-Production (Using Fixtures)**
```
Test with 1000 fake events, 10k fake invitations, 50k fake RSVPs
Verify:
  - [ ] Indexes are efficient (query plans show index usage)
  - [ ] Foreign key constraints prevent orphaned data
  - [ ] Capacity check query uses index effectively
  - [ ] RSVP lock scheduler is fast enough for 1k events/batch
  - [ ] Dashboard query (aggregate counts) completes < 1sec for 10k RSVPs
```

**Production Migration**
```
Step 1: Create tables (Postgres allows concurrent reads during table creation)
Step 2: Create indexes (can be done online in Postgres 11+; allow concurrent data modifications)
Step 3: Verify indexes exist; run EXPLAIN ANALYZE on key queries
Step 4: Zero downtime; old system keeps working; new tables are empty
```

**Schema Evolution (Later, If Needed)**
```
If adding new column (e.g., event.notify_before_minutes):
  - Use online schema change (Postgres ALTER TABLE ... ADD COLUMN with DEFAULT)
  - No locks; existing queries unaffected
  - Rollback: DROP COLUMN (also online)

If changing column type (e.g., invitation.email VARCHAR(100) → VARCHAR(255)):
  - Use table recreation or ALTER COLUMN with conversion
  - Test conversion on production backup first
  - Plan maintenance window if necessary
```

---

### Backward Compatibility Concerns

**Q: What breaks if we roll back?**

**Scenario A: No Events Created Yet**
- Rollback is safe; drop RSVP/Invitation/Event tables
- Users see feature disappear; no confusion
- Risk: LOW

**Scenario B: Events Created, No RSVPs Yet**
- Rollback loses event records (data loss)
- Risk: MEDIUM (data loss, but no external commitments yet)
- Mitigation: Backup database before full rollout

**Scenario C: Events Have RSVPs, Invitations Sent**
- Rollback: Emails are already sent; invitees still have RSVP links
- Rolling back causes:
  - Links become 404 (confusing to invitees)
  - No way to RSVP (event is deleted)
  - Support tickets flood in
- Risk: CRITICAL; **Cannot rollback in this state**
- Mitigation: Have a "rollback plan" that does NOT delete tables, but hides UI instead

**Q: Can we hide the feature instead of deleting tables?**
- Yes: Set feature flag to false; UI disappears; tables remain; data is preserved
- This is the safe rollback: users don't see broken links; data is not lost
- Much better than table deletion

**Q: What about old code reading/writing to new tables?**
- Current design: All event/invitation/RSVP logic is new; old system doesn't touch it
- Risk: LOW (clear separation)
- If future features need to touch RSVP (e.g., email service), ensure backward compatibility

---

### Feature Flags and Configuration

**Required Flags**
```
EVENTS_FEATURE_ENABLED (boolean, default=false)
  - Controls whether Event RSVP feature is visible/active
  - If false: API endpoints return 403; frontend menu hidden
  - Rollout: false → 1% → 10% → 100%

EMAIL_SERVICE_ENABLED (boolean, default=false)
  - If false: emails are logged but not sent (safe dry-run)
  - If true: emails are actually sent to provider

EMAIL_PROVIDER (enum: "sendgrid" | "aws_ses" | "local_smtp", default="local_smtp")
  - Which email service to use

RSVP_MAX_CONCURRENT_PER_EVENT (integer, default=100)
  - Document; prevent misconfiguration of pessimistic lock timeout

SCHEDULER_TIMEZONE (string, default="UTC")
  - Timezone for scheduler job (when to lock events)
```

**Configuration Management**
```
Store in: application.yml (Spring Boot config)
Or: Environment variables (12-factor app principles)
Or: Database config table (runtime changeability)

Recommendation: Env vars + defaults in code; allows changing flags without redeployment (if using ConfigMap or env var source)
```

---

### Rollback Concerns (What Actually Matters)

**Real Rollback Scenarios**

**RB1: Data Corruption (Capacity Violated)**
```
Scenario: Bug in capacity check; 102 people confirmed for 100-seat event
Symptom: Host checks attendance; count is wrong
Realization: System is broken
Rollback Option A: Fix the data (manual SQL: demote 2 people to Waitlist)
Rollback Option B: Hide feature + fix code + re-enable
Risk: Data loss if we delete RSVP records
Better Approach: Don't rollback; fix forward (correct the data, fix the code, redeploy)
```

**RB2: Email Service Outage**
```
Scenario: SendGrid is down; invitations not sent; feature flag can't fix this
Symptom: Host creates event; clicks "Send Invitations"; gets success message; no emails arrive
Rollback: Feature flag = OFF (hides the create event UI; but damage is already done)
Prevention: Monitoring + alerting on email service health
Fix: Wait for email vendor to recover; implement retry queue
```

**RB3: Lock Job Never Runs (Silent Data Corruption)**
```
Scenario: Scheduler job is disabled by accident; RSVP lock job never sets rsvps_locked=true
Result: Two days of events have rsvps_locked=false; RSVPs are still being accepted after event starts
Detection: Manual: query SELECT * FROM event WHERE date_time < now() AND rsvps_locked=false; should be 0
Rollback: Not possible; data is already corrupted
Prevention: Monitoring; alert if job hasn't run in 1 hour
Fix: Run lock job manually; update all past events: UPDATE event SET rsvps_locked=true WHERE date_time < now()
```

**RB4: Race Condition (Capacity Overbooking)**
```
Scenario: Two people RSVP Yes simultaneously; both check capacity, both see 45/50; both insert
Result: 51/50 confirmed (bug)
Detection: Query SELECT COUNT(*) > max_capacity
Rollback: Demote one person to Waitlist
Prevention: Load testing; pessimistic lock testing; proper isolation level
```

---

### Operationally Sensitive Areas (What Will Bite You)

**OS1: First Event Creation in Production (The "Oh, It Actually Works" Moment)**
```
What Happens: First real user creates an event, invites 5 people
Reality Check: Email service actually works. Invitations actually send. Links actually work.
What Can Go Wrong:
  - Email templates have a typo; all 5 people see broken email
  - RSVP link uses wrong URL; invitees get 404
  - Capacity check has an off-by-one error; only 4 people can RSVP instead of 5
  - User feedback: "Your system didn't work; none of my friends could RSVP"
Prevention: Pre-prod testing with realEmail service; manual end-to-end test (create event, send invite, RSVP, verify)
```

**OS2: The First RSVP (Capacity Logic Actually Matters)**
```
What Happens: First real invitee clicks RSVP link, selects "Yes"
Reality Check: Capacity check runs; count is updated; dashboard updates
What Can Go Wrong:
  - Capacity check is wrong (off-by-one: allows 51 when max=50)
  - Pessimistic lock hits timeout (event.host_id row locked; RSVP waits 30 seconds)
  - Email confirmation never arrives (email job failed silently)
Prevention: Stress test capacity logic; test with 10 simultaneous RSVPs to same small event
```

**OS3: The First Waitlist Promotion (Auto-Promotion Actually Works)**
```
What Happens: Someone confirms → No; system auto-promotes first waitlisted attendee
Reality Check: Promotion happens; email is sent; dashboard updates
What Can Go Wrong:
  - Waitlist is empty, but system tries to promote; NULL pointer exception
  - Two people change to No simultaneously; both try to promote first waitlisted; duplicate promotion
  - Promotion email is sent, but RSVP status is not updated
Prevention: Manual test: create event, fill it, add waitlist, change one Yes → No, verify promotion email and status
```

**OS4: The First Scheduled Lock Job (Midnight/Event Start Time)**
```
What Happens: Event start time arrives; scheduler job runs; rsvps_locked = true
Reality Check: Future RSVPs are rejected; locking actually works
What Can Go Wrong:
  - Job never runs (disabled, crashed, misconfigured)
  - Job runs but queries are slow; locks all events for 5 minutes
  - Job runs at wrong time (UTC vs local time confusion)
  - Job queries are wrong; locks events that haven't started
Prevention: Run scheduler in staging first; verify it executes at right time; monitor run time
```

**OS5: The First Email Rate Limit (Bulk Invite Actually Works)**
```
What Happens: Host invites 500 people; email job submits all to provider
Reality Check: Provider rate-limits at 100/sec; remaining 400 queue and arrive later
Reality Expectation (Host Expectation): All emails arrive instantly
What Can Go Wrong:
  - Host expects instant confirmation "Invitations sent"; expects all emails within minutes
  - Host thinks system is broken when last 100 emails arrive 4 minutes later
  - Host re-sends invitations (now 1000 emails); provider rejects as spam
Prevention: UX message that says "Bulk invitations will be sent over the next X minutes"; set user expectation
```

---

### 🚨 What Looks Simple But Isn't Thought Through

**The Simple Thing**: "Just enable the feature flag when we're ready to go live."

**What's Actually Complicated**:

1. **First Invitations Sent (The Point of No Return)**
   - Once invitations are in users' inboxes, we can't easily rollback
   - Invitees have RSVP links pointing to our server
   - If we rollback/disable feature, links break
   - Support is flooded with "Why can't I RSVP?"
   - We're now obligated to keep the feature working until all events end

2. **Email Service Integration Looks Simple ("Just Configure SMTP")**
   - But actually requires:
     - Provider account (SendGrid, AWS SES, etc.)
     - API keys / authentication
     - TLS/STARTTLS configuration
     - Rate limit handling (each provider has limits)
     - Retry logic (what if provider is down?)
     - Monitoring (emails sending? Why did 5 fail in the last hour?)
     - Spam filtering (make sure our emails aren't flagged)
     - Real provider testing (localhost SMTP won't catch rate limits)
   - Rollout step 3 (email integration dry run) is deceptively complex

3. **First Bulk Event Invite (Large-Scale Email Stress)**
   - Host with 1000 invitees creates event
   - Emails must be sent; but provider rate-limits at 100/sec
   - System designer didn't think about this: "Emails are async, so it scales"
   - Reality: 1000 invites = 10 seconds minimum; if retry + backoff, could be 1 minute+
   - Host is confused; "Is the system broken?"
   - No monitoring on email queue depth; ops doesn't notice problem until customer complains
   - **This is simple in design docs; complicated in production**

4. **The Silent Capacity Violation (It Just Quietly Happens)**
   - Race condition violates capacity; 102 people confirmed for 100-seat event
   - System doesn't crash; doesn't alarm; just commits bad data to DB
   - Host is unaware for hours/days
   - Only discovered when host manually counts: "Why do I have 102 RSVPs?"
   - No monitoring query to detect this; ops has no idea it's happened
   - **Simple to overlook; critical to monitor**

5. **Event Lock Job Doesn't Run (Most Dangerous Because It's Silent)**
   - Job is scheduled; code looks correct
   - But no monitoring that it actually runs every hour
   - Event starts; RSVPs should stop; but job failed
   - Two days later, RSVPs are still being accepted after event starts
   - Manual check: "Oh, lock job has been dead for 2 days"
   - **Looks like it works (no errors); actually broken**

---

### Rollout Checklist (Concrete, Not Generic)

**Database Setup**
- [ ] Run migrations in staging; verify schema is correct
- [ ] Run on production backup; verify no errors
- [ ] Verify indexes exist and are used (EXPLAIN ANALYZE)
- [ ] Test capacity constraint query: SELECT COUNT(*) FROM rsvp WHERE event_id=X AND status='Yes'; verify < 1sec

**Email Service**
- [ ] Configure SendGrid/AWS SES account
- [ ] Test API key (send 1 test email; verify arrival)
- [ ] Test rate limiting (send 1000 emails; measure throughput; confirm provider doesn't reject)
- [ ] Verify email templates render correctly (no null fields, no broken links)
- [ ] Test with invalid email (verify graceful error, not crash)

**Backend Deployment**
- [ ] Deploy to staging with feature flag=false
- [ ] Run smoke test: /health endpoint should return 200
- [ ] Verify database is reachable (SELECT 1 from event; should work)
- [ ] Load test: 10 concurrent requests to /events; verify no timeouts
- [ ] Deploy to production with feature flag=false
- [ ] Verify production deployment: curl /health; check logs

**Frontend Deployment**
- [ ] Deploy UI with feature flag=false (components hidden)
- [ ] Verify: navigate to /events; should 404 or redirect
- [ ] Check browser console; should have no errors
- [ ] Verify old UI (dashboard) still works

**Canary Rollout (1%)**
- [ ] Enable feature for 1% of users
- [ ] Wait 30 minutes; monitor every 5 minutes:
  - [ ] Error rate on /events endpoints (should be < 1%)
  - [ ] Database latency (p99 < 500ms)
  - [ ] Email send success (> 99%)
- [ ] Manual test: create event, invite 5 people, verify emails arrive
- [ ] Check for capacity violations: SELECT COUNT(*) WHERE status='Yes' > max_capacity; should be 0

**Full Rollout (100%)**
- [ ] Enable for all users
- [ ] Monitor continuously for first 4 hours
- [ ] Watch customer support channel for complaints
- [ ] Verify lock job runs at midnight

**24-Hour Post-Rollout Check**
- [ ] Count events created: SELECT COUNT(*) FROM event; should be > 0
- [ ] Count RSVPs submitted: SELECT COUNT(*) FROM rsvp; should be > 0
- [ ] Count locked events: SELECT COUNT(*) FROM event WHERE rsvps_locked=true; should be > 0
- [ ] Capacity violations: SELECT * FROM event WHERE (SELECT COUNT(*) FROM rsvp WHERE status='Yes') > max_capacity; should be empty
- [ ] Email delivery: spot-check 5 random invitations in user's inbox; should exist

---

## 17. Confirmed Requirements (From Discussion)
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
