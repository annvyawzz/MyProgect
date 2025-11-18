package com.psyche.platform.prediction;

import com.psyche.platform.prediction.model.*;
import com.psyche.platform.prediction.service.PredictionService;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {
    
    private final PredictionService predictionService;
    
    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }
    
    @PostMapping("/calculate")
    public MBTIResult calculatePrediction(@RequestBody @Valid PredictionRequest request) {
        return predictionService.calculateMBTI(request);
    }
    
    @PostMapping("/calculate-advanced")
    public MBTIResult calculateAdvanced(@RequestBody @Valid PredictionRequest request,
                                      @RequestParam String strategy) {
        return predictionService.calculateWithStrategy(request, strategy);
    }
    
    @GetMapping("/history/{userId}")
    public Object getPredictionHistory(@PathVariable String userId) {
        return predictionService.getUserHistory(userId);
    }
}
