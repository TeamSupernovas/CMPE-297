import React, { useReducer, useState } from 'react';
import axios from 'axios';
import Welcome from './Welcome';
import Register from './register';
import PasswordRecovery from './PasswordRecovery';
import { auth, googleAuthProvider, githubAuthProvider, signInWithPopup } from './firebaseConfig';

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


function App() {

  const [state, dispatch] = useReducer(reducer, initialState);
  console.log('Current State:', state);
  const handleOAuthLogin = async (provider, authProvider) => {
    try {
      const result = await signInWithPopup(auth, authProvider);
      const idToken = await result.user.getIdToken();

      const backendResponse = await axios.post('http://localhost:8098/login', {
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
      const response = await axios.post('http://localhost:8098/login', {
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
      dispatch({ type: 'SET_MESSAGE', payload: 'Login failed: ' + error.response.data.error });
    }
  };

 const handle2FAVerification = async (e) => {
    e.preventDefault();
    try {
      const route = state.loginMethod === 'email' ? 'validate_2fa_v2' : 'validate_2fa';
      const response = await axios.post(`http://localhost:8098/${route}`, {
        email: state.email,
        token: state.twoFactorCode,
      });

if (response.data.status === "2FA verified") {
  console.log("2FA Verification Successful", response.data);
  dispatch({ type: 'SET_USER', payload: response.data });
  dispatch({ type: 'SET_2FA_REQUIRED', payload: false });
  dispatch({ type: 'SET_MESSAGE', payload: '2FA verification successful' });
  // Optionally, you can also reset the twoFactorCode state here
  dispatch({ type: 'UPDATE_2FA_CODE', payload: '' });
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

return (
  <div>
    {state.user ? (
      // When the user is set, show the Welcome page
      <Welcome user={state.user} />
    ) : state.twoFactorRequired ? (
      // When 2FA is required, show the 2FA verification form
      <div>
        <h2>Two Factor Authentication</h2>
        <form onSubmit={handle2FAVerification}>
          <input
            type="text"
            value={state.twoFactorCode}
            onChange={(e) => dispatch({ type: 'UPDATE_2FA_CODE', payload: e.target.value })}
            placeholder="2FA Code"
            required
          />
          <button type="submit">Verify</button>
        </form>
      </div>
    ) : state.showRegister ? (
      // When showing the registration form
      <Register
        onRegisterSuccess={(userData) => dispatch({ type: 'SET_USER', payload: userData })}
        prepopulatedEmail={state.prepopulatedEmail}
      />
    ) : (
      // Default case, show the login form
      <>
        <h2>Login</h2>
        <form onSubmit={handleEmailPasswordLogin}>
          <input
            type="text"
            value={state.email}
            onChange={(e) => dispatch({ type: 'UPDATE_EMAIL', payload: e.target.value })}
            placeholder="Username"
            required
          />
          <input
            type="password"
            value={state.password}
            onChange={(e) => dispatch({ type: 'UPDATE_PASSWORD', payload: e.target.value })}
            placeholder="Password"
            required
          />
          <button type="submit">Login</button>
        </form>
        <button onClick={() => handleOAuthLogin('google', googleAuthProvider)}>Login with Google</button>
        <button onClick={() => handleOAuthLogin('github', githubAuthProvider)}>Login with GitHub</button>
        <p onClick={togglePasswordRecovery} style={{ cursor: 'pointer', color: 'blue', textDecoration: 'underline' }}>
          Forgot Password?
        </p>
        {state.message && <p>{state.message}</p>}
        {showPasswordRecovery && <PasswordRecovery />}
      </>
    )}
  </div>
);


}

export default App;

