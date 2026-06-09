import React, { useState } from 'react';
import { Settings as SettingsIcon, LogOut, Calendar, Save, Key, ExternalLink } from 'lucide-react';
import { updateDoc, doc } from 'firebase/firestore';
import { db, auth } from '../firebase';
import { signOut } from 'firebase/auth';

const Settings = ({ user, startDate, onUpdateDate }) => {
    const [dateInput, setDateInput] = useState('');
    const [geminiKey, setGeminiKey] = useState(localStorage.getItem('geminiApiKey') || '');
    const [keySaved, setKeySaved] = useState(false);

    const handleSave = async () => {
        if (!dateInput) return;
        try {
            const dateObj = new Date(dateInput);
            await updateDoc(doc(db, "users", user.uid), {
                startDate: dateObj.toISOString()
            });
            onUpdateDate(dateObj);
            alert("Fecha actualizada correctamente.");
        } catch (e) {
            console.error(e);
            alert("Error al guardar la fecha.");
        }
    };

    const handleSaveApiKey = () => {
        localStorage.setItem('geminiApiKey', geminiKey.trim());
        setKeySaved(true);
        setTimeout(() => setKeySaved(false), 2500);
    };

    const handleLogout = async () => {
        if (confirm("¿Cerrar sesión?")) {
            await signOut(auth);
            window.location.href = "/";
        }
    };

    return (
        <div className="settings-view fade-in">
            <div className="settings-header">
                <SettingsIcon size={32} className="settings-icon" />
                <h2>Ajustes</h2>
            </div>

            <div className="settings-section">
                <h3><Calendar size={20} /> Editar Fecha de Inicio</h3>
                <p>Si la fecha mostrada no es correcta, puedes ajustarla aquí.</p>
                <div className="date-input-group">
                    <input
                        type="datetime-local"
                        value={dateInput}
                        onChange={(e) => setDateInput(e.target.value)}
                        className="date-input"
                    />
                    <button onClick={handleSave} className="save-btn">
                        <Save size={18} />
                        Guardar
                    </button>
                </div>
            </div>

            <div className="settings-section">
                <h3><Key size={20} /> API Key de Gemini (IA)</h3>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
                    Necesaria para el análisis de fotos del Check-In Diario.
                    Obtén tu clave gratuita en{' '}
                    <a
                        href="https://aistudio.google.com/app/apikey"
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{ color: 'var(--primary-color)', display: 'inline-flex', alignItems: 'center', gap: '0.2rem' }}
                    >
                        Google AI Studio <ExternalLink size={12} />
                    </a>
                </p>
                <div className="date-input-group">
                    <input
                        type="password"
                        value={geminiKey}
                        onChange={(e) => setGeminiKey(e.target.value)}
                        placeholder="AIza..."
                        className="date-input"
                        style={{ fontFamily: 'monospace', letterSpacing: '0.05em' }}
                    />
                    <button
                        onClick={handleSaveApiKey}
                        className="save-btn"
                        style={keySaved ? { background: '#22c55e' } : {}}
                    >
                        <Save size={18} />
                        {keySaved ? '¡Guardado!' : 'Guardar Clave'}
                    </button>
                </div>
                {geminiKey && (
                    <p style={{ fontSize: '0.75rem', color: '#22c55e', marginTop: '0.5rem' }}>
                        ✓ API Key configurada
                    </p>
                )}
            </div>

            <div className="settings-section">
                <h3>Cuenta</h3>
                <p>Usuario: {user.isAnonymous ? 'Anónimo' : user.email}</p>
                <button onClick={handleLogout} className="logout-btn">
                    <LogOut size={18} />
                    Cerrar Sesión
                </button>
            </div>
        </div>
    );
};

export default Settings;
