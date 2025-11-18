/* 
 * Service class responsible for all the service layer within application
 *  
*/

package com.pacoca.screenmatch.services;
import com.pacoca.screenmatch.model.episode.Episode;
import com.pacoca.screenmatch.model.episode.EpisodeRecord;
import com.pacoca.screenmatch.model.season.Season;
import com.pacoca.screenmatch.model.season.SeasonRecord;
import com.pacoca.screenmatch.model.serie.Show;
import com.pacoca.screenmatch.model.serie.ShowRecord;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import io.github.cdimascio.dotenv.Dotenv;




public class Service {
    // variables
    Dotenv dotenv = Dotenv.load();
    private final APIService APISERVICE = new APIService();
    private final Converter CONVERTER = new Converter();
    private final String URL = "https://www.omdbapi.com/?t=";
    private final String API_KEY = dotenv.get("OMDB_API_KEY");
    private static Scanner sc = new Scanner(System.in);

    
    // the front door of the app. The starting menu.
    public void menu() {
        
        var menu = "\n" +
                "1 - Search a show\n" +
                "2 - Get a recommendation\n" +
                "3 - Display a show's best 5 episodes\n" +
                "4 - Display search history\n" +
                "5 - Get a recommendation\n" +
                "0 - Exit";

        // pre set values to start loop
        int option = -1;

        // while user doesnt select exit (option 0) keep looping
        while (option !=0) {
            //show menu
            System.out.println(menu);

            try {
                // try to parse user option
                option = Integer.parseInt(sc.nextLine());
                processMenu(option);
            
            } catch (NumberFormatException e) {
                //if not a valid option set up default value and restart
                option = -1;
            }
            }

    }


    // communicate with APIService layer and search for a movie/show.
    public Show getShow(String show) {

        String json = APISERVICE.getData(URL + show.replace(" ", "+") + "&apikey=" + API_KEY);
        ShowRecord formattedJson = CONVERTER.dataConverter(json, ShowRecord.class);

        // return formattedJson as a serie.
        return new Show(formattedJson);
    }


    // get the best 5 episodes for a specific show
    public void best5(String show) {
        Show showData = getShow(show);
        
         // setup a new array for episodes and save the show
        List<Episode> episodes = getEpisodes(showData);
       
        // filter episodes with rating and sort them. limit to 5 and get episode data
        episodes.stream()
                .filter(e -> !e.getRating().equalsIgnoreCase( "N/A") )
                .distinct()
                .sorted((e1,e2) -> Double.compare(
                        Double.parseDouble(e2.getRating()),
                        Double.parseDouble(e1.getRating())
                ))
                .limit(5) // limit to 5
                .map(epData -> epData.getTitle() + " - " + epData.getRating() + " - Season " + epData.getSeason())
                .forEach(e -> System.out.println("\n" + e + "\n")); // get data and print
    }


    // get seasons for a specific show
    public List<Season> getSeasons(Show show) {
        //setup a clean array
        List<Season> seasons = new ArrayList<>();
        

        // for each season set rating as 0 and seasonNumber equals to index
        for (int i = 1; i <= show.getTotalSeason(); i++) {
            double seasonRating = 0;
            int seasonNumber = i;

            // retrieve season data
            SeasonRecord seasonData = getSeasonData(show, i);
            
            // save season episodes
            List<Episode> seasonEpisodes = seasonData.episodeData().stream()
                    .map(epRecord -> new Episode(epRecord, seasonNumber))
                    .collect(Collectors.toList());

            for (Episode ep : seasonEpisodes) {
                
                // if ep rating is null or 0 ignore.
                if (ep.getRating().equalsIgnoreCase("N/A") || ep.getRating().equalsIgnoreCase("0.0")) {
                    continue;
                }
                // else add the episode rating and add to the sum
                seasonRating += Double.parseDouble(ep.getRating());

            }

            // divide the sum by the size of episodes and get seasonRating
            seasonRating /= seasonEpisodes.size();
            // add season to a list
            seasons.add(new Season(seasonData, seasonEpisodes, seasonRating));
        }

        return seasons;
    }

    //get all episodes of a show
    public List<Episode> getEpisodes(Show show) {
        List<Episode> allEpisodes = new ArrayList<>();
        // get all seasons
        List<Season> seasons = getSeasons(show);

        // for season save episodes and for season episode save in a all episodes list
        for (Season season : seasons){
            
            List<Episode> seasonEpisodes = season.getEpisodes();
            seasonEpisodes.forEach(ep -> {
                allEpisodes.add(ep);
            });
        }

        return allEpisodes;


    }
    
    // handle all the system processing. the backennd of our menu.
    public void processMenu(int option) {
        Show show = null; 

        //switch option based on users choice
        switch (option) {
            // search for a show    
            case 1:
                    System.out.println("Type a show: ");
                    show = getShow(sc.nextLine());
                    System.out.println(show.getTitle() + "\n - " + show.getRating() + "\n - " + show.getTotalSeason() + " seasons" + "\n - " + show.getCast() + "\n - " + show.getPlot() + "\n - " + show.getGenre());
                    break;
            // get a show's seasons      
            case 2:
                    System.out.println("Type a show: ");
                    show = getShow(sc.nextLine());
                    getSeasons(show).forEach(s -> System.out.println(s.getSeasonNumber() + " - " + s.getRating()));
                    break;
            // get the best 5 episodes of a show        
            case 3:
                    System.out.println("Type a show: ");
                    show = getShow(sc.nextLine());
                    best5(show.getTitle());
                    break;
            // display user's search history
            case 4:
                    if (show == null) {
                        System.out.println("History is currently empty");
                    }
                    Show.listSearchedSeries();
                    break;
            // get AI'S recommendation         
            case 5:
                    try {
                        String suggestion = APIService.GetRecomendation();
                        System.out.println(suggestion);
                    } catch (IOException | InterruptedException e) {
                        System.out.println("Error: " + e.getMessage() + "\n we were unable to get the recomendations, please try again later.");
                    }
            // exit
            case 0:
                    System.out.println("Exiting...");
                    break;
            }
    }


    // get specific season data. 
    public SeasonRecord getSeasonData(Show show, int i) {
        // communicate to APISERVICE and retrieve season
        String seasonJson = APISERVICE.getData(URL + show.getTitle().replace(" ", "+") + "&season=" + i + "&apikey=" + API_KEY);
        //save season into a record class
        SeasonRecord seasonFormattedJson = CONVERTER.dataConverter(seasonJson, SeasonRecord.class);

        // return a record for each season
        return seasonFormattedJson;
    }
}
