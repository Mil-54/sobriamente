import React, { useState, useEffect } from 'react';
import { Quote } from 'lucide-react';

const quotes = [
    "Un día a la vez.",
    "La recuperación es un proceso, no una carrera.",
    "Tu pasado no define tu futuro.",
    "Eres más fuerte de lo que crees.",
    "Cada paso cuenta, por pequeño que sea.",
    "La sobriedad es el regalo que te das a ti mismo.",
    "No estás solo en este camino.",
    "Hoy es una nueva oportunidad para empezar de nuevo.",
    "La paz mental vale más que cualquier placer momentáneo.",
    "Cree en ti mismo y en todo lo que eres."
];

const MotivationSection = () => {
    const [quote, setQuote] = useState("");

    useEffect(() => {
        // Select a random quote on mount
        const randomQuote = quotes[Math.floor(Math.random() * quotes.length)];
        setQuote(randomQuote);
    }, []);

    const getNewQuote = () => {
        let newQuote = quote;
        while (newQuote === quote) {
            newQuote = quotes[Math.floor(Math.random() * quotes.length)];
        }
        setQuote(newQuote);
    };

    return (
        <div className="motivation-section">
            <div className="quote-icon">
                <Quote size={32} />
            </div>
            <blockquote className="quote-text">
                "{quote}"
            </blockquote>
            <button onClick={getNewQuote} className="new-quote-btn">
                Nueva Frase
            </button>
        </div>
    );
};

export default MotivationSection;
