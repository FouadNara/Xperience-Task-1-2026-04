import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import './App.css';
import api from './services/bulkSendApi';
import { User } from './types';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';
import Dashboard from './components/Dashboard';
import EventPage from './components/EventPage';
import RSVPPage from './components/RSVPPage';
import Navigation from './components/Navigation';

function App() {
  const [currentUser, setCurrentUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    checkCurrentUser();
  }, []);

  const checkCurrentUser = async () => {
    try {
      const user = await api.getCurrentUser();
      if (user) {
        setCurrentUser(user as any);
      }
    } catch (error) {
      console.error('Failed to check current user:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = (user: User) => {
    setCurrentUser(user);
  };

  const handleLogout = async () => {
    try {
      await api.logout();
      setCurrentUser(null);
    } catch (error) {
      console.error('Failed to logout:', error);
    }
  };

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Router>
      <div className="app">
        {currentUser && <Navigation user={currentUser} onLogout={handleLogout} />}
        <main className="main-content">
          <Routes>
            <Route
              path="/login"
              element={currentUser ? <Navigate to="/" /> : <LoginPage onLogin={handleLogin} />}
            />
            <Route
              path="/register"
              element={currentUser ? <Navigate to="/" /> : <RegisterPage onRegister={handleLogin} />}
            />
            <Route
              path="/rsvp/:token"
              element={<RSVPPage />}
            />
            <Route
              path="/event/:id"
              element={currentUser ? <EventPage /> : <Navigate to="/login" />}
            />
            <Route
              path="/"
              element={currentUser ? <Dashboard /> : <Navigate to="/login" />}
            />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
