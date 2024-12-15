import React, { useEffect } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';

import AuthenticationPage from './AuthenticationPage';
import NavigationBar from './NavigationBar';
import MyProducts from './MyProducts';
import ProductDetails from './ProductDetails';
import AddProduct from './AddProduct'; 
import Recommender from './Recommender';
import Chat from './Chat';

import './App.css';

const App = () => {
  const user = useSelector((state) => state.user);
  const role = useSelector((state) => state.role);
  const dispatch = useDispatch();

  useEffect(() => {
    console.log('Redux User State:', user);
    console.log('Redux Role State:', role);
  }, [user, role]);

  useEffect(() => {
    if (user === -1) {
      // If user is not logged in (i.e., user === -1), automatically navigate to the login page
      // This logic assumes that the user state will be -1 upon logout
      // Use window.location to perform this redirection
      window.location = '/login';
    }
  }, [user, dispatch]);

  return (
    <Router>
      {user !== '' && <NavigationBar />} {/* Conditionally render NavigationBar based on user state */}
      <div className="app-container">
      
      <div className="main-content">
      <Routes>
      <Route path="/add-product" element={<AddProduct />} />
        <Route path="/login" element={<AuthenticationPage />} />
        <Route path="/my-products" element={<MyProducts />} />
        <Route path="/recommender" element={<Recommender />} />
        <Route path="/chatbot" element={<Chat />} />
        <Route path="/" element={<MyProducts />} />
        <Route path="/product-details/:productId" element={<ProductDetails />} />
        {/* ... other routes ... */}
      </Routes>
      </div>
      </div>
    </Router>
  );
};

export default App;
