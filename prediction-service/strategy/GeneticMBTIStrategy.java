package com.psyche.platform.prediction.strategy;

import com.psyche.platform.prediction.model.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Генетическая стратегия расчета MBTI на основе формулы:
 * P(Trait_i) = w_g × G_i + w_e × E_i
 * где 40% - генетика родителей, 60% - влияние среды
 */
@Component
public class GeneticMBTIStrategy implements MBTIPredictionStrategy {
    
    // Веса влияния (40% генетика, 60% среда)
    private static final Double GENETIC_WEIGHT = 0.4;
    private static final Double ENVIRONMENT_WEIGHT = 0.6;
    
    // Пороги для определения рисков
    private static final Double HIGH_EXTROVERSION_THRESHOLD = 0.7;
    private static final Double LOW_EMPATHY_THRESHOLD = 0.3;
    private static final Double HIGH_BULLYING_RISK_THRESHOLD = 0.7;
    
    @Override
    public MBTIResult predict(ParentsGeneticData parents, EnvironmentData environment) {
        validateInput(parents, environment);
        
        Map<String, Double> traitScores = calculateAllTraits(parents, environment);
        String mbtiType = determineMBTIType(traitScores);
        Double bullyingRisk = calculateBullyingRisk(traitScores, environment);
        String analysis = generateDetailedAnalysis(mbtiType, traitScores, bullyingRisk);
        
        return MBTIResult.builder()
            .mbtiType(mbtiType)
            .traitScores(traitScores)
            .confidence(calculateConfidence(traitScores))
            .bullyingRisk(bullyingRisk)
            .analysis(analysis)
            .strategyUsed(getStrategyName())
            .build();
    }
    
    /**
     * Расчет всех 8 черт MBTI (4 дихотомии)
     */
    private Map<String, Double> calculateAllTraits(ParentsGeneticData parents, EnvironmentData environment) {
        Map<String, Double> traits = new HashMap<>();
        
        // Extraversion/Introversion
        traits.put("E", calculateExtraversion(parents, environment));
        traits.put("I", 1 - traits.get("E"));
        
        // Intuition/Sensing
        traits.put("N", calculateIntuition(parents, environment));
        traits.put("S", 1 - traits.get("N"));
        
        // Thinking/Feeling
        traits.put("T", calculateThinking(parents, environment));
        traits.put("F", 1 - traits.get("T"));
        
        // Judging/Perceiving
        traits.put("J", calculateJudging(parents, environment));
        traits.put("P", 1 - traits.get("J"));
        
        return traits;
    }
    
    /**
     * Расчет экстраверсии: E = (G_E * 0.4) + (E_E * 0.6)
     */
    private Double calculateExtraversion(ParentsGeneticData parents, EnvironmentData environment) {
        Double geneticE = calculateGeneticExtraversion(parents);
        Double environmentE = calculateEnvironmentalExtraversion(environment);
        
        return applyWeights(geneticE, environmentE);
    }
    
    private Double calculateGeneticExtraversion(ParentsGeneticData parents) {
        return (parents.getFatherExtraversion() + parents.getMotherExtraversion()) / 2;
    }
    
    private Double calculateEnvironmentalExtraversion(EnvironmentData environment) {
        double base = 0.5;
        
        // Порядок рождения: первенцы часто более экстравертны
        if (environment.getBirthOrder() == 1) {
            base += 0.15;
        } else if (environment.getBirthOrder() >= 3) {
            base -= 0.1; // Младшие дети могут быть более адаптивными
        }
        
        // Влияние типа школы
        base += getSchoolExtraversionImpact(environment.getSchoolType());
        
        // Влияние друзей
        if (environment.getFriendsInfluence() != null) {
            base += environment.getFriendsInfluence() * 0.2;
        }
        
        // Наличие братьев/сестер
        if (Boolean.TRUE.equals(environment.getHasSiblings())) {
            base += 0.1; // Социальное взаимодействие
        }
        
        return normalizeScore(base);
    }
    
    private Double getSchoolExtraversionImpact(String schoolType) {
        return switch (schoolType != null ? schoolType.toUpperCase() : "NEUTRAL") {
            case "ACTIVE", "АКТИВНАЯ" -> 0.25;
            case "STRICT", "СТРОГАЯ" -> -0.15;
            case "CREATIVE", "ТВОРЧЕСКАЯ" -> 0.1;
            default -> 0.0;
        };
    }
    
