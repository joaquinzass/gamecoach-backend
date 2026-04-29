package com.gamecoach.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnalysisService {

    private final RiotService riotService;
    private final GroqService groqService;

    public AnalysisService(RiotService riotService, GroqService groqService) {
        this.riotService = riotService;
        this.groqService = groqService;
    }

    public Map<String, Object> analyze(String gameName, String tagLine, String game) {
        String puuid = riotService.getPuuid(gameName, tagLine);
        List<Map<String, Object>> stats = game.equals("valorant")
                ? getValorantStats(puuid)
                : getLolStats(puuid);

        String prompt   = buildPrompt(gameName, game, stats);
        String analysis = groqService.analyze(prompt);

        Map<String, Object> result = new HashMap<>();
        result.put("player",          gameName);
        result.put("game",            game);
        result.put("matchesAnalyzed", stats.size());
        result.put("stats",           stats);
        result.put("analysis",        analysis);
        return result;
    }

    // ── LoL: extrae stats por partida ──
    private List<Map<String, Object>> getLolStats(String puuid) {
        List<String> matchIds = riotService.getLolMatchIds(puuid);
        List<Map<String, Object>> stats = new ArrayList<>();

        for (String matchId : matchIds) {
            Map match = riotService.getLolMatchDetail(matchId);
            Map info  = (Map) match.get("info");
            List<Map> participants = (List<Map>) info.get("participants");

            for (Map p : participants) {
                if (puuid.equals(p.get("puuid"))) {
                    Map<String, Object> s = new HashMap<>();
                    s.put("champion",    p.get("championName"));
                    s.put("kills",       p.get("kills"));
                    s.put("deaths",      p.get("deaths"));
                    s.put("assists",     p.get("assists"));
                    s.put("win",         p.get("win"));
                    s.put("visionScore", p.get("visionScore"));
                    s.put("totalDamage", p.get("totalDamageDealtToChampions"));
                    stats.add(s);
                    break;
                }
            }
        }
        return stats;
    }

    // ── Valorant: extrae stats por partida ──
    private List<Map<String, Object>> getValorantStats(String puuid) {
        List<String> matchIds = riotService.getValorantMatchIds(puuid);
        List<Map<String, Object>> stats = new ArrayList<>();

        for (String matchId : matchIds) {
            Map match   = riotService.getValorantMatchDetail(matchId);
            Map info    = (Map) match.get("matchInfo");
            List<Map> players = (List<Map>) match.get("players");

            for (Map p : players) {
                if (puuid.equals(p.get("puuid"))) {
                    Map<String, Object> ps = (Map<String, Object>) p.get("stats");
                    Map<String, Object> s  = new HashMap<>();
                    s.put("champion",    p.get("characterId")); // agente en Valorant
                    s.put("kills",       ps.get("kills"));
                    s.put("deaths",      ps.get("deaths"));
                    s.put("assists",     ps.get("assists"));
                    s.put("win",         getValorantWin(match, puuid));
                    s.put("visionScore", 0); // Valorant no tiene vision score
                    s.put("totalDamage", ps.getOrDefault("damage", 0));
                    stats.add(s);
                    break;
                }
            }
        }
        return stats;
    }

    // ── Determina si el jugador ganó en Valorant ──
    private boolean getValorantWin(Map match, String puuid) {
        List<Map> players = (List<Map>) match.get("players");
        List<Map> teams   = (List<Map>) match.get("teams");

        String teamId = null;
        for (Map p : players) {
            if (puuid.equals(p.get("puuid"))) {
                teamId = (String) p.get("teamId");
                break;
            }
        }
        if (teamId == null || teams == null) return false;
        for (Map team : teams) {
            if (teamId.equals(team.get("teamId"))) {
                return Boolean.TRUE.equals(team.get("won"));
            }
        }
        return false;
    }

    // ── Prompt dinámico según el juego ──
    private String buildPrompt(String gameName, String game, List<Map<String, Object>> stats) {
        String gameLabel = game.equals("valorant") ? "Valorant" : "League of Legends";
        StringBuilder sb = new StringBuilder();
        sb.append("Sos un coach profesional de ").append(gameLabel)
                .append(". Analizá las últimas ").append(stats.size())
                .append(" partidas del jugador ").append(gameName).append(":\n\n");

        for (int i = 0; i < stats.size(); i++) {
            Map<String, Object> s = stats.get(i);
            sb.append("Partida ").append(i + 1).append(": ")
                    .append(game.equals("valorant") ? "Agente: " : "Campeón: ")
                    .append(s.get("champion")).append(", ")
                    .append("K/D/A: ").append(s.get("kills")).append("/")
                    .append(s.get("deaths")).append("/").append(s.get("assists")).append(", ")
                    .append("Victoria: ").append(s.get("win")).append(", ")
                    .append("Daño: ").append(s.get("totalDamage")).append("\n");
        }

        sb.append("\nDame un análisis en español con: ")
                .append("1) Qué está haciendo bien, ")
                .append("2) Qué está haciendo mal con datos concretos, ")
                .append("3) Exactamente 3 consejos específicos para mejorar. ")
                .append("Sé directo y usa los números de las partidas.");
        return sb.toString();
    }
}