# Event RSVP Manager - Testing Guide

This guide walks you through testing the Event RSVP Manager implementation.

## Pre-Test Checklist

Before running tests, ensure:

- [ ] PostgreSQL is running and database `hero` with schema `hero` is created
- [ ] Maven is installed (or using `mvnw.cmd`)
- [ ] Node.js 20+ is installed
- [ ] Port 8280 (backend) and 5173 (frontend) are available
- [ ] All code changes have been committed

## Quick Start

1. Open PowerShell in the project root
2. Run: `.\start.ps1`
3. Wait for both services to start:
   - Backend: http://localhost:8280
   - Frontend: http://localhost:5173

## Test Scenarios

### Test 1: User Registration and Login

**Objective:** Verify authentication flow works correctly

**Steps:**
1. Navigate to http://localhost:5173
2. Click "Register" or go to /register
3. Fill in:
   - Full Name: "John Doe"
   - Email: "john@example.com"
   - Password: "password123"
4. Click "Register"
5. Should redirect to Dashboard
6. Logout and log back in with the credentials

**Expected Results:**
- ✅ User can register with email, password, and name
- ✅ User is logged in after registration
- ✅ Session is maintained (can access Dashboard)
- ✅ Can logout and login again

**Troubleshooting:**
- If registration fails, check backend logs for database errors
- If login fails, verify credentials are correct
- If session not maintained, check CORS configuration

---

### Test 2: Create Event

**Objective:** Verify event creation with proper validation

**Steps:**
1. Login as user created in Test 1
2. Click "Create New Event"
3. Fill in form:
   - Title: "Coffee Meetup"
   - Description: "Let's have coffee together"
   - Date & Time: Select a date 3 days from now at 2:00 PM
   - Location: "Starbucks Downtown"
   - Max Capacity: 10
4. Click "Create Event"

**Expected Results:**
- ✅ Form appears and is fillable
- ✅ Event is created successfully
- ✅ Redirects to event details page
- ✅ Event appears in "My Events" list

**Edge Cases to Test:**
- Try creating with past date (should fail with validation error)
- Try creating with invalid date/time (should fail)
- Try creating without required fields (should fail)

---

### Test 3: Send Invitations

**Objective:** Verify invitation system works

**Steps:**
1. From Test 2, you're on the event details page
2. Click "Send Invitations"
3. Enter test emails (one per line):
   ```
   alice@example.com
   bob@example.com
   charlie@example.com
   ```
4. Click "Send Invitations"

**Expected Results:**
- ✅ Invitation form appears
- ✅ Invitations are sent
- ✅ Modal closes
- ✅ Attendees section shows 3 entries (no responses yet)

**What's Happening Behind the Scenes:**
- Backend generates unique tokens for each invitee
- Tokens stored in database with invitation records
- Logs indicate email sent (real email integration not yet implemented)

---

### Test 4: RSVP via Invitation Link (Without Login)

**Objective:** Verify invitees can RSVP with just a token

**Steps:**
1. Check browser console or backend logs for RSVP token
2. Navigate to: `http://localhost:5173/rsvp/{TOKEN}` (replace with actual token)
3. View event details without being logged in
4. Click "Yes" button
5. Verify success message

**Expected Results:**
- ✅ Can view event details using only token
- ✅ Can submit RSVP without login
- ✅ Success message displays
- ✅ Can change response (click "Maybe", then "No")

**Important Security Note:**
- Anyone with the token can RSVP as that invitee
- No additional verification required (by design)
- In production, might add email verification step

---

### Test 5: Capacity and Waitlist

**Objective:** Verify capacity constraint and waitlist promotion

**Steps:**
1. Create a new event with Max Capacity = 2
2. Send invitations to 4 people:
   ```
   person1@example.com
   person2@example.com
   person3@example.com
   person4@example.com
   ```
3. Get the RSVP links for each
4. Have first 2 people respond "Yes"
5. Have person 3 respond "Yes" (should be waitlisted)
6. Have person 4 respond "Maybe" (should be confirmed)
7. View Dashboard → should show:
   - Yes: 2
   - Waitlisted: 1
   - Maybe: 1
8. Go back to first person's RSVP and change from "Yes" to "No"
9. Check stats again → person 3 should be promoted from waitlist to "Yes"