    /**
     * Расчет интуиции (Intuition)
     */
    private Double calculateIntuition(ParentsGeneticData parents, EnvironmentData environment) {
        Double geneticN = (parents.getFatherIntuition() + parents.getMotherIntuition()) / 2;
        // Интуиция меньше зависит от среды, больше от генетики
        return (geneticN * 0.7) + (0.3 * getEnvironmentalCreativity(environment));
    }
    
    private Double getEnvironmentalCreativity(EnvironmentData environment) {
        double base = 0.5;
        if ("CREATIVE".equalsIgnoreCase(environment.getSchoolType())) {
            base += 0.2;
        }
        return normalizeScore(base);
    }
    
    /**
     * Расчет мышления (Thinking)
     */
    private Double calculateThinking(ParentsGeneticData parents, EnvironmentData environment) {
        Double geneticT = (parents.getFatherThinking() + parents.getMotherThinking()) / 2;
        Double environmentT = getEnvironmentalThinking(environment);
        
        return (geneticT * 0.6) + (environmentT * 0.4);
    }
    
    private Double getEnvironmentalThinking(EnvironmentData environment) {
        double base = 0.5;
        if ("STRICT".equalsIgnoreCase(environment.getFamilyEnvironment())) {
            base += 0.15; // Строгое воспитание развивает аналитическое мышление
        }
        return base;
    }
    
    /**
     * Расчет суждения (Judging)
     */
    private Double calculateJudging(ParentsGeneticData parents, EnvironmentData environment) {
        Double geneticJ = (parents.getFatherJudging() + parents.getMotherJudging()) / 2;
        Double environmentJ = getEnvironmentalJudging(environment);
        
        return (geneticJ * 0.5) + (environmentJ * 0.5);
    }
    
    private Double getEnvironmentalJudging(EnvironmentData environment) {
        double base = 0.5;
        if ("STRICT".equalsIgnoreCase(environment.getFamilyEnvironment())) {
            base += 0.25; // Строгая среда усиливает Judging
        } else if ("SUPPORTIVE".equalsIgnoreCase(environment.getFamilyEnvironment())) {
            base -= 0.1; // Поддерживающая среда может развивать Perceiving
        }
        return normalizeScore(base);
    }
    
    /**
     * Определение типа MBTI на основе scores
     */
    private String determineMBTIType(Map<String, Double> traitScores) {
        StringBuilder mbti = new StringBuilder();
        
        // E/I
        mbti.append(traitScores.get("E") >= 0.5 ? "E" : "I");
        // N/S
        mbti.append(traitScores.get("N") >= 0.5 ? "N" : "S");
        // T/F
        mbti.append(traitScores.get("T") >= 0.5 ? "T" : "F");
        // J/P
        mbti.append(traitScores.get("J") >= 0.5 ? "J" : "P");
        
        return mbti.toString();
    }
    
    /**
     * Расчет риска буллинга на основе черт личности
     */
    private Double calculateBullyingRisk(Map<String, Double> traitScores, EnvironmentData environment) {
        double risk = 0.0;
        
        // Высокая экстраверсия + низкая эмпатия = основной риск
        if (traitScores.get("E") > HIGH_EXTROVERSION_THRESHOLD && 
            traitScores.get("F") < LOW_EMPATHY_THRESHOLD) {
            risk += 0.6;
        }
        
        // Высокое мышление + низкая эмпатия
        if (traitScores.get("T") > 0.8 && traitScores.get("F") < 0.4) {
            risk += 0.3;
        }
        
        // Агрессивная среда увеличивает риск
        if ("STRICT".equalsIgnoreCase(environment.getFamilyEnvironment())) {
            risk += 0.2;
        }
        
        // Отсутствие братьев/сестер может увеличивать риск
        if (Boolean.FALSE.equals(environment.getHasSiblings())) {
            risk += 0.1;
        }
        
        return normalizeScore(risk);
    }
    
    /**
     * Расчет уверенности предсказания
     */
    private Double calculateConfidence(Map<String, Double> traitScores) {
        // Уверенность выше когда черты ярко выражены
        double avgDeviation = traitScores.values().stream()
            .mapToDouble(score -> Math.abs(score - 0.5))
            .average()
            .orElse(0.0);
        
        return normalizeScore(avgDeviation * 2);
    }
    
