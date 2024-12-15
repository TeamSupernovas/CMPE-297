import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import PriceHistoryGraph from './PriceHistoryGraph'; // Import the PriceHistoryGraph component
import './ProductDetails.css';

const ProductDetails = () => {
  const { productId } = useParams();
  const navigate = useNavigate();
  const [productDetails, setProductDetails] = useState(null);
  const INTERVAL = 30000; 
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  useEffect(() => {
    const fetchProductDetails = () => {
      setLoading(true);
      console.log("fetch products")
      console.log(`http://localhost:8082/user-products/${productId}`)
      fetch(`http://localhost:8082/user-products/${productId}`)
        .then(response => response.json())
        .then(data => {
          console.log(data.productDTO.currentPrice !== 0)
          if (data.productDTO.currentPrice !== 0) {
            setProductDetails(data.productDTO);
            clearInterval(intervalId); // Clear interval if fetch is successful
          }
        })
        .catch(error => {
          console.error('Error fetching product details:', error);
          setError('Failed to fetch product details');
        })
        .finally(() => setLoading(false));
    };

    fetchProductDetails(); // Run immediately on component mount

    const intervalId = setInterval(fetchProductDetails, INTERVAL); // Set up the interval for retries
  
    return () => clearInterval(intervalId); // Cleanup interval on component unmount
  }, [productId]); // Dependency array to trigger useEffect when productId changes


  if (!productDetails) {
    return <div>Hang tight we are fetching your product details...</div>;
  }
  if (loading) return <div>Loading...</div>;
  if (error) return <div>{error}</div>;

 
  return (
    <div className="product-details-container">
<div className="product-info">
<button className="back-button" onClick={() => navigate(-1)}>Back to Products</button>
  {productDetails.name && <h2>{productDetails.name}</h2>}
  
  {productDetails.description && <p><strong>Description:</strong> {productDetails.description}</p>}
  {productDetails.size && <p><strong>Size:</strong> {productDetails.size}</p>}
  {productDetails.color && <p><strong>Color:</strong> {productDetails.color}</p>}
  {productDetails.productImageURL && 
    <img src={productDetails.productImageURL} alt={`Image of ${productDetails.name}`} className="product-image" />}
  {productDetails.commentSummary && <p><strong>Comments:</strong> {productDetails.commentSummary}</p>}
  
  {productDetails.previousPrice > 0 && (
    <p>
      <span className="price-strike">${productDetails.previousPrice}</span>
      <span className="price-current">${productDetails.currentPrice}</span>
    </p>
  )}
  {productDetails.previousPrice === 0 && productDetails.currentPrice && (
    <p><strong>Current Price:</strong> ${productDetails.currentPrice}</p>
  )}

  {/* Add more product details here as needed */}
 
</div>


      <div className="product-graph">
        <PriceHistoryGraph productId={productId} />
      </div>
    </div>
  );
};

export default ProductDetails;
