package Api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    String API_TOKEN;

    public KVTaskClient() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/register");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        API_TOKEN = response.body();
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(String.format("http://localhost:8080/save/%s?API_TOKEN=%s", key, API_TOKEN));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public String load(String key) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create(String.format("http://localhost:8080/load/%s?API_TOKEN=%s", key, API_TOKEN));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}
