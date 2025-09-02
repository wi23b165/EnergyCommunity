// src/main/java/at/fhtw/producer/service/WeatherFactor.java
package at.fhtw.producer.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherFactor {

    private final RestTemplate http = new RestTemplate();

    @Value("${weather.enabled:true}")
    boolean enabled;

    @Value("${weather.url:https://api.open-meteo.com/v1/forecast?latitude=48.21&longitude=16.37&current=cloud_cover}")
    String url;

    /** Liefert einen Faktor ~0.3..1.3 je nach Bewölkung. Disabled → 1.0. */
    public double factor() {
        if (!enabled) return 1.0;
        try {
            JsonNode node = http.getForObject(url, JsonNode.class);
            double cloud = node.path("current").path("cloud_cover").asDouble(50.0); // 0..100
            double f = 1.3 - (cloud / 100.0); // viel Sonne => ~1.3
            return Math.max(0.3, Math.min(1.3, f));
        } catch (Exception e) {
            return 1.0;
        }
    }
}
