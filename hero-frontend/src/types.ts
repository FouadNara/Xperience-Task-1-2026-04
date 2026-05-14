export interface User {
  id: string;
  email: string;
  fullName: string;
}

export interface Event {
  id: string;
  title: string;
  description: string;
  dateTime: string;
  location: string;
  maxCapacity: number | null;
  status: 'ACTIVE' | 'CLOSED' | 'CANCELLED';
  rsvpsLocked: boolean;
  createdAt: string;
}

export interface RSVP {
  id: string;
  eventId: string;
  inviteeEmail: string;
  status: 'YES' | 'NO' | 'MAYBE' | 'WAITLISTED';
  positionInWaitlist: number | null;
  updatedAt: string;
}

export interface Invitation {
  id: string;
  eventId: string;
  inviteeEmail: string;
  token: string;
  emailSentAt: string | null;
  emailFailed: boolean;
  createdAt: string;
}

export interface AttendanceStats {
  yesCount: number;
  noCount: number;
  maybeCount: number;
  waitlistedCount: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}

export interface EventRequest {
  title: string;
  description: string;
  dateTime: string;
  location: string;
  maxCapacity: number | null;
}