    /**
     * Генерация детального анализа
     */
    private String generateDetailedAnalysis(String mbtiType, Map<String, Double> traitScores, Double bullyingRisk) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("🎭 ДЕТАЛЬНЫЙ АНАЛИЗ ЛИЧНОСТИ\n\n");
        analysis.append("Тип MBTI: ").append(mbtiType).append("\n");
        analysis.append(String.format("Уверенность предсказания: %.0f%%\n\n", calculateConfidence(traitScores) * 100));
        
        // Анализ по дихотомиям
        analysis.append("📊 РАСПРЕДЕЛЕНИЕ ЧЕРТ:\n");
        analysis.append(String.format("• Экстраверсия (E): %.0f%% / Интроверсия (I): %.0f%%\n", 
            traitScores.get("E") * 100, traitScores.get("I") * 100));
        analysis.append(String.format("• Интуиция (N): %.0f%% / Сенсорика (S): %.0f%%\n", 
            traitScores.get("N") * 100, traitScores.get("S") * 100));
        analysis.append(String.format("• Мышление (T): %.0f%% / Чувство (F): %.0f%%\n", 
            traitScores.get("T") * 100, traitScores.get("F") * 100));
        analysis.append(String.format("• Суждение (J): %.0f%% / Восприятие (P): %.0f%%\n\n", 
            traitScores.get("J") * 100, traitScores.get("P") * 100));
        
        // Анализ риска буллинга
        analysis.append("⚠️  АНАЛИЗ РИСКОВ:\n");
        if (bullyingRisk > HIGH_BULLYING_RISK_THRESHOLD) {
            analysis.append("• ВЫСОКИЙ РИСК БУЛЛИНГА (").append(String.format("%.0f%%", bullyingRisk * 100)).append(")\n");
            analysis.append("• Может проявлять агрессию к сверстникам\n");
            analysis.append("• Склонен к доминированию в группе\n");
            analysis.append("🎯 Рекомендации: развитие эмпатии, командный спорт, работа с психологом\n");
        } else if (bullyingRisk > 0.4) {
            analysis.append("• СРЕДНИЙ РИСК БУЛЛИНГА (").append(String.format("%.0f%%", bullyingRisk * 100)).append(")\n");
            analysis.append("• В конфликтных ситуациях может проявлять агрессию\n");
            analysis.append("🎯 Рекомендации: учить конструктивному разрешению конфликтов\n");
        } else {
            analysis.append("• НИЗКИЙ РИСК БУЛЛИНГА (").append(String.format("%.0f%%", bullyingRisk * 100)).append(")\n");
            analysis.append("• Скорее всего, будет мирно взаимодействовать со сверстниками\n");
        }
        
        // Особенности типа
        analysis.append("\n💫 ОСОБЕННОСТИ ТИПА ").append(mbtiType).append(":\n");
        analysis.append(getTypeDescription(mbtiType));
        
        return analysis.toString();
    }
    
    private String getTypeDescription(String mbtiType) {
        return switch (mbtiType) {
            case "ENTJ" -> "• Прирожденный лидер, стратег\n• Решителен, любит challenges\n• Может быть слишком критичным";
            case "ENFJ" -> "• Вдохновитель, харизматичный\n• Чуткий к эмоциям других\n• Старается угодить всем";
            case "INTJ" -> "• Стратег, независимый мыслитель\n• Целеустремленный, перфекционист\n• Может быть отстраненным";
            case "ENTP" -> "• Новатор, любит дебаты\n• Быстро думает, предприимчивый\n• Может быть противоречивым";
            case "ESTJ" -> "• Организатор, практичный\n• Ответственный, традиционный\n• Может быть жестким";
            case "ESFJ" -> "• Заботливый, популярный\n• Ответственный, гармоничный\n• Чувствителен к критике";
            case "ISTJ" -> "• Ответственный, реалистичный\n• Трудолюбивый, традиционный\n• Может сопротивляться изменениям";
            case "ISFJ" -> "• Защитник, преданный\n• Теплый, практичный\n• Избегает конфликтов";
            default -> "• Уникальное сочетание черт личности";
        };
    }
    
    // Вспомогательные методы
    private Double applyWeights(Double genetic, Double environmental) {
        return (genetic * GENETIC_WEIGHT) + (environmental * ENVIRONMENT_WEIGHT);
    }
    
    private Double normalizeScore(Double score) {
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    @Override
    public String getStrategyName() {
        return "GENETIC_MBTI_STRATEGY";
    }
    
    @Override
    public String getDescription() {
        return "Генетический алгоритм предсказания MBTI (40% генетика + 60% среда)";
    }
}