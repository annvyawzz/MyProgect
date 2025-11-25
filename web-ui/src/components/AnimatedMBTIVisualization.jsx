import React, { useState, useEffect, useRef } from 'react';
import './AnimatedMBTIVisualization.css';

const AnimatedMBTIVisualization = ({ predictionResult }) => {
    const [animatedScores, setAnimatedScores] = useState({});
    const [currentPhase, setCurrentPhase] = useState(0);
    const canvasRef = useRef(null);

    // Анимация появления результатов
    useEffect(() => {
        if (predictionResult) {
            animateResults();
        }
    }, [predictionResult]);

    const animateResults = () => {
        const traits = ['E', 'I', 'N', 'S', 'T', 'F', 'J', 'P'];
        const duration = 2000; // 2 секунды анимации
        
        traits.forEach((trait, index) => {
            setTimeout(() => {
                setAnimatedScores(prev => ({
                    ...prev,
                    [trait]: predictionResult.traitScores[trait]
                }));
                
                if (index === traits.length - 1) {
                    setCurrentPhase(1); // Переход к фазе анализа
                }
            }, index * 250);
        });
    };

    // Отрисовка графа личности на Canvas
    useEffect(() => {
        if (currentPhase >= 1 && canvasRef.current) {
            drawPersonalityGraph();
        }
    }, [currentPhase, animatedScores]);

    const drawPersonalityGraph = () => {
        const canvas = canvasRef.current;
        const ctx = canvas.getContext('2d');
        const centerX = canvas.width / 2;
        const centerY = canvas.height / 2;
        
        // Очистка canvas
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        
        // Отрисовка радиального графа
        const traits = ['E', 'N', 'T', 'J', 'I', 'S', 'F', 'P'];
        const radius = 120;
        
        ctx.beginPath();
        ctx.strokeStyle = '#4CAF50';
        ctx.lineWidth = 3;
        
        traits.forEach((trait, index) => {
            const angle = (index * 2 * Math.PI) / traits.length;
            const score = animatedScores[trait] || 0;
            const pointRadius = radius * score;
            
            const x = centerX + pointRadius * Math.cos(angle);
            const y = centerY + pointRadius * Math.sin(angle);
            
            if (index === 0) {
                ctx.moveTo(x, y);
            } else {
                ctx.lineTo(x, y);
            }
            
            // Точки на графике
            ctx.beginPath();
            ctx.arc(x, y, 8, 0, 2 * Math.PI);
            ctx.fillStyle = getTraitColor(trait);
            ctx.fill();
            
            // Подписи
            ctx.fillStyle = '#333';
            ctx.font = '14px Arial';
            ctx.fillText(trait, x - 5, y - 10);
        });
        
        ctx.closePath();
        ctx.stroke();
        
        // Анимация заполнения
        animateGraphFill(ctx, centerX, centerY, traits, radius);
    };

    const animateGraphFill = (ctx, centerX, centerY, traits, radius) => {
        let progress = 0;
        const animate = () => {
            if (progress < 1) {
                progress += 0.02;
                
                ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);
                drawFilledGraph(ctx, centerX, centerY, traits, radius, progress);
                requestAnimationFrame(animate);
            }
        };
        animate();
    };

    const drawFilledGraph = (ctx, centerX, centerY, traits, radius, progress) => {
        ctx.beginPath();
        
        traits.forEach((trait, index) => {
            const angle = (index * 2 * Math.PI) / traits.length;
            const score = (animatedScores[trait] || 0) * progress;
            const pointRadius = radius * score;
            
            const x = centerX + pointRadius * Math.cos(angle);
            const y = centerY + pointRadius * Math.sin(angle);
            
            if (index === 0) {
                ctx.moveTo(x, y);
            } else {
                ctx.lineTo(x, y);
            }
        });
        
        ctx.closePath();
        ctx.fillStyle = 'rgba(76, 175, 80, 0.3)';
        ctx.fill();
        ctx.strokeStyle = '#4CAF50';
        ctx.lineWidth = 2;
        ctx.stroke();
    };

    const getTraitColor = (trait) => {
        const colors = {
            'E': '#FF6B6B', 'I': '#4ECDC4',
            'N': '#45B7D1', 'S': '#96CEB4', 
            'T': '#FECA57', 'F': '#FF9FF3',
            'J': '#54A0FF', 'P': '#5F27CD'
        };
        return colors[trait] || '#CCCCCC';
    };

    if (!predictionResult) {
        return <div className="loading-animation">Загрузка анализа...</div>;
    }

    return (
        <div className="visualization-container">
            <h3>🎭 Визуализация профиля личности</h3>
            
            <div className="visualization-content">
                <div className="graph-section">
                    <canvas 
                        ref={canvasRef}
                        width={400} 
                        height={400}
                        className="personality-canvas"
                    />
                </div>
                
                <div className="scores-section">
                    <h4>Результаты по шкалам:</h4>
                    {Object.entries(animatedScores).map(([trait, score]) => (
                        <div key={trait} className="trait-bar">
                            <span className="trait-label">{trait}</span>
                            <div className="bar-container">
                                <div 
                                    className="bar-fill"
                                    style={{
                                        width: `${(score || 0) * 100}%`,
                                        backgroundColor: getTraitColor(trait),
                                        transition: 'width 1s ease-in-out'
                                    }}
                                />
                            </div>
                            <span className="score-value">
                                {((score || 0) * 100).toFixed(0)}%
                            </span>
                        </div>
                    ))}
                </div>
            </div>
            
            {/* Анимированный анализ рисков */}
            <div className={`risk-analysis ${currentPhase >= 1 ? 'visible' : ''}`}>
                <h4>📊 Анализ рисков:</h4>
                <div className="risk-meter">
                    <div 
                        className="risk-fill"
                        style={{
                            width: `${predictionResult.bullyingRisk * 100}%`,
                            backgroundColor: predictionResult.bullyingRisk > 0.7 ? '#FF6B6B' : 
                                           predictionResult.bullyingRisk > 0.4 ? '#FECA57' : '#4ECDC4'
                        }}
                    />
                </div>
                <p>Риск буллинга: {(predictionResult.bullyingRisk * 100).toFixed(0)}%</p>
            </div>
        </div>
    );
};

export default AnimatedMBTIVisualization;