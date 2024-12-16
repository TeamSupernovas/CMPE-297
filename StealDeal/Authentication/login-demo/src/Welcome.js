import React from 'react';

function Welcome({ user }) {
console.log(user);
  return (
    <div>
      <h2>Welcome, {user.username}!</h2>
      <p>Email: {user.email}</p>
      <p>User ID: {user.userID}</p>
    </div>
  );
}

export default Welcome;

