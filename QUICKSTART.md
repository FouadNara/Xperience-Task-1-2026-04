# Quick Start Guide

Get the Event RSVP Manager running in 3 steps.

## Step 1: Prepare Database

Open PowerShell and create the PostgreSQL database:

```powershell
# Connect to PostgreSQL
psql -U postgres

# In psql prompt:
CREATE DATABASE hero;
CREATE SCHEMA hero;
\q
```

Or if using pgAdmin:
1. Right-click "Databases" → Create → Database
2. Name: `hero`
3. Create
4. Right-click `hero` → Query Tool
5. Run: `CREATE SCHEMA hero;`

## Step 2: Start Everything

In the project root directory, open PowerShell and run:

```powershell
.\start.ps1
```

This will:
- Kill any existing process on port 8280
- Start backend (Spring Boot) on port 8280
- Start frontend (Vite + React) on port 5173
- Keep both running until you press CTRL+C

Wait for both services to fully start (watch for server ready messages).

## Step 3: Test It Out

1. Open browser: **http://localhost:5173**
2. Click "Register"
3. Fill in:
   - Full Name: `John Doe`
   - Email: `john@example.com`
   - Password: `password123`
4. Click "Register" → should show Dashboard
5. Click "Create New Event"
6. Fill in:
   - Title: `Coffee Meetup`
   - Date & Time: Pick a time 3 days from now
   - Location: `Downtown`
   - Max Capacity: `10`
7. Click "Create"
8. Click "View & Manage"
9. Scroll down and click "Send Invitations"
10. Enter: `alice@example.com`, `bob@example.com`
11. Click "Send Invitations"
12. Copy one of the RSVP links from the logs
13. Open in new incognito window: `http://localhost:5173/rsvp/{TOKEN}`
14. Click "Yes"
15. Go back to dashboard → see stats updated

**Success!** You've completed the core workflow.

## Next Steps

See [TESTING.md](./TESTING.md) for comprehensive test scenarios.

## Troubleshooting

**Backend won't start:**
- Check PostgreSQL is running: `psql --version`
- Check database exists: `psql -U postgres -l | grep hero`
- Check port 8280 is free: `netstat -ano | findstr :8280`

**Frontend won't start:**
- Check Node is installed: `node --version`
- Check port 5173 is free: `netstat -ano | findstr :5173`

**Can't login after register:**
- Check backend logs for database errors
- Verify PostgreSQL connection in `application.yml`

**RSVP link doesn't work:**
- Copy the token from backend logs (search for "Token:")
- Make sure to use full URL with token

## Documentation

- [README.md](./README.md) - Full setup & deployment guide
- [DESIGN.md](./DESIGN.md) - Architecture & design decisions
- [TESTING.md](./TESTING.md) - Comprehensive test guide
- [IMPLEMENTATION_SUMMARY.md](./IMPLEMENTATION_SUMMARY.md) - What was built

---

**Questions?** Check TESTING.md for detailed test scenarios or README.md for troubleshooting tips.
