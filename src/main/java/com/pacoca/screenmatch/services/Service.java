package com.pacoca.screenmatch.services;

import com.pacoca.screenmatch.model.Serie;
import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

// Service class responsible to handle API requests

public class Service {

    private Serie serie = new Serie();

    public String getData(String URL) {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();;
        HttpResponse<String> response = null;

        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response.body();
    }

     public static String GetRecomendation() throws IOException, InterruptedException {
        String searchHistory = Arrays.toString(Serie.getSearchedSeries());
        String prompt = "based on my previous search, give me recomendations of shows that I would like to watch: " + searchHistory.substring(1, searchHistory.length() - 1) + " .";
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("OPENAI_API_KEY");
        String json = """
        {
          "model": "gpt-3.5-turbo",
          "messages": [{
                        "role": "user",
                        "content": "%s"
                        }
                ]
        }
        """.formatted(prompt);

         HttpRequest request = HttpRequest.newBuilder()
                 .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                 .header("Content-Type", "application/json")
                 .header("Authorization", "Bearer " + apiKey)
                 .POST(HttpRequest.BodyPublishers.ofString(json))
                 .build();

         HttpResponse<String> response = HttpClient.newHttpClient()
                 .send(request, HttpResponse.BodyHandlers.ofString());


         String recomendation =Converter.convertFromChatGPT(response.body());
         System.out.println(recomendation);


         return "s";
    }

}
