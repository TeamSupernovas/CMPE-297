import React, { useState } from 'react';
import axios from 'axios';

function TwoFactorAuthentication({ on2FASuccess, userEmail }) {
  const [twoFactorCode, setTwoFactorCode] = useState('');
  const [message, setMessage] = useState('');

  const handleVerify2FA = async () => {
    try {
      const verifyResponse = await axios.post('http://localhost:8098/validate_2fa', {
        email: userEmail,
        token: twoFactorCode
      });
      if (verifyResponse.status === 200) {
        on2FASuccess(verifyResponse.data);
      } else {
        setMessage('2FA verification failed');
      }
    } catch (error) {
      setMessage('2FA verification failed: ' + error.response.data.error);
    }
  };

  return (
    <div>
      <h3>Two-Factor Authentication</h3>
      <p>Please enter the 2FA code from your authentication app:</p>
      <input
        type="text"
        value={twoFactorCode}
        onChange={(e) => setTwoFactorCode(e.target.value)}
        placeholder="Enter 2FA Code"
        required
      />
      <button onClick={handleVerify2FA}>Verify</button>
      {message && <p>{message}</p>}
    </div>
  );
}

export default TwoFactorAuthentication;

