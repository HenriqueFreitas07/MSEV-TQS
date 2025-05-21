package tqs.msev.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tqs.msev.backend.dto.Coordinates;

@Slf4j
@Service
public class GeocodingService {
    private static final String API_URL = "https://api.geoapify.com/v1/geocode/search";
    private final RestTemplate restTemplate;

    @Value("${geocoding.apikey}")
    private String apiKey;

    public GeocodingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Coordinates getCoordinatesForAddress(String address) {
        String url = UriComponentsBuilder.fromUriString(API_URL)
                .queryParam("text", address)
                .queryParam("format", "json")
                .queryParam("apiKey", apiKey)
                .encode()
                .toUriString();

        log.info("Making an HTTP request to geocoding API for address {}", address);
        JSONObject json = new JSONObject(restTemplate.getForEntity(url, String.class).getBody());
        JSONArray resultsArray = json.getJSONArray("results");

        if (resultsArray.isEmpty()) return null;

        JSONObject results = resultsArray.getJSONObject(0);

        double lat = results.getDouble("lat");
        double lon = results.getDouble("lon");

        return new Coordinates(lat, lon);
    }
}
