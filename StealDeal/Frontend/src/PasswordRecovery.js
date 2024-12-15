import React, { useState } from 'react';
import axios from 'axios';
import "./PasswordRecovery.css"
import { useNavigate } from 'react-router-dom';

function PasswordRecovery() {
  const [email, setEmail] = useState('');
  const [twoFactorCode, setTwoFactorCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [isVerified, setIsVerified] = useState(false);
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleVerify = async () => {
    try {
      const response = await axios.post('http://localhost:8098/auth/request_password_recovery', { email, token: twoFactorCode });
      if (response.status === 200) {
        setIsVerified(true);
        setMessage('Verification successful. Please enter your new password.');
      } else {
        setMessage('Verification failed.');
      }
    } catch (error) {
      setMessage('Verification failed: ' + error.response.data.error);
    }
  };

  const handleResetPassword = async () => {
    try {
      const response = await axios.put('http://localhost:8098/auth/reset_password', { email, newPassword });
      if (response.status === 200) {
        setMessage('Password reset successfully.');
        // Resetting the state to initial form after successful password reset
        setIsVerified(false);
        setEmail('');
        setTwoFactorCode('');
        setNewPassword('');
        navigate('/login');
      } else {
        setMessage('Password reset failed.');
      }
    } catch (error) {
      setMessage('Password reset failed: ' + error.response.data.error);
    }
  };

  // Inside your Password Recovery component
return (
  <div className="password-recovery-container">
    {!isVerified ? (
      <div className="password-recovery-form">
        <h2>Password Recovery</h2>
        <input 
          type="email" 
          value={email} 
          onChange={(e) => setEmail(e.target.value)} 
          placeholder="Username" 
          className="recovery-input"
        />
        <input 
          type="text" 
          value={twoFactorCode} 
          onChange={(e) => setTwoFactorCode(e.target.value)} 
          placeholder="2FA Code" 
          className="recovery-input"
        />
        <button onClick={handleVerify} className="recovery-button">Verify</button>
      </div>
    ) : (
      <div className="password-reset-form">
        <h2>Reset Password</h2>
        <input 
          type="password" 
          value={newPassword} 
          onChange={(e) => setNewPassword(e.target.value)} 
          placeholder="New Password" 
          className="recovery-input"
        />
        <button onClick={handleResetPassword} className="recovery-button">Reset Password</button>
      </div>
    )}
    {message && <p className="recovery-message">{message}</p>}
  </div>
);

}

export default PasswordRecovery;


