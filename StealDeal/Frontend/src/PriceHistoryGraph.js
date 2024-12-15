import React, { useState, useEffect } from 'react';
import { Line } from 'react-chartjs-2';
import { useParams } from 'react-router-dom';
import './PriceHistoryGraph.css'; 
import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
  } from 'chart.js';
  
  ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
  );



const PriceHistoryGraph = () => {
  const { productId } = useParams();
  const [priceHistory, setPriceHistory] = useState([]);

  useEffect(() => {
    fetch(`http://localhost:8082/user-products/${productId}/price-history`)
      .then(response => {
        if (!response.ok) {
          throw new Error('Network response was not ok');
        }
        return response.json();
      })
      .then(data => {
        setPriceHistory(data.priceHistory);
      })
      .catch(error => {
        console.error('Error fetching price history:', error);
        //setError('Failed to fetch price history');
      });
  }, [productId]);
  
   
  const options = {
    scales: {
      y: {
        title: {
          display: true,
          text: 'Price'
        }
      },
      x: {
        title: {
          display: true,
          text: 'Time'
        }
      }
    }
   }
  // Prepare data for the chart
  const chartData = {
    labels: priceHistory.map(item => item.priceRecordTime),
    datasets: [
      {
        label: 'Price Over Time',
        data: priceHistory.map(item => item.price),
        fill: false,
        backgroundColor: 'rgb(75, 192, 192)',
        borderColor: 'rgba(75, 192, 192, 0.2)',
      },
    ],
        
  };

  return (
    <div className="price-history-graph">
    <Line data={chartData} options={options}/>
  </div>
  );
};

export default PriceHistoryGraph;
