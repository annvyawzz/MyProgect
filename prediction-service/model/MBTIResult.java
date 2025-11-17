package com.psyche.platform.prediction.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class MBTIResult
  {
    private String mbtiType;
    private Map<String, Double> traitScores; 
    private Double confidence;
    private Double bullyingRisk;
    private String analysis;
   
    public static MBTIResult createHighConfidenceResult(String type, Double confidence) 
    {
        return MBTIResult.builder()
            .mbtiType(type)
            .confidence(confidence)
            .bullyingRisk(0.0)
            .build();
    }
}
