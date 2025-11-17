package com.psyche.platform.prediction.model;

import lombok.Data;

@Data
public class ParentsGeneticData 
{
    private Double fatherExtraversion;    // 0.0 - 1.0
    private Double fatherIntuition;       // 0.0 - 1.0  
    private Double fatherThinking;        // 0.0 - 1.0
    private Double fatherJudging;         // 0.0 - 1.0
    
    private Double motherExtraversion;    // 0.0 - 1.0
    private Double motherIntuition;       // 0.0 - 1.0
    private Double motherThinking;        // 0.0 - 1.0
    private Double motherJudging;         // 0.0 - 1.0
    
    // Composite Pattern: родители как составной элемент
    public Double getAverageExtraversion() 
  {
        return (fatherExtraversion + motherExtraversion) / 2;
    }
    
    public Double getAverageIntuition()
  {
        return (fatherIntuition + motherIntuition) / 2;
    }
}
