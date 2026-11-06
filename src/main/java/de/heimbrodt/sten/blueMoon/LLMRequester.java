package de.heimbrodt.sten.blueMoon;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class LLMRequester {
    LLMRequester() {

    }

    private boolean checkLLAMAServer(String hostname) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(hostname + "health")).timeout(Duration.ofSeconds(5)).build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200 && response.body().contains("\"ok\"");
        } catch (Exception e) {
            return false;
        }
    }

    public CompletableFuture<String> talkToPlayer(BlueMoonPlayer bp, BlueMoonVillager bv, String userText) {
        String host = "http://localhost:8080/";
        if (checkLLAMAServer("http://192.168.2.131:8080/")) {
            host = "http://192.168.2.131:8080/";
        }

        HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
        String body = new Gson().toJson(Map.of(
                "messages", List.of(
                        Map.of("role", "system", "content", bv.getBackstory()),
                        Map.of("role", "user", "content",
                                (bv.getMemories().isEmpty() ? "" : "=== Vergangene Gespräche ===\n" + bv.getMemories() + "\n") +
                                "=== Aktuelle Nachricht ===\n" + bp.getPlayer().getName() + ": " + userText)),
                "chat_template_kwargs", Map.of("enable_thinking", false),
                "max_tokens", 80, "temperature", 1.0));

        HttpRequest request = HttpRequest.newBuilder(URI.create(host + "v1/chat/completions"))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(1000))
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();

        CompletableFuture<HttpResponse<String>> futureResponse = http.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        CompletableFuture<String> futureString = futureResponse.thenApply(
                r -> JsonParser.parseString(r.body()).getAsJsonObject()
                        .getAsJsonArray("choices").get(0).getAsJsonObject()
                        .getAsJsonObject("message").get("content").getAsString().trim()
        ).exceptionally(e -> "Hrrm. Mein Gehirn ist gerade offline. Zeig das mal Sten: " + e.toString());

        return futureString;
    }
}
