import React, { useState, useEffect, useRef } from 'react';
import { useSelector } from 'react-redux';

const Chat = () => {
  const userId = useSelector((state) => state.user.userID);
  const [users, setUsers] = useState([]);
  const [products, setProducts] = useState([]);
  const [chat, setChat] = useState([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);
  const TOKEN="OPEN AI TOKEN"

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [chat]);

  // Fetch all users
  useEffect(() => {
    fetch(`http://localhost:8088/users`)
      .then((response) => {
        if (!response.ok) {
          throw new Error('Network Error');
        }
        return response.json();
      })
      .then((data) => {
        console.log(data);
        setUsers(data || []);
      })
      .catch((error) => {
        console.error('Error fetching users:', error);
      });
  }, []);

  // Fetch products for all users
  useEffect(() => {
    const fetchAllProducts = async () => {
      try {
        const allProducts = [];
        for (const user of users) {
          const response = await fetch(`http://localhost:8082/user-products/user/${user.ID}?page=0&size=100&sort=createdAt`);
          if (!response.ok) {
            throw new Error(`Failed to fetch products for user ${user.Username}`);
          }
          const data = await response.json();
          allProducts.push(...(data.productDTOs || []));
        }
        console.log(allProducts);
        setProducts(allProducts);
      } catch (error) {
        console.error('Error fetching products:', error);
      }
    };

    if (users.length > 0) {
      fetchAllProducts();
    }
  }, [users]);

  const handleSend = async () => {
    if (input.trim() === '') return;

    const userMessage = { sender: 'user', text: input };
    setChat((prevChat) => [...prevChat, userMessage]);
    setIsLoading(true);

    try {
      const response = await fetch('https://api.openai.com/v1/chat/completions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: TOKEN,
         },
        body: JSON.stringify({
          model: 'gpt-4',
          messages: [
            { role: 'system', content: 'You are a helpful assistant providing answers for the products given.' },
            {
              role: 'user',
              content: `Here are the products: ${JSON.stringify(products)}. Based on these, answer the query: "${input}".`,
            },
          ],
        }),
      });

      const data = await response.json();

      if (response.ok) {
        const botResponse = { sender: 'bot', text: data.choices[0].message.content };
        setChat((prevChat) => [...prevChat, botResponse]);
      } else {
        throw new Error(data.error.message || 'Failed to fetch recommendations');
      }
    } catch (error) {
      console.error('Error calling OpenAI API:', error);
      const botResponse = { sender: 'bot', text: 'Sorry, there was an error processing your request.' };
      setChat((prevChat) => [...prevChat, botResponse]);
    } finally {
      setIsLoading(false);
      setInput('');
    }
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="chat-page">
      <div className="chat-container">
        <div className="chat-header">
          <h1>Products Chat Bot</h1>
        </div>
        
        <div className="messages-container">
          <div className="messages-wrapper">
            {chat.map((message, index) => (
              <div
                key={index}
                className={`message ${message.sender === 'user' ? 'user-message' : 'bot-message'}`}
              >
                <div className="message-content">
                  {message.text}
                </div>
              </div>
            ))}
            {isLoading && (
              <div className="message bot-message">
                <div className="message-content loading">
                  <div className="typing-indicator">
                    <span></span>
                    <span></span>
                    <span></span>
                  </div>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>
        </div>

        <div className="input-container">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Ask for recommendations..."
            disabled={isLoading}
          />
          <button 
            onClick={handleSend}
            disabled={isLoading || !input.trim()}
            className={isLoading ? 'loading' : ''}
          >
            <svg 
              viewBox="0 0 24 24" 
              fill="none" 
              stroke="currentColor" 
              strokeWidth="2" 
              strokeLinecap="round" 
              strokeLinejoin="round"
            >
              <path d="M22 2L11 13M22 2l-7 20-4-9-9-4 20-7z" />
            </svg>
          </button>
        </div>
      </div>

      <style jsx>{`
        .chat-page {
          height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #6e8efb, #a777e3);
          padding: 20px;
          font-family: system-ui, -apple-system, sans-serif;
          position: fixed;
          width: 100%;
          top: 0;
          left: 0;
          overflow: hidden;
        }

        .chat-container {
          width: 100%;
          max-width: 800px;
          height: 80vh;
          background: white;
          border-radius: 16px;
          box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
          display: flex;
          flex-direction: column;
          overflow: hidden;
        }

        .chat-header {
          padding: 20px;
          background: white;
          border-bottom: 1px solid #eaeaea;
          text-align: center;
          flex-shrink: 0;
        }

        .chat-header h1 {
          margin: 0;
          font-size: 1.5rem;
          color: #333;
        }

        .messages-container {
          flex: 1;
          position: relative;
          background: white;
        }

        .messages-wrapper {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          overflow-y: auto;
          padding: 20px;
          display: flex;
          flex-direction: column;
          gap: 12px;
        }

        .messages-wrapper::-webkit-scrollbar {
          width: 8px;
        }

        .messages-wrapper::-webkit-scrollbar-track {
          background: #f1f1f1;
          border-radius: 4px;
        }

        .messages-wrapper::-webkit-scrollbar-thumb {
          background: #c1c1c1;
          border-radius: 4px;
        }

        .messages-wrapper::-webkit-scrollbar-thumb:hover {
          background: #a8a8a8;
        }

        .message {
          display: flex;
          align-items: flex-end;
          margin-bottom: 10px;
        }

        .user-message {
          justify-content: flex-end;
        }

        .message-content {
          max-width: 70%;
          padding: 12px 16px;
          border-radius: 16px;
          font-size: 0.95rem;
          line-height: 1.4;
        }

        .user-message .message-content {
          background: #6e8efb;
          color: white;
          border-bottom-right-radius: 4px;
        }

        .bot-message .message-content {
          background: #f0f2f5;
          color: #333;
          border-bottom-left-radius: 4px;
        }

        .input-container {
          padding: 20px;
          background: white;
          border-top: 1px solid #eaeaea;
          display: flex;
          gap: 10px;
          flex-shrink: 0;
        }

        input {
          flex: 1;
          padding: 12px 16px;
          border: 1px solid #eaeaea;
          border-radius: 24px;
          font-size: 0.95rem;
          transition: border-color 0.2s;
        }

        input:focus {
          outline: none;
          border-color: #6e8efb;
        }

        input:disabled {
          background: #f5f5f5;
          cursor: not-allowed;
        }

        button {
          width: 44px;
          height: 44px;
          border-radius: 22px;
          border: none;
          background: #6e8efb;
          color: white;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: background 0.2s;
        }

        button:hover {
          background: #5b7df6;
        }

        button:disabled {
          background: #ccc;
          cursor: not-allowed;
        }

        button svg {
          width: 20px;
          height: 20px;
        }

        .typing-indicator {
          display: flex;
          gap: 4px;
        }

        .typing-indicator span {
          width: 8px;
          height: 8px;
          background: #999;
          border-radius: 50%;
          animation: typing 1s infinite ease-in-out;
        }

        .typing-indicator span:nth-child(1) { animation-delay: 0.2s; }
        .typing-indicator span:nth-child(2) { animation-delay: 0.3s; }
        .typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

        @keyframes typing {
          0%, 100% { transform: translateY(0); }
          50% { transform: translateY(-5px); }
        }

        @media (max-width: 600px) {
          .chat-page {
            padding: 0;
          }
          
          .chat-container {
            height: 100vh;
            border-radius: 0;
          }

          .message-content {
            max-width: 85%;
          }
        }
      `}</style>
    </div>
  );
};

export default Chat;