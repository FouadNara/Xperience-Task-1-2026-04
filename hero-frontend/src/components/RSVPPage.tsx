import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import api from '../services/bulkSendApi';
import { Event, RSVP } from '../types';

export default function RSVPPage() {
  const { token } = useParams<{ token: string }>();
  const [event, setEvent] = useState<Event | null>(null);
  const [rsvp, setRsvp] = useState<RSVP | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  useEffect(() => {
    if (token) {
      loadRSVPData();
    }
  }, [token]);

  const loadRSVPData = async () => {
    if (!token) return;
    try {
      setLoading(true);
      const rsvpData = await api.getRSVP(token);
      setRsvp(rsvpData);
      const eventData = await api.getEvent(rsvpData.eventId);
      setEvent(eventData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Invalid or expired invitation link');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (status: 'YES' | 'NO' | 'MAYBE') => {
    if (!token) return;

    setError('');
    setSubmitting(true);

    try {
      const updated = await api.submitRSVP(token, status);
      setRsvp(updated);
      setSubmitted(true);
      
      // Reset the submitted message after 5 seconds
      setTimeout(() => setSubmitted(false), 5000);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit RSVP');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="rsvp-container"><div className="loading">Loading invitation...</div></div>;
  }

  if (error && !event) {
    return (
      <div className="rsvp-container">
        <div className="error-box">
          <h1>Invitation Error</h1>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  if (!event) {
    return <div className="rsvp-container"><div>Event not found</div></div>;
  }

  const isLocked = new Date() >= new Date(event.dateTime);

  return (
    <div className="rsvp-container">
      <div className="rsvp-box">
        <h1>Event Invitation</h1>
        
        <div className="event-details">
          <h2>{event.title}</h2>
          {event.description && <p className="description">{event.description}</p>}
          
          <div className="details-grid">
            <div className="detail-item">
              <span className="label">📅 Date & Time:</span>
              <span className="value">
                {new Date(event.dateTime).toLocaleDateString()} at {new Date(event.dateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
            {event.location && (
              <div className="detail-item">
                <span className="label">📍 Location:</span>
                <span className="value">{event.location}</span>
              </div>
            )}
            {event.maxCapacity && (
              <div className="detail-item">
                <span className="label">👥 Max Capacity:</span>
                <span className="value">{event.maxCapacity}</span>
              </div>
            )}
          </div>
        </div>

        {isLocked && !rsvp && (
          <div className="warning-message">
            ⚠️ This event has already started. RSVPs are no longer being accepted.
          </div>
        )}

        {error && <div className="error-message">{error}</div>}

        {submitted && (
          <div className="success-message">
            ✅ Your RSVP has been recorded! Thank you.
          </div>
        )}

        {rsvp && !isLocked && (
          <div className="current-status">
            <p>Your current response: <strong>{rsvp.status}</strong></p>
            {rsvp.positionInWaitlist && (
              <p>Waitlist position: #{rsvp.positionInWaitlist}</p>
            )}
          </div>
        )}

        {!isLocked ? (
          <div className="rsvp-options">
            <p>Will you be attending?</p>
            <div className="button-group">
              <button
                onClick={() => handleSubmit('YES')}
                disabled={submitting}
                className={`rsvp-btn yes ${rsvp?.status === 'YES' ? 'active' : ''}`}
              >
                ✅ Yes
              </button>
              <button
                onClick={() => handleSubmit('MAYBE')}
                disabled={submitting}
                className={`rsvp-btn maybe ${rsvp?.status === 'MAYBE' ? 'active' : ''}`}
              >
                ❓ Maybe
              </button>
              <button
                onClick={() => handleSubmit('NO')}
                disabled={submitting}
                className={`rsvp-btn no ${rsvp?.status === 'NO' ? 'active' : ''}`}
              >
                ❌ No
              </button>
            </div>
          </div>
        ) : (
          <div className="event-locked">
            <p>This event has started and is no longer accepting RSVPs.</p>
          </div>
        )}
      </div>
    </div>
  );
}
