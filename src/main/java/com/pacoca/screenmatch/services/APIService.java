/* 
 * APIService class responsible for handling API connections
 *
 *  such:
 *  - get data from external APIs 
 *  - connect to OPEN AI API and handle full communication
 * 
*/
package com.pacoca.screenmatch.services;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import com.pacoca.screenmatch.model.serie.Show;



public class APIService {
    // Retrieve data from URL passed as argument (e.g: retrieve data from omdb api)
    public String getData(String URL) {

        // create a client and a request direct it to the url
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).build();
        // create a response object
        HttpResponse<String> response = null;

        try {
            // try to send the request to client. 
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            // catch exception and throw runtimeException
            throw new RuntimeException(e);
        }

        // return body which has been parsed to string using HttpResponse.BodyHandlers.ofString()
        return response.body();
    }


    // get recommendation from OPEN AI model. 
    public static String GetRecomendation() throws IOException, InterruptedException {
        
        // retrieve searchHistory 
        String searchHistory = Arrays.toString(Show.getSearchedSeries());
        String recomendation =Converter.convertFromChatGPT(openAiChat(searchHistory));

        return recomendation;
    }

    // actual communication with openAI API
    public static String openAiChat(String searchHistory) {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("OPENAI_API_KEY");


        //create prompt for recommendations based on searchHistory
        String prompt = "Using my previous search history, identify the patterns in the kinds of shows I enjoy (genre, tone, language, release year, etc.) and recommend new TV shows I’m likely to enjoy." + searchHistory.substring(1, searchHistory.length() - 1) + " .";
        
        // setup json for API request
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
        

        try {
            // create a request directed to openAI API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // send request and save responde as variable
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());


            //return response body
            return response.body();

        } catch (IOException | InterruptedException e) {
            // catch exception and throw runtimeException
            throw new RuntimeException(e);
        }
    }

}