**Expected Results:**
- ✅ First 2 "Yes" responses are confirmed
- ✅ Person 3 "Yes" response is waitlisted (not confirmed)
- ✅ Person 4 "Maybe" is confirmed (doesn't count toward capacity)
- ✅ When first person changes to "No", person 3 is auto-promoted
- ✅ Stats update in real-time

**What's Tested:**
- Capacity constraint enforcement
- Waitlist ordering (FIFO)
- Auto-promotion logic
- Status transitions

---

### Test 6: Event Lock After Start Time

**Objective:** Verify RSVPs are locked after event starts

**Steps:**
1. Create an event with start time in the past (e.g., 1 hour ago)
   - Note: Frontend validation might reject this; you may need to manually test via API
2. OR: Create event with future time, wait for it to start
3. Try to access RSVP link and submit response

**Expected Results:**
- ✅ RSVP page shows warning that event has started
- ✅ Cannot submit RSVP
- ✅ RSVP buttons are disabled

**How It Works:**
- Frontend checks current time vs event.dateTime
- Backend also checks before accepting any RSVP
- Event.rsvpsLocked flag set by scheduler
- Scheduler runs every 60 seconds to lock events

---

### Test 7: Event Management (Cancel/Close)

**Objective:** Verify host can manage event status

**Steps:**
1. Create a new event
2. Send invitations to 2 people
3. Have them RSVP "Yes"
4. As host, click "Close Event" button
5. Verify status changes to "CLOSED"
6. Try to send more invitations (should fail with message)

**Alternative: Cancel Event**
1. After closing, try to "Cancel Event" instead
2. Status should change to "CANCELLED"
3. Cannot send invitations for cancelled events

**Expected Results:**
- ✅ Host can close event to new responses
- ✅ Host can cancel event entirely
- ✅ Status updates in real-time
- ✅ Cannot send invitations to closed/cancelled events

---

### Test 8: Host Dashboard

**Objective:** Verify dashboard shows correct information

**Steps:**
1. Create 3 events with various statuses
2. View Dashboard
3. Check each event card shows:
   - Title
   - Date/Time
   - Location
   - Status
   - Capacity (if set)
   - "View & Manage" button

**Expected Results:**
- ✅ All created events appear
- ✅ Cards show correct information
- ✅ Can click through to event details
- ✅ Event list updates when new events created

---

### Test 9: Attendance Statistics

**Objective:** Verify stats are calculated correctly

**Steps:**
1. Create event with 6 invitations
2. Get RSVP links for each
3. Submit responses:
   - Person 1: Yes
   - Person 2: Yes
   - Person 3: No
   - Person 4: Maybe
   - Person 5: Maybe
   - Person 6: No response (not RSVP'd)
4. View event details
5. Check "Attendance Overview" stats

**Expected Results:**
- ✅ Yes: 2
- ✅ No: 2
- ✅ Maybe: 2
- ✅ Waitlisted: 0 (or > 0 if capacity constraint active)
- ✅ Total responses = 4 (person 6 hasn't responded)

---

### Test 10: Error Handling

**Objective:** Verify graceful error handling

**Test Cases:**
1. Try to access invalid RSVP token: `http://localhost:5173/rsvp/invalid-token`
   - Expected: Error message "Invalid or expired invitation link"

2. Try to access another user's event details via URL: `/event/{another-users-event-id}`
   - Expected: Error message "Unauthorized"

3. Try to submit RSVP to cancelled event
   - Expected: Error message "Cannot RSVP to a cancelled event"

4. Network error simulation:
   - Stop backend server
   - Try to load dashboard
   - Expected: Error message, graceful recovery when backend comes back

**Expected Results:**
- ✅ Clear error messages for all failure scenarios
- ✅ No crashes or blank screens
- ✅ Can recover from errors

---

## Advanced Test Scenarios

### Test A: Concurrent RSVP Submissions

**Objective:** Test race condition handling (capacity constraint)

**Steps:**
1. Create event with Max Capacity = 1
2. Send 3 invitations
3. Quickly submit "Yes" responses from all 3 in quick succession
4. Check final state

**Expected Results:**
- ✅ Only 1 person confirmed
- ✅ Other 2 are waitlisted (or one is confirmed and others waitlisted)
- ✅ Total capacity not exceeded

**Note:** This tests the database constraint and pessimistic locking logic.

---

### Test B: Mobile Responsiveness

**Objective:** Verify UI works on mobile screens

**Steps:**
1. Open browser DevTools (F12)
2. Toggle device toolbar (mobile view)
3. Test all pages:
   - Auth pages (login/register)
   - Dashboard (event list)
   - Event details
   - RSVP page

**Expected Results:**
- ✅ Layout is responsive (no horizontal scrolling)
- ✅ Buttons and forms are touch-friendly
- ✅ Text is readable
- ✅ Navigation works on mobile

---

## Database Inspection

To verify data is stored correctly:

### Using pgAdmin

1. Open pgAdmin → connect to local PostgreSQL
2. Navigate to: hero → Schemas → hero → Tables
3. Inspect tables:
   - `users` - User accounts
   - `events` - Events created
   - `invitations` - Invitations sent (with unique tokens)
   - `rsvps` - RSVP responses (with status and waitlist position)

### Sample SQL Queries

```sql
-- See all events
SELECT * FROM hero.events;

-- See all invitations for an event
SELECT * FROM hero.invitations WHERE event_id = 'EVENT_ID';

-- See RSVP stats for an event
SELECT status, COUNT(*) as count FROM hero.rsvps 
WHERE event_id = 'EVENT_ID' 
GROUP BY status;

-- See waitlist for an event (ordered by position)
SELECT invitee_email, position_in_waitlist, created_at 
FROM hero.rsvps 
WHERE event_id = 'EVENT_ID' AND status = 'WAITLISTED'
ORDER BY position_in_waitlist;
```

---

## Performance Testing

### Load Testing (Optional)

Create multiple events and invitations to test performance:

```bash
# Run this in bash/shell to create multiple events
for i in {1..50}; do
  curl -X POST http://localhost:8280/api/events \
    -H "Content-Type: application/json" \
    -H "Cookie: JSESSIONID=YOUR_SESSION_ID" \
    -d "{
      \"title\": \"Event $i\",
      \"dateTime\": \"2026-06-15T14:00:00\",
      \"maxCapacity\": 100
    }"
done
```

---

## Known Limitations & TODO

- [ ] Email service is logging-only (not actually sending emails)
- [ ] Pessimistic locking not yet fully implemented
- [ ] No audit logging
- [ ] No rate limiting
- [ ] No two-factor authentication
- [ ] No guest list visibility

---

## Debugging Tips

### Backend Debugging

1. Check logs in the backend terminal
2. Enable SQL logging in application.yml (already enabled: `show-sql: true`)
3. Use pgAdmin to inspect database state
4. Add breakpoints in VS Code (if using Java extension)

### Frontend Debugging

1. Open browser DevTools (F12)
2. Check Console for JavaScript errors
3. Check Network tab to see API calls
4. Check Storage for session cookies

### Common Issues

**Issue:** "Port 8280 already in use"
- **Solution:** `start.ps1` should auto-kill the process, or manually: `netstat -ano | findstr :8280` then `taskkill /PID {PID}`

**Issue:** "Database connection refused"
- **Solution:** 
  ```bash
  # Check if PostgreSQL is running
  psql --version
  # Connect to check
  psql -U postgres -d hero
  ```

**Issue:** "CORS error in frontend"
- **Solution:** Check SecurityConfig.java has correct origins and methods

**Issue:** Frontend can't reach backend API
- **Solution:** 
  - Verify backend is running on 8280
  - Check firewall rules
  - Test: `curl http://localhost:8280/api/auth/me`

---

## Success Criteria

You've successfully implemented the Event RSVP Manager when:

- ✅ Users can register and login
- ✅ Hosts can create events with date/time/capacity
- ✅ Hosts can invite guests via email (with unique tokens)
- ✅ Invitees can RSVP without login using token
- ✅ Capacity constraints are enforced
- ✅ Waitlist auto-promotes when space opens
- ✅ RSVPs are locked after event start time
- ✅ Hosts can view live attendance dashboard
- ✅ UI is responsive and user-friendly
- ✅ All error cases handled gracefully

---

## Support

If you encounter issues:
1. Check the backend logs for detailed error messages
2. Check browser console for frontend errors
3. Inspect database state with pgAdmin
4. Review the DESIGN.md for architectural context
5. Check README.md for setup instructions
