import React, { useState } from 'react';
import { BookOpen, ChevronDown, ChevronUp, Brain, Moon, Coffee, Heart } from 'lucide-react';

const categories = [
    { id: 'all', name: 'Todos', icon: BookOpen },
    { id: 'anxiety', name: 'Ansiedad', icon: Brain },
    { id: 'sleep', name: 'Sueño', icon: Moon },
    { id: 'nutrition', name: 'Nutrición', icon: Coffee },
    { id: 'wellness', name: 'Bienestar', icon: Heart },
];

const articles = [
    {
        id: 1,
        category: 'anxiety',
        title: "Manejo de la Ansiedad",
        content: "La ansiedad es común en la recuperación. Practica la técnica 4-7-8: Inhala por 4 segundos, mantén por 7, y exhala por 8. Esto ayuda a calmar el sistema nervioso."
    },
    {
        id: 2,
        category: 'anxiety',
        title: "Identificando Detonantes",
        content: "Los detonantes pueden ser personas, lugares o emociones. Lleva un diario para identificar qué situaciones te provocan deseos de consumir y planea cómo evitarlas o enfrentarlas."
    },
    {
        id: 3,
        category: 'sleep',
        title: "La Importancia del Sueño",
        content: "El sueño reparador es vital para la salud mental. Intenta mantener un horario regular y evita pantallas una hora antes de dormir."
    },
    {
        id: 4,
        category: 'nutrition',
        title: "Nutrición y Recuperación",
        content: "Una dieta balanceada ayuda a estabilizar el estado de ánimo. Prioriza alimentos ricos en proteínas, grasas saludables y vegetales."
    },
    {
        id: 5,
        category: 'wellness',
        title: "Meditación Mindfulness",
        content: "Dedica 5 minutos al día a observar tus pensamientos sin juzgarlos. Esto fortalece tu capacidad de respuesta ante los impulsos."
    },
    {
        id: 6,
        category: 'wellness',
        title: "El Poder de la Gratitud",
        content: "Escribir tres cosas por las que estás agradecido cada día puede cambiar tu perspectiva y reducir el estrés."
    }
];

const EducationSection = () => {
    const [expandedId, setExpandedId] = useState(null);
    const [selectedCategory, setSelectedCategory] = useState('all');

    const toggleArticle = (id) => {
        setExpandedId(expandedId === id ? null : id);
    };

    const filteredArticles = selectedCategory === 'all'
        ? articles
        : articles.filter(article => article.category === selectedCategory);

    return (
        <div className="education-section">
            <div className="section-header">
                <BookOpen size={24} className="section-icon" />
                <h3>Educación y Consejos</h3>
            </div>

            <div className="category-filters">
                {categories.map(cat => (
                    <button
                        key={cat.id}
                        className={`category-btn ${selectedCategory === cat.id ? 'active' : ''}`}
                        onClick={() => setSelectedCategory(cat.id)}
                    >
                        <cat.icon size={16} />
                        <span>{cat.name}</span>
                    </button>
                ))}
            </div>

            <div className="articles-list">
                {filteredArticles.map((article) => (
                    <div key={article.id} className="article-card">
                        <div
                            className="article-header"
                            onClick={() => toggleArticle(article.id)}
                        >
                            <div className="article-title-row">
                                <span className={`category-tag ${article.category}`}>
                                    {categories.find(c => c.id === article.category)?.name}
                                </span>
                                <h4>{article.title}</h4>
                            </div>
                            {expandedId === article.id ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
                        </div>
                        {expandedId === article.id && (
                            <div className="article-content">
                                <p>{article.content}</p>
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default EducationSection;
