package com.pacoca.screenmatch.services;

import com.pacoca.screenmatch.model.episode.Episode;
import com.pacoca.screenmatch.model.season.Season;
import com.pacoca.screenmatch.model.serie.Show;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link Service}.
 *
 * <h2>Test focus</h2>
 * <ul>
 *     <li><b>best5(String show)</b>:
 *         <ul>
 *             <li>Uses {@link Service#getEpisodes(Show)} to obtain episodes.</li>
 *             <li>Filters out "N/A" ratings.</li>
 *             <li>Sorts by numeric rating (descending).</li>
 *             <li>Prints only the best 5 episodes.</li>
 *         </ul>
 *     </li>
 *     <li><b>getEpisodes(Show show)</b>:
 *         <ul>
 *             <li>Flattens all {@link Season#getEpisodes()} from all seasons returned by {@link Service#getSeasons(Show)}.</li>
 *         </ul>
 *     </li>
 *     <li><b>processMenu(int option)</b>:
 *         <ul>
 *             <li>Case 4: prints an "empty history" message and calls {@link Show#listSearchedSeries()}.</li>
 *             <li>Case 5: calls {@link APIService#GetRecomendation()} and prints suggestion, then falls through to "Exiting...".</li>
 *             <li>Case 0: prints "Exiting...".</li>
 *         </ul>
 *     </li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class ServiceTest {

    /**
     * Custom Service subclass used for testing.
     *
     * We override:
     *  - getShow(...)     → to return a stubbed Show
     *  - getSeasons(...)  → to return stubbed seasons
     *  - getEpisodes(...) → to optionally return stubbed episodes, otherwise fall back to real logic
     */
    static class TestableService extends Service {
        private Show showStub;
        private List<Season> seasonsStub;
        private List<Episode> episodesStub;

        public void setShowStub(Show showStub) {
            this.showStub = showStub;
        }

        public void setSeasonsStub(List<Season> seasonsStub) {
            this.seasonsStub = seasonsStub;
        }

        public void setEpisodesStub(List<Episode> episodesStub) {
            this.episodesStub = episodesStub;
        }

        @Override
        public Show getShow(String show) {
            // For tests, ignore the actual show name and return the stub
            return showStub;
        }

        @Override
        public List<Season> getSeasons(Show show) {
            return seasonsStub;
        }

        @Override
        public List<Episode> getEpisodes(Show show) {
            // If we explicitly set an episodes stub, use it (for best5 test)
            if (episodesStub != null) {
                return episodesStub;
            }
            // Otherwise, use the real implementation which relies on getSeasons()
            return super.getEpisodes(show);
        }
    }

    // Capture System.out
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private PrintStream originalOut;

    @Mock
    Dotenv dotenv;

    @BeforeEach
    void setUpStreams() {
        originalOut = System.out;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    // -------------------------------------------------------------------------
    // Tests for best5(String show)
    // -------------------------------------------------------------------------

    @Test
    void best5_shouldPrintTop5Episodes_sortedByRating_descending_andIgnoreNA() {
        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class)) {
            // Mock dotenv so Service constructor doesn't hit a real .env
            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);
            when(dotenv.get("OMDB_API_KEY")).thenReturn("test-key");

            TestableService service = new TestableService();

            // Stub show
            Show mockShow = mock(Show.class);
            service.setShowStub(mockShow);

            // Create mock episodes with different ratings
            Episode ep1 = mock(Episode.class);
            when(ep1.getTitle()).thenReturn("Episode 1");
            when(ep1.getRating()).thenReturn("9.5");
            when(ep1.getSeason()).thenReturn(1);

            Episode ep2 = mock(Episode.class);
            // We only need rating for ep2 because it's filtered out before title/season are used
            when(ep2.getRating()).thenReturn("N/A"); // filtered out

            Episode ep3 = mock(Episode.class);
            when(ep3.getTitle()).thenReturn("Episode 3");
            when(ep3.getRating()).thenReturn("8.0");
            when(ep3.getSeason()).thenReturn(2);

            Episode ep4 = mock(Episode.class);
            when(ep4.getTitle()).thenReturn("Episode 4");
            when(ep4.getRating()).thenReturn("9.8");
            when(ep4.getSeason()).thenReturn(2);

            Episode ep5 = mock(Episode.class);
            when(ep5.getTitle()).thenReturn("Episode 5");
            when(ep5.getRating()).thenReturn("7.0");
            when(ep5.getSeason()).thenReturn(3);

            Episode ep6 = mock(Episode.class);
            when(ep6.getTitle()).thenReturn("Episode 6");
            when(ep6.getRating()).thenReturn("9.0");
            when(ep6.getSeason()).thenReturn(3);

            // Stub getEpisodes to return all episodes (including one "N/A")
            service.setEpisodesStub(List.of(ep1, ep2, ep3, ep4, ep5, ep6));

            // Act
            service.best5("any show name");

            // Assert: capture output
            String output = outContent.toString();

            // 1) "N/A" episode should not appear
            assertFalse(output.contains("Episode 2"),
                    "Episode with rating 'N/A' should not be printed.");

            // 2) Exactly 5 episodes should appear
            int countPrinted = 0;
            for (String title : new String[]{"Episode 1", "Episode 3", "Episode 4", "Episode 5", "Episode 6"}) {
                if (output.contains(title)) {
                    countPrinted++;
                }
            }
            assertEquals(5, countPrinted, "Exactly 5 episodes should be printed.");

            // 3) Order should be by rating: 9.8, 9.5, 9.0, 8.0, 7.0
            int indexEp4 = output.indexOf("Episode 4"); // 9.8
            int indexEp1 = output.indexOf("Episode 1"); // 9.5
            int indexEp6 = output.indexOf("Episode 6"); // 9.0
            int indexEp3 = output.indexOf("Episode 3"); // 8.0
            int indexEp5 = output.indexOf("Episode 5"); // 7.0

            assertTrue(indexEp4 < indexEp1 && indexEp1 < indexEp6 && indexEp6 < indexEp3 && indexEp3 < indexEp5,
                    "Episodes should be printed in descending rating order: 9.8 > 9.5 > 9.0 > 8.0 > 7.0");
        }
    }


    // -------------------------------------------------------------------------
    // Tests for getEpisodes(Show show)
    // -------------------------------------------------------------------------

    @Test
    void getEpisodes_shouldReturnAllEpisodesFromAllSeasons_inOrder() {
        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class)) {
            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);
            when(dotenv.get("OMDB_API_KEY")).thenReturn("test-key");

            TestableService service = new TestableService();
            Show mockShow = mock(Show.class);

            // Create mock seasons and episodes
            Episode ep1 = mock(Episode.class);
            Episode ep2 = mock(Episode.class);
            Episode ep3 = mock(Episode.class);

            Season season1 = mock(Season.class);
            when(season1.getEpisodes()).thenReturn(List.of(ep1, ep2));

            Season season2 = mock(Season.class);
            when(season2.getEpisodes()).thenReturn(List.of(ep3));

            // Stub seasons; do NOT stub episodesStub, so getEpisodes() uses super implementation
            service.setSeasonsStub(List.of(season1, season2));

            // Act
            List<Episode> result = service.getEpisodes(mockShow);

            // Assert
            assertNotNull(result, "Result list should not be null.");
            assertEquals(3, result.size(), "All episodes from all seasons should be returned.");
            assertEquals(ep1, result.get(0));
            assertEquals(ep2, result.get(1));
            assertEquals(ep3, result.get(2));
        }
    }

    // -------------------------------------------------------------------------
    // Tests for processMenu(int option)
    // -------------------------------------------------------------------------

    @Test
    void processMenu_case4_shouldPrintEmptyHistoryMessageAndCallShowList() {
        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class);
             MockedStatic<Show> showStatic = mockStatic(Show.class)) {

            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);
            when(dotenv.get("OMDB_API_KEY")).thenReturn("test-key");

            Service service = new Service();

            // Act
            service.processMenu(4);

            // Assert
            String output = outContent.toString();
            assertTrue(output.contains("History is currently empty"),
                    "Expected 'History is currently empty' to be printed for case 4.");

            showStatic.verify(Show::listSearchedSeries, times(1));
        }
    }

    @Test
    void processMenu_case5_shouldPrintSuggestionAndExitMessage() throws Exception {
        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class);
             MockedStatic<APIService> apiServiceStatic = mockStatic(APIService.class)) {

            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);
            when(dotenv.get("OMDB_API_KEY")).thenReturn("test-key");

            String fakeSuggestion = "Watch Breaking Bad and Dark next!";
            apiServiceStatic.when(APIService::GetRecomendation)
                    .thenReturn(fakeSuggestion);

            Service service = new Service();

            // Act
            service.processMenu(5);

            // Assert
            String output = outContent.toString();
            assertTrue(output.contains(fakeSuggestion),
                    "Expected AI suggestion to be printed for case 5.");
            assertTrue(output.contains("Exiting..."),
                    "Expected 'Exiting...' to be printed due to fall-through to case 0.");

            apiServiceStatic.verify(APIService::GetRecomendation, times(1));
        }
    }

    @Test
    void processMenu_case0_shouldPrintExitingMessage() {
        try (MockedStatic<Dotenv> dotenvStatic = mockStatic(Dotenv.class)) {
            dotenvStatic.when(Dotenv::load).thenReturn(dotenv);
            when(dotenv.get("OMDB_API_KEY")).thenReturn("test-key");

            Service service = new Service();

            // Act
            service.processMenu(0);

            // Assert
            String output = outContent.toString();
            assertTrue(output.contains("Exiting..."),
                    "Expected 'Exiting...' to be printed for case 0.");
        }
    }
}
