import React, { useEffect, useState } from 'react';
import { Award, Lock, Star } from 'lucide-react';
import { differenceInDays } from 'date-fns';

const milestones = [
    { days: 1, title: 'Primer Paso', description: '24 horas de claridad.' },
    { days: 3, title: 'Desintoxicación', description: 'El cuerpo empieza a sanar.' },
    { days: 7, title: 'Una Semana', description: 'Primera gran victoria.' },
    { days: 14, title: 'Dos Semanas', description: 'Construyendo el hábito.' },
    { days: 30, title: 'Un Mes', description: 'Claridad mental renovada.' },
    { days: 90, title: 'Tres Meses', description: 'Un nuevo estilo de vida.' },
    { days: 365, title: 'Un Año', description: 'Una vuelta al sol sobrio.' },
];

const Achievements = ({ startDate }) => {
    const [daysSober, setDaysSober] = useState(0);

    useEffect(() => {
        if (startDate) {
            const days = differenceInDays(new Date(), new Date(startDate));
            setDaysSober(days);
        }
    }, [startDate]);

    return (
        <div className="achievements-section">
            <div className="section-header">
                <Award size={24} className="section-icon" />
                <h3>Logros</h3>
            </div>

            <div className="medals-grid">
                {milestones.map((milestone) => {
                    const isUnlocked = daysSober >= milestone.days;

                    return (
                        <div
                            key={milestone.days}
                            className={`medal-card ${isUnlocked ? 'unlocked' : 'locked'}`}
                        >
                            <div className="medal-icon">
                                {isUnlocked ? <Star size={24} fill="currentColor" /> : <Lock size={24} />}
                            </div>
                            <div className="medal-info">
                                <h4>{milestone.title}</h4>
                                <p>{milestone.description}</p>
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
};

export default Achievements;
