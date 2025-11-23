package com.psyche.platform.prediction.strategy;

import com.psyche.platform.prediction.model.*;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
public class GeneticMBTIStrategy implements MBTIPredictionStrategy {
    
    private static final Double GENETIC_WEIGHT = 0.4;
    private static final Double ENVIRONMENT_WEIGHT = 0.6;
    
    @Override
    public MBTIResult predict(ParentsGeneticData parents, EnvironmentData environment) {
        Map<String, Double> traitScores = new HashMap<>();
        
        // Расчет каждой дихотомии MBTI: P(Trait_i) = w_g × G_i + w_e × E_i
        traitScores.put("E", calculateExtraversion(parents, environment));
        traitScores.put("I", 1 - traitScores.get("E"));
        traitScores.put("N", calculateIntuition(parents, environment));
        traitScores.put("S", 1 - traitScores.get("N"));
        traitScores.put("T", calculateThinking(parents, environment));
        traitScores.put("F", 1 - traitScores.get("T"));
        traitScores.put("J", calculateJudging(parents, environment));
        traitScores.put("P", 1 - traitScores.get("J"));
        
        String mbtiType = determineMBTIType(traitScores);
        Double bullyingRisk = calculateBullyingRisk(traitScores, environment);
        
        return MBTIResult.builder()
            .mbtiType(mbtiType)
            .traitScores(traitScores)
            .confidence(calculateConfidence(traitScores))
            .bullyingRisk(bullyingRisk)
            .analysis(generateAnalysis(mbtiType, bullyingRisk))
            .build();
    }
    
    private Double calculateExtraversion(ParentsGeneticData parents, EnvironmentData environment) {
        Double geneticE = parents.getAverageExtraversion();
        Double environmentE = calculateEnvironmentExtraversion(environment);
        
        return (geneticE * GENETIC_WEIGHT) + (environmentE * ENVIRONMENT_WEIGHT);
    }
    
    private Double calculateEnvironmentExtraversion(EnvironmentData environment) {
        Double base = 0.5;
        
        // Порядок рождения: первенцы часто более экстравертны
        if (environment.getBirthOrder() == 1) {
            base += 0.15;
        }
        
        // Влияние школы
        if ("ACTIVE".equals(environment.getSchoolType())) {
            base += 0.2;
        }
        
        // Влияние друзей
        base += environment.getFriendsInfluence() * 0.15;
        
        return Math.max(0, Math.min(1, base));
    }
    
    private Double calculateIntuition(ParentsGeneticData parents, EnvironmentData environment) {
        Double geneticN = parents.getAverageIntuition();
        // Среда меньше влияет на интуицию/сенсорику
        return (geneticN * 0.7) + (0.3 * 0.5); // 70% генетика
    }
    
    private Double calculateThinking(ParentsGeneticData parents, EnvironmentData environment) {
        Double geneticT = (parents.getFatherThinking() + parents.getMotherThinking()) / 2;
        return (geneticT * 0.6) + (0.4 * 0.5); // 60% генетика
    }
    
    private Double calculateJudging(ParentsGeneticData parents, EnvironmentData environment) {
        Double geneticJ = (parents.getFatherJudging() + parents.getMotherJudging()) / 2;
        
        // Строгая среда усиливает Judging
        Double environmentJ = "STRICT".equals(environment.getFamilyEnvironment()) ? 0.7 : 0.5;
        
        return (geneticJ * 0.5) + (environmentJ * 0.5);
    }
    
    private String determineMBTIType(Map<String, Double> traitScores) {
        StringBuilder mbti = new StringBuilder();
        
        mbti.append(traitScores.get("E") > traitScores.get("I") ? "E" : "I");
        mbti.append(traitScores.get("N") > traitScores.get("S") ? "N" : "S");
        mbti.append(traitScores.get("T") > traitScores.get("F") ? "T" : "F");
        mbti.append(traitScores.get("J") > traitScores.get("P") ? "J" : "P");
        
        return mbti.toString();
    }
    
    private Double calculateBullyingRisk(Map<String, Double> traitScores, EnvironmentData environment) {
        Double risk = 0.0;
        
        // Высокая экстраверсия + низкое чувство = риск буллинга
        if (traitScores.get("E") > 0.7 && traitScores.get("F") < 0.3) {
            risk += 0.6;
        }
        
        // Высокое мышление + низкая эмпатия
        if (traitScores.get("T") > 0.8) {
            risk += 0.3;
        }
        
        // Агрессивная среда увеличивает риск
        if ("STRICT".equals(environment.getFamilyEnvironment())) {
            risk += 0.2;
        }
        
        return Math.max(0, Math.min(1, risk));
    }
    
    private Double calculateConfidence(Map<String, Double> traitScores) {
        // Уверенность выше когда черты ярко выражены
        double maxDeviation = traitScores.values().stream()
            .mapToDouble(score -> Math.abs(score - 0.5))
            .max()
            .orElse(0.0);
        
        return maxDeviation * 2; // 0.0 - 1.0
    }
    
    private String generateAnalysis(String mbtiType, Double bullyingRisk) {
        StringBuilder analysis = new StringBuilder();
        
        analysis.append("Тип MBTI: ").append(mbtiType).append("\n\n");
        
        if (bullyingRisk > 0.7) {
            analysis.append("⚠️ ВЫСОКИЙ РИСК БУЛЛИНГА!\n");
            analysis.append("Ребенок может проявлять агрессию к сверстникам\n");
            analysis.append("Рекомендации: развитие эмпатии, спорт, психолог\n");
        } else if (bullyingRisk > 0.4) {
            analysis.append("🔶 СРЕДНИЙ РИСК БУЛЛИНГА\n");
            analysis.append("Следить за социальными взаимодействиями\n");
        } else {
            analysis.append("✅ НИЗКИЙ РИСК БУЛЛИНГА\n");
        }
        
        // Анализ по типу MBTI
        if (mbtiType.startsWith("E")) {
            analysis.append("\n🎯 ЭКСТРАВЕРТ: Общительный, энергичный, лидер");
        } else {
            analysis.append("\n🎯 ИНТРОВЕРТ: Вдумчивый, ценит уединение");
        }
        
        return analysis.toString();
    }
    
    @Override
    public String getStrategyName() {
        return "GENETIC_STRATEGY";
    }
}
