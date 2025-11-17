package com.psyche.platform.prediction.model;

import lombok.Data;

@Data
public class EnvironmentData
  {
    private Integer birthOrder;           
    private String schoolType;           // "ACTIVE", "CALM", "STRICT"
    private Double friendsInfluence;     // 0.0 - 1.0
    private Boolean hasSiblings;
    private String familyEnvironment;    // "SUPPORTIVE", "STRICT", "NEUTRAL"
    
    //для расчета влияния среды
    public Double calculateEnvironmentalImpact() {
        double baseImpact = 0.5;
        
        //влияние порядка рождения
        baseImpact += calculateBirthOrderImpact();
        
        // Влияние школы
        baseImpact += calculateSchoolImpact();
        
        return Math.max(0, Math.min(1, baseImpact));
    }
    
    private Double calculateBirthOrderImpact()
    {
        return switch (birthOrder) {
            case 1 -> 0.1;  // Первенец - более ответственный
            case 2 -> -0.1; // Средний - более адаптивный  
            default -> 0.0;
        };
    }
    
    private Double calculateSchoolImpact()
    {
        return switch (schoolType) {
            case "ACTIVE" -> 0.2;
            case "STRICT" -> -0.1;
            default -> 0.0;
        };
    }
}
