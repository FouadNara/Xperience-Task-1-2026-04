import { Link } from 'react-router-dom';
import { User } from '../types';

interface NavigationProps {
  user: User;
  onLogout: () => void;
}

export default function Navigation({ user, onLogout }: NavigationProps) {
  return (
    <nav className="navbar">
      <div className="nav-container">
        <Link to="/" className="nav-logo">
          🎉 Event RSVP Manager
        </Link>
        <div className="nav-menu">
          <span className="user-info">Welcome, {user.fullName}</span>
          <button onClick={onLogout} className="logout-btn">
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}
