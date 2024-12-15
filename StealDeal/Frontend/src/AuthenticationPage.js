import React, { useReducer, useState } from 'react';
import axios from 'axios';
import Welcome from './Welcome';
import Register from './register';
import PasswordRecovery from './PasswordRecovery';
import { auth, googleAuthProvider, githubAuthProvider, signInWithPopup } from './firebaseConfig';

import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import "./AuthenticationPage.css"


const initialState = {
  user: null,
  message: '',
  showRegister: false,
  prepopulatedEmail: '',
  email: '',
  password: '',
  twoFactorRequired: false,
  twoFactorCode: '',
};

function reducer(state, action) {
  switch (action.type) {
    case 'SET_USER':
      localStorage.setItem('user', JSON.stringify(action.payload));
      return { ...state, user: action.payload };
    case 'SET_MESSAGE':
      return { ...state, message: action.payload };
    case 'UPDATE_EMAIL':
      return { ...state, email: action.payload };
    case 'UPDATE_PASSWORD':
      return { ...state, password: action.payload };
    case 'PREPARE_FOR_REGISTER':
      return { ...state, prepopulatedEmail: action.payload, showRegister: true };
    case 'SET_2FA_REQUIRED':
      return { ...state, twoFactorRequired: action.payload };
    case 'UPDATE_2FA_CODE':
      return { ...state, twoFactorCode: action.payload };
    case 'SET_LOGIN_METHOD':
      return { ...state, loginMethod: action.payload };
    case 'SET_USER_ID':
      return { ...state, userID: action.payload };  // New reducer case for user ID
    case 'SET_USERNAME':
      return { ...state, username: action.payload }; // New reducer case for username
    default:
      throw new Error();
  }
}


function AuthenticationPage() {
    const navigate = useNavigate();

  const [state, dispatch] = useReducer(reducer, initialState);
  const reduxDispatch = useDispatch(); 
  console.log('Current State:', state);
  const handleOAuthLogin = async (provider, authProvider) => {
    try {
      const result = await signInWithPopup(auth, authProvider);
      const idToken = await result.user.getIdToken();
      console.log(idToken);
      const backendResponse = await axios.post('http://localhost:8098/auth/login', {
        type: provider,
        idToken: idToken,
      });
      console.log("Inside Google auth")
      console.log(backendResponse.data)
      if (backendResponse.data.status === "User not registered") {
        dispatch({ type: 'PREPARE_FOR_REGISTER', payload: backendResponse.data.email });
      } else if (backendResponse.data.status === "2FA required") {
 dispatch({ type: 'SET_2FA_REQUIRED', payload: true });
      dispatch({ type: 'SET_LOGIN_METHOD', payload: provider }); 
      dispatch({ type: 'UPDATE_EMAIL', payload: backendResponse.data.email });
      // Optionally, dispatch other relevant data like userID and username
      dispatch({ type: 'SET_USER_ID', payload: backendResponse.data.userID });
      dispatch({ type: 'SET_USERNAME', payload: backendResponse.data.username });



      } else {
        dispatch({ type: 'SET_USER', payload: backendResponse.data });
      }
    } catch (error) {
      dispatch({ type: 'SET_MESSAGE', payload: `${provider.charAt(0).toUpperCase() + provider.slice(1)} login failed: ` + error.message });
    }
  };

  const handleEmailPasswordLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post('http://localhost:8098/auth/login', {
        type: 'email',
        email: state.email,
        password: state.password,
      });

      if (response.data.status === "2FA required") {
    dispatch({ type: 'SET_2FA_REQUIRED', payload: true });
      dispatch({ type: 'SET_LOGIN_METHOD', payload: 'email' });
      } else {
        dispatch({ type: 'SET_USER', payload: response.data });
      }
    } catch (error) {
      dispatch({ type: 'SET_MESSAGE', payload: 'Login failed: ' });
    }
  };

 const handle2FAVerification = async (e) => {
    e.preventDefault();
    try {
      const route = state.loginMethod === 'email' ? 'validate_2fa_v2' : 'validate_2fa';
      const response = await axios.post(`http://localhost:8098/auth/${route}`, {
        email: state.email,
        token: state.twoFactorCode,
      });

if (response.data.status === "2FA verified") {
    reduxDispatch({ type: 'SET_USER', payload: response.data });
    console.log("Before dispatch")
    console.log(response.data )
    reduxDispatch({ type: 'SET_ROLE', payload: response.data });
  dispatch({ type: 'SET_USER', payload: response.data });
  dispatch({ type: 'SET_2FA_REQUIRED', payload: false });
  dispatch({ type: 'SET_MESSAGE', payload: '2FA verification successful' });
  // Optionally, you can also reset the twoFactorCode state here
  dispatch({ type: 'UPDATE_2FA_CODE', payload: '' });
  navigate('/'); // Redirect to the main page
} else {
        console.log("2FA Verification Failed", response.data);
        dispatch({ type: 'SET_MESSAGE', payload: '2FA verification failed' });
      }
    } catch (error) {
      console.error("2FA Verification Error", error);
      dispatch({ type: 'SET_MESSAGE', payload: '2FA verification failed: ' + error.response?.data?.error || error.message });
    }
  };

const [showPasswordRecovery, setShowPasswordRecovery] = useState(false);

const togglePasswordRecovery = () => {
  setShowPasswordRecovery(!showPasswordRecovery);
};
if (state.user) {
    navigate('/my-products');
  }
  return (
    <div className="auth-wrapper">
      <div className="auth-inner">
        <div className="auth-header">
          <h1>stealDeal</h1>
        </div>

        {state.user ? (
          <div className="redirect-message">Redirecting...</div>
        ) : state.twoFactorRequired ? (
          <div className="auth-form">
            <h2>Two Factor Authentication</h2>
            <form onSubmit={handle2FAVerification}>
              <input
                type="text"
                value={state.twoFactorCode}
                onChange={(e) => dispatch({ type: 'UPDATE_2FA_CODE', payload: e.target.value })}
                placeholder="2FA Code"
                required
                className="auth-input"
              />
              <button type="submit" className="auth-button">Verify</button>
            </form>
          </div>
        ) : state.showRegister ? (
          <Register
            onRegisterSuccess={(userData) => dispatch({ type: 'SET_USER', payload: userData })}
            prepopulatedEmail={state.prepopulatedEmail}
          />
        ) : (
          <div className="auth-form">
            <h2>Login</h2>
            <form onSubmit={handleEmailPasswordLogin}>
              <input
                type="text"
                value={state.email}
                onChange={(e) => dispatch({ type: 'UPDATE_EMAIL', payload: e.target.value })}
                placeholder="Username"
                required
                className="auth-input"
              />
              <input
                type="password"
                value={state.password}
                onChange={(e) => dispatch({ type: 'UPDATE_PASSWORD', payload: e.target.value })}
                placeholder="Password"
                required
                className="auth-input"
              />
              <button type="submit" className="auth-button">Login</button>
            </form>
            <div className="oauth-buttons">
              <button onClick={() => handleOAuthLogin('google', googleAuthProvider)} className="google-login">Login with Google</button>
              <button onClick={() => handleOAuthLogin('github', githubAuthProvider)} className="github-login">Login with GitHub</button>
            </div>
            <p onClick={() => navigate('/password-recovery')} style={{ cursor: 'pointer', color: 'blue', textDecoration: 'underline' }}>
  Forgot Password?
</p>
            {state.message && <p className="auth-message">{state.message}</p>}
            {showPasswordRecovery && <PasswordRecovery />}
          </div>
        )}
      </div>
    </div>
  );


}

export default AuthenticationPage;