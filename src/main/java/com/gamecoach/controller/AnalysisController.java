package com.gamecoach.controller;

import com.gamecoach.service.AnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    // /api/analyze/{game}/{gameName}/{tagLine}
    // game = "lol" o "valorant"
    @GetMapping("/analyze/{game}/{gameName}/{tagLine}")
    public ResponseEntity<Map<String, Object>> analyze(
            @PathVariable String game,
            @PathVariable String gameName,
            @PathVariable String tagLine) {
        Map<String, Object> result = analysisService.analyze(gameName, tagLine, game);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GameCoach running!");
    }
}