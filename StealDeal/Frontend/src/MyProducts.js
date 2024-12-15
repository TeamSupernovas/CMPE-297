import React, { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import './MyProducts.css'; // Ensure your CSS file is linked
import { Link } from 'react-router-dom';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faPlus } from '@fortawesome/free-solid-svg-icons';
const getRandomColor = () => {
  const letters = '0123456789ABCDEF';
  let color = '#';
  for (let i = 0; i < 6; i++) {
    color += letters[Math.floor(Math.random() * 16)];
  }
  return color;
};
const predefinedColors = [
  "linear-gradient(to left, #667eea, #764ba2)",
  // Add more colors as needed
];
const getColorForProduct = (index) => {
  return predefinedColors[index % predefinedColors.length];
};
const MyProducts = () => {
  const userId = useSelector((state) => state.user.userID);
  const [products, setProducts] = useState([]);



  useEffect(() => {
    console.log(userId)
    //console.log(`http://localhost:8082/user-products/${userId}?page=0&size=100&sort=createdAt`)
    fetch(`http://localhost:8082/user-products/user/${userId}?page=0&size=100&sort=createdAt`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        console.log(response);
        return response.json();
      })
      .then(data => {
        setProducts(data.productDTOs);
      })
      .catch(error => {
        console.error('Error fetching products:', error);
      });
  }, [userId]);
  

  // Render product cards based on the available data
  return (
    <div className="products-container">
     <Link 
  to="/add-product" 
  className="product-card" 
  style={{ 
    background: "linear-gradient(to left, #667eea, #764ba2)" 
  }}
>
    <FontAwesomeIcon icon={faPlus} size="5x" /> {/* Font Awesome Plus Icon */}
</Link>
      {products.map((product) => (
        console.log(product),
        <Link to={`/product-details/${product.productId}`} key={product.productID} className="product-card" style={{ background:  getColorForProduct(product.productId)  }}>
          <h3>{product.name || product.productID}</h3>
        </Link>
      ))}
    </div>
  );
};

export default MyProducts;
