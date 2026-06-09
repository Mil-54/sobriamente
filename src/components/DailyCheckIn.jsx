import React, { useState, useRef, useEffect } from 'react';
import { Camera, Upload, Loader, CheckCircle, AlertTriangle, XCircle, Clock, ChevronDown, ChevronUp, Sparkles } from 'lucide-react';
import { db } from '../firebase';
import { collection, addDoc, query, orderBy, limit, getDocs, Timestamp } from 'firebase/firestore';

const RESULT_CONFIG = {
  safe: {
    label: 'Sin señales visibles de consumo',
    color: '#22c55e',
    bg: 'rgba(34, 197, 94, 0.1)',
    border: 'rgba(34, 197, 94, 0.3)',
    icon: CheckCircle,
    emoji: '🟢',
  },
  caution: {
    label: 'Algunos indicadores observados',
    color: '#f59e0b',
    bg: 'rgba(245, 158, 11, 0.1)',
    border: 'rgba(245, 158, 11, 0.3)',
    icon: AlertTriangle,
    emoji: '🟡',
  },
  risk: {
    label: 'Posibles señales detectadas',
    color: '#ef4444',
    bg: 'rgba(239, 68, 68, 0.1)',
    border: 'rgba(239, 68, 68, 0.3)',
    icon: XCircle,
    emoji: '🔴',
  },
};

