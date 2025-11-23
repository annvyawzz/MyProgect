package com.psyche.platform.prediction.strategy;

import com.psyche.platform.prediction.model.MBTIResult;
import com.psyche.platform.prediction.model.ParentsGeneticData;
import com.psyche.platform.prediction.model.EnvironmentData;

// Strategy Pattern
public interface MBTIPredictionStrategy 
{
    MBTIResult predict(ParentsGeneticData parents, EnvironmentData environment);
    String getStrategyName();
}
