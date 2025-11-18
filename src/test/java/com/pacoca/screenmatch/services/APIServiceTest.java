package com.pacoca.screenmatch.services;

import com.pacoca.screenmatch.model.serie.Show;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link APIService}.
 *
 * <h2>What we test</h2>
 * <ul>
 *     <li><b>getData(String)</b>
 *         <ul>
 *             <li>Uses {@link HttpClient#newHttpClient()} and {@link HttpClient#send(HttpRequest, HttpResponse.BodyHandler)}.</li>
 *             <li>Returns the response body as String.</li>
 *         </ul>
 *     </li>
 *     <li><b>openAiChat(String)</b>
 *         <ul>
 *             <li>Loads API key from {@link Dotenv}.</li>
 *             <li>Builds a POST request to the OpenAI Chat Completions endpoint.</li>
 *             <li>Returns the raw response body.</li>
 *         </ul>
 *     </li>
 *     <li><b>GetRecomendation()</b>
 *         <ul>
 *             <li>Reads search history from {@link Show#getSearchedSeries()}.</li>
 *             <li>Calls {@link APIService#openAiChat(String)} with the formatted history.</li>
 *             <li>Parses the OpenAI response via {@link Converter#convertFromChatGPT(String)}.</li>
 *             <li>Returns the parsed recommendation string.</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <p>All external dependencies (HTTP calls, environment variables, static helpers)
 * are mocked so tests are fast, deterministic, and do not require network or a real .env file.</p>
 */
@ExtendWith(MockitoExtension.class)
class APIServiceTest {

    @Mock
    Dotenv dotenv; // used when mocking Dotenv.load()

    // -------------------------------------------------------------------------
    // Tests for getData(String URL)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getData should return response body when HTTP call succeeds")
    void getData_shouldReturnResponseBody_whenHttpCallSucceeds() throws Exception {
        // Arrange: mock HttpClient and HttpResponse, and static HttpClient.newHttpClient()
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        try (MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {
            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(mockClient);
            when(mockResponse.body()).thenReturn("OK BODY");

            // capture the HttpRequest passed to send()
            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

            when(mockClient.send(
                    requestCaptor.capture(),
                    any(HttpResponse.BodyHandler.class))
            ).thenReturn(mockResponse);

            APIService apiService = new APIService();
            String url = "https://example.com/data";

            // Act
            String result = apiService.getData(url);

            // Assert: response body
            assertEquals("OK BODY", result);

            // And verify the request was built for the correct URL
            HttpRequest sentRequest = requestCaptor.getValue();
            assertEquals(URI.create(url), sentRequest.uri(), "getData should create a request with the provided URL");
        }
    }

    // -------------------------------------------------------------------------
    // Tests for openAiChat(String searchHistory)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("openAiChat should call OpenAI API and return raw response body")
    void openAiChat_shouldReturnRawResponseBody_fromOpenAI() throws Exception {
        // Arrange: mock dotenv and HttpClient
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class);
             MockedStatic<HttpClient> httpClientStatic = mockStatic(HttpClient.class)) {

            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);
            when(dotenv.get("OPENAI_API_KEY")).thenReturn("test-api-key");

            httpClientStatic.when(HttpClient::newHttpClient).thenReturn(mockClient);
            when(mockResponse.body()).thenReturn("{\"mock\":\"openai-response\"}");

            ArgumentCaptor<HttpRequest> requestCaptor = ArgumentCaptor.forClass(HttpRequest.class);

            when(mockClient.send(
                    requestCaptor.capture(),
                    any(HttpResponse.BodyHandler.class))
            ).thenReturn(mockResponse);

            String searchHistory = "[Breaking Bad, Dark]";

            // Act
            String result = APIService.openAiChat(searchHistory);

            // Assert: raw body is returned
            assertEquals("{\"mock\":\"openai-response\"}", result);

            // Verify request properties
            HttpRequest sentRequest = requestCaptor.getValue();
            assertEquals(
                    URI.create("https://api.openai.com/v1/chat/completions"),
                    sentRequest.uri(),
                    "openAiChat should target the OpenAI chat completions endpoint"
            );

            String authHeader = sentRequest.headers()
                    .firstValue("Authorization")
                    .orElse("");
            assertEquals("Bearer test-api-key", authHeader,
                    "openAiChat should send Authorization header with Bearer token from .env");
        }
    }

    // -------------------------------------------------------------------------
    // Tests for GetRecomendation()
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("GetRecomendation should use search history, OpenAI, and Converter to build recommendation")
    void getRecommendation_shouldReturnConvertedRecommendation_fromOpenAIResponse() throws Exception {
        // Arrange: fake search history and OpenAI response
        String[] historyArray = new String[]{"Breaking Bad", "Dark"};
        String expectedSearchHistory = Arrays.toString(historyArray); // "[Breaking Bad, Dark]"
        String rawOpenAiResponse = "{\"choices\":[{\"message\":{\"content\":\"You should watch Better Call Saul\"}}]}";
        String parsedRecommendation = "You should watch Better Call Saul";

        try (MockedStatic<Show> showStatic = mockStatic(Show.class);
             MockedStatic<APIService> apiServiceStatic = mockStatic(APIService.class);
             MockedStatic<Converter> converterStatic = mockStatic(Converter.class)) {

            // Show.getSearchedSeries() → our fake search history
            showStatic.when(Show::getSearchedSeries).thenReturn(historyArray);

            // Let GetRecomendation() call the real static method implementation
            apiServiceStatic.when(APIService::GetRecomendation).thenCallRealMethod();

            // But mock openAiChat(history) to return our fake JSON
            apiServiceStatic
                    .when(() -> APIService.openAiChat(expectedSearchHistory))
                    .thenReturn(rawOpenAiResponse);

            // Converter.convertFromChatGPT(rawJson) → parsed recommendation
            converterStatic
                    .when(() -> Converter.convertFromChatGPT(rawOpenAiResponse))
                    .thenReturn(parsedRecommendation);

            // Act
            String result = APIService.GetRecomendation();

            // Assert
            assertEquals(parsedRecommendation, result,
                    "GetRecomendation should return the parsed recommendation from Converter");

            // Verify that openAiChat was called with the Arrays.toString(...) of the history
            apiServiceStatic.verify(
                    () -> APIService.openAiChat(expectedSearchHistory),
                    times(1)
            );

            // Verify converter was called with raw OpenAI response
            converterStatic.verify(
                    () -> Converter.convertFromChatGPT(rawOpenAiResponse),
                    times(1)
            );
        }
    }
}
