import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import api from '../services/bulkSendApi';
import { Event, EventRequest } from '../types';

export default function Dashboard() {
  const [events, setEvents] = useState<Event[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState<EventRequest>({
    title: '',
    description: '',
    dateTime: '',
    location: '',
    maxCapacity: null,
  });
  const navigate = useNavigate();

  useEffect(() => {
    loadEvents();
  }, []);

  const loadEvents = async () => {
    try {
      setLoading(true);
      const data = await api.getMyEvents();
      setEvents(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load events');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'maxCapacity' ? (value ? parseInt(value) : null) : value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const newEvent = await api.createEvent(formData);
      setEvents([...events, newEvent]);
      setShowForm(false);
      setFormData({
        title: '',
        description: '',
        dateTime: '',
        location: '',
        maxCapacity: null,
      });
      navigate(`/event/${newEvent.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to create event');
    }
  };

  if (loading) {
    return <div className="container">Loading events...</div>;
  }

  return (
    <div className="container">
      <div className="dashboard-header">
        <h1>Your Events</h1>
        <button
          onClick={() => setShowForm(!showForm)}
          className="create-event-btn"
        >
          {showForm ? 'Cancel' : '+ Create New Event'}
        </button>
      </div>

      {error && <div className="error-message">{error}</div>}

      {showForm && (
        <div className="form-container">
          <h2>Create New Event</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="title">Event Title *</label>
              <input
                id="title"
                name="title"
                type="text"
                value={formData.title}
                onChange={handleInputChange}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="description">Description</label>
              <textarea
                id="description"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="dateTime">Date & Time *</label>
              <input
                id="dateTime"
                name="dateTime"
                type="datetime-local"
                value={formData.dateTime}
                onChange={handleInputChange}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="location">Location</label>
              <input
                id="location"
                name="location"
                type="text"
                value={formData.location}
                onChange={handleInputChange}
              />
            </div>
            <div className="form-group">
              <label htmlFor="maxCapacity">Max Capacity (Optional)</label>
              <input
                id="maxCapacity"
                name="maxCapacity"
                type="number"
                value={formData.maxCapacity || ''}
                onChange={handleInputChange}
                min="1"
              />
            </div>
            <button type="submit" className="submit-btn">
              Create Event
            </button>
          </form>
        </div>
      )}

      <div className="events-grid">
        {events.length === 0 ? (
          <p className="no-events">No events yet. Create one to get started!</p>
        ) : (
          events.map((event) => (
            <div key={event.id} className="event-card">
              <h3>{event.title}</h3>
              <p className="event-date">
                📅 {new Date(event.dateTime).toLocaleDateString()} {new Date(event.dateTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </p>
              {event.location && <p className="event-location">📍 {event.location}</p>}
              <p className="event-status">Status: {event.status}</p>
              {event.maxCapacity && (
                <p className="event-capacity">Capacity: {event.maxCapacity}</p>
              )}
              <Link to={`/event/${event.id}`} className="view-btn">
                View & Manage
              </Link>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
