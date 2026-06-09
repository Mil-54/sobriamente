import React, { useState, useEffect } from 'react';
import { differenceInDays, differenceInHours, differenceInMinutes, differenceInSeconds } from 'date-fns';
import { Clock, RefreshCw } from 'lucide-react';

const SobrietyCounter = ({ startDate, onReset }) => {
    const [timeElapsed, setTimeElapsed] = useState({
        days: 0,
        hours: 0,
        minutes: 0,
        seconds: 0
    });

    useEffect(() => {
        if (!startDate) return;

        const interval = setInterval(() => {
            const now = new Date();
            const start = new Date(startDate);

            const days = differenceInDays(now, start);
            const hours = differenceInHours(now, start) % 24;
            const minutes = differenceInMinutes(now, start) % 60;
            const seconds = differenceInSeconds(now, start) % 60;

            setTimeElapsed({ days, hours, minutes, seconds });
        }, 1000);

        return () => clearInterval(interval);
    }, [startDate]);

    if (!startDate) {
        return (
            <div className="counter-placeholder">
                <p>Selecciona tu fecha de inicio para comenzar.</p>
            </div>
        );
    }

    return (
        <div className="sobriety-counter">
            <div className="counter-header">
                <Clock size={24} />
                <h2>Tiempo Sobrio</h2>
            </div>

            <div className="counter-display">
                <div className="time-unit">
                    <span className="number">{timeElapsed.days}</span>
                    <span className="label">Días</span>
                </div>
                <div className="separator">:</div>
                <div className="time-unit">
                    <span className="number">{timeElapsed.hours}</span>
                    <span className="label">Hrs</span>
                </div>
                <div className="separator">:</div>
                <div className="time-unit">
                    <span className="number">{timeElapsed.minutes}</span>
                    <span className="label">Min</span>
                </div>
                <div className="separator">:</div>
                <div className="time-unit">
                    <span className="number">{timeElapsed.seconds}</span>
                    <span className="label">Seg</span>
                </div>
            </div>

            <button onClick={onReset} className="reset-button">
                <RefreshCw size={16} />
                <span>Reiniciar Contador</span>
            </button>
        </div>
    );
};

export default SobrietyCounter;