function formatDate(timestamp) {
  if (!timestamp) return '';
  const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp);
  return date.toLocaleDateString('es-MX', { weekday: 'short', day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' });
}

function getTodayStart() {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  return d;
}

const DailyCheckIn = ({ userId }) => {
  const [imageFile, setImageFile] = useState(null);
  const [imagePreview, setImagePreview] = useState(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [result, setResult] = useState(null);
  const [details, setDetails] = useState('');
  const [history, setHistory] = useState([]);
  const [hasCheckedInToday, setHasCheckedInToday] = useState(false);
  const [showHistory, setShowHistory] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(true);
  const [error, setError] = useState('');
  const fileInputRef = useRef(null);
  const cameraInputRef = useRef(null);

  useEffect(() => {
    if (userId) loadHistory();
  }, [userId]);

  const loadHistory = async () => {
    setLoadingHistory(true);
    try {
      const todayStart = getTodayStart();
      const checkinsRef = collection(db, 'users', userId, 'checkins');
      const q = query(checkinsRef, orderBy('createdAt', 'desc'), limit(7));
      const snapshot = await getDocs(q);
      const items = snapshot.docs.map(d => ({ id: d.id, ...d.data() }));
      setHistory(items);

      // Check if already checked in today
      const todayCheckin = items.find(item => {
        const itemDate = item.createdAt?.toDate ? item.createdAt.toDate() : new Date(item.createdAt);
        return itemDate >= todayStart;
      });
      if (todayCheckin) {
        setHasCheckedInToday(true);
        setResult(todayCheckin.result);
        setDetails(todayCheckin.details || '');
      }
    } catch (e) {
      console.error('Error loading checkins:', e);
    } finally {
      setLoadingHistory(false);
    }
  };

  const handleImageSelect = (e) => {
    const file = e.target.files[0];
    if (!file) return;
    setImageFile(file);
    setResult(null);
    setDetails('');
    setError('');
    const reader = new FileReader();
    reader.onloadend = () => setImagePreview(reader.result);
    reader.readAsDataURL(file);
  };

  const clearImage = () => {
    setImageFile(null);
    setImagePreview(null);
    setResult(null);
    setDetails('');
    setError('');
    if (fileInputRef.current) fileInputRef.current.value = '';
    if (cameraInputRef.current) cameraInputRef.current.value = '';
  };

  const analyzeImage = async () => {
    const apiKey = localStorage.getItem('geminiApiKey');
    if (!apiKey) {
      setError('Por favor agrega tu API Key de Gemini en la sección de Ajustes primero.');
      return;
    }
    if (!imagePreview) return;

    setIsAnalyzing(true);
    setError('');

    try {
      // Convert image to base64
      const base64 = imagePreview.split(',')[1];
      const mimeType = imageFile.type || 'image/jpeg';

      const prompt = `Eres un asistente de apoyo en recuperación de adicciones. Analiza esta foto de una persona y proporciona una evaluación de bienestar general basada en indicadores visuales como: apariencia general, estado de los ojos (rojos, cristalinos, cansados), expresión facial, coherencia visual y posibles señales de consumo reciente de sustancias.

IMPORTANTE: Esta no es una evaluación médica. Es una herramienta de apoyo motivacional.

Responde ÚNICAMENTE con este JSON (sin texto adicional):
{
  "result": "safe" | "caution" | "risk",
  "summary": "Una frase corta del análisis (máx 20 palabras)",
  "details": "Explicación breve de 2-3 oraciones sobre lo observado, con tono compasivo y motivador."
}`;

      const response = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey}`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            contents: [{
              parts: [
                { text: prompt },
                { inline_data: { mime_type: mimeType, data: base64 } }
              ]
            }],
            generationConfig: { temperature: 0.3, maxOutputTokens: 512 }
          })
        }
      );

      if (!response.ok) {
        const errData = await response.json();
        throw new Error(errData?.error?.message || 'Error al conectar con la IA');
      }

      const data = await response.json();
      const text = data.candidates?.[0]?.content?.parts?.[0]?.text || '';

      let parsed;
      try {
        const jsonMatch = text.match(/\{[\s\S]*\}/);
        parsed = JSON.parse(jsonMatch ? jsonMatch[0] : text);
      } catch {
        throw new Error('La IA no devolvió un formato válido. Intenta de nuevo.');
      }

      const resultKey = ['safe', 'caution', 'risk'].includes(parsed.result) ? parsed.result : 'caution';
      setResult(resultKey);
      setDetails(parsed.details || '');

      // Save to Firestore (without image to save storage quota)
      if (userId) {
        await addDoc(collection(db, 'users', userId, 'checkins'), {
          result: resultKey,
          summary: parsed.summary || '',
          details: parsed.details || '',
          createdAt: Timestamp.now(),
        });
        setHasCheckedInToday(true);
        await loadHistory();
      }
    } catch (e) {
      console.error('Analysis error:', e);
      setError(e.message || 'Error durante el análisis. Verifica tu API Key.');
    } finally {
      setIsAnalyzing(false);
    }
  };

  const ResultIcon = result ? RESULT_CONFIG[result]?.icon : null;

  return (
    <div className="checkin-section">
      <div className="section-header">
        <Camera size={24} className="section-icon" style={{ color: 'var(--primary-color)' }} />
        <h3>Check-In Diario</h3>
        {hasCheckedInToday && (
          <span className="checkin-done-badge">✓ Hecho hoy</span>
        )}
      </div>

      {!hasCheckedInToday ? (
        <div className="checkin-body">
          <p className="checkin-subtitle">
            <Sparkles size={14} style={{ display: 'inline', marginRight: '0.3rem', color: 'var(--accent-color)' }} />
            Tómate una foto y la IA analizará tu bienestar con compasión y respeto.
          </p>

          {!imagePreview ? (
            <div className="checkin-actions">
              <button
                className="checkin-btn camera"
                onClick={() => cameraInputRef.current?.click()}
              >
                <Camera size={20} />
                <span>Tomar Foto</span>
              </button>
              <button
                className="checkin-btn gallery"
                onClick={() => fileInputRef.current?.click()}
              >
                <Upload size={20} />
                <span>Subir Foto</span>
              </button>
              <input
                ref={cameraInputRef}
                type="file"
                accept="image/*"
                capture="user"
                style={{ display: 'none' }}
                onChange={handleImageSelect}
              />
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                style={{ display: 'none' }}
                onChange={handleImageSelect}
              />
            </div>
          ) : (
            <div className="checkin-preview-area">
              <div className="checkin-preview-wrapper">
                <img src={imagePreview} alt="Tu foto" className="checkin-preview-img" />
                <button className="checkin-clear-btn" onClick={clearImage}>✕</button>
              </div>
              <button
                className="checkin-analyze-btn"
                onClick={analyzeImage}
                disabled={isAnalyzing}
              >
                {isAnalyzing ? (
                  <>
                    <Loader size={18} className="spin-icon" />
                    <span>Analizando...</span>
                  </>
                ) : (
                  <>
                    <Sparkles size={18} />
                    <span>Analizar con IA</span>
                  </>
                )}
              </button>
              {error && <p className="checkin-error">{error}</p>}
            </div>
          )}
        </div>
      ) : (
        result && (
          <div
            className="checkin-result-card"
            style={{
              background: RESULT_CONFIG[result].bg,
              borderColor: RESULT_CONFIG[result].border,
            }}
          >
            <div className="result-header" style={{ color: RESULT_CONFIG[result].color }}>
              {ResultIcon && <ResultIcon size={22} />}
              <span>{RESULT_CONFIG[result].emoji} {RESULT_CONFIG[result].label}</span>
            </div>
            {details && <p className="result-details">{details}</p>}
            <p className="result-note">Vuelve mañana para tu próximo check-in.</p>
          </div>
        )
      )}

      {/* History */}
      {history.length > 0 && (
        <div className="checkin-history">
          <button
            className="history-toggle"
            onClick={() => setShowHistory(!showHistory)}
          >
            <Clock size={16} />
            <span>Historial ({history.length})</span>
            {showHistory ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
          </button>
          {showHistory && (
            <div className="history-list">
              {history.map((item) => {
                const cfg = RESULT_CONFIG[item.result] || RESULT_CONFIG.caution;
                return (
                  <div
                    key={item.id}
                    className="history-item"
                    style={{ borderLeft: `3px solid ${cfg.color}` }}
                  >
                    <div className="history-item-header">
                      <span style={{ color: cfg.color, fontWeight: 600, fontSize: '0.85rem' }}>
                        {cfg.emoji} {cfg.label}
                      </span>
                      <span className="history-date">{formatDate(item.createdAt)}</span>
                    </div>
                    {item.details && (
                      <p className="history-details">{item.details}</p>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default DailyCheckIn;
