package com.psyche.platform.prediction.strategy;

import com.psyche.platform.prediction.model.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Поведенческая стратегия расчета MBTI 
 * Основана на наблюдаемых поведенческих паттернах
 */
@Component
public class BehavioralMBTIStrategy implements MBTIPredictionStrategy {
    
    @Override
    public MBTIResult predict(ParentsGeneticData parents, EnvironmentData environment) {
        validateInput(parents, environment);
        
        Map<String, Double> traitScores = calculateBehavioralTraits(parents, environment);
        String mbtiType = determineMBTIType(traitScores);
        
        return MBTIResult.builder()
            .mbtiType(mbtiType)
            .traitScores(traitScores)
            .confidence(0.75) // Поведенческий анализ обычно менее точен
            .bullyingRisk(calculateBehavioralBullyingRisk(traitScores, environment))
            .analysis("Поведенческий анализ на основе паттернов взаимодействия")
            .strategyUsed(getStrategyName())
            .build();
    }
    
    private Map<String, Double> calculateBehavioralTraits(ParentsGeneticData parents, EnvironmentData environment) {
        Map<String, Double> traits = new HashMap<>();
        
        // Упрощенный поведенческий анализ
        traits.put("E", estimateBehavioralExtraversion(environment));
        traits.put("I", 1 - traits.get("E"));
        traits.put("N", 0.6); // Поведенчески сложно определить
        traits.put("S", 0.4);
        traits.put("T", estimateBehavioralThinking(parents, environment));
        traits.put("F", 1 - traits.get("T"));
        traits.put("J", estimateBehavioralJudging(environment));
        traits.put("P", 1 - traits.get("J"));
        
        return traits;
    }
    
    private Double estimateBehavioralExtraversion(EnvironmentData environment) {
        double base = 0.5;
        if ("ACTIVE".equalsIgnoreCase(environment.getSchoolType())) {
            base += 0.3;
        }
        if (environment.getFriendsInfluence() != null && environment.getFriendsInfluence() > 0.7) {
            base += 0.2;
        }
        return normalizeScore(base);
    }
    
    private Double estimateBehavioralThinking(ParentsGeneticData parents, EnvironmentData environment) {
        // Наследование поведенческих паттернов от родителей
        double geneticT = (parents.getFatherThinking() + parents.getMotherThinking()) / 2;
        return geneticT * 0.8; // Сильное генетическое влияние на мышление
    }
    
    private Double estimateBehavioralJudging(EnvironmentData environment) {
        double base = 0.5;
        if ("STRICT".equalsIgnoreCase(environment.getFamilyEnvironment())) {
            base += 0.4;
        }
        return normalizeScore(base);
    }
    
    private Double calculateBehavioralBullyingRisk(Map<String, Double> traitScores, EnvironmentData environment) {
        // Поведенческий риск выше в строгих средах
        double risk = traitScores.get("E") > 0.7 ? 0.4 : 0.1;
        if ("STRICT".equalsIgnoreCase(environment.getFamilyEnvironment())) {
            risk += 0.3;
        }
        return normalizeScore(risk);
    }
    
    private String determineMBTIType(Map<String, Double> traitScores) {
        StringBuilder mbti = new StringBuilder();
        mbti.append(traitScores.get("E") >= 0.5 ? "E" : "I");
        mbti.append(traitScores.get("N") >= 0.5 ? "N" : "S");
        mbti.append(traitScores.get("T") >= 0.5 ? "T" : "F");
        mbti.append(traitScores.get("J") >= 0.5 ? "J" : "P");
        return mbti.toString();
    }
    
    private Double normalizeScore(Double score) {
        return Math.max(0.0, Math.min(1.0, score));
    }
    
    @Override
    public String getStrategyName() {
        return "BEHAVIORAL_MBTI_STRATEGY";
    }
    
    @Override
    public String getDescription() {
        return "Поведенческий анализ на основе наблюдаемых паттернов";
    }
}
