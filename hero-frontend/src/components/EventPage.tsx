import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/bulkSendApi';
import { Event, RSVP, AttendanceStats } from '../types';

export default function EventPage() {
  const { id } = useParams<{ id: string }>();
  const [event, setEvent] = useState<Event | null>(null);
  const [attendees, setAttendees] = useState<RSVP[]>([]);
  const [stats, setStats] = useState<AttendanceStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [emails, setEmails] = useState('');
  const [inviting, setInviting] = useState(false);
  const [showInviteForm, setShowInviteForm] = useState(false);

  useEffect(() => {
    if (id) {
      loadEventData();
    }
  }, [id]);

  const loadEventData = async () => {
    if (!id) return;
    try {
      setLoading(true);
      const [eventData, attendeesData, statsData] = await Promise.all([
        api.getEvent(id),
        api.getAttendees(id),
        api.getAttendanceStats(id),
      ]);
      setEvent(eventData);
      setAttendees(attendeesData);
      setStats(statsData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load event');
    } finally {
      setLoading(false);
    }
  };

  const handleSendInvitations = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    const emailList = emails
      .split('\n')
      .map((email) => email.trim())
      .filter((email) => email.length > 0);

    if (emailList.length === 0) {
      setError('Please enter at least one email address');
      return;
    }

    try {
      setInviting(true);
      if (id) {
        await api.sendInvitations(id, emailList);
        setEmails('');
        setShowInviteForm(false);
        await loadEventData();
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to send invitations');
    } finally {
      setInviting(false);
    }
  };

  const handleCancelEvent = async () => {
    if (!id || !confirm('Are you sure you want to cancel this event?')) return;

    try {
      await api.cancelEvent(id);
      await loadEventData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to cancel event');
    }
  };

  const handleCloseEvent = async () => {
    if (!id || !confirm('Are you sure you want to close this event to new responses?')) return;

    try {
      await api.closeEvent(id);
      await loadEventData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to close event');
    }
  };

  if (loading) {
    return <div className="container">Loading event...</div>;
  }

  if (!event) {
    return <div className="container">Event not found</div>;
  }

  return (
    <div className="container event-page">
      <div className="event-header">
        <h1>{event.title}</h1>
        <div className="event-meta">
          <p>📅 {new Date(event.dateTime).toLocaleDateString()} {new Date(event.dateTime).toLocaleTimeString()}</p>
          {event.location && <p>📍 {event.location}</p>}
          {event.maxCapacity && <p>👥 Max Capacity: {event.maxCapacity}</p>}
          <p>Status: <span className={`status-${event.status.toLowerCase()}`}>{event.status}</span></p>
        </div>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="event-content">
        <section className="stats-section">
          <h2>Attendance Overview</h2>
          {stats && (
            <div className="stats-grid">
              <div className="stat-card yes">
                <div className="stat-number">{stats.yesCount}</div>
                <div className="stat-label">Yes</div>
              </div>
              <div className="stat-card no">
                <div className="stat-number">{stats.noCount}</div>
                <div className="stat-label">No</div>
              </div>
              <div className="stat-card maybe">
                <div className="stat-number">{stats.maybeCount}</div>
                <div className="stat-label">Maybe</div>
              </div>
              <div className="stat-card waitlist">
                <div className="stat-number">{stats.waitlistedCount}</div>
                <div className="stat-label">Waitlisted</div>
              </div>
            </div>
          )}
        </section>

        <section className="invitations-section">
          <div className="section-header">
            <h2>Send Invitations</h2>
            <button
              onClick={() => setShowInviteForm(!showInviteForm)}
              className="invite-btn"
              disabled={event.status === 'CANCELLED'}
            >
              {showInviteForm ? 'Cancel' : '+ Send Invitations'}
            </button>
          </div>

          {showInviteForm && (
            <form onSubmit={handleSendInvitations} className="invite-form">
              <label>Enter email addresses (one per line):</label>
              <textarea
                value={emails}
                onChange={(e) => setEmails(e.target.value)}
                placeholder="john@example.com&#10;jane@example.com&#10;bob@example.com"
                rows={5}
              />
              <button type="submit" disabled={inviting} className="submit-btn">
                {inviting ? 'Sending...' : 'Send Invitations'}
              </button>
            </form>
          )}
        </section>

        <section className="attendees-section">
          <h2>Attendees</h2>
          {attendees.length === 0 ? (
            <p>No RSVPs yet</p>
          ) : (
            <div className="attendees-table">
              <table>
                <thead>
                  <tr>
                    <th>Email</th>
                    <th>Status</th>
                    <th>Last Updated</th>
                  </tr>
                </thead>
                <tbody>
                  {attendees.map((rsvp) => (
                    <tr key={rsvp.id} className={`status-${rsvp.status.toLowerCase()}`}>
                      <td>{rsvp.inviteeEmail}</td>
                      <td>
                        <span className={`status-badge ${rsvp.status.toLowerCase()}`}>
                          {rsvp.status}
                          {rsvp.positionInWaitlist && ` (#${rsvp.positionInWaitlist})`}
                        </span>
                      </td>
                      <td>{new Date(rsvp.updatedAt).toLocaleDateString()}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </section>

        <section className="actions-section">
          <h2>Event Actions</h2>
          <div className="action-buttons">
            {event.status === 'ACTIVE' && (
              <>
                <button onClick={handleCloseEvent} className="close-btn">
                  Close Event
                </button>
                <button onClick={handleCancelEvent} className="cancel-btn">
                  Cancel Event
                </button>
              </>
            )}
            {event.status !== 'CANCELLED' && event.status !== 'ACTIVE' && (
              <p>Event is {event.status.toLowerCase()}</p>
            )}
          </div>
        </section>
      </div>
    </div>
  );
}
