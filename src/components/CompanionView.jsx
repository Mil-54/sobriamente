import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { doc, getDoc, addDoc, collection } from 'firebase/firestore';
import { db } from '../firebase';
import SobrietyCounter from './SobrietyCounter';
import Achievements from './Achievements';
import { Heart, Home, Send, ThumbsUp } from 'lucide-react';

const CompanionView = () => {
    const { userId } = useParams();
    const [startDate, setStartDate] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [supportSent, setSupportSent] = useState(false);

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const docRef = doc(db, "users", userId);
                const docSnap = await getDoc(docRef);

                if (docSnap.exists()) {
                    setStartDate(new Date(docSnap.data().startDate));
                } else {
                    setError("Usuario no encontrado o no ha iniciado su viaje aún.");
                }
            } catch (err) {
                console.error(err);
                setError("Error al cargar los datos.");
            } finally {
                setLoading(false);
            }
        };

        fetchUserData();
    }, [userId]);

    const handleSendSupport = async () => {
        setSupportSent(true);
        try {
            await addDoc(collection(db, "users", userId, "notifications"), {
                type: 'support',
                message: '¡Alguien te ha enviado apoyo!',
                timestamp: new Date().toISOString(),
                read: false
            });
        } catch (e) {
            console.error("Error sending support", e);
        }
        setTimeout(() => setSupportSent(false), 3000);
    };

    if (loading) return <div className="loading">Cargando progreso...</div>;
    if (error) return <div className="error-message">{error}</div>;

    return (
        <div className="companion-view fade-in">
            <div className="companion-header">
                <Heart size={48} className="heart-icon" />
                <h2>Acompañando a un Ser Querido</h2>
                <p>Estás viendo el progreso de alguien especial. ¡Tu apoyo es fundamental!</p>
            </div>

            <div className="actions-row">
                <button
                    className={`support-btn ${supportSent ? 'sent' : ''}`}
                    onClick={handleSendSupport}
                    disabled={supportSent}
                >
                    {supportSent ? (
                        <>
                            <ThumbsUp size={20} />
                            <span>¡Apoyo Enviado!</span>
                        </>
                    ) : (
                        <>
                            <Send size={20} />
                            <span>Enviar Ánimo</span>
                        </>
                    )}
                </button>
            </div>

            <div className="dashboard companion-dashboard">
                <div className="read-only-notice">
                    <p>Modo Lectura</p>
                </div>
                <SobrietyCounter startDate={startDate} onReset={() => { }} />
                <Achievements startDate={startDate} />
            </div>

            <Link to="/" className="back-home-btn">
                <Home size={20} />
                <span>Ir al Inicio</span>
            </Link>
        </div>
    );
};

export default CompanionView;
