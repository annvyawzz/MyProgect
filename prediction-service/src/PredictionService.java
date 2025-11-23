package com.psyche.platform.prediction.command;

import com.psyche.platform.prediction.model.MBTIResult;

// Command Pattern
public interface PredictionCommand {
    MBTIResult execute();
    void undo();
    String getCommandName();
}
