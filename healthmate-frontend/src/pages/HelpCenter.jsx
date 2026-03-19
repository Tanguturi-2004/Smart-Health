import React, { useState } from 'react';
import './HelpCenter.css';

const FAQ_DATA = [
    {
        question: "How do I track my workouts?",
        answer: "Navigate to the 'Workouts' section from the sidebar. You can log individual exercises, sets, and reps. Your progress will be automatically reflected in the Dashboard charts."
    },
    {
        question: "Can I set custom calorie goals?",
        answer: "Yes! Go to the 'Goal Center'. You can define your daily calorie targets, macro ratios, and weight goals. We'll provide recommendations based on your BMI."
    },
    {
        question: "How is my BMI calculated?",
        answer: "We use your height and weight from your profile to calculate BMI using the standard formula. You can update these anytime in the 'Vitals Hub' or 'Profile' page."
    },
    {
        question: "What is the Meal Studio?",
        answer: "Meal Studio is where you log your daily intake. You can search for foods or add custom meals. It tracks your progress against your daily goals in real-time."
    },
    {
        question: "How do I change my theme?",
        answer: "You can toggle between Dark and Light mode using the sun/moon icon at the bottom of the sidebar."
    }
];



const HelpCenter = () => {
    const [activeFaq, setActiveFaq] = useState(null);

    return (
        <div className="help-container">
            <header className="help-header">
                <h1>Help & Support</h1>
                <p>Your companion for a healthier lifestyle. How can we assist you today?</p>
            </header>

            <div className="help-content-wrapper">
                {/* FAQ Section */}
                <section className="faq-section">
                    <div className="section-card">
                        <h2 className="section-title">Common Questions</h2>
                        <div className="faq-list">
                            {FAQ_DATA.map((faq, index) => (
                                <div
                                    key={index}
                                    className={`faq-item ${activeFaq === index ? 'active' : ''}`}
                                    onClick={() => setActiveFaq(activeFaq === index ? null : index)}
                                >
                                    <div className="faq-question">
                                        <span>{faq.question}</span>
                                        <span className="faq-icon">{activeFaq === index ? '−' : '+'}</span>
                                    </div>
                                    {activeFaq === index && (
                                        <div className="faq-answer show">
                                            {faq.answer}
                                        </div>
                                    )}
                                </div>
                            ))}
                        </div>
                    </div>
                </section>
            </div>
        </div>
    );
};

export default HelpCenter;
