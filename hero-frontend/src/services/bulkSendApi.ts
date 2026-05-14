import { Event, RSVP, EventRequest, LoginRequest, RegisterRequest, AttendanceStats } from '../types';

const API_BASE_URL = 'http://localhost:8280/api';

const api = {
  // Auth endpoints
  async register(data: RegisterRequest): Promise<{ userId: string; email: string; fullName: string }> {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  async login(data: LoginRequest): Promise<{ userId: string; email: string; fullName: string }> {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  async logout(): Promise<void> {
    await fetch(`${API_BASE_URL}/auth/logout`, {
      method: 'POST',
      credentials: 'include',
    });
  },

  async getCurrentUser(): Promise<{ userId: string; email: string; fullName: string } | null> {
    try {
      const response = await fetch(`${API_BASE_URL}/auth/me`, {
        credentials: 'include',
      });
      if (!response.ok) return null;
      return response.json();
    } catch {
      return null;
    }
  },

  // Event endpoints
  async createEvent(data: EventRequest): Promise<Event> {
    const response = await fetch(`${API_BASE_URL}/events`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  async getEvent(eventId: string): Promise<Event> {
    const response = await fetch(`${API_BASE_URL}/events/${eventId}`, {
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  async getMyEvents(): Promise<Event[]> {
    const response = await fetch(`${API_BASE_URL}/events`, {
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  async updateEvent(eventId: string, data: EventRequest): Promise<Event> {
    const response = await fetch(`${API_BASE_URL}/events/${eventId}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(data),
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  async cancelEvent(eventId: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/events/${eventId}/cancel`, {
      method: 'POST',
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
  },

  async closeEvent(eventId: string): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/events/${eventId}/close`, {
      method: 'POST',
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
  },

  // Invitation endpoints
  async sendInvitations(eventId: string, emails: string[]): Promise<void> {
    const response = await fetch(`${API_BASE_URL}/events/${eventId}/invitations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ emails }),
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
  },

  async getInvitations(eventId: string): Promise<any[]> {
    const response = await fetch(`${API_BASE_URL}/events/${eventId}/invitations`, {
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  // RSVP endpoints
  async submitRSVP(token: string, status: 'YES' | 'NO' | 'MAYBE'): Promise<RSVP> {
    const response = await fetch(`${API_BASE_URL}/rsvp/${token}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ status }),
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  async getRSVP(token: string): Promise<RSVP> {
    const response = await fetch(`${API_BASE_URL}/rsvp/${token}`);
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  async getAttendees(eventId: string): Promise<RSVP[]> {
    const response = await fetch(`${API_BASE_URL}/events/${eventId}/attendees`, {
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },

  async getAttendanceStats(eventId: string): Promise<AttendanceStats> {
    const response = await fetch(`${API_BASE_URL}/events/${eventId}/attendees/stats`, {
      credentials: 'include',
    });
    if (!response.ok) throw new Error(await response.text());
    return response.json();
  },
};

export default api;
