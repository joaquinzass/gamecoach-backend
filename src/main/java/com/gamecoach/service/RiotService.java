package com.gamecoach.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class RiotService {

    @Value("${riot.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Shared: obtener PUUID por nombre y tag ──
    public String getPuuid(String gameName, String tagLine) {
        String url = "https://americas.api.riotgames.com/riot/account/v1/accounts/by-riot-id/"
                + gameName + "/" + tagLine;
        return (String) getWithAuth(url, Map.class).get("puuid");
    }

    // ── League of Legends: últimas 5 partidas ──
    public List<String> getLolMatchIds(String puuid) {
        String url = "https://americas.api.riotgames.com/lol/match/v5/matches/by-puuid/"
                + puuid + "/ids?start=0&count=5";
        return getWithAuth(url, List.class);
    }

    public Map getLolMatchDetail(String matchId) {
        String url = "https://americas.api.riotgames.com/lol/match/v5/matches/" + matchId;
        return getWithAuth(url, Map.class);
    }

    // ── Valorant: últimas 5 partidas ──
    public List<String> getValorantMatchIds(String puuid) {
        String url = "https://americas.api.riotgames.com/val/match/v1/matchlists/by-puuid/" + puuid;
        Map response = getWithAuth(url, Map.class);
        List<Map> history = (List<Map>) response.get("history");
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < Math.min(5, history.size()); i++) {
            ids.add((String) history.get(i).get("matchId"));
        }
        return ids;
    }

    public Map getValorantMatchDetail(String matchId) {
        String url = "https://api.henrikdev.xyz/valorant/v3/matches/{region}/{name}/{tag}" + matchId;
        return getWithAuth(url, Map.class);
    }

    // ── Helper: GET autenticado ──
    private <T> T getWithAuth(String url, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Riot-Token", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, type).getBody();
    }
}