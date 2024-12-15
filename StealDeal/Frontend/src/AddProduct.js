import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import "./AddProduct.css"
const AddProduct = () => {
  const navigate = useNavigate();
  const userId = useSelector(state => state.user.userID); // Replace with your user ID path in the store
  const [storeName, setStoreName] = useState('');
  const [url, setUrl] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();

    const productData = {
      userId,
      storeName,
      url
    };

    try {
        //console.log(productData)
      const response = await fetch('http://localhost:8082/user-products/', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(productData),
      });
      console.log(response)
      if (response.ok) {
        navigate('/my-products'); // Redirect to MyProducts on successful submission
      } else {
        // Handle errors
        console.error('Failed to add product');
      }
    } catch (error) {
      console.error('Error:', error);
    }
  };

  return (
    <div className="add-product-form">
      <h2>Add New Product</h2>
      <form onSubmit={handleSubmit}>

        <label>
          Store Name:
          <select value={storeName} onChange={(e) => setStoreName(e.target.value)}>
            <option value="gap">Gap</option>
            <option value="bananarepublic">Banana Republic</option>
            <option value="oldnavy">Old Navy</option>
            <option value="athleta">Athleta</option>
          </select>
        </label>
        <label>
          URL:
          <input
            type="text"
            value={url}
            onChange={(e) => setUrl(e.target.value)}
            required
          />
        </label>
        <button type="submit">Add Product</button>
      </form>
    </div>
  );
};

export default AddProduct;
