import React, { useState, useEffect, useRef } from 'react';
import { IoChatbubblesOutline, IoCloseOutline, IoSend } from 'react-icons/io5';
import api from '../services/api';
import './Chatbot.css';

const Chatbot = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [messages, setMessages] = useState([
        { id: 1, text: "Hello! I'm your HealthMate Assistant. How can I help you today?", sender: 'bot' }
    ]);
    const [inputValue, setInputValue] = useState('');
    const [isTyping, setIsTyping] = useState(false);
    const messagesEndRef = useRef(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages, isTyping]);

    const handleSend = async () => {
        if (!inputValue.trim()) return;

        const userMessage = {
            id: Date.now(),
            text: inputValue,
            sender: 'user'
        };

        setMessages(prev => [...prev, userMessage]);
        setInputValue('');
        setIsTyping(true);

        try {
            const response = await api.post('/chat', {
                message: inputValue
            });

            const botMessage = {
                id: Date.now() + 1,
                text: response.data.reply,
                sender: 'bot'
            };

            setMessages(prev => [...prev, botMessage]);
        } catch (error) {
            console.error("Chat Error:", error);
            const errorMessage = {
                id: Date.now() + 1,
                text: "I'm having trouble connecting right now. Please try again later!",
                sender: 'bot'
            };
            setMessages(prev => [...prev, errorMessage]);
        } finally {
            setIsTyping(false);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleSend();
        }
    };

    return (
        <div className="chatbot-container">
            {!isOpen && (
                <button className="chatbot-toggle" onClick={() => setIsOpen(true)}>
                    <IoChatbubblesOutline />
                </button>
            )}

            {isOpen && (
                <div className="chatbot-window">
                    <div className="chatbot-header">
                        <h3><IoChatbubblesOutline /> HealthMate AI</h3>
                        <button className="chatbot-close" onClick={() => setIsOpen(false)}>
                            <IoCloseOutline />
                        </button>
                    </div>

                    <div className="chatbot-messages">
                        {messages.map((msg) => (
                            <div key={msg.id} className={`message ${msg.sender}`}>
                                {msg.sender === 'bot'
                                    ? msg.text.split(/(\*\*.*?\*\*)/).map((part, i) =>
                                        part.startsWith('**') && part.endsWith('**')
                                            ? <strong key={i}>{part.slice(2, -2)}</strong>
                                            : part
                                    )
                                    : msg.text
                                }
                            </div>
                        ))}
                        {isTyping && (
                            <div className="typing-indicator">
                                <div className="typing-dot"></div>
                                <div className="typing-dot"></div>
                                <div className="typing-dot"></div>
                            </div>
                        )}
                        <div ref={messagesEndRef} />
                    </div>

                    <div className="chatbot-input">
                        <input
                            type="text"
                            placeholder="Ask me anything..."
                            value={inputValue}
                            onChange={(e) => setInputValue(e.target.value)}
                            onKeyPress={handleKeyPress}
                        />
                        <button className="chatbot-send" onClick={handleSend}>
                            <IoSend />
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Chatbot;
