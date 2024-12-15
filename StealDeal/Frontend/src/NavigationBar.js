import React from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link, useNavigate } from 'react-router-dom';
import "./NavigationBar.css"
const NavigationBar = () => {
  const user = useSelector((state) => state.user);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleLogout = () => {
    dispatch({ type: 'LOGOUT' });
    navigate('/login'); // Navigate to login page after logout
  };

  return (
    <nav>
      <ul>
        {/* Show different navigation items based on user's login status */}
        {user !== '' ? (
          // Navigation items for logged-in users
          <>
            <li>
              <Link to="/my-products">My Products</Link>
            </li>
            <li>
              <Link to="/recommender">Recommendation Bot</Link>
            </li>
            <li>
              <Link to="/chatbot">Chat Bot</Link>
            </li>
            <li>
              <button onClick={handleLogout}>Logout</button>
            </li>
          </>
        ) : (
          // Navigation items for logged-out users
          <>
            <li>
              <Link to="/login">Login</Link>
            </li>
            {/* Include any other links appropriate for logged-out users */}
          </>
        )}
      </ul>
    </nav>
  );
};

export default NavigationBar;