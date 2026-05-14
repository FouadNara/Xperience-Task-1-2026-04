# Xperience Task — Event RSVP Manager

> **Xperience Educational Program — Task 01**
> Practice guide: [Building a Design File from Scratch with an AI Partner](https://xperience.works/educational-content/building-a-design-file-from-scratch-with-an-ai-partner)

---

## What is this task?

You are given an empty full-stack scaffold (Spring Boot + React).
Your job is **not** to just build features—it is to first produce a serious design file by following the 18-step guide above, using an AI partner (GitHub Copilot, ChatGPT, Claude, etc.).

Only after your design file is solid should you move on to implementation.

---

## The Feature: Event RSVP Manager

A small web application where users can host events and collect RSVPs from invitees.

### Raw feature brief

- A user can create an event with a title, description, date/time, location, and optional max-capacity.
- The creator becomes the **host** of that event.
- The host can invite people by email.
- Each invitee receives a unique link and can respond: **Yes / No / Maybe**.
- The host sees a live attendance dashboard with counts and a list of attendees.
- If the event has a max-capacity and it is reached, new "Yes" RSVPs go to a **waitlist**.
- A waitlisted attendee automatically moves to confirmed if a confirmed attendee changes their RSVP to No.
- The host can cancel the event or close it to further responses at any time.
- An invitee can change their RSVP at any point **before** the event starts.
- After the event start time, all RSVPs are locked.

Use this brief as input for **Step 02 – Capture the raw feature brief** in the guide.

---

## Tech Stack (already set up for you)

| Layer     | Technology                        |
|-----------|-----------------------------------|
| Backend   | Java 17, Spring Boot 4, Spring MVC, Spring Data JPA |
| Database  | PostgreSQL                        |
| Frontend  | React 19, TypeScript, Vite        |

---

## Prerequisites

Make sure the following are installed on your machine:

- [ ] Java 17+
- [ ] Maven (or use the included `mvnw` wrapper)
- [ ] Node.js 20+
- [ ] PostgreSQL (running locally on port 5432)
- [ ] pgAdmin (PostgreSQL GUI client)
- [ ] Git
- [ ] VS Code
- [ ] GitHub Copilot extension for VS Code

### Install VS Code

Download and install from [code.visualstudio.com](https://code.visualstudio.com/). Accept the defaults.

### Install pgAdmin

Download and install from [pgadmin.org](https://www.pgadmin.org/download/). During installation, **set the PostgreSQL superuser password to `1234`**. Use pgAdmin to create the database and run SQL in the Setup step below.

### Install GitHub Copilot

1. Open VS Code → press `Ctrl+Shift+X` to open the Extensions panel
2. Search for **GitHub Copilot** and click **Install**
3. When prompted, sign in with your GitHub account
4. Also install **GitHub Copilot Chat** for the chat interface

You'll use Copilot as your AI partner throughout the design phase.

---

## Setup

### 1. Clone the repository

```bash
git clone git@github.com:RamiY123/Xperience-Task-1-2026-04.git
cd Xperience-Task-1-2026-04
```

### 2. Create the database

Open your PostgreSQL client and run:

```sql
CREATE DATABASE hero;
CREATE SCHEMA hero;
```

The default credentials expected by the app are `postgres / 1234`.
If you used a different password, update `hero-backend/src/main/resources/application.yml`.

### 3. Start the application

On Windows (PowerShell):

```powershell
.\start.ps1
```

This opens two terminal windows:
- **Backend** → http://localhost:8280
- **Frontend** → http://localhost:5171

---

## Your Task

### Phase 1 — Design (mandatory)

Open `DESIGN.md` at the root of the repo. This is your design file.

Work through all **18 steps** of the guide with your AI partner:

| # | Step |
|---|------|
| 01 | Choose the setup |
| 02 | Capture the raw feature brief |
| 03 | Write the problem statement |
| 04 | Define goals and non-goals |
| 05 | Capture context and constraints |
| 06 | Separate facts, assumptions, and open questions |
| 07 | Identify actors and workflows |
| 08 | Define invariants |
| 09 | Generate first-pass architecture |
| 10 | Define data ownership and state model |
| 11 | Add trust boundaries and security notes |
| 12 | Add concurrency and correctness notes |
| 13 | Add scalability and multi-tenancy notes |
| 14 | Add risks and failure notes |
| 15 | Generate alternatives and tradeoffs |
| 16 | Add rollout / migration notes |
| 17 | Assemble the first complete design draft |
| 18 | Run the pre-review weakness check |

Each step produces a section you copy into `DESIGN.md`.
Working notes and AI back-and-forth stay **out** of the file.

> A good design file for this feature should cover: a clear problem statement, explicit state machines for both Event and RSVP, named invariants (capacity, lock-after-start, waitlist promotion), trust boundaries between host and invitee, and at least one concurrency scenario (two simultaneous RSVPs filling the last spot).

### Phase 2 — Implementation (stretch goal)

After your design is complete, implement the system according to the design document.

#### Backend Implementation

The backend uses Spring Boot 4 with the following key components:

**Entities:**
- `User` - Represents authenticated users (event hosts)
- `Event` - Represents events created by hosts
- `Invitation` - Tracks invitations sent to invitees (with unique tokens)
- `RSVP` - Records RSVP responses (Yes/No/Maybe/Waitlisted)

**Key Services:**
- `AuthService` - User registration and login
- `EventService` - Event management (create, update, cancel)
- `InvitationService` - Invitation generation and email sending
- `RSVPService` - RSVP submission and capacity/waitlist logic
- `EmailService` - Email notifications

**Important Design Decisions:**
- Invitees RSVP via unique tokens without requiring login
- Capacity checks use database transactions to prevent overbooking
- Event RSVP lock is enforced both by timestamp check and database flag
- Automatic waitlist promotion when a confirmed attendee changes to "No"

#### Frontend Implementation

The frontend uses React 19 with TypeScript and Vite:

**Key Components:**
- `LoginPage` - User authentication
- `RegisterPage` - User registration
- `Dashboard` - Host's main page to view and create events
- `EventPage` - Event details, invitations, and attendance tracking
- `RSVPPage` - Invitee's RSVP interface
- `Navigation` - Top navigation with logout

**Features:**
- Session-based authentication with credential storage
- Real-time event creation and management
- Invitation email sending via backend
- RSVP submission and changes (before event start)
- Live attendance statistics and attendee lists

---

## Running the Application

### 1. Prerequisites

Ensure PostgreSQL is running and the database is created:

```sql
CREATE DATABASE hero;
CREATE SCHEMA hero;
```

Update database credentials in `hero-backend/src/main/resources/application.yml` if needed.

### 2. Run Everything with One Command

On Windows (PowerShell):

```powershell
.\start.ps1
```

This script:
- Clears port 8280 (backend)
- Starts Maven build and Spring Boot backend on port 8280
- Installs frontend dependencies and starts Vite dev server on port 5173

### 3. Access the Application

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8280/api

### 4. Test the Flow

1. **Register**: Create a user account
2. **Create Event**: Click "Create New Event" and fill in details
3. **Send Invitations**: Go to event details and send invitations to test emails
4. **RSVP via Link**: Copy the RSVP link and open in a new window
5. **View Dashboard**: Check attendance stats and attendee list
6. **Test Capacity**: Create an event with maxCapacity and watch waitlist in action
7. **Test Event Lock**: Try to change RSVP after event start time (should be locked)

---

## Key Features Implemented

✅ User Registration & Login (session-based)  
✅ Event Creation with Date, Location, and Capacity  
✅ Email Invitation Sending with Unique Tokens  
✅ RSVP Submission (Yes/No/Maybe)  
✅ Capacity Management with Automatic Waitlist  
✅ Automatic Waitlist Promotion  
✅ RSVP Locking After Event Start Time  
✅ Event Cancellation & Closing  
✅ Live Attendance Dashboard with Statistics  
✅ Host-Only Event Management  
✅ Responsive UI Design  

---

## Architecture Notes

### Database Design

- Uses UUID primary keys for all entities
- Unique constraints enforce single RSVP per invitee per event
- Foreign keys with proper cascade rules
- JSON/SQL queries for stats aggregation

### Concurrency Safety

- Pessimistic locking on capacity checks (ready for enhancement)
- Serializable isolation option for critical transactions
- Unique constraints prevent duplicate data

### Security

- Password hashing with BCrypt
- Session-based authentication
- Authorization checks on all protected endpoints
- CORS configuration for frontend communication
- Input validation on all requests

### Email Service

Currently logging to console; ready to integrate with:
- SendGrid
- AWS SES
- Mailgun
- Custom SMTP

---

## Future Enhancements

- [ ] Email integration with real provider
- [ ] Pessimistic locking for concurrent RSVP submissions
- [ ] Audit logging for all operations
- [ ] JWT token-based authentication
- [ ] Guest list visibility options
- [ ] Event reminders before start time
- [ ] Custom RSVP questions/forms
- [ ] Multi-timezone support
- [ ] Mobile app
- [ ] Event discovery / public event listings

---

## Troubleshooting

### Port 8280 or 5173 already in use

The `start.ps1` script automatically frees port 8280. For port 5173, update `hero-frontend/vite.config.ts`:

```typescript
export default defineConfig({
  server: {
    port: 3000 // Change to any available port
  }
})
```

### Database connection failed

Check:
1. PostgreSQL is running (`psql --version`)
2. Database "hero" exists (`\l` in psql)
3. Schema "hero" exists (`\dn` in psql)
4. Credentials in `application.yml` match your setup

### Frontend can't reach backend

Check:
1. Backend is running on port 8280
2. CORS is properly configured in `SecurityConfig.java`
3. Network is available (no firewall blocking localhost)

---

## Design Document

See [DESIGN.md](./DESIGN.md) for the complete architecture and design decisions.

Once your design file passes your own pre-review check (Step 18), implement the feature in the scaffold:

- Add JPA entities and repositories in `hero-backend/`
- Add REST endpoints in a new controller
- Wire up the React frontend in `hero-frontend/src/`

Commit your design file and code separately so reviewers can read the design before the code.

---

## Deliverables

| Deliverable | Where |
|-------------|-------|
| Completed design file | `DESIGN.md` |
| (Stretch) Working implementation | `hero-backend/` and `hero-frontend/` |

---

## Evaluation Criteria

Your design file will be reviewed against the **Definition of Success** from the guide:

- [ ] Clear problem statement
- [ ] Bounded scope (explicit non-goals)
- [ ] Visible assumptions, separated from facts
- [ ] Explicit workflows for all key actors
- [ ] Named invariants
- [ ] Real architecture boundaries
- [ ] Explicit state ownership
- [ ] First-pass trust / concurrency / scale treatment
- [ ] Visible risks and tradeoffs
- [ ] Unresolved open questions listed

---

## Tips

- Follow the guide steps **in order**. Jumping to architecture before invariants produces weak design.
- Interrogate AI output—do not copy it directly into `DESIGN.md`.
- Open questions are a first-class section, not a cosmetic one.
- Fluent AI output is not the same as disciplined output.

Good luck.
